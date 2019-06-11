package com.example.beam.etl.shape;

import com.example.beam.etl.common.ErrorBuilder;
import com.example.beam.etl.schema.GenderType;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.values.TupleTag;

public class ToPersonTargetShapeFn
    extends DoFn<com.google.api.services.bigquery.model.TableRow, com.google.api.services.bigquery.model.TableRow>
{
    public static TupleTag<String> deadLetterTag = new TupleTag<String>();
    public static TupleTag<com.google.api.services.bigquery.model.TableRow> successTag = new TupleTag<com.google.api.services.bigquery.model.TableRow>();

    @DoFn.ProcessElement
    public void processElement(DoFn.ProcessContext c) {
        try {
            ErrorBuilder errorBuilder = new ErrorBuilder();
            com.google.api.services.bigquery.model.TableRow outputRow = new com.google.api.services.bigquery.model.TableRow();
            com.google.api.services.bigquery.model.TableRow personSourceRow = c.element();
            id(personSourceRow, outputRow, errorBuilder);
            gender(outputRow, errorBuilder);
            if (!outputRow.isEmpty()) {
                c.output(successTag, outputRow);
            }
            if (errorBuilder.length()> 0) {
                errorBuilder.addError(outputRow.toString());
                throw new Exception(errorBuilder.toString());
            }
        } catch (final Throwable oops) {
            c.output(deadLetterTag, oops.getMessage());
        }
    }

    private void id(com.google.api.services.bigquery.model.TableRow personSourceRow, com.google.api.services.bigquery.model.TableRow outputRow, ErrorBuilder errorBuilder) {
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

    private void gender(com.google.api.services.bigquery.model.TableRow outputRow, ErrorBuilder errorBuilder) {
        Object personSourceRow_gender_code = personSourceRow.get("personSourceRow_gender_code");
        if (personSourceRow_gender_code!= null) {
            com.google.api.services.bigquery.model.TableRow genderRow = new com.google.api.services.bigquery.model.TableRow();
            GenderType gender = GenderType.findByGenderCode(personSourceRow_gender_code.toString());
            gender_id(gender, genderRow, errorBuilder);
            gender_name(gender, genderRow, errorBuilder);
            genderRow.set("genderCode", personSourceRow_gender_code);
            outputRow.set("gender", genderRow);
        }
    }

    private void gender_id(GenderType gender, com.google.api.services.bigquery.model.TableRow outputRow, ErrorBuilder errorBuilder) {
        Object id = gender.getId().getLocalName();
        if (id!= null) {
            outputRow.set("id", id);
        } else {
            errorBuilder.addError("Cannot set gender.id because {GenderType}.id is null");
        }
    }

    private void gender_name(GenderType gender, com.google.api.services.bigquery.model.TableRow outputRow, ErrorBuilder errorBuilder) {
        Object name = gender.getName();
        if (name!= null) {
            outputRow.set("name", name);
        } else {
            errorBuilder.addError("Cannot set gender.name because {GenderType}.name is null");
        }
    }
}
