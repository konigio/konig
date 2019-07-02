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
            com.google.api.services.bigquery.model.TableRow personSourceRow = ((com.google.api.services.bigquery.model.TableRow) c.element());
            heightInches(personSourceRow, outputRow, errorBuilder);
            id(personSourceRow, outputRow, errorBuilder);
            isDummyFlag(personSourceRow, outputRow, errorBuilder);
            if ((!outputRow.isEmpty())&&errorBuilder.isEmpty()) {
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

    private void heightInches(com.google.api.services.bigquery.model.TableRow personSourceRow, com.google.api.services.bigquery.model.TableRow outputRow, ErrorBuilder errorBuilder) {
        Object person_height = ((personSourceRow == null)?null:personSourceRow.get("person_height"));
        if (person_height!= null) {
            outputRow.set("heightInches", floatValue(person_height, errorBuilder));
        }
    }

    private Float floatValue(Object person_height, ErrorBuilder errorBuilder) {
        try {
            if ((person_height!= null)&&(person_height instanceof Float)) {
                return ((Float) person_height);
            }
        } catch (final Exception ex) {
            String message = String.format("Invalid Float value %s for field heightInches;", String.valueOf(person_height));
            errorBuilder.addError(message);
        }
        return null;
    }

    private void id(com.google.api.services.bigquery.model.TableRow personSourceRow, com.google.api.services.bigquery.model.TableRow outputRow, ErrorBuilder errorBuilder) {
        Object person_id = ((personSourceRow == null)?null:personSourceRow.get("person_id"));
        if (person_id!= null) {
            outputRow.set("id", concat("http://example.com/person/", person_id));
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

    private void isDummyFlag(com.google.api.services.bigquery.model.TableRow personSourceRow, com.google.api.services.bigquery.model.TableRow outputRow, ErrorBuilder errorBuilder) {
        Object dummy_flag = ((personSourceRow == null)?null:personSourceRow.get("dummy_flag"));
        if (dummy_flag!= null) {
            outputRow.set("isDummyFlag", booleanValue(dummy_flag, errorBuilder));
        }
    }

    private Boolean booleanValue(Object dummy_flag, ErrorBuilder errorBuilder) {
        try {
            if ((dummy_flag!= null)&&(dummy_flag instanceof Boolean)) {
                return "true".equalsIgnoreCase(((Boolean) dummy_flag));
            }
        } catch (final Exception ex) {
            String message = String.format("Invalid Boolean value %s for field isDummyFlag;", String.valueOf(dummy_flag));
            errorBuilder.addError(message);
        }
        return null;
    }
}
