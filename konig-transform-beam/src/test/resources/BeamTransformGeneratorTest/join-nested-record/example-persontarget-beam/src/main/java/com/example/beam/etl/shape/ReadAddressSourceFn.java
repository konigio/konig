package com.example.beam.etl.shape;

import java.io.InputStream;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import com.google.api.services.bigquery.model.TableRow;
import org.apache.beam.sdk.io.FileIO;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.DoFn.ProcessContext;
import org.apache.beam.sdk.transforms.DoFn.ProcessElement;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.TupleTag;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReadAddressSourceFn
    extends DoFn<FileIO.ReadableFile, KV<String, TableRow>>
{
    private static final Logger LOGGER = LoggerFactory.getLogger("ReadFn");
    public static TupleTag<String> deadLetterTag = new TupleTag<String>();
    public static TupleTag<KV<String, TableRow>> successTag = new TupleTag<KV<String,TableRow>>();

    @ProcessElement
    public void processElement(ProcessContext c) {
        try {
            FileIO.ReadableFile f = c.element();
            ReadableByteChannel rbc = f.open();
            InputStream stream = Channels.newInputStream(rbc);
            try {
                CSVParser csv = CSVParser.parse(stream, StandardCharsets.UTF_8, CSVFormat.RFC4180 .withFirstRecordAsHeader().withSkipHeaderRecord());
                validateHeaders(csv);
                for (CSVRecord record: csv) {
                    StringBuilder builder = new StringBuilder();
                    try {
                        TableRow row = new TableRow();
                        String addressOf = stringValue(csv, "addressOf", record, builder);
                        if (addressOf!= null) {
                            row.set("addressOf", addressOf);
                        }
                        String city = stringValue(csv, "city", record, builder);
                        if (city!= null) {
                            row.set("city", city);
                        }
                        String state = stringValue(csv, "state", record, builder);
                        if (state!= null) {
                            row.set("state", state);
                        }
                        if (!row.isEmpty()) {
                            c.output(successTag, KV.of(addressOf.toString(), row));
                        }
                        if (builder.length()> 0) {
                            throw new Exception(builder.toString());
                        }
                    } catch (final Exception e) {
                        HashMap<String, String> recordMap = ((HashMap<String, String> ) record.toMap());
                        StringBuilder br = new StringBuilder();
                        br.append("ETL_ERROR_MESSAGE");
                        br.append(",");
                        br.append(StringUtils.join(recordMap.keySet(), ','));
                        br.append("\n");
                        br.append(e.getMessage());
                        br.append(",");
                        br.append(StringUtils.join(recordMap.values(), ','));
                        c.output(deadLetterTag, br.toString());
                    }
                }
            } finally {
                stream.close();
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    private void validateHeaders(CSVParser csv) {
        HashMap<String, Integer> headerMap = ((HashMap<String, Integer> ) csv.getHeaderMap());
        StringBuilder builder = new StringBuilder();
        validateHeader(headerMap, "addressOf", builder);
        validateHeader(headerMap, "city", builder);
        validateHeader(headerMap, "state", builder);
        if (builder.length()> 0) {
            LOGGER.warn("Mapping for {} not found", builder.toString());
        }
    }

    private void validateHeader(HashMap headerMap, String columnName, StringBuilder builder) {
        if (headerMap.get(columnName) == null) {
            builder.append(columnName);
            builder.append(";");
        }
    }

    private String stringValue(CSVParser csv,
        String fieldName,
        CSVRecord record,
        StringBuilder exceptionMessageBr)
        throws Exception
    {
        HashMap<String, Integer> headerMap = ((HashMap<String, Integer> ) csv.getHeaderMap());
        if (headerMap.get(fieldName)!= null) {
            {
                String stringValue = record.get(fieldName);
                if (stringValue!= null) {
                    stringValue = stringValue.trim();
                    if (stringValue.equals("InjectErrorForTesting")) {
                        throw new Exception("Error in pipeline : InjectErrorForTesting");
                    }
                    if (stringValue.length()> 0) {
                        try {
                            return stringValue;
                        } catch (final Exception ex) {
                            String message = String.format("Invalid string value for %s;", fieldName);
                            exceptionMessageBr.append(message);
                        }
                    }
                }
            }
        }
        return null;
    }
}
