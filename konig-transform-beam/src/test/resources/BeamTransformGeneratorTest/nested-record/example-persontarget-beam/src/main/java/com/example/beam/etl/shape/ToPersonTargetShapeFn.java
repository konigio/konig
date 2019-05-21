package com.example.beam.etl.shape;

import com.google.api.services.bigquery.model.TableRow;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.DoFn.ProcessContext;
import org.apache.beam.sdk.transforms.DoFn.ProcessElement;

public class ToPersonTargetShapeFn
    extends DoFn<TableRow, TableRow>
{

    @ProcessElement
    public void processElement(ProcessContext c) {
        try {
            TableRow inputRow = c.element();
            TableRow outputRow = new TableRow();
            Object id = concat("http://example.com/person/", required(inputRow, "person_id"));
            outputRow.set("id", id);
            TableRow address = new TableRow();
            Object id = inputRow.get("id");
            if (id!= null) {
                address.set("id", id);
            }
            Object city = inputRow.get("city");
            if (city!= null) {
                address.set("addressLocality", city);
            }
            Object state = inputRow.get("state");
            if (state!= null) {
                address.set("addressRegion", state);
            }
            if (!address.isEmpty()) {
                outputRow.set("address", address);
            }
            if (!outputRow.isEmpty()) {
                c.output(outputRow);
            }
        } catch (final Throwable oops) {
            oops.printStackTrace();
        }
    }

    private Object required(TableRow row, String fieldName) {
        Object value = row.get(fieldName);
        if (value == null) {
            throw new RuntimeException((("Field "+ fieldName)+" must not be null."));
        }
        return value;
    }

    private String concat(Object... args) {
        StringBuilder builder = new StringBuilder();
        for (Object value: args) {
            builder.append(value.toString());
        }
        return builder.toString();
    }
}
