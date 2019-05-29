package com.example.beam.etl.shape;

import java.util.Iterator;
import com.example.beam.etl.common.ErrorBuilder;
import com.google.api.services.bigquery.model.TableRow;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.join.CoGbkResult;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.TupleTag;

public class MergeAddressSourceAndPersonNameSourceFn
    extends DoFn<KV<String, CoGbkResult> , TableRow>
{

    @DoFn.ProcessElement
    public void processElement(DoFn.ProcessContext c) {
        try {
            ErrorBuilder errorBuilder = new ErrorBuilder();
            KV<String, CoGbkResult> e = c.element();
            TableRow outputRow = new TableRow();
            TableRow addressSourceRow = sourceRow(e, PersonTargetShapeBeam.addressSourceTag);
            TableRow personNameSourceRow = sourceRow(e, PersonTargetShapeBeam.personNameSourceTag);
            id(personNameSourceRow, outputRow, errorBuilder);
            address(addressSourceRow, outputRow, errorBuilder);
            givenName(personNameSourceRow, outputRow, errorBuilder);
            if (!outputRow.isEmpty()) {
                c.output(outputRow);
            }
        }
    }

    private void id(TableRow personNameSourceRow, TableRow outputRow, ErrorBuilder errorBuilder) {
        Object id = ((personNameSourceRow == null)?null:personNameSourceRow.get("id"));
        if (id!= null) {
            outputRow.set("id", id);
        } else {
            errorBuilder.addError("Cannot set id because {PersonNameSourceShape}.id is null");
        }
    }

    private void address(TableRow addressSourceRow, TableRow outputRow, ErrorBuilder errorBuilder) {
        TableRow address = new TableRow();
        address_addressLocality(addressSourceRow, address, errorBuilder);
        address_addressRegion(addressSourceRow, address, errorBuilder);
        if (!address.isEmpty()) {
            outputRow.set("address", address);
        }
    }

    private void address_addressLocality(TableRow addressSourceRow, TableRow outputRow, ErrorBuilder errorBuilder) {
        Object city = ((addressSourceRow == null)?null:addressSourceRow.get("city"));
        if (city!= null) {
            outputRow.set("addressLocality", city);
        } else {
            errorBuilder.addError("Cannot set address.addressLocality because {AddressSourceShape}.city is null");
        }
    }

    private void address_addressRegion(TableRow addressSourceRow, TableRow outputRow, ErrorBuilder errorBuilder) {
        Object state = ((addressSourceRow == null)?null:addressSourceRow.get("state"));
        if (state!= null) {
            outputRow.set("addressRegion", state);
        } else {
            errorBuilder.addError("Cannot set address.addressRegion because {AddressSourceShape}.state is null");
        }
    }

    private void givenName(TableRow personNameSourceRow, TableRow outputRow, ErrorBuilder errorBuilder) {
        Object first_name = ((personNameSourceRow == null)?null:personNameSourceRow.get("first_name"));
        if (first_name!= null) {
            outputRow.set("givenName", first_name);
        } else {
            errorBuilder.addError("Cannot set givenName because {PersonNameSourceShape}.first_name is null");
        }
    }

    public TableRow sourceRow(KV<String, CoGbkResult> e, TupleTag<TableRow> tag) {
        Iterator<TableRow> sequence = e.getValue().getAll(tag).iterator();
        return (sequence.hasNext()?sequence.next():null);
    }
}
