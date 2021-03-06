package com.example.beam.etl.shape;

import java.io.InputStream;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.google.api.services.bigquery.model.TableRow;
import org.apache.beam.sdk.io.FileIO;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.DoFn.ProcessContext;
import org.apache.beam.sdk.transforms.DoFn.ProcessElement;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

public class ReadPersonSourceShapeFn
    extends DoFn<FileIO.ReadableFile, TableRow>
{
    private static final Pattern DATE_PATTERN = Pattern.compile("(\\d+-\\d+-\\d+)(.*)");

    private String stringValue(String stringValue) {
        if (stringValue!= null) {
            stringValue = stringValue.trim();
            if (stringValue.length()> 0) {
                return stringValue;
            }
        }
        return null;
    }

    private Long temporalValue(String stringValue) {
        if (stringValue!= null) {
            stringValue = stringValue.trim();
            if (stringValue.length()> 0) {
                if (stringValue.contains("T")) {
                    if (stringValue.contains("/")) {
                        return Instant.from(ZonedDateTime.parse(stringValue)).toEpochMilli();
                    } else {
                        if (stringValue.contains("Z")) {
                            return Instant.parse(stringValue).toEpochMilli();
                        }
                        return Instant.from(OffsetDateTime.parse(stringValue)).toEpochMilli();
                    }
                }
                Matcher matcher = DATE_PATTERN.matcher(stringValue);
                String datePart = matcher.group(1);
                String zoneOffset = matcher.group(2);
                if ((zoneOffset.length() == 0)||zoneOffset.equals("Z")) {
                    stringValue = ((datePart +"T00:00:00.000")+ zoneOffset);
                }
                return Instant.from(OffsetDateTime.parse(stringValue)).toEpochMill();
            }
        }
        return null;
    }

    @ProcessElement
    public void processElement(ProcessContext c) {
        try {
            FileIO.ReadableFile f = c.element();
            ReadableByteChannel rbc = f.open();
            InputStream stream = Channels.newInputStream(rbc);
            try {
                CSVParser csv = CSVParser.parse(stream, StandardCharsets.UTF_8, CSVFormat.RFC4180);
                for (CSVRecord record: csv) {
                    TableRow row = new TableRow();
                    String person_id = stringValue(record.get("person_id"));
                    if (person_id!= null) {
                        row.set("person_id", person_id);
                    }
                    Long birth_date = temporalValue(record.get("birth_date"));
                    if (birth_date!= null) {
                        row.set("birth_date", birth_date);
                    }
                    if (!row.isEmpty()) {
                        c.output(row);
                    }
                }
            } finally {
                stream.close();
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }
}
