package com.example.beam.etl.shape;

import java.util.Iterator;
import com.example.beam.etl.common.ErrorBuilder;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.DoFn.ProcessContext;
import org.apache.beam.sdk.transforms.join.CoGbkResult;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.TupleTag;

public class MergePersonAlumniOfAndPersonNameFn
    extends DoFn<KV<String, CoGbkResult> , com.google.api.services.bigquery.model.TableRow>
{
    public static TupleTag<String> deadLetterTag = new TupleTag<String>();
    public static TupleTag<com.google.api.services.bigquery.model.TableRow> successTag = new TupleTag<com.google.api.services.bigquery.model.TableRow>();

    @DoFn.ProcessElement
    public void processElement(ProcessContext c) {
        try {
            ErrorBuilder errorBuilder = new ErrorBuilder();
            com.google.api.services.bigquery.model.TableRow outputRow = new com.google.api.services.bigquery.model.TableRow();
            KV<String, CoGbkResult> e = c.element();
            com.google.api.services.bigquery.model.TableRow personAlumniOfRow = sourceRow(e, BqPersonShapeBeam.personAlumniOfTag);
            com.google.api.services.bigquery.model.TableRow personNameRow = sourceRow(e, BqPersonShapeBeam.personNameTag);
            id(personAlumniOfRow, outputRow, errorBuilder);
            alumniOf(personAlumniOfRow, outputRow, errorBuilder);
            givenName(personNameRow, outputRow, errorBuilder);
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

    public com.google.api.services.bigquery.model.TableRow sourceRow(KV<String, CoGbkResult> e, TupleTag<com.google.api.services.bigquery.model.TableRow> tag) {
        Iterator<com.google.api.services.bigquery.model.TableRow> sequence = e.getValue().getAll(tag).iterator();
        return (sequence.hasNext()?sequence.next():null);
    }

    private boolean id(com.google.api.services.bigquery.model.TableRow personAlumniOfRow, com.google.api.services.bigquery.model.TableRow outputRow, ErrorBuilder errorBuilder) {
        Object ID = ((personAlumniOfRow == null)?null:personAlumniOfRow.get("ID"));
        if (ID!= null) {
            outputRow.set("id", ID);
            return true;
        } else {
            errorBuilder.addError("Cannot set id because {PersonAlumniOfShape}.ID is null");
            return false;
        }
    }

    private boolean alumniOf(com.google.api.services.bigquery.model.TableRow personAlumniOfRow, com.google.api.services.bigquery.model.TableRow outputRow, ErrorBuilder errorBuilder) {
        Object alumni_of = ((personAlumniOfRow == null)?null:personAlumniOfRow.get("alumni_of"));
        if (alumni_of!= null) {
            outputRow.set("alumniOf", alumni_of);
            return true;
        } else {
            errorBuilder.addError("Cannot set alumniOf because {PersonAlumniOfShape}.alumni_of is null");
            return false;
        }
    }

    private boolean givenName(com.google.api.services.bigquery.model.TableRow personNameRow, com.google.api.services.bigquery.model.TableRow outputRow, ErrorBuilder errorBuilder) {
        Object first_name = ((personNameRow == null)?null:personNameRow.get("first_name"));
        if (first_name!= null) {
            outputRow.set("givenName", first_name);
            return true;
        } else {
            errorBuilder.addError("Cannot set givenName because {PersonNameShape}.first_name is null");
            return false;
        }
    }
}
