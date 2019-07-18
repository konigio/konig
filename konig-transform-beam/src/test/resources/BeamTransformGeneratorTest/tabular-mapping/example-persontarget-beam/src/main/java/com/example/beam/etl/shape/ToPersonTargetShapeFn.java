package com.example.beam.etl.shape;

import com.example.beam.etl.common.ErrorBuilder;
import com.google.api.services.bigquery.model.TableRow;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.DoFn.ProcessElement;
import org.apache.beam.sdk.transforms.DoFn.ProcessContext;
import org.apache.beam.sdk.values.TupleTag;

public class ToPersonTargetShapeFn
    extends DoFn<TableRow, TableRow>
{
    public static TupleTag<String> deadLetterTag = new TupleTag<String>();
    public static TupleTag<TableRow> successTag = new TupleTag<TableRow>();

    @ProcessElement
    public void processElement(ProcessContext c) {
        ErrorBuilder errorBuilder = new ErrorBuilder();
        try {
            TableRow outputRow = new TableRow();
            TableRow personSourceRow = ((TableRow) c.element());
            id(errorBuilder, outputRow, personSourceRow);
            givenName(errorBuilder, outputRow, personSourceRow);
            if (outputRow.isEmpty()) {
                errorBuilder.addError("record is empty");
            }
            if (!errorBuilder.isEmpty()) {
                c.output(deadLetterTag, errorBuilder.toString());
            } else {
                c.output(successTag, outputRow);
            }
        } catch (final Throwable oops) {
            errorBuilder.addError(oops.getMessage());
            c.output(deadLetterTag, errorBuilder.toString());
        }
    }

    private String id(ErrorBuilder errorBuilder, TableRow personTargetRow, TableRow personSourceRow) {
        String id = ((String) concat("http://example.com/person/", personSourceRow.get("person_id")));
        if (id!= null) {
            personTargetRow.set("id", id);
        } else {
            errorBuilder.addError("Required property 'id' is null");
        }
        return id;
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

    private String givenName(ErrorBuilder errorBuilder, TableRow personTargetRow, TableRow personSourceRow) {
        String givenName = ((String) personSourceRow.get("first_name"));
        if (givenName!= null) {
            personTargetRow.set("givenName", givenName);
        }
        return givenName;
    }
}
