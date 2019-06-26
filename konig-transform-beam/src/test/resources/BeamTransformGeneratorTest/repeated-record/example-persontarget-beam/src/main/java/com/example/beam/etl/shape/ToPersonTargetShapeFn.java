package com.example.beam.etl.shape;

import java.util.ArrayList;
import java.util.List;
import com.example.beam.etl.common.ErrorBuilder;
import com.example.beam.etl.schema.GenderType;
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
            parent(personSourceRow, outputRow, errorBuilder);
            id(personSourceRow, outputRow, errorBuilder);
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

    private void parent(com.google.api.services.bigquery.model.TableRow personSourceRow, com.google.api.services.bigquery.model.TableRow outputRow, ErrorBuilder errorBuilder) {
        List<com.google.api.services.bigquery.model.TableRow> list = new ArrayList();
        parent_0(list, personSourceRow, errorBuilder);
        parent_1(list, personSourceRow, errorBuilder);
        if (!list.isEmpty()) {
            outputRow.set("parent", list);
        } else {
            errorBuilder.addError("The 'parent' collection must contain at least one value, but found none");
        }
    }

    private void parent_0(List<com.google.api.services.bigquery.model.TableRow> list, com.google.api.services.bigquery.model.TableRow personSourceRow, ErrorBuilder errorBuilder) {
        com.google.api.services.bigquery.model.TableRow parentRow = new com.google.api.services.bigquery.model.TableRow();
        parent_0_gender(parentRow);
        parent_0_givenName(personSourceRow, parentRow);
        if (!parentRow.isEmpty()) {
            list.add(parentRow);
        }
    }

    private void parent_0_gender(com.google.api.services.bigquery.model.TableRow outputRow) {
        com.google.api.services.bigquery.model.TableRow genderRow = new com.google.api.services.bigquery.model.TableRow();
        GenderType gender = GenderType.findByLocalName("Male");
        parent_0_gender_id(genderRow);
        parent_0_gender_name(gender, genderRow);
        if (!genderRow.isEmpty()) {
            outputRow.set("gender", genderRow);
        }
    }

    private void parent_0_gender_id(com.google.api.services.bigquery.model.TableRow outputRow) {
        Object id = "Male";
        if (id!= null) {
            outputRow.set("id", id);
        }
    }

    private void parent_0_gender_name(GenderType gender, com.google.api.services.bigquery.model.TableRow outputRow) {
        Object name = gender.getName();
        if (name!= null) {
            outputRow.set("name", name);
        }
    }

    private void parent_0_givenName(com.google.api.services.bigquery.model.TableRow personSourceRow, com.google.api.services.bigquery.model.TableRow outputRow) {
        Object father_name = personSourceRow.get("father_name");
        if (father_name!= null) {
            outputRow.set("givenName", father_name);
        }
    }

    private void parent_1(List<com.google.api.services.bigquery.model.TableRow> list, com.google.api.services.bigquery.model.TableRow personSourceRow, ErrorBuilder errorBuilder) {
        com.google.api.services.bigquery.model.TableRow parentRow = new com.google.api.services.bigquery.model.TableRow();
        parent_1_gender(parentRow);
        parent_1_givenName(personSourceRow, parentRow);
        if (!parentRow.isEmpty()) {
            list.add(parentRow);
        }
    }

    private void parent_1_gender(com.google.api.services.bigquery.model.TableRow outputRow) {
        com.google.api.services.bigquery.model.TableRow genderRow = new com.google.api.services.bigquery.model.TableRow();
        GenderType gender = GenderType.findByLocalName("Female");
        parent_1_gender_id(genderRow);
        parent_1_gender_name(gender, genderRow);
        if (!genderRow.isEmpty()) {
            outputRow.set("gender", genderRow);
        }
    }

    private void parent_1_gender_id(com.google.api.services.bigquery.model.TableRow outputRow) {
        Object id = "Female";
        if (id!= null) {
            outputRow.set("id", id);
        }
    }

    private void parent_1_gender_name(GenderType gender, com.google.api.services.bigquery.model.TableRow outputRow) {
        Object name = gender.getName();
        if (name!= null) {
            outputRow.set("name", name);
        }
    }

    private void parent_1_givenName(com.google.api.services.bigquery.model.TableRow personSourceRow, com.google.api.services.bigquery.model.TableRow outputRow) {
        Object mother_name = personSourceRow.get("mother_name");
        if (mother_name!= null) {
            outputRow.set("givenName", mother_name);
        }
    }

    private void id(com.google.api.services.bigquery.model.TableRow personSourceRow, com.google.api.services.bigquery.model.TableRow outputRow, ErrorBuilder errorBuilder) {
        Object person_id = ((personSourceRow == null)?null:personSourceRow.get("person_id"));
        if (person_id!= null) {
            outputRow.set("id", concat("http://example.com/person/", person_id));
        } else {
            errorBuilder.addError("Cannot set id because {PersonSourceShape}.person_id is null");
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
}
