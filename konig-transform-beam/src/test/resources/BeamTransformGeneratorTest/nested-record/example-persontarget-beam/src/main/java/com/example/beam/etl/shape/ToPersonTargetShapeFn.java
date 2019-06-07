package com.example.beam.etl.shape;

import com.example.beam.etl.common.ErrorBuilder;
import com.google.api.services.bigquery.model.TableRow;
import org.apache.beam.sdk.transforms.DoFn;

public class ToPersonTargetShapeFn
    extends DoFn<TableRow, TableRow>
{

    @DoFn.ProcessElement
    public void processElement(DoFn.ProcessContext c) {
        try {
            ErrorBuilder errorBuilder = new ErrorBuilder();
            TableRow outputRow = new TableRow();
            TableRow personSourceRow = c.element();
            id(personSourceRow, outputRow, errorBuilder);
            address(personSourceRow, outputRow, errorBuilder);
            if (!outputRow.isEmpty()) {
                c.output(outputRow);
            }
        }
    }

    private void id(TableRow personSourceRow, TableRow outputRow, ErrorBuilder errorBuilder) {
        Object person_id = ((personSourceRow == null)?null:personSourceRow.get("person_id"));
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

    private void address(TableRow personSourceRow, TableRow outputRow, ErrorBuilder errorBuilder) {
        TableRow address = new TableRow();
        address_id(personSourceRow, address, errorBuilder);
        address_addressLocality(personSourceRow, address, errorBuilder);
        address_addressRegion(personSourceRow, address, errorBuilder);
        if (!address.isEmpty()) {
            outputRow.set("address", address);
        }
    }

    private void address_id(TableRow personSourceRow, TableRow outputRow, ErrorBuilder errorBuilder) {
        Object address_id = ((personSourceRow == null)?null:personSourceRow.get("address_id"));
        if (address_id!= null) {
            outputRow.set("id", address_id);
        } else {
            errorBuilder.addError("Cannot set address.id because {PersonSourceShape}.address_id is null");
        }
    }

    private void address_addressLocality(TableRow personSourceRow, TableRow outputRow, ErrorBuilder errorBuilder) {
        Object city = ((personSourceRow == null)?null:personSourceRow.get("city"));
        if (city!= null) {
            outputRow.set("addressLocality", city);
        } else {
            errorBuilder.addError("Cannot set address.addressLocality because {PersonSourceShape}.city is null");
        }
    }

    private void address_addressRegion(TableRow personSourceRow, TableRow outputRow, ErrorBuilder errorBuilder) {
        Object state = ((personSourceRow == null)?null:personSourceRow.get("state"));
        if (state!= null) {
            outputRow.set("addressRegion", state);
        } else {
            errorBuilder.addError("Cannot set address.addressRegion because {PersonSourceShape}.state is null");
        }
    }
}
