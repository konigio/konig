package com.example.beam.etl.shape;

import com.example.beam.etl.common.ErrorBuilder;
import com.google.cloud.Date;
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
            givenName(personSourceRow, outputRow, errorBuilder);
            modified(outputRow, errorBuilder);
            if (!outputRow.isEmpty()) {
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

    private boolean id(com.google.api.services.bigquery.model.TableRow personSourceRow, com.google.api.services.bigquery.model.TableRow outputRow, ErrorBuilder errorBuilder) {
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

    private boolean givenName(com.google.api.services.bigquery.model.TableRow personSourceRow, com.google.api.services.bigquery.model.TableRow outputRow, ErrorBuilder errorBuilder) {
        Object first_name = ((personSourceRow == null)?null:personSourceRow.get("first_name"));
        if (first_name!= null) {
            outputRow.set("givenName", first_name);
            return true;
        } else {
            errorBuilder.addError("Cannot set givenName because {PersonSourceShape}.first_name is null");
            return false;
        }
    }

    private boolean modified(com.google.api.services.bigquery.model.TableRow outputRow, ErrorBuilder errorBuilder) {
        outputRow.set("modified", new Long(new Date().getTime()));
        return true;
    }
}
