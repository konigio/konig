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
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReadPersonSourceShapeFn
    extends DoFn<FileIO.ReadableFile, TableRow>
{
    private static final Logger LOGGER = LoggerFactory.getLogger("ReadFn");

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
                validateHeaders(csv);
                for (CSVRecord record: csv) {
                    TableRow row = new TableRow();
                    if (record.get("address_id")!= null) {
                        String address_id = stringValue(record.get("address_id"));
                        if (address_id!= null) {
                            row.set("address_id", address_id);
                        }
                    }
                    if (record.get("city")!= null) {
                        String city = stringValue(record.get("city"));
                        if (city!= null) {
                            row.set("city", city);
                        }
                    }
                    if (record.get("id")!= null) {
                        String id = stringValue(record.get("id"));
                        if (id!= null) {
                            row.set("id", id);
                        }
                    }
                    if (record.get("person_id")!= null) {
                        String person_id = stringValue(record.get("person_id"));
                        if (person_id!= null) {
                            row.set("person_id", person_id);
                        }
                    }
                    if (record.get("state")!= null) {
                        String state = stringValue(record.get("state"));
                        if (state!= null) {
                            row.set("state", state);
                        }
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

    private void validateHeaders(CSVParser csv) {
        HashMap<String, Integer> headerMap = ((HashMap<String, Integer> ) csv.getHeaderMap());
        StringBuilder builder = new StringBuilder();
        validateHeader(headerMap, "address_id", builder);
        validateHeader(headerMap, "city", builder);
        validateHeader(headerMap, "id", builder);
        validateHeader(headerMap, "person_id", builder);
        validateHeader(headerMap, "state", builder);
        if (builder.length()> 0) {
            LOGGER.warn("Mapping for {} not found", builder.toString());
        }
    }

    private void validateHeader(HashMap headerMap, String columnName, StringBuilder builder) {
        if (headerMap.get(columnName) == null) {
            builder.append(columnName);
        }
    }
}
