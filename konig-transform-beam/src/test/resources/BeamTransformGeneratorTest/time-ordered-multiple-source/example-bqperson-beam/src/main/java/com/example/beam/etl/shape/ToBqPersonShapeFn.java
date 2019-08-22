package com.example.beam.etl.shape;

import java.text.MessageFormat;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.example.beam.etl.common.ErrorBuilder;
import com.example.beam.etl.shape.BqPersonShapeBeam.Options;
import com.fasterxml.uuid.Generators;
import com.google.api.client.util.DateTime;
import com.google.api.services.bigquery.model.TableRow;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.DoFn.ProcessElement;
import org.apache.beam.sdk.transforms.DoFn.ProcessContext;
import org.apache.beam.sdk.transforms.join.CoGbkResult;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.TupleTag;

public class ToBqPersonShapeFn
    extends DoFn<KV<String, CoGbkResult> , TableRow>
{
    public static TupleTag<TableRow> deadLetterTag = (new TupleTag<TableRow>(){});
    public static TupleTag<TableRow> successTag = (new TupleTag<TableRow>(){});
    private static final Pattern DATE_PATTERN = Pattern.compile("(\\d+-\\d+-\\d+)(.*)");

    @ProcessElement
    public void processElement(ProcessContext c, PipelineOptions pipelineOptions) {
        Options options = pipelineOptions.as(Options.class);
        ErrorBuilder errorBuilder = new ErrorBuilder();
        try {
            TableRow outputRow = new TableRow();
            KV<String, CoGbkResult> e = c.element();
            TableRow personContactRow = latestSourceRow(e, BqPersonShapeBeam.personContactTag);
            TableRow personNameRow = latestSourceRow(e, BqPersonShapeBeam.personNameTag);
            if ((personContactRow == null)&&(personNameRow == null)) {
                return;
            }
            List<ToBqPersonShapeFn.SourceProcessor> sourceList = new ArrayList();
            if (personContactRow!= null) {
                sourceList.add(ToBqPersonShapeFn.PersonContactProcessor.instance(personContactRow));
            }
            if (personNameRow!= null) {
                sourceList.add(ToBqPersonShapeFn.PersonNameProcessor.instance(personNameRow));
            }
            Collections.sort(sourceList);
            for (ToBqPersonShapeFn.SourceProcessor processor: sourceList) {
                processor.run(options, errorBuilder, outputRow);
            }
            if (outputRow.isEmpty()) {
                errorBuilder.addError("record is empty");
            }
            if (!errorBuilder.isEmpty()) {
                TableRow errorRow = new TableRow();
                errorRow.set("errorId", Generators.timeBasedGenerator().generate().toString());
                errorRow.set("errorCreated", (new Date().getTime()/ 1000));
                errorRow.set("errorMessage", errorBuilder.toString());
                errorRow.set("pipelineJobName", options.getJobName());
                errorRow.set("PersonContact", personContactRow);
                errorRow.set("PersonName", personNameRow);
                c.output(deadLetterTag, errorRow);
            } else {
                c.output(successTag, outputRow);
            }
        } catch (final Throwable oops) {
            oops.printStackTrace();
        }
    }

    private Long unixTime(String fieldName, String fieldValue, ErrorBuilder errorBuilder)
        throws Exception
    {
        if (fieldValue == null) {
            return null;
        }
        if (fieldValue.length()> 0) {
            try {
                DateTime dateTimeValue = new DateTime(fieldValue);
                if (dateTimeValue.isDateOnly()) {
                    return (dateTimeValue.getValue()/ 1000);
                }
                if (fieldValue.contains("T")) {
                    if (fieldValue.contains("/")) {
                        return (Instant.from(ZonedDateTime.parse(fieldValue)).toEpochMilli()/ 1000);
                    } else {
                        if (fieldValue.contains("Z")) {
                            return (Instant.parse(fieldValue).toEpochMilli()/ 1000);
                        }
                        return (Instant.from(OffsetDateTime.parse(fieldValue)).toEpochMilli()/ 1000);
                    }
                }
                Matcher matcher = DATE_PATTERN.matcher(fieldValue);
                if (matcher.matches()) {
                    String datePart = matcher.group(1);
                    String zoneOffset = matcher.group(2);
                    if ((zoneOffset.length() == 0)||zoneOffset.equals("Z")) {
                        fieldValue = ((datePart +"T00:00:00.000")+ zoneOffset);
                    }
                    return (Instant.from(OffsetDateTime.parse(fieldValue)).toEpochMilli()/ 1000);
                }
            } catch (final Exception ex) {
                String message = MessageFormat.format("{0} has invalid date value ''{1}''", fieldName, fieldValue);
                errorBuilder.addError(message);
            }
        }
        return null;
    }

    private TableRow latestSourceRow(KV<String, CoGbkResult> e, TupleTag<TableRow> tag) {
        String latest = null;
        TableRow result = null;
        Iterator<TableRow> sequence = e.getValue().getAll(tag).iterator();
        while (sequence.hasNext()) {
            TableRow row = sequence.next();
            String modified = row.get("modified").toString();
            if ((latest == null)||(modified.compareTo(latest)< 0)) {
                latest = modified;
                result = row;
            }
        }
        return result;
    }

    private class PersonContactProcessor
        extends ToBqPersonShapeFn.SourceProcessor
    {

        private PersonContactProcessor(long modified, TableRow sourceRow) {
            super(modified, sourceRow);
        }

        private static ToBqPersonShapeFn.PersonContactProcessor instance(TableRow row) {
            Long millis = unixTime("{PersonContactShape}.modified", row.get("modified"), errorBuilder);
            if (millis == null) {
                return null;
            }
            return new ToBqPersonShapeFn.PersonContactProcessor(millis, row);
        }

        public void run(Options options, ErrorBuilder errorBuilder, TableRow bqPersonRow) {
            id(errorBuilder, bqPersonRow, sourceRow);
            phoneNumber(errorBuilder, bqPersonRow, sourceRow);
            modified(errorBuilder, bqPersonRow, sourceRow);
        }

        private String id(ErrorBuilder errorBuilder, TableRow bqPersonRow, TableRow personContactRow) {
            String id = ((bqPersonRow!= null)?((String) bqPersonRow.get("id")):null);
            if (id!= null) {
                return id;
            }
            id = personContactRow.get("id");
            if (id!= null) {
                bqPersonRow.set("id", id);
            } else {
                errorBuilder.addError("Required property 'id' is null");
            }
            return id;
        }

        private String phoneNumber(ErrorBuilder errorBuilder, TableRow bqPersonRow, TableRow personContactRow) {
            String phoneNumber = ((bqPersonRow!= null)?((String) bqPersonRow.get("phoneNumber")):null);
            if (phoneNumber!= null) {
                return phoneNumber;
            }
            phoneNumber = personContactRow.get("phone_number");
            if (phoneNumber!= null) {
                bqPersonRow.set("phoneNumber", phoneNumber);
            }
            return phoneNumber;
        }

        private Long modified(ErrorBuilder errorBuilder, TableRow bqPersonRow, TableRow personContactRow) {
            Long modified = ((bqPersonRow!= null)?((Long) bqPersonRow.get("modified")):null);
            if (modified!= null) {
                return modified;
            }
            modified = personContactRow.get("modified");
            if (modified!= null) {
                bqPersonRow.set("modified", modified);
            }
            return modified;
        }
    }

    private class PersonNameProcessor
        extends ToBqPersonShapeFn.SourceProcessor
    {

        private PersonNameProcessor(long modified, TableRow sourceRow) {
            super(modified, sourceRow);
        }

        private static ToBqPersonShapeFn.PersonNameProcessor instance(TableRow row) {
            Long millis = unixTime("{PersonNameShape}.modified", row.get("modified"), errorBuilder);
            if (millis == null) {
                return null;
            }
            return new ToBqPersonShapeFn.PersonNameProcessor(millis, row);
        }

        public void run(Options options, ErrorBuilder errorBuilder, TableRow bqPersonRow) {
            id(errorBuilder, bqPersonRow, sourceRow);
            givenName(errorBuilder, bqPersonRow, sourceRow);
            modified(errorBuilder, bqPersonRow, sourceRow);
        }

        private String id(ErrorBuilder errorBuilder, TableRow bqPersonRow, TableRow personNameRow) {
            String id = ((bqPersonRow!= null)?((String) bqPersonRow.get("id")):null);
            if (id!= null) {
                return id;
            }
            id = personNameRow.get("id");
            if (id!= null) {
                bqPersonRow.set("id", id);
            } else {
                errorBuilder.addError("Required property 'id' is null");
            }
            return id;
        }

        private String givenName(ErrorBuilder errorBuilder, TableRow bqPersonRow, TableRow personNameRow) {
            String givenName = ((bqPersonRow!= null)?((String) bqPersonRow.get("givenName")):null);
            if (givenName!= null) {
                return givenName;
            }
            givenName = personNameRow.get("first_name");
            if (givenName!= null) {
                bqPersonRow.set("givenName", givenName);
            }
            return givenName;
        }

        private Long modified(ErrorBuilder errorBuilder, TableRow bqPersonRow, TableRow personNameRow) {
            Long modified = ((bqPersonRow!= null)?((Long) bqPersonRow.get("modified")):null);
            if (modified!= null) {
                return modified;
            }
            modified = personNameRow.get("modified");
            if (modified!= null) {
                bqPersonRow.set("modified", modified);
            }
            return modified;
        }
    }

    private abstract class SourceProcessor
        implements Comparable<ToBqPersonShapeFn.SourceProcessor>
    {
        private long modified;
        protected TableRow sourceRow;

        protected SourceProcessor(long modified, TableRow sourceRow) {
            modified = modified;
            sourceRow = sourceRow;
        }

        public int compareTo(ToBqPersonShapeFn.SourceProcessor other) {
            long delta = (other.modified -modified);
            return ((delta< 0)?-1 :((delta == 0)? 0 : 1));
        }

        public abstract void run(Options options, ErrorBuilder errorBuilder, TableRow bqPersonRow);
    }
}
