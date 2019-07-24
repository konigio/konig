package com.example.beam.etl.shape;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import com.example.beam.etl.common.ErrorBuilder;
import com.fasterxml.uuid.Generators;
import com.google.api.services.bigquery.model.TableRow;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.DoFn.ProcessElement;
import org.apache.beam.sdk.transforms.DoFn.ProcessContext;
import org.apache.beam.sdk.values.TupleTag;

public class ToAnimalTargetShapeFn
    extends DoFn<TableRow, TableRow>
{
    public static TupleTag<TableRow> deadLetterTag = new TupleTag<TableRow>();
    public static TupleTag<TableRow> successTag = new TupleTag<TableRow>();

    @ProcessElement
    public void processElement(ProcessContext c, PipelineOptions options) {
        ErrorBuilder errorBuilder = new ErrorBuilder();
        try {
            TableRow outputRow = new TableRow();
            TableRow animalSourceRow = ((TableRow) c.element());
            species(errorBuilder, outputRow, animalSourceRow);
            genus(errorBuilder, outputRow);
            id(errorBuilder, outputRow, animalSourceRow);
            if (outputRow.isEmpty()) {
                errorBuilder.addError("record is empty");
            }
            if (!errorBuilder.isEmpty()) {
                TableRow errorRow = new TableRow();
                errorRow.set("errorId", Generators.timeBasedGenerator().generate());
                errorRow.set("errorCreated", (new Date().getTime()/ 1000));
                errorRow.set("errorMessage", errorBuilder.toString());
                errorRow.set("pipelineJobName", options.getJobName());
                errorRow.set("AnimalSource", animalSourceRow);
                c.output(deadLetterTag, errorRow);
            } else {
                c.output(successTag, outputRow);
            }
        } catch (final Throwable oops) {
            oops.printStackTrace();
        }
    }

    private TableRow species(ErrorBuilder errorBuilder, TableRow animalTargetRow, TableRow animalSourceRow) {
        com.example.beam.etl.ex.Species species = com.example.beam.etl.ex.Species.findByLocalName(((String) animalSourceRow.get("species")));
        TableRow speciesRow = new TableRow();
        species_id(errorBuilder, speciesRow, species);
        species_name(errorBuilder, speciesRow, species);
        if (!speciesRow.isEmpty()) {
            animalTargetRow.set("species", speciesRow);
        } else {
            errorBuilder.addError("Required property 'species' is null");
        }
        return speciesRow;
    }

    private String species_id(ErrorBuilder errorBuilder, TableRow speciesRow, com.example.beam.etl.ex.Species species) {
        String id = ((String)((species!= null)?species.getId().getLocalName():null));
        if (id!= null) {
            speciesRow.set("id", id);
        } else {
            errorBuilder.addError("Required property 'species.id' is null");
        }
        return id;
    }

    private String species_name(ErrorBuilder errorBuilder, TableRow speciesRow, com.example.beam.etl.ex.Species species) {
        String name = ((String)((species!= null)?species.getName():null));
        if (name!= null) {
            speciesRow.set("name", name);
        } else {
            errorBuilder.addError("Required property 'species.name' is null");
        }
        return name;
    }

    private TableRow genus(ErrorBuilder errorBuilder, TableRow animalTargetRow) {
        com.example.beam.etl.ex.Genus genus = case1(errorBuilder, animalTargetRow);
        TableRow genusRow = new TableRow();
        genus_id(errorBuilder, genusRow, genus);
        genus_name(errorBuilder, genusRow, genus);
        if (!genusRow.isEmpty()) {
            animalTargetRow.set("genus", genusRow);
        } else {
            errorBuilder.addError("Required property 'genus' is null");
        }
        return genusRow;
    }

    private com.example.beam.etl.ex.Genus case1(ErrorBuilder errorBuilder, TableRow animalTargetRow) {
        com.example.beam.etl.ex.Genus genusValue = null;
        if (case1_when1(errorBuilder, animalTargetRow)) {
            genusValue = com.example.beam.etl.ex.Genus.findByLocalName("Pan");
        } else {
            if (case1_when2(errorBuilder, animalTargetRow)) {
                genusValue = com.example.beam.etl.ex.Genus.findByLocalName("Pongo");
            }
        }
        return genusValue;
    }

    private boolean case1_when1(ErrorBuilder errorBuilder, TableRow animalTargetRow) {
        Set<Object> set = new HashSet();
        set.add("Pan troglodytes");
        set.add("Pan paniscus");
        Object species_name = get(animalTargetRow, "species", "name");
        return set.contains(species_name);
    }

    private Object get(Object value, String... fieldNameList) {
        for (String fieldName: fieldNameList) {
            if (value instanceof TableRow) {
                value = ((TableRow) value).get(fieldName);
            } else {
                return null;
            }
        }
        return value;
    }

    private boolean case1_when2(ErrorBuilder errorBuilder, TableRow animalTargetRow) {
        Set<Object> set = new HashSet();
        set.add("Pongo abelii");
        set.add("Pongo pygmaeus");
        set.add("Pongo tapanuliensis");
        Object species_name = get(animalTargetRow, "species", "name");
        return set.contains(species_name);
    }

    private String genus_id(ErrorBuilder errorBuilder, TableRow genusRow, com.example.beam.etl.ex.Genus genus) {
        String id = ((String)((genus!= null)?genus.getId().getLocalName():null));
        if (id!= null) {
            genusRow.set("id", id);
        } else {
            errorBuilder.addError("Required property 'genus.id' is null");
        }
        return id;
    }

    private String genus_name(ErrorBuilder errorBuilder, TableRow genusRow, com.example.beam.etl.ex.Genus genus) {
        String name = ((String)((genus!= null)?genus.getName():null));
        if (name!= null) {
            genusRow.set("name", name);
        } else {
            errorBuilder.addError("Required property 'genus.name' is null");
        }
        return name;
    }

    private String id(ErrorBuilder errorBuilder, TableRow animalTargetRow, TableRow animalSourceRow) {
        String id = ((String) animalSourceRow.get("id"));
        if (id!= null) {
            animalTargetRow.set("id", id);
        } else {
            errorBuilder.addError("Required property 'id' is null");
        }
        return id;
    }
}
