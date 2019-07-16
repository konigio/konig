package com.example.beam.etl.shape;

import com.example.beam.etl.common.ErrorBuilder;
import com.google.api.services.bigquery.model.TableRow;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.values.TupleTag;

public class ToPersonTargetShapeFn
    extends DoFn<TableRow, TableRow>
{
    public static TupleTag<String> deadLetterTag = new TupleTag<String>();
    public static TupleTag<TableRow> successTag = new TupleTag<TableRow>();

    @DoFn.ProcessElement
    public void processElement(DoFn.ProcessContext c) {
        ErrorBuilder errorBuilder = new ErrorBuilder();
        try {
            TableRow outputRow = new TableRow();
            TableRow personSourceRow = ((TableRow) c.element());
            id(errorBuilder, outputRow, personSourceRow);
            gender(errorBuilder, outputRow, personSourceRow);
            if (outputRow.isEmpty()) {
                errorBuilder.addError("record is empty");
            }
            if (!errorBuilder.isEmpty()) {
                c.output(deadLetterTag, errorBuilder.toString());
            } else {
                c.output(successTag, outputRow);
            }
        } catch (final Throwable oops) {
            errorBuilder.addError(oops.getMessage());
            c.output(deadLetterTag, errorBuilder.toString());
        }
    }

    private String id(ErrorBuilder errorBuilder, TableRow personTargetRow, TableRow personSourceRow) {
        String id = ((String) concat("http://example.com/person/", personSourceRow.get("person_id")));
        if (id!= null) {
            personTargetRow.set("id", id);
        } else {
            errorBuilder.addError("Required property 'id' is null");
        }
        return id;
    }

    private String concat(Object... arg) {
        for (Object obj: arg) {
            if (obj == null) {
                return null;
            }
        }
        StringBuilder builder = new StringBuilder();
        for (Object obj: arg) {
            builder.append(obj);
        }
        return builder.toString();
    }

    private TableRow gender(ErrorBuilder errorBuilder, TableRow personTargetRow, TableRow personSourceRow) {
        com.example.beam.etl.schema.GenderType gender = com.example.beam.etl.schema.GenderType.findByLocalName(((String) personSourceRow.get("gender_id")));
        TableRow genderRow = new TableRow();
        gender_id(errorBuilder, genderRow, personSourceRow);
        gender_name(errorBuilder, genderRow, gender);
        if (!genderRow.isEmpty()) {
            personTargetRow.set("gender", genderRow);
        }
        return genderRow;
    }

    private String gender_id(ErrorBuilder errorBuilder, TableRow genderRow, TableRow personSourceRow) {
        String id = ((String) personSourceRow.get("gender_id"));
        if (id!= null) {
            genderRow.set("id", id);
        } else {
            errorBuilder.addError("Required property 'gender.id' is null");
        }
        return id;
    }

    private String gender_name(ErrorBuilder errorBuilder, TableRow genderRow, com.example.beam.etl.schema.GenderType gender) {
        String name = ((String)((gender!= null)?gender.getName():null));
        if (name!= null) {
            genderRow.set("name", name);
        } else {
            errorBuilder.addError("Required property 'gender.name' is null");
        }
        return name;
    }
}
