package com.example.beam.etl.shape;

import java.util.Iterator;
import com.example.beam.etl.common.ErrorBuilder;
import com.google.api.services.bigquery.model.TableRow;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.join.CoGbkResult;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.TupleTag;

public class MergePersonAlumniOfAndPersonNameFn
    extends DoFn<KV<String, CoGbkResult> , TableRow>
{

    @DoFn.ProcessElement
    public void processElement(DoFn.ProcessContext c) {
        try {
            ErrorBuilder errorBuilder = new ErrorBuilder();
            TableRow outputRow = new TableRow();
            KV<String, CoGbkResult> e = c.element();
            TableRow personAlumniOfRow = sourceRow(e, BqPersonShapeBeam.personAlumniOfTag);
            TableRow personNameRow = sourceRow(e, BqPersonShapeBeam.personNameTag);
            id(personAlumniOfRow, outputRow, errorBuilder);
            alumniOf(personAlumniOfRow, outputRow, errorBuilder);
            givenName(personNameRow, outputRow, errorBuilder);
            if (!outputRow.isEmpty()) {
                c.output(outputRow);
            }
        }
    }

    public TableRow sourceRow(KV<String, CoGbkResult> e, TupleTag<TableRow> tag) {
        Iterator<TableRow> sequence = e.getValue().getAll(tag).iterator();
        return (sequence.hasNext()?sequence.next():null);
    }

    private void id(TableRow personAlumniOfRow, TableRow outputRow, ErrorBuilder errorBuilder) {
        Object ID = ((personAlumniOfRow == null)?null:personAlumniOfRow.get("ID"));
        if (ID!= null) {
            outputRow.set("id", ID);
        } else {
            errorBuilder.addError("Cannot set id because {PersonAlumniOfShape}.ID is null");
        }
    }

    private void alumniOf(TableRow personAlumniOfRow, TableRow outputRow, ErrorBuilder errorBuilder) {
        Object alumni_of = ((personAlumniOfRow == null)?null:personAlumniOfRow.get("alumni_of"));
        if (alumni_of!= null) {
            outputRow.set("alumniOf", alumni_of);
        } else {
            errorBuilder.addError("Cannot set alumniOf because {PersonAlumniOfShape}.alumni_of is null");
        }
    }

    private void givenName(TableRow personNameRow, TableRow outputRow, ErrorBuilder errorBuilder) {
        Object first_name = ((personNameRow == null)?null:personNameRow.get("first_name"));
        if (first_name!= null) {
            outputRow.set("givenName", first_name);
        } else {
            errorBuilder.addError("Cannot set givenName because {PersonNameShape}.first_name is null");
        }
    }
}
