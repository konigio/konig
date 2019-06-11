package com.example.beam.etl.shape;

import java.util.Iterator;
import java.util.Map;
import com.example.beam.etl.common.ErrorBuilder;
import com.google.api.client.util.DateTime;
import com.google.cloud.Date;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.DoFn.ProcessElement;
import org.apache.beam.sdk.transforms.join.CoGbkResult;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.TupleTag;

public class PersonTargetMergeFn
    extends DoFn<KV<String, CoGbkResult> , com.google.api.services.bigquery.model.TableRow>
{
    public static TupleTag<String> deadLetterTag = new TupleTag<String>();
    public static TupleTag<com.google.api.services.bigquery.model.TableRow> successTag = new TupleTag<com.google.api.services.bigquery.model.TableRow>();

    @ProcessElement
    public void processElement(DoFn.ProcessContext c) {
        try {
            KV<String, CoGbkResult> e = c.element();
            com.google.api.services.bigquery.model.TableRow outputRow = baselineRow(e, personTargetTag);
            overlay(outputRow, e, personSourceTag);
            if (!outputRow.isEmpty()) {
                c.output(successTag, outputRow);
            }
        } catch (final Throwable oops) {
            c.output(deadLetterTag, oops.getMessage());
        }
    }

    private com.google.api.services.bigquery.model.TableRow baselineRow(KV<String, CoGbkResult> e, TupleTag<com.google.api.services.bigquery.model.TableRow> tupleTag) {
        Iterator<com.google.api.services.bigquery.model.TableRow> sequence = e.getValue().getAll(tupleTag).iterator();
        com.google.api.services.bigquery.model.TableRow result = null;
        Long latest = null;
        while (sequence.hasNext()) {
            com.google.api.services.bigquery.model.TableRow row = sequence.next();
            Long modified = dateTime(row, "modified");
            if ((modified!= null)&&((latest == null)||(modified >latest))) {
                latest = modified;
                result = row;
            }
        }
        if (result == null) {
            result = new com.google.api.services.bigquery.model.TableRow();
        }
        return result;
    }

    private Long dateTime(com.google.api.services.bigquery.model.TableRow row, String fieldName) {
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

    private void overlay(com.google.api.services.bigquery.model.TableRow outputRow, KV<String, CoGbkResult> e, TupleTag<com.google.api.services.bigquery.model.TableRow> tupleTag) {
        Iterator<com.google.api.services.bigquery.model.TableRow> sequence = e.getValue().getAll(tupleTag).iterator();
        while (sequence.hasNext()) {
            com.google.api.services.bigquery.model.TableRow sourceRow = sequence.next();
            com.google.api.services.bigquery.model.TableRow targetRow = transform(sourceRow);
            if (targetRow!= null) {
                copy(targetRow, outputRow);
            }
        }
    }

    private com.google.api.services.bigquery.model.TableRow transform(com.google.api.services.bigquery.model.TableRow sourceRow) {
        com.google.api.services.bigquery.model.TableRow targetRow = new com.google.api.services.bigquery.model.TableRow();
        ErrorBuilder errorBuilder = new ErrorBuilder();
        id(sourceRow, targetRow, errorBuilder);
        email(sourceRow, targetRow, errorBuilder);
        givenName(sourceRow, targetRow, errorBuilder);
        modified(targetRow, errorBuilder);
        return targetRow;
    }

    private void id(com.google.api.services.bigquery.model.TableRow sourceRow, com.google.api.services.bigquery.model.TableRow outputRow, ErrorBuilder errorBuilder) {
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

    private void email(com.google.api.services.bigquery.model.TableRow sourceRow, com.google.api.services.bigquery.model.TableRow outputRow, ErrorBuilder errorBuilder) {
        Object email = ((sourceRow == null)?null:sourceRow.get("email"));
        if (email!= null) {
            outputRow.set("email", email);
        } else {
            errorBuilder.addError("Cannot set email because {PersonSourceShape}.email is null");
        }
    }

    private void givenName(com.google.api.services.bigquery.model.TableRow sourceRow, com.google.api.services.bigquery.model.TableRow outputRow, ErrorBuilder errorBuilder) {
        Object first_name = ((sourceRow == null)?null:sourceRow.get("first_name"));
        if (first_name!= null) {
            outputRow.set("givenName", first_name);
        } else {
            errorBuilder.addError("Cannot set givenName because {PersonSourceShape}.first_name is null");
        }
    }

    private void modified(com.google.api.services.bigquery.model.TableRow outputRow, ErrorBuilder errorBuilder) {
        outputRow.set("modified", new Long(new Date().getTime()));
    }

    private void copy(com.google.api.services.bigquery.model.TableRow targetRow, com.google.api.services.bigquery.model.TableRow outputRow) {
        for (Map.Entry<String, Object> entry: targetRow.entrySet()) {
            String fieldName = entry.getKey();
            Object value = entry.getValue();
            if (value instanceof com.google.api.services.bigquery.model.TableRow) {
                Object outputValue = outputRow.get(fieldName);
                if (outputValue instanceof com.google.api.services.bigquery.model.TableRow) {
                    copy(((com.google.api.services.bigquery.model.TableRow) value), ((com.google.api.services.bigquery.model.TableRow) outputValue));
                } else {
                    outputRow.put(fieldName, value);
                }
            } else {
                outputRow.put(fieldName, value);
            }
        }
    }
}
