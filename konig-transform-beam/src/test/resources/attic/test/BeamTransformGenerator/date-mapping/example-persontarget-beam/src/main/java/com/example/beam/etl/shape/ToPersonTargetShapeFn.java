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
            StringBuilder idBuilder = new StringBuilder();
            idBuilder.append("http://example.com/person/");
            idBuilder.append(inputRow.get("person_id"));
            outputRow.set("id", idBuilder.toString());
            Object birth_date = inputRow.get("birth_date");
            if (birth_date!= null) {
                outputRow.set("birthDate", birth_date);
            }
            if (!outputRow.isEmpty()) {
                c.output(outputRow);
            }
        } catch (final Throwable oops) {
            oops.printStackTrace();
        }
    }
}
