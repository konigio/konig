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

public class ReadPersonSourceShapeFn
    extends DoFn<FileIO.ReadableFile, TableRow>
{

    private Long longValue(String stringValue)
        throws Exception
    {
        if (stringValue!= null) {
            stringValue = stringValue.trim();
            if (stringValue == "InjectErrorForTesting") {
                throw new Exception("Error in pipeline : InjectErrorForTesting");
            }
            if (stringValue.length()> 0) {
                return new Long(stringValue);
            }
        }
        return null;
    }

    private String stringValue(String stringValue)
        throws Exception
    {
        if (stringValue!= null) {
            stringValue = stringValue.trim();
            if (stringValue == "InjectErrorForTesting") {
                throw new Exception("Error in pipeline : InjectErrorForTesting");
            }
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
                    Long person_height = longValue(record.get("person_height"));
                    if (person_height!= null) {
                        row.set("person_height", person_height);
                    }
                    String person_id = stringValue(record.get("person_id"));
                    if (person_id!= null) {
                        row.set("person_id", person_id);
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
