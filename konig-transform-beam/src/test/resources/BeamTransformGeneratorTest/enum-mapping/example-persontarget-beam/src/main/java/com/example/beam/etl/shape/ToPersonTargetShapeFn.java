package com.example.beam.etl.shape;

import java.text.MessageFormat;
import java.util.Date;
import com.example.beam.etl.common.ErrorBuilder;
import com.example.beam.etl.shape.PersonTargetShapeBeam.Options;
import com.fasterxml.uuid.Generators;
import com.google.api.services.bigquery.model.TableRow;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.DoFn.ProcessElement;
import org.apache.beam.sdk.transforms.DoFn.ProcessContext;
import org.apache.beam.sdk.values.TupleTag;

public class ToPersonTargetShapeFn
    extends DoFn<TableRow, TableRow>
{
    public static TupleTag<TableRow> deadLetterTag = (new TupleTag<TableRow>(){});
    public static TupleTag<TableRow> successTag = (new TupleTag<TableRow>(){});

    @ProcessElement
    public void processElement(ProcessContext c, PipelineOptions pipelineOptions) {
        Options options = pipelineOptions.as(Options.class);
        ErrorBuilder errorBuilder = new ErrorBuilder();
        try {
            TableRow outputRow = new TableRow();
            TableRow personSourceRow = ((TableRow) c.element());
            id(errorBuilder, outputRow, personSourceRow);
            gender(errorBuilder, outputRow, personSourceRow);
            if (outputRow.isEmpty()) {
                errorBuilder.addError("record is empty");
            }
            if (!errorBuilder.isEmpty()) {
                TableRow errorRow = new TableRow();
                errorRow.set("errorId", Generators.timeBasedGenerator().generate().toString());
                errorRow.set("errorCreated", (new Date().getTime()/ 1000));
                errorRow.set("errorMessage", errorBuilder.toString());
                errorRow.set("pipelineJobName", options.getJobName());
                errorRow.set("PersonSource", personSourceRow);
                c.output(deadLetterTag, errorRow);
            } else {
                c.output(successTag, outputRow);
            }
        } catch (final Throwable oops) {
            oops.printStackTrace();
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

    private TableRow gender(ErrorBuilder errorBuilder, TableRow personTargetRow, TableRow personSourceRow) {
        TableRow genderRow = new TableRow();
        String gender_code = ((String) personSourceRow.get("gender_code"));
        if (gender_code == null) {
            errorBuilder.addError("Cannot set required property 'gender' because '{PersonSourceShape}.gender_code' is null");
            return genderRow;
        }
        com.example.beam.etl.schema.GenderType gender = com.example.beam.etl.schema.GenderType.findByGenderCode(gender_code);
        if (gender == null) {
            String msg = MessageFormat.format("Cannot set gender because '{PersonSourceShape}.gender_code' = ''{0}'' does not map to a valid enum value", gender_code);
            errorBuilder.addError(msg);
            return genderRow;
        }
        gender_id(errorBuilder, genderRow, gender);
        gender_name(errorBuilder, genderRow, gender);
        gender_genderCode(errorBuilder, genderRow, gender);
        if (!genderRow.isEmpty()) {
            personTargetRow.set("gender", genderRow);
        } else {
            errorBuilder.addError("Required property 'gender' is null");
        }
        return genderRow;
    }

    private String gender_id(ErrorBuilder errorBuilder, TableRow genderRow, com.example.beam.etl.schema.GenderType gender) {
        String id = ((String)((gender!= null)?gender.getId().getLocalName():null));
        if (id!= null) {
            genderRow.set("id", id);
        } else {
            errorBuilder.addError("Required property 'gender.id' is null");
        }
        return id;
    }

    private String gender_name(ErrorBuilder errorBuilder, TableRow genderRow, com.example.beam.etl.schema.GenderType gender) {
        String name = ((String)((gender!= null)?gender.getName():null));
        if (name!= null) {
            genderRow.set("name", name);
        } else {
            errorBuilder.addError("Required property 'gender.name' is null");
        }
        return name;
    }

    private String gender_genderCode(ErrorBuilder errorBuilder, TableRow genderRow, com.example.beam.etl.schema.GenderType gender) {
        String genderCode = ((String)((gender!= null)?gender.getGenderCode():null));
        if (genderCode!= null) {
            genderRow.set("genderCode", genderCode);
        } else {
            errorBuilder.addError("Required property 'gender.genderCode' is null");
        }
        return genderCode;
    }
}
