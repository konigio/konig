package com.example.beam.etl.shape;

import java.util.Iterator;
import java.util.Map;
import com.example.beam.etl.common.ErrorBuilder;
import com.google.api.client.util.DateTime;
import com.google.api.services.bigquery.model.TableRow;
import com.google.cloud.Date;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.DoFn.ProcessElement;
import org.apache.beam.sdk.transforms.join.CoGbkResult;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.TupleTag;

public class PersonTargetMergeFn
    extends DoFn<KV<String, CoGbkResult> , TableRow>
{

    @ProcessElement
    public void processElement(DoFn.ProcessContext c) {
        try {
            KV<String, CoGbkResult> e = c.element();
            TableRow outputRow = baselineRow(e, personTargetTag);
            overlay(outputRow, e, personSourceTag);
            if (!outputRow.isEmpty()) {
                c.output(outputRow);
            }
        } catch (final Throwable oops) {
            oops.printStackTrace()();
        }
    }

    private TableRow baselineRow(KV<String, CoGbkResult> e, TupleTag<TableRow> tupleTag) {
        Iterator<TableRow> sequence = e.getValue().getAll(tupleTag).iterator();
        TableRow result = null;
        Long latest = null;
        while (sequence.hasNext()) {
            TableRow row = sequence.next();
            Long modified = dateTime(row, "modified");
            if ((modified!= null)&&((latest == null)||(modified >latest))) {
                latest = modified;
                result = row;
            }
        }
        if (result == null) {
            result = new TableRow();
        }
        return result;
    }

    private Long dateTime(TableRow row, String fieldName) {
        Object value = row.get(fieldName);
        Long result = null;
        if (value instanceof String) {
            result = new DateTime(((String) value)).getValue();
            row.set(fieldName, result);
        } else {
            if (value instanceof Long) {
                result = ((Long) value);
            }
        }
        return result;
    }

    private void overlay(TableRow outputRow, KV<String, CoGbkResult> e, TupleTag<TableRow> tupleTag) {
        Iterator<TableRow> sequence = e.getValue().getAll(tupleTag).iterator();
        while (sequence.hasNext()) {
            TableRow sourceRow = sequence.next();
            TableRow targetRow = transform(sourceRow);
            if (targetRow!= null) {
                copy(targetRow, outputRow);
            }
        }
    }

    private TableRow transform(TableRow sourceRow) {
        TableRow targetRow = new TableRow();
        ErrorBuilder errorBuilder = new ErrorBuilder();
        id(sourceRow, targetRow, errorBuilder);
        email(sourceRow, targetRow, errorBuilder);
        givenName(sourceRow, targetRow, errorBuilder);
        modified(targetRow, errorBuilder);
        return targetRow;
    }

    private void id(TableRow sourceRow, TableRow outputRow, ErrorBuilder errorBuilder) {
        Object person_id = ((sourceRow == null)?null:sourceRow.get("person_id"));
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

    private void email(TableRow sourceRow, TableRow outputRow, ErrorBuilder errorBuilder) {
        Object email = ((sourceRow == null)?null:sourceRow.get("email"));
        if (email!= null) {
            outputRow.set("email", email);
        } else {
            errorBuilder.addError("Cannot set email because {PersonSourceShape}.email is null");
        }
    }

    private void givenName(TableRow sourceRow, TableRow outputRow, ErrorBuilder errorBuilder) {
        Object first_name = ((sourceRow == null)?null:sourceRow.get("first_name"));
        if (first_name!= null) {
            outputRow.set("givenName", first_name);
        } else {
            errorBuilder.addError("Cannot set givenName because {PersonSourceShape}.first_name is null");
        }
    }

    private void modified(TableRow outputRow, ErrorBuilder errorBuilder) {
        outputRow.set("modified", new Long(new Date().getTime()));
    }

    private void copy(TableRow targetRow, TableRow outputRow) {
        for (Map.Entry<String, Object> entry: targetRow.entrySet()) {
            String fieldName = entry.getKey();
            Object value = entry.getValue();
            if (value instanceof TableRow) {
                Object outputValue = outputRow.get(fieldName);
                if (outputValue instanceof TableRow) {
                    copy(((TableRow) value), ((TableRow) outputValue));
                } else {
                    outputRow.put(fieldName, value);
                }
            } else {
                outputRow.put(fieldName, value);
            }
        }
    }
}
