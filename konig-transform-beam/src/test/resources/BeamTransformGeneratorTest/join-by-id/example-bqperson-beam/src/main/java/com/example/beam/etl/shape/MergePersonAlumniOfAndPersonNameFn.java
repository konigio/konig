package com.example.beam.etl.shape;

import com.google.api.services.bigquery.model.TableRow;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.join.CoGbkResult;
import org.apache.beam.sdk.values.KV;

public class MergePersonAlumniOfAndPersonNameFn
    extends DoFn<KV<String, CoGbkResult> , TableRow>
{

    @DoFn.ProcessElement
    public void processElement(DoFn.ProcessContext c) {
        try {
            CoGbkResult e = c.element();
            TableRow outputRow = new TableRow();
            processPersonAlumniOf(e, outputRow);
            processPersonName(e, outputRow);
            if (!outputRow.isEmpty()) {
                c.output(e);
            }
        } catch (final Throwable oops) {
            oops.printStackTrace();
        }
    }

    private void processPersonAlumniOf(KV<String, CoGbkResult> e, TableRow outputRow)
        throws Throwable
    {
        Iterable<TableRow> inputRowList = e.getValue().getAll(BqPersonShapeBeam.personAlumniOfTag);
        for (TableRow inputRow: inputRowList) {
            Object id = inputRow.get("id");
            if (id!= null) {
                outputRow.set("id", id);
            }
            Object alumniOf = inputRow.get("alumniOf");
            if (alumniOf!= null) {
                outputRow.set("alumniOf", alumniOf);
            }
        }
    }

    private void processPersonName(KV<String, CoGbkResult> e, TableRow outputRow)
        throws Throwable
    {
        Iterable<TableRow> inputRowList = e.getValue().getAll(BqPersonShapeBeam.personNameTag);
        for (TableRow inputRow: inputRowList) {
            Object givenName = inputRow.get("givenName");
            if (givenName!= null) {
                outputRow.set("givenName", givenName);
            }
        }
    }
}
