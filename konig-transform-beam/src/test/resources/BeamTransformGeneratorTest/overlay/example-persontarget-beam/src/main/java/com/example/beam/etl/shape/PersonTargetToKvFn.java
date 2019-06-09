package com.example.beam.etl.shape;

import com.google.api.services.bigquery.model.TableRow;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.DoFn.ProcessElement;
import org.apache.beam.sdk.values.KV;

public class PersonTargetToKvFn {

    @ProcessElement
    public void processElement(DoFn.ProcessContext c) {
        try {
            TableRow row = c.element();
            String key = getKey(row);
            c.output(KV.of(key, row));
        } catch (final Throwable oops) {
            oops.printStackTrace()();
        }
    }

    private String getKey(TableRow row) {
        return ((String) row.get("id"));
    }
}
