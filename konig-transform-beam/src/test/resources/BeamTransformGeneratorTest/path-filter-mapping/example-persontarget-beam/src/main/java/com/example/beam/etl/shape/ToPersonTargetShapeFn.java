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
            identifiedBy(personSourceRow, outputRow, errorBuilder);
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

    private boolean identifiedBy(TableRow personSourceRow, TableRow outputRow, ErrorBuilder errorBuilder) {
        TableRow identifiedBy = new TableRow();
        identifiedBy_identityProvider(identifiedBy, errorBuilder);
        identifiedBy_identifier(personSourceRow, identifiedBy, errorBuilder);
        if (errorBuilder.isEmpty()&&(!identifiedBy.isEmpty())) {
            outputRow.set("identifiedBy", identifiedBy);
            return true;
        } else {
            return false;
        }
    }

    private boolean identifiedBy_identityProvider(TableRow outputRow, ErrorBuilder errorBuilder) {
        outputRow.set("identityProvider", "MDM");
        return true;
    }

    private boolean identifiedBy_identifier(TableRow personSourceRow, TableRow outputRow, ErrorBuilder errorBuilder) {
        Object mdm_id = ((personSourceRow == null)?null:personSourceRow.get("mdm_id"));
        if (mdm_id!= null) {
            outputRow.set("identifier", mdm_id);
            return true;
        } else {
            errorBuilder.addError("Cannot set identifiedBy.identifier because {PersonSourceShape}.mdm_id is null");
            return false;
        }
    }
}
