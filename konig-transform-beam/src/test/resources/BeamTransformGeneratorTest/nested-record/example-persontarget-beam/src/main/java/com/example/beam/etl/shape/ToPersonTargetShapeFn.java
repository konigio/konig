package com.example.beam.etl.shape;

import com.example.beam.etl.common.ErrorBuilder;
import com.google.api.services.bigquery.model.TableRow;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.DoFn.ProcessContext;

public class ToPersonTargetShapeFn
    extends DoFn<TableRow, TableRow>
{

    @DoFn.ProcessElement
    public void processElement(ProcessContext c) {
        try {
            ErrorBuilder errorBuilder = new ErrorBuilder();
            TableRow outputRow = new TableRow();
            TableRow personSourceRow = c.element();
            id(personSourceRow, outputRow, errorBuilder);
            address(personSourceRow, outputRow, errorBuilder);
            if (!outputRow.isEmpty()) {
                c.output(outputRow);
            }
        } catch (final Throwable oops) {
            oops.printStackTrace();
        }
    }

    private boolean id(TableRow personSourceRow, TableRow outputRow, ErrorBuilder errorBuilder) {
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

    private boolean address(TableRow personSourceRow, TableRow outputRow, ErrorBuilder errorBuilder) {
        TableRow address = new TableRow();
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

    private boolean address_id(TableRow personSourceRow, TableRow outputRow, ErrorBuilder errorBuilder) {
        Object address_id = ((personSourceRow == null)?null:personSourceRow.get("address_id"));
        if (address_id!= null) {
            outputRow.set("id", address_id);
            return true;
        } else {
            errorBuilder.addError("Cannot set address.id because {PersonSourceShape}.address_id is null");
            return false;
        }
    }

    private boolean address_addressLocality(TableRow personSourceRow, TableRow outputRow, ErrorBuilder errorBuilder) {
        Object city = ((personSourceRow == null)?null:personSourceRow.get("city"));
        if (city!= null) {
            outputRow.set("addressLocality", city);
            return true;
        } else {
            errorBuilder.addError("Cannot set address.addressLocality because {PersonSourceShape}.city is null");
            return false;
        }
    }

    private boolean address_addressRegion(TableRow personSourceRow, TableRow outputRow, ErrorBuilder errorBuilder) {
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
