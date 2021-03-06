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
import org.apache.beam.sdk.values.KV;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

public class ReadPersonNameFn
    extends DoFn<FileIO.ReadableFile, KV<String, TableRow>>
{

    private String stringValue(String stringValue)
        throws Exception
    {
        if (stringValue!= null) {
            stringValue = stringValue.trim();
            if (stringValue.equals("InjectErrorForTesting")) {
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
                    String first_name = stringValue(record.get("first_name"));
                    if (first_name!= null) {
                        row.set("first_name", first_name);
                    }
                    String id = stringValue(record.get("id"));
                    if (id!= null) {
                        row.set("id", id);
                    }
                    if (!row.isEmpty()) {
                        c.output(KV.of(id.toString(), row));
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
