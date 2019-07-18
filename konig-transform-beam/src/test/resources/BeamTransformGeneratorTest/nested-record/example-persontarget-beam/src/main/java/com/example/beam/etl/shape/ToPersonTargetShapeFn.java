package com.example.beam.etl.shape;

import com.example.beam.etl.common.ErrorBuilder;
import com.google.api.services.bigquery.model.TableRow;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.DoFn.ProcessElement;
import org.apache.beam.sdk.transforms.DoFn.ProcessContext;
import org.apache.beam.sdk.values.TupleTag;

public class ToPersonTargetShapeFn
    extends DoFn<TableRow, TableRow>
{
    public static TupleTag<String> deadLetterTag = new TupleTag<String>();
    public static TupleTag<TableRow> successTag = new TupleTag<TableRow>();

    @ProcessElement
    public void processElement(ProcessContext c) {
        ErrorBuilder errorBuilder = new ErrorBuilder();
        try {
            TableRow outputRow = new TableRow();
            TableRow personSourceRow = ((TableRow) c.element());
            id(errorBuilder, outputRow, personSourceRow);
            address(errorBuilder, outputRow, personSourceRow);
            if (outputRow.isEmpty()) {
                errorBuilder.addError("record is empty");
            }
            if (!errorBuilder.isEmpty()) {
                c.output(deadLetterTag, errorBuilder.toString());
            } else {
                c.output(successTag, outputRow);
            }
        } catch (final Throwable oops) {
            errorBuilder.addError(oops.getMessage());
            c.output(deadLetterTag, errorBuilder.toString());
        }
    }

    private String id(ErrorBuilder errorBuilder, TableRow personTargetRow, TableRow personSourceRow) {
        String id = ((String) concat("http://example.com/person/", personSourceRow.get("person_id")));
        if (id!= null) {
            personTargetRow.set("id", id);
        } else {
            errorBuilder.addError("Required property 'id' is null");
        }
        return id;
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

    private TableRow address(ErrorBuilder errorBuilder, TableRow personTargetRow, TableRow personSourceRow) {
        TableRow addressRow = new TableRow();
        address_id(errorBuilder, addressRow, personSourceRow);
        address_addressLocality(errorBuilder, addressRow, personSourceRow);
        address_addressRegion(errorBuilder, addressRow, personSourceRow);
        if (addressRow!= null) {
            personTargetRow.set("address", addressRow);
        }
        return addressRow;
    }

    private String address_id(ErrorBuilder errorBuilder, TableRow addressRow, TableRow personSourceRow) {
        String id = ((String) personSourceRow.get("address_id"));
        if (id!= null) {
            addressRow.set("id", id);
        } else {
            errorBuilder.addError("Required property 'address.id' is null");
        }
        return id;
    }

    private String address_addressLocality(ErrorBuilder errorBuilder, TableRow addressRow, TableRow personSourceRow) {
        String addressLocality = ((String) personSourceRow.get("city"));
        if (addressLocality!= null) {
            addressRow.set("addressLocality", addressLocality);
        }
        return addressLocality;
    }

    private String address_addressRegion(ErrorBuilder errorBuilder, TableRow addressRow, TableRow personSourceRow) {
        String addressRegion = ((String) personSourceRow.get("state"));
        if (addressRegion!= null) {
            addressRow.set("addressRegion", addressRegion);
        }
        return addressRegion;
    }
}
