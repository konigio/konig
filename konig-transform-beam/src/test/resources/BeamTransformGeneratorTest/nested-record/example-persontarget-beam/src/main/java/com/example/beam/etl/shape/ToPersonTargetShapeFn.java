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
            id(personSourceRow, outputRow, errorBuilder);
            address(personSourceRow, outputRow, errorBuilder);
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

    private boolean address(com.google.api.services.bigquery.model.TableRow personSourceRow, com.google.api.services.bigquery.model.TableRow outputRow, ErrorBuilder errorBuilder) {
        com.google.api.services.bigquery.model.TableRow address = new com.google.api.services.bigquery.model.TableRow();
        address_id(personSourceRow, address, errorBuilder);
        address_addressLocality(personSourceRow, address, errorBuilder);
        address_addressRegion(personSourceRow, address, errorBuilder);
        if (errorBuilder.isEmpty()&&(!address.isEmpty())) {
            outputRow.set("address", address);
            return true;
        } else {
            return false;
        }
    }

    private boolean address_id(com.google.api.services.bigquery.model.TableRow personSourceRow, com.google.api.services.bigquery.model.TableRow outputRow, ErrorBuilder errorBuilder) {
        Object address_id = ((personSourceRow == null)?null:personSourceRow.get("address_id"));
        if (address_id!= null) {
            outputRow.set("id", address_id);
            return true;
        } else {
            errorBuilder.addError("Cannot set address.id because {PersonSourceShape}.address_id is null");
            return false;
        }
    }

    private boolean address_addressLocality(com.google.api.services.bigquery.model.TableRow personSourceRow, com.google.api.services.bigquery.model.TableRow outputRow, ErrorBuilder errorBuilder) {
        Object city = ((personSourceRow == null)?null:personSourceRow.get("city"));
        if (city!= null) {
            outputRow.set("addressLocality", city);
            return true;
        } else {
            errorBuilder.addError("Cannot set address.addressLocality because {PersonSourceShape}.city is null");
            return false;
        }
    }

    private boolean address_addressRegion(com.google.api.services.bigquery.model.TableRow personSourceRow, com.google.api.services.bigquery.model.TableRow outputRow, ErrorBuilder errorBuilder) {
        Object state = ((personSourceRow == null)?null:personSourceRow.get("state"));
        if (state!= null) {
            outputRow.set("addressRegion", state);
            return true;
        } else {
            errorBuilder.addError("Cannot set address.addressRegion because {PersonSourceShape}.state is null");
            return false;
        }
    }
}
