package com.example.beam.etl.shape;

import com.google.api.services.bigquery.model.TableRow;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.DoFn.ProcessElement;
import org.apache.beam.sdk.transforms.DoFn.ProcessContext;
import org.apache.beam.sdk.transforms.DoFn.ProcessElement;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.TupleTag;

public class PersonNameToKvFn
    extends DoFn<TableRow, KV<String, TableRow>>
{
    public static TupleTag<String> deadLetterTag = (new TupleTag<String>(){});
    public static TupleTag<KV<String, TableRow>> successTag = (new TupleTag<KV<String,TableRow>>(){});

    @ProcessElement
    public void processElement(ProcessContext c) {
        try {
            TableRow row = c.element();
            String id = ((row!= null)?((String) row.get("id")):null);
            if (id!= null) {
                c.output(successTag, KV.of(id, row));
            }
        } catch (final Throwable oops) {
            c.output(deadLetterTag, oops.getMessage());
        }
    }
}
