package com.example.beam.etl.shape;

import com.example.beam.etl.common.ErrorBuilder;
import com.example.beam.etl.schema.GenderType;
import com.google.api.services.bigquery.model.TableRow;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.DoFn.ProcessContext;

public class ToPersonTargetShapeFn
    extends DoFn<TableRow, TableRow>
{

    @DoFn.ProcessElement
    public void processElement(ProcessContext c) {
        try {
            ErrorBuilder errorBuilder = new ErrorBuilder();
            TableRow outputRow = new TableRow();
            TableRow personSourceRow = c.element();
            id(personSourceRow, outputRow, errorBuilder);
            gender(outputRow, errorBuilder);
            if (!outputRow.isEmpty()) {
                c.output(outputRow);
            }
        } catch (final Throwable oops) {
            oops.printStackTrace();
        }
    }

    private boolean id(TableRow personSourceRow, TableRow outputRow, ErrorBuilder errorBuilder) {
        Object person_id = ((personSourceRow == null)?null:personSourceRow.get("person_id"));
        if (person_id!= null) {
            outputRow.set("id", concat("http://example.com/person/", person_id));
            return true;
        } else {
            errorBuilder.addError("Cannot set id because {PersonSourceShape}.person_id is null");
            return false;
        }
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

    private boolean gender(TableRow outputRow, ErrorBuilder errorBuilder) {
        Object personSourceRow_gender_code = personSourceRow.get("personSourceRow_gender_code");
        if (personSourceRow_gender_code!= null) {
            TableRow genderRow = new TableRow();
            GenderType gender = GenderType.findByGenderCode(personSourceRow_gender_code.toString());
            gender_id(gender, genderRow, errorBuilder);
            gender_name(gender, genderRow, errorBuilder);
            genderRow.set("genderCode", personSourceRow_gender_code);
            outputRow.set("gender", genderRow);
        }
    }

    private boolean gender_id(GenderType gender, TableRow outputRow, ErrorBuilder errorBuilder) {
        Object id = gender.getId().getLocalName();
        if (id!= null) {
            outputRow.set("id", id);
            return true;
        } else {
            errorBuilder.addError("Cannot set gender.id because {GenderType}.id is null");
            return false;
        }
    }

    private boolean gender_name(GenderType gender, TableRow outputRow, ErrorBuilder errorBuilder) {
        Object name = gender.getName();
        if (name!= null) {
            outputRow.set("name", name);
            return true;
        } else {
            errorBuilder.addError("Cannot set gender.name because {GenderType}.name is null");
            return false;
        }
    }
}
