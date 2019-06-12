package com.example.beam.etl.shape;

import java.io.InputStream;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import org.apache.beam.sdk.io.FileIO;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.DoFn.ProcessContext;
import org.apache.beam.sdk.transforms.DoFn.ProcessElement;
import org.apache.beam.sdk.values.TupleTag;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReadPersonSourceShapeFn
    extends DoFn<FileIO.ReadableFile, com.google.api.services.bigquery.model.TableRow>
{
    private static final Logger LOGGER = LoggerFactory.getLogger("ReadFn");
    public static TupleTag<String> deadLetterTag = new TupleTag<String>();
    public static TupleTag<com.google.api.services.bigquery.model.TableRow> successTag = new TupleTag<com.google.api.services.bigquery.model.TableRow>();

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
                        com.google.api.services.bigquery.model.TableRow row = new com.google.api.services.bigquery.model.TableRow();
                        String crm_id = stringValue(csv, "crm_id", record, builder);
                        if (crm_id!= null) {
                            row.set("crm_id", crm_id);
                        }
                        String first_name = stringValue(csv, "first_name", record, builder);
                        if (first_name!= null) {
                            row.set("first_name", first_name);
                        }
                        String mdm_id = stringValue(csv, "mdm_id", record, builder);
                        if (mdm_id!= null) {
                            row.set("mdm_id", mdm_id);
                        }
                        String originating_feed = stringValue(csv, "originating_feed", record, builder);
                        if (originating_feed!= null) {
                            row.set("originating_feed", originating_feed);
                        }
                        String originating_system = stringValue(csv, "originating_system", record, builder);
                        if (originating_system!= null) {
                            row.set("originating_system", originating_system);
                        }
                        String originating_system = stringValue(csv, "originating_system", record, builder);
                        if (originating_system!= null) {
                            row.set("originating_system", originating_system);
                        }
                        if (!row.isEmpty()) {
                            c.output(successTag, row);
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
        validateHeader(headerMap, "crm_id", builder);
        validateHeader(headerMap, "first_name", builder);
        validateHeader(headerMap, "mdm_id", builder);
        validateHeader(headerMap, "originating_feed", builder);
        validateHeader(headerMap, "originating_system", builder);
        validateHeader(headerMap, "originating_system", builder);
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
