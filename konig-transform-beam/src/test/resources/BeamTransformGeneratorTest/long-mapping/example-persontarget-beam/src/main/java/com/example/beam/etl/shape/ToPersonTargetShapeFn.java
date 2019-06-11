package com.example.beam.etl.shape;

import com.example.beam.etl.common.ErrorBuilder;
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
            heightInches(personSourceRow, outputRow, errorBuilder);
            id(personSourceRow, outputRow, errorBuilder);
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

    private void heightInches(com.google.api.services.bigquery.model.TableRow personSourceRow, com.google.api.services.bigquery.model.TableRow outputRow, ErrorBuilder errorBuilder) {
        Object person_height = ((personSourceRow == null)?null:personSourceRow.get("person_height"));
        if (person_height!= null) {
            outputRow.set("heightInches", person_height);
        } else {
            errorBuilder.addError("Cannot set heightInches because {PersonSourceShape}.person_height is null");
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
}
