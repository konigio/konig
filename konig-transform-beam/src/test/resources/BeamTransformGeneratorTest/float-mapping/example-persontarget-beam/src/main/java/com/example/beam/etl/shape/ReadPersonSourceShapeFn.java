package com.example.beam.etl.shape;

import java.io.InputStream;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import com.fasterxml.uuid.Generators;
import com.google.api.services.bigquery.model.TableRow;
import org.apache.beam.sdk.io.FileIO;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.DoFn.ProcessElement;
import org.apache.beam.sdk.transforms.DoFn.ProcessContext;
import org.apache.beam.sdk.transforms.DoFn.ProcessContext;
import org.apache.beam.sdk.transforms.DoFn.ProcessElement;
import org.apache.beam.sdk.values.TupleTag;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReadPersonSourceShapeFn
    extends DoFn<FileIO.ReadableFile, TableRow>
{
    private static final Logger LOGGER = LoggerFactory.getLogger("ReadFn");
    public static TupleTag<TableRow> deadLetterTag = (new TupleTag<TableRow>(){});
    public static TupleTag<TableRow> successTag = (new TupleTag<TableRow>(){});

    @ProcessElement
    public void processElement(ProcessContext c, PipelineOptions options) {
        try {
            FileIO.ReadableFile f = c.element();
            ReadableByteChannel rbc = f.open();
            InputStream stream = Channels.newInputStream(rbc);
            try {
                CSVParser csv = CSVParser.parse(stream, StandardCharsets.UTF_8, CSVFormat.RFC4180 .withFirstRecordAsHeader().withSkipHeaderRecord());
                validateHeaders(csv);
                for (CSVRecord record: csv) {
                    StringBuilder builder = new StringBuilder();
                    TableRow row = new TableRow();
                    Float person_height = floatValue(csv, "person_height", record, builder);
                    if (person_height!= null) {
                        row.set("person_height", person_height);
                    }
                    String person_id = stringValue(csv, "person_id", record, builder);
                    if (person_id!= null) {
                        row.set("person_id", person_id);
                    }
                    if (row.isEmpty()) {
                        builder.append("record is empty");
                    }
                    if (builder.length()> 0) {
                        TableRow errorRow = new TableRow();
                        errorRow.set("errorId", Generators.timeBasedGenerator().generate().toString());
                        errorRow.set("errorCreated", (new Date().getTime()/ 1000));
                        errorRow.set("errorMessage", builder.toString());
                        errorRow.set("pipelineJobName", options.getJobName());
                        errorRow.set("PersonSource", row);
                        c.output(deadLetterTag, errorRow);
                    } else {
                        c.output(successTag, row);
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
        validateHeader(headerMap, "person_height", builder);
        validateHeader(headerMap, "person_id", builder);
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

    private Float floatValue(CSVParser csv,
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
                            return new Float(stringValue);
                        } catch (final Exception ex) {
                            String message = String.format("Invalid float value for %s;", fieldName);
                            exceptionMessageBr.append(message);
                        }
                    }
                }
            }
        }
        return null;
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
