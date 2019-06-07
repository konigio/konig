package com.example.beam.etl.shape;

import com.example.beam.etl.common.ErrorBuilder;
import com.example.beam.etl.schema.GenderType;
import com.google.api.services.bigquery.model.TableRow;
import org.apache.beam.sdk.transforms.DoFn;

public class ToPersonTargetShapeFn
    extends DoFn<TableRow, TableRow>
{

    @DoFn.ProcessElement
    public void processElement(DoFn.ProcessContext c) {
        try {
            ErrorBuilder errorBuilder = new ErrorBuilder();
            TableRow outputRow = new TableRow();
            TableRow personSourceRow = c.element();
            id(personSourceRow, outputRow, errorBuilder);
            gender(outputRow, errorBuilder);
            givenName(personSourceRow, outputRow, errorBuilder);
            if (!outputRow.isEmpty()) {
                c.output(outputRow);
            }
        }
    }

    private void id(TableRow personSourceRow, TableRow outputRow, ErrorBuilder errorBuilder) {
        Object person_id = ((personSourceRow == null)?null:personSourceRow.get("person_id"));
        if (person_id!= null) {
            outputRow.set("id", concat("http://example.com/person/", person_id));
        } else {
            errorBuilder.addError("Cannot set id because {PersonSourceShape}.person_id is null");
        }
    }

    private String concat(Object arg) {
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

    private void gender(TableRow outputRow, ErrorBuilder errorBuilder) {
        TableRow genderRow = new TableRow();
        GenderType gender = GenderType.Male;
        gender_id(gender, genderRow, errorBuilder);
        gender_name(gender, genderRow, errorBuilder);
    }

    private void gender_id(GenderType gender, TableRow outputRow, ErrorBuilder errorBuilder) {
        Object id = gender.getId().getLocalName();
        if (id!= null) {
            outputRow.set("id", id);
        } else {
            errorBuilder.addError("Cannot set gender.id because {GenderType}.id is null");
        }
    }

    private void gender_name(GenderType gender, TableRow outputRow, ErrorBuilder errorBuilder) {
        Object name = gender.getName();
        if (name!= null) {
            outputRow.set("name", name);
        } else {
            errorBuilder.addError("Cannot set gender.name because {GenderType}.name is null");
        }
    }

    private void givenName(TableRow personSourceRow, TableRow outputRow, ErrorBuilder errorBuilder) {
        Object first_name = ((personSourceRow == null)?null:personSourceRow.get("first_name"));
        if (first_name!= null) {
            outputRow.set("givenName", first_name);
        } else {
            errorBuilder.addError("Cannot set givenName because {PersonSourceShape}.first_name is null");
        }
    }
}
