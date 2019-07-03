package com.example.beam.etl.shape;

import java.util.HashSet;
import java.util.Set;
import com.example.beam.etl.common.ErrorBuilder;
import com.example.beam.etl.ex.Species;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.DoFn.ProcessContext;
import org.apache.beam.sdk.values.TupleTag;

public class ToAnimalTargetShapeFn
    extends DoFn<com.google.api.services.bigquery.model.TableRow, com.google.api.services.bigquery.model.TableRow>
{
    public static TupleTag<String> deadLetterTag = new TupleTag<String>();
    public static TupleTag<com.google.api.services.bigquery.model.TableRow> successTag = new TupleTag<com.google.api.services.bigquery.model.TableRow>();

    @DoFn.ProcessElement
    public void processElement(ProcessContext c) {
        try {
            ErrorBuilder errorBuilder = new ErrorBuilder();
            com.google.api.services.bigquery.model.TableRow outputRow = new com.google.api.services.bigquery.model.TableRow();
            com.google.api.services.bigquery.model.TableRow animalSourceRow = ((com.google.api.services.bigquery.model.TableRow) c.element());
            species(animalSourceRow, outputRow, errorBuilder);
            genus(outputRow, errorBuilder);
            id(animalSourceRow, outputRow, errorBuilder);
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

    private void species(com.google.api.services.bigquery.model.TableRow animalSourceRow, com.google.api.services.bigquery.model.TableRow outputRow, ErrorBuilder errorBuilder) {
        Object animalSourceRow_species = animalSourceRow.get("species");
        if (animalSourceRow_species!= null) {
            com.google.api.services.bigquery.model.TableRow speciesRow = new com.google.api.services.bigquery.model.TableRow();
            Species species = Species.findByLocalName(animalSourceRow_species.toString());
            speciesRow.set("id", animalSourceRow_species);
            species_name(species, speciesRow, errorBuilder);
            if (!outputRow.isEmpty()) {
                outputRow.set("species", speciesRow);
            }
        }
    }

    private void species_name(Species species, com.google.api.services.bigquery.model.TableRow outputRow, ErrorBuilder errorBuilder) {
        Object name = species.getName();
        if (name!= null) {
            outputRow.set("name", name);
        } else {
            errorBuilder.addError("Cannot set species.name because {Species}.name is null");
        }
    }

    private void genus(com.google.api.services.bigquery.model.TableRow outputRow, ErrorBuilder errorBuilder) {
        com.example.beam.etl.ex.Genus genus = case1(outputRow, errorBuilder);
        if (genus!= null) {
            com.google.api.services.bigquery.model.TableRow genusRow = new com.google.api.services.bigquery.model.TableRow();
            genus_id(genus, genusRow, errorBuilder);
            genus_name(genus, genusRow, errorBuilder);
            outputRow.set("genus", genusRow);
        }
        errorBuilder.addError("Required field 'genus' is NULL");
    }

    private com.example.beam.etl.ex.Genus case1(com.google.api.services.bigquery.model.TableRow animalTargetRow, ErrorBuilder errorBuilder) {
        com.example.beam.etl.ex.Genus genusValue = null;
        if (case1_when1(animalTargetRow, errorBuilder)) {
            genusValue = com.example.beam.etl.ex.Genus.findByLocalName("Pan");
        } else {
            if (case1_when2(animalTargetRow, errorBuilder)) {
                genusValue = com.example.beam.etl.ex.Genus.findByLocalName("Pongo");
            }
        }
        return genusValue;
    }

    private boolean case1_when1(com.google.api.services.bigquery.model.TableRow animalTargetRow, ErrorBuilder errorBuilder) {
        Set<Object> set = new HashSet();
        set.add("Pan troglodytes");
        set.add("Pan paniscus");
        Object species_name = get(animalTargetRow, "species", "name");
        return set.contains(species_name);
    }

    private Object get(Object value, String... fieldNameList) {
        for (String fieldName: fieldNameList) {
            if (value instanceof com.google.api.services.bigquery.model.TableRow) {
                value = ((com.google.api.services.bigquery.model.TableRow) value).get(fieldName);
            } else {
                return null;
            }
        }
        return value;
    }

    private boolean case1_when2(com.google.api.services.bigquery.model.TableRow animalTargetRow, ErrorBuilder errorBuilder) {
        Set<Object> set = new HashSet();
        set.add("Pongo abelii");
        set.add("Pongo pygmaeus");
        set.add("Pongo tapanuliensis");
        Object species_name = get(animalTargetRow, "species", "name");
        return set.contains(species_name);
    }

    private void genus_id(com.example.beam.etl.ex.Genus genus, com.google.api.services.bigquery.model.TableRow genusRow, ErrorBuilder errorBuilder) {
        genusRow.set("id", genus.getId().getLocalName());
    }

    private void genus_name(com.example.beam.etl.ex.Genus genus, com.google.api.services.bigquery.model.TableRow genusRow, ErrorBuilder errorBuilder) {
        Object name = genus.getName();
        if (name!= null) {
            genusRow.set("name", name);
        } else {
            errorBuilder.addError(("{AnimalTargetShape}.genus.name must not be null, but is not defined for "+ genus.name()));
        }
    }

    private void id(com.google.api.services.bigquery.model.TableRow animalSourceRow, com.google.api.services.bigquery.model.TableRow outputRow, ErrorBuilder errorBuilder) {
        Object id = ((animalSourceRow == null)?null:animalSourceRow.get("id"));
        if (id!= null) {
            outputRow.set("id", id);
        }
    }
}
