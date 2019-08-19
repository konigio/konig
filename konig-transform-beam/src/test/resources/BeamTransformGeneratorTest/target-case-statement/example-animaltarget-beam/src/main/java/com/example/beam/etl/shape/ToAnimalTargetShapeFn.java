package com.example.beam.etl.shape;

import java.text.MessageFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import com.example.beam.etl.common.ErrorBuilder;
import com.example.beam.etl.shape.AnimalTargetShapeBeam.Options;
import com.fasterxml.uuid.Generators;
import com.google.api.services.bigquery.model.TableRow;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.DoFn.ProcessElement;
import org.apache.beam.sdk.transforms.DoFn.ProcessContext;
import org.apache.beam.sdk.values.TupleTag;

public class ToAnimalTargetShapeFn
    extends DoFn<TableRow, TableRow>
{
    public static TupleTag<TableRow> deadLetterTag = (new TupleTag<TableRow>(){});
    public static TupleTag<TableRow> successTag = (new TupleTag<TableRow>(){});

    @ProcessElement
    public void processElement(ProcessContext c, Options options) {
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
                errorRow.set("errorId", Generators.timeBasedGenerator().generate().toString());
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
        TableRow speciesRow = new TableRow();
        String species = ((String) animalSourceRow.get("species"));
        if (species == null) {
            errorBuilder.addError("Cannot set required property 'species' because '{AnimalSourceShape}.species' is null");
            return speciesRow;
        }
        com.example.beam.etl.ex.Species species1 = com.example.beam.etl.ex.Species.findByLocalName(species);
        if (species1 == null) {
            String msg = MessageFormat.format("Cannot set species because '{AnimalSourceShape}.species' = ''{0}'' does not map to a valid enum value", species);
            errorBuilder.addError(msg);
            return speciesRow;
        }
        species_id(errorBuilder, speciesRow, species1);
        species_name(errorBuilder, speciesRow, species1);
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
        TableRow genusRow = new TableRow();
        com.example.beam.etl.ex.Genus genus = case1(errorBuilder, animalTargetRow);
        if (genus == null) {
            errorBuilder.addError("Cannot set {AnimalTargetShape}.genus because CASE WHEN {AnimalTargetShape}.species.name IN ( \"Pan troglodytes\" , \"Pan paniscus\" ) THEN <http://example.com/ns/core/Pan> WHEN {AnimalTargetShape}.species.name IN ( \"Pongo abelii\" , \"Pongo pygmaeus\" , \"Pongo tapanuliensis\" ) THEN <http://example.com/ns/core/Pongo> END evaluates to null");
            return genusRow;
        }
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
