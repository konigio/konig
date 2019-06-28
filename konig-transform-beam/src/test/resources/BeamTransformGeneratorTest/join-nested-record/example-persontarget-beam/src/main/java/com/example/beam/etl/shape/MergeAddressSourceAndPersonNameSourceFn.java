package com.example.beam.etl.shape;

import java.util.Iterator;
import com.example.beam.etl.common.ErrorBuilder;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.DoFn.ProcessContext;
import org.apache.beam.sdk.transforms.join.CoGbkResult;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.TupleTag;

public class MergeAddressSourceAndPersonNameSourceFn
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
            com.google.api.services.bigquery.model.TableRow addressSourceRow = sourceRow(e, PersonTargetShapeBeam.addressSourceTag);
            com.google.api.services.bigquery.model.TableRow personNameSourceRow = sourceRow(e, PersonTargetShapeBeam.personNameSourceTag);
            id(personNameSourceRow, outputRow, errorBuilder);
            address(addressSourceRow, outputRow, errorBuilder);
            givenName(personNameSourceRow, outputRow, errorBuilder);
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

    public com.google.api.services.bigquery.model.TableRow sourceRow(KV<String, CoGbkResult> e, TupleTag<com.google.api.services.bigquery.model.TableRow> tag) {
        Iterator<com.google.api.services.bigquery.model.TableRow> sequence = e.getValue().getAll(tag).iterator();
        return (sequence.hasNext()?sequence.next():null);
    }

    private void id(com.google.api.services.bigquery.model.TableRow personNameSourceRow, com.google.api.services.bigquery.model.TableRow outputRow, ErrorBuilder errorBuilder) {
        Object id = ((personNameSourceRow == null)?null:personNameSourceRow.get("id"));
        if (id!= null) {
            outputRow.set("id", id);
        }
    }

    private void address(com.google.api.services.bigquery.model.TableRow addressSourceRow, com.google.api.services.bigquery.model.TableRow outputRow, ErrorBuilder errorBuilder) {
        com.google.api.services.bigquery.model.TableRow address = new com.google.api.services.bigquery.model.TableRow();
        address_addressLocality(addressSourceRow, address, errorBuilder);
        address_addressRegion(addressSourceRow, address, errorBuilder);
        if (errorBuilder.isEmpty()&&(!address.isEmpty())) {
            outputRow.set("address", address);
        }
    }

    private void address_addressLocality(com.google.api.services.bigquery.model.TableRow addressSourceRow, com.google.api.services.bigquery.model.TableRow outputRow, ErrorBuilder errorBuilder) {
        Object city = ((addressSourceRow == null)?null:addressSourceRow.get("city"));
        if (city!= null) {
            outputRow.set("addressLocality", city);
        }
    }

    private void address_addressRegion(com.google.api.services.bigquery.model.TableRow addressSourceRow, com.google.api.services.bigquery.model.TableRow outputRow, ErrorBuilder errorBuilder) {
        Object state = ((addressSourceRow == null)?null:addressSourceRow.get("state"));
        if (state!= null) {
            outputRow.set("addressRegion", state);
        }
    }

    private void givenName(com.google.api.services.bigquery.model.TableRow personNameSourceRow, com.google.api.services.bigquery.model.TableRow outputRow, ErrorBuilder errorBuilder) {
        Object first_name = ((personNameSourceRow == null)?null:personNameSourceRow.get("first_name"));
        if (first_name!= null) {
            outputRow.set("givenName", first_name);
        } else {
            errorBuilder.addError("Cannot set givenName because {PersonNameSourceShape}.first_name is null");
        }
    }
}
