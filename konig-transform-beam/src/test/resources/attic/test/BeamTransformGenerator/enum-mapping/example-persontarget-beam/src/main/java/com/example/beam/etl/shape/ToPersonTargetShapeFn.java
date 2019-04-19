package com.example.beam.etl.shape;

import com.example.beam.etl.schema.GenderType;
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
            transformGender(inputRow, outputRow);
            if (!outputRow.isEmpty()) {
                c.output(outputRow);
            }
        } catch (final Throwable oops) {
            oops.printStackTrace();
        }
    }

    private void transformGender(TableRow inputRow, TableRow outputRow) {
        GenderType gender = GenderType.findByGenderCode(inputRow.get("gender_code").toString());
        TableRow genderRow = new TableRow();
        Object id = gender.getId();
        if (id!= null) {
            genderRow.set("id", id);
        }
        Object name = gender.getName();
        if (name!= null) {
            genderRow.set("name", name);
        }
        Object gender_code = inputRow.get("gender_code");
        if (gender_code!= null) {
            genderRow.set("genderCode", gender_code);
        }
        if (!gender.isEmpty()) {
            outputRow.set("gender", genderRow);
        }
    }
}
