package com.example.beam.etl.shape;

import java.io.InputStream;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.fasterxml.uuid.Generators;
import com.google.api.client.util.DateTime;
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
    private static final Pattern DATE_PATTERN = Pattern.compile("(\\d+-\\d+-\\d+)(.*)");

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
                    String birth_date = stringValue(csv, "birth_date", record, builder);
                    if (birth_date!= null) {
                        row.set("birth_date", birth_date);
                    }
                    Long modified_date = temporalValue(csv, "modified_date", record, builder);
                    if (modified_date!= null) {
                        row.set("modified_date", modified_date);
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
        validateHeader(headerMap, "birth_date", builder);
        validateHeader(headerMap, "modified_date", builder);
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

    private Long temporalValue(CSVParser csv,
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
                            DateTime dateTimeValue = new DateTime(stringValue);
                            if (dateTimeValue.isDateOnly()) {
                                return (dateTimeValue.getValue()/ 1000);
                            }
                            if (stringValue.contains("T")) {
                                if (stringValue.contains("/")) {
                                    return (Instant.from(ZonedDateTime.parse(stringValue)).toEpochMilli()/ 1000);
                                } else {
                                    if (stringValue.contains("Z")) {
                                        return (Instant.parse(stringValue).toEpochMilli()/ 1000);
                                    }
                                    return (Instant.from(OffsetDateTime.parse(stringValue)).toEpochMilli()/ 1000);
                                }
                            }
                            Matcher matcher = DATE_PATTERN.matcher(stringValue);
                            if (matcher.matches()) {
                                String datePart = matcher.group(1);
                                String zoneOffset = matcher.group(2);
                                if ((zoneOffset.length() == 0)||zoneOffset.equals("Z")) {
                                    stringValue = ((datePart +"T00:00:00.000")+ zoneOffset);
                                }
                                return (Instant.from(OffsetDateTime.parse(stringValue)).toEpochMilli()/ 1000);
                            }
                        } catch (final Exception ex) {
                            String message = String.format("Invalid temporal value for %s;", fieldName);
                            exceptionMessageBr.append(message);
                        }
                    }
                }
            }
        }
        return null;
    }
}
