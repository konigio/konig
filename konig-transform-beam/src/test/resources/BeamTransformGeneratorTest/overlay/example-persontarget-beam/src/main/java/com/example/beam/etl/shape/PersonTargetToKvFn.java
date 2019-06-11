package com.example.beam.etl.shape;

import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.DoFn.ProcessElement;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.TupleTag;

public class PersonTargetToKvFn {
    public static TupleTag<String> deadLetterTag = new TupleTag<String>();
    public static TupleTag<com.google.api.services.bigquery.model.TableRow> successTag = new TupleTag<com.google.api.services.bigquery.model.TableRow>();

    @ProcessElement
    public void processElement(DoFn.ProcessContext c) {
        try {
            com.google.api.services.bigquery.model.TableRow row = c.element();
            String key = getKey(row);
            c.output(successTag, KV.of(key, row));
        } catch (final Throwable oops) {
            c.output(deadLetterTag, oops.getMessage());
        }
    }

    private String getKey(com.google.api.services.bigquery.model.TableRow row) {
        return ((String) row.get("id"));
    }
}
