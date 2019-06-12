package com.example.beam.etl.shape;

import com.example.beam.etl.common.ErrorBuilder;
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
            com.google.api.services.bigquery.model.TableRow personSourceRow = c.element();
            id(personSourceRow, outputRow, errorBuilder);
            identifiedBy(personSourceRow, outputRow, errorBuilder);
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

    private boolean identifiedBy(com.google.api.services.bigquery.model.TableRow personSourceRow, com.google.api.services.bigquery.model.TableRow outputRow, ErrorBuilder errorBuilder) {
        com.google.api.services.bigquery.model.TableRow identifiedBy = new com.google.api.services.bigquery.model.TableRow();
        identifiedBy_identityProvider(identifiedBy, errorBuilder);
        identifiedBy_identifier(personSourceRow, identifiedBy, errorBuilder);
        if (errorBuilder.isEmpty()&&(!identifiedBy.isEmpty())) {
            outputRow.set("identifiedBy", identifiedBy);
            return true;
        } else {
            return false;
        }
    }

    private boolean identifiedBy_identityProvider(com.google.api.services.bigquery.model.TableRow outputRow, ErrorBuilder errorBuilder) {
        outputRow.set("identityProvider", "MDM");
        return true;
    }

    private boolean identifiedBy_identifier(com.google.api.services.bigquery.model.TableRow personSourceRow, com.google.api.services.bigquery.model.TableRow outputRow, ErrorBuilder errorBuilder) {
        Object mdm_id = ((personSourceRow == null)?null:personSourceRow.get("mdm_id"));
        if (mdm_id!= null) {
            outputRow.set("identifier", mdm_id);
            return true;
        } else {
            errorBuilder.addError("Cannot set identifiedBy.identifier because {PersonSourceShape}.mdm_id is null");
            return false;
        }
    }
}
