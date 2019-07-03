package com.example.beam.etl.shape;

import com.example.beam.etl.common.ErrorBuilder;
import com.example.beam.etl.schema.GenderType;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.DoFn.ProcessContext;
import org.apache.beam.sdk.values.TupleTag;

public class ToPersonTargetShapeFn
    extends DoFn<com.google.api.services.bigquery.model.TableRow, com.google.api.services.bigquery.model.TableRow>
{
    public static TupleTag<String> deadLetterTag = new TupleTag<String>();
    public static TupleTag<com.google.api.services.bigquery.model.TableRow> successTag = new TupleTag<com.google.api.services.bigquery.model.TableRow>();

    @DoFn.ProcessElement
    public void processElement(ProcessContext c) {
        try {
            ErrorBuilder errorBuilder = new ErrorBuilder();
            com.google.api.services.bigquery.model.TableRow outputRow = new com.google.api.services.bigquery.model.TableRow();
            com.google.api.services.bigquery.model.TableRow personSourceRow = ((com.google.api.services.bigquery.model.TableRow) c.element());
            id(personSourceRow, outputRow, errorBuilder);
            gender(personSourceRow, outputRow, errorBuilder);
            if ((!outputRow.isEmpty())&&errorBuilder.isEmpty()) {
                c.output(successTag, outputRow);
            }
            if (!errorBuilder.isEmpty()) {
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

    private void gender(com.google.api.services.bigquery.model.TableRow personSourceRow, com.google.api.services.bigquery.model.TableRow outputRow, ErrorBuilder errorBuilder) {
        Object personSourceRow_gender_id = personSourceRow.get("gender_id");
        if (personSourceRow_gender_id!= null) {
            com.google.api.services.bigquery.model.TableRow genderRow = new com.google.api.services.bigquery.model.TableRow();
            GenderType gender = GenderType.findByLocalName(personSourceRow_gender_id.toString());
            genderRow.set("id", personSourceRow_gender_id);
            gender_name(gender, genderRow, errorBuilder);
            if (!outputRow.isEmpty()) {
                outputRow.set("gender", genderRow);
            }
        }
    }

    private void gender_name(GenderType gender, com.google.api.services.bigquery.model.TableRow outputRow, ErrorBuilder errorBuilder) {
        Object name = gender.getName();
        if (name!= null) {
            outputRow.set("name", stringValue(name, errorBuilder, "name"));
        } else {
            errorBuilder.addError("Cannot set gender.name because {GenderType}.name is null");
        }
    }

    private String stringValue(Object name, ErrorBuilder errorBuilder, String targetPropertyName) {
        try {
            if ((name!= null)&&(name instanceof String)) {
                return ((String) name);
            }
        } catch (final Exception ex) {
            String message = String.format("Invalid String value %s for field %s;", String.valueOf(name), targetPropertyName);
            errorBuilder.addError(message);
        }
        return null;
    }
}
