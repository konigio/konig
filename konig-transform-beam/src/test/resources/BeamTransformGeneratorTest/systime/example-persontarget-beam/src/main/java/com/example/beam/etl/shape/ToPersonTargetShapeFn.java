package com.example.beam.etl.shape;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.example.beam.etl.common.ErrorBuilder;
import com.example.beam.etl.shape.PersonTargetShapeBeam.Options;
import com.fasterxml.uuid.Generators;
import com.google.api.client.util.DateTime;
import com.google.api.services.bigquery.model.TableRow;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.DoFn.ProcessElement;
import org.apache.beam.sdk.transforms.DoFn.ProcessContext;
import org.apache.beam.sdk.values.TupleTag;

public class ToPersonTargetShapeFn
    extends DoFn<TableRow, TableRow>
{
    public static TupleTag<TableRow> deadLetterTag = (new TupleTag<TableRow>(){});
    public static TupleTag<TableRow> successTag = (new TupleTag<TableRow>(){});
    private static final Pattern DATE_PATTERN = Pattern.compile("(\\d+-\\d+-\\d+)(.*)");

    @ProcessElement
    public void processElement(ProcessContext c, Options options) {
        ErrorBuilder errorBuilder = new ErrorBuilder();
        try {
            TableRow outputRow = new TableRow();
            TableRow personSourceRow = ((TableRow) c.element());
            id(errorBuilder, outputRow, personSourceRow);
            givenName(errorBuilder, outputRow, personSourceRow);
            modified(errorBuilder, outputRow, options);
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

    private String givenName(ErrorBuilder errorBuilder, TableRow personTargetRow, TableRow personSourceRow) {
        String givenName = ((String) personSourceRow.get("first_name"));
        if (givenName!= null) {
            personTargetRow.set("givenName", givenName);
        }
        return givenName;
    }

    private Long modified(ErrorBuilder errorBuilder, TableRow personTargetRow, Options options)
        throws Exception
    {
        Long modified = temporalValue(options.getModifiedDate());
        modified = ((modified == null)?new Long((new Date().getTime()/ 1000L)):modified);
        if (modified!= null) {
            personTargetRow.set("modified", modified);
        } else {
            errorBuilder.addError("Required property 'modified' is null");
        }
        return modified;
    }

    private Long temporalValue(String stringValue)
        throws Exception
    {
        if (stringValue.length()> 0) {
            try {
                DateTime dateTimeValue = new DateTime(stringValue);
                if (dateTimeValue.isDateOnly()) {
                    return (dateTimeValue.getValue()/ 1000);
                }
                if (stringValue.contains("T")) {
                    if (stringValue.contains("/")) {
                        return (Instant.from(ZonedDateTime.parse(stringValue)).toEpochMilli()/ 1000);
                    } else {
                        if (stringValue.contains("Z")) {
                            return (Instant.parse(stringValue).toEpochMilli()/ 1000);
                        }
                        return (Instant.from(OffsetDateTime.parse(stringValue)).toEpochMilli()/ 1000);
                    }
                }
                Matcher matcher = DATE_PATTERN.matcher(stringValue);
                if (matcher.matches()) {
                    String datePart = matcher.group(1);
                    String zoneOffset = matcher.group(2);
                    if ((zoneOffset.length() == 0)||zoneOffset.equals("Z")) {
                        stringValue = ((datePart +"T00:00:00.000")+ zoneOffset);
                    }
                    return (Instant.from(OffsetDateTime.parse(stringValue)).toEpochMilli()/ 1000);
                }
            } catch (final Exception ex) {
                String message = String.format("Invalid {PersonTargetShape}.modified date %s;", stringValue);
                throw new Exception(message);
            }
        }
        return null;
    }
}
