package com.example.beam.etl.shape;

import java.util.Date;
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
            id(personSourceRow, outputRow, errorBuilder);
            modifiedDate(personSourceRow, outputRow, errorBuilder);
            birthDate(personSourceRow, outputRow, errorBuilder);
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

    private void modifiedDate(com.google.api.services.bigquery.model.TableRow personSourceRow, com.google.api.services.bigquery.model.TableRow outputRow, ErrorBuilder errorBuilder) {
        Object modified_date = ((personSourceRow == null)?null:personSourceRow.get("modified_date"));
        if (modified_date!= null) {
            outputRow.set("modifiedDate", longValue(modified_date, errorBuilder));
        }
    }

    private Long longValue(Object modified_date, ErrorBuilder errorBuilder) {
        try {
            if ((modified_date!= null)&&(modified_date instanceof Long)) {
                return ((Long) modified_date);
            }
        } catch (final Exception ex) {
            String message = String.format("Invalid Long value %s for field modifiedDate;", String.valueOf(modified_date));
            errorBuilder.addError(message);
        }
        return null;
    }

    private void birthDate(com.google.api.services.bigquery.model.TableRow personSourceRow, com.google.api.services.bigquery.model.TableRow outputRow, ErrorBuilder errorBuilder) {
        Object birth_date = ((personSourceRow == null)?null:personSourceRow.get("birth_date"));
        if (birth_date!= null) {
            outputRow.set("birthDate", dateValue(birth_date, errorBuilder));
        }
    }

    private Date dateValue(Object birth_date, ErrorBuilder errorBuilder) {
        try {
            if ((birth_date!= null)&&(birth_date instanceof Date)) {
                return ((Date) birth_date);
            }
        } catch (final Exception ex) {
            String message = String.format("Invalid Date value %s for field birthDate;", String.valueOf(birth_date));
            errorBuilder.addError(message);
        }
        return null;
    }
}
