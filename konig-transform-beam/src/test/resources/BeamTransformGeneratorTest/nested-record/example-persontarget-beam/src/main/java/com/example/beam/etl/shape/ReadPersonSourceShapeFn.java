package com.example.beam.etl.shape;

import java.io.InputStream;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;
import com.google.api.services.bigquery.model.TableRow;
import org.apache.beam.sdk.io.FileIO;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.DoFn.ProcessContext;
import org.apache.beam.sdk.transforms.DoFn.ProcessElement;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;

public class ReadPersonSourceShapeFn
    extends DoFn<FileIO.ReadableFile, TableRow>
{
    private static final Logger LOGGER = Logger.getGlobal();

    private String stringValue(String stringValue) {
        if (stringValue!= null) {
            stringValue = stringValue.trim();
            if (stringValue.length()> 0) {
                return stringValue;
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
                CSVParser csv = CSVParser.parse(stream, StandardCharsets.UTF_8, CSVFormat.RFC4180 .withFirstRecordAsHeader().withSkipHeaderRecord());
                for (CSVRecord record: csv) {
                    TableRow row = new TableRow();
                    try {
                        String address_id = stringValue(record.get("address_id"));
                        if (address_id!= null) {
                            row.set("address_id", address_id);
                        }
                    } catch (final Exception e) {
                        LOGGER.warning(e.getMessage());
                    }
                    try {
                        String city = stringValue(record.get("city"));
                        if (city!= null) {
                            row.set("city", city);
                        }
                    } catch (final Exception e) {
                        LOGGER.warning(e.getMessage());
                    }
                    try {
                        String id = stringValue(record.get("id"));
                        if (id!= null) {
                            row.set("id", id);
                        }
                    } catch (final Exception e) {
                        LOGGER.warning(e.getMessage());
                    }
                    try {
                        String person_id = stringValue(record.get("person_id"));
                        if (person_id!= null) {
                            row.set("person_id", person_id);
                        }
                    } catch (final Exception e) {
                        LOGGER.warning(e.getMessage());
                    }
                    try {
                        String state = stringValue(record.get("state"));
                        if (state!= null) {
                            row.set("state", state);
                        }
                    } catch (final Exception e) {
                        LOGGER.warning(e.getMessage());
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
