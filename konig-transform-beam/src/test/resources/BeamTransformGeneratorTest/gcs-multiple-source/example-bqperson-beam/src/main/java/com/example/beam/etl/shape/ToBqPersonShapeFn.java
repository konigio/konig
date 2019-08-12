package com.example.beam.etl.shape;

import java.util.Date;
import java.util.Iterator;
import com.example.beam.etl.common.ErrorBuilder;
import com.fasterxml.uuid.Generators;
import com.google.api.services.bigquery.model.TableRow;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.DoFn.ProcessElement;
import org.apache.beam.sdk.transforms.DoFn.ProcessContext;
import org.apache.beam.sdk.transforms.join.CoGbkResult;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.TupleTag;

public class ToBqPersonShapeFn
    extends DoFn<KV<String, CoGbkResult> , TableRow>
{
    public static TupleTag<TableRow> deadLetterTag = (new TupleTag<TableRow>(){});
    public static TupleTag<TableRow> successTag = (new TupleTag<TableRow>(){});

    @ProcessElement
    public void processElement(ProcessContext c, PipelineOptions options) {
        ErrorBuilder errorBuilder = new ErrorBuilder();
        try {
            TableRow outputRow = new TableRow();
            KV<String, CoGbkResult> e = c.element();
            TableRow personContactRow = sourceRow(e, BqPersonShapeBeam.personContactTag);
            TableRow personNameRow = sourceRow(e, BqPersonShapeBeam.personNameTag);
            id(errorBuilder, outputRow, personContactRow, personNameRow);
            phoneNumber(errorBuilder, outputRow, personContactRow);
            givenName(errorBuilder, outputRow, personNameRow);
            if (outputRow.isEmpty()) {
                errorBuilder.addError("record is empty");
            }
            if (!errorBuilder.isEmpty()) {
                TableRow errorRow = new TableRow();
                errorRow.set("errorId", Generators.timeBasedGenerator().generate().toString());
                errorRow.set("errorCreated", (new Date().getTime()/ 1000));
                errorRow.set("errorMessage", errorBuilder.toString());
                errorRow.set("pipelineJobName", options.getJobName());
                errorRow.set("PersonContact", personContactRow);
                errorRow.set("PersonName", personNameRow);
                c.output(deadLetterTag, errorRow);
            } else {
                c.output(successTag, outputRow);
            }
        } catch (final Throwable oops) {
            oops.printStackTrace();
        }
    }

    private String id(ErrorBuilder errorBuilder,
        TableRow bqPersonRow,
        TableRow personContactRow,
        TableRow personNameRow) {
        String id = personContactRow.get("id");
        id = ((id == null)?personNameRow.get("id"):id);
        if (id!= null) {
            bqPersonRow.set("id", id);
        } else {
            errorBuilder.addError("Required property 'id' is null");
        }
        return id;
    }

    private String phoneNumber(ErrorBuilder errorBuilder, TableRow bqPersonRow, TableRow personContactRow) {
        String phoneNumber = ((String) personContactRow.get("phone_number"));
        if (phoneNumber!= null) {
            bqPersonRow.set("phoneNumber", phoneNumber);
        }
        return phoneNumber;
    }

    private String givenName(ErrorBuilder errorBuilder, TableRow bqPersonRow, TableRow personNameRow) {
        String givenName = ((String) personNameRow.get("first_name"));
        if (givenName!= null) {
            bqPersonRow.set("givenName", givenName);
        }
        return givenName;
    }

    private TableRow sourceRow(KV<String, CoGbkResult> e, TupleTag<TableRow> tag) {
        Iterator<TableRow> sequence = e.getValue().getAll(tag).iterator();
        return (sequence.hasNext()?sequence.next():null);
    }
}
