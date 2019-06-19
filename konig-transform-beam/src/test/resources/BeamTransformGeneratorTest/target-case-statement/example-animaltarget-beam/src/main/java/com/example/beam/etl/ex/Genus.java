package com.example.beam.etl.ex;

import java.util.HashMap;
import java.util.Map;
import com.example.beam.etl.rdf.IRI;

public enum Genus {
    Pan,
    Pongo;
    private static final Map<String, Genus> localNameMap = new HashMap<String, Genus>();
    private IRI id;
    private String name;
    private String comment;

    static {
        Genus.Pan.id("http://example.com/ns/core/", "Pan").name("Pan").comment("Chimpanzees");
        Genus.Pongo.id("http://example.com/ns/core/", "Pongo").name("Pongo").comment("Orangutans");
    }

    public static Genus findByLocalName(String localName) {
        return localNameMap.get(localName);
    }

    private Genus id(String namespace, String localName) {
        id = new IRI(namespace, localName);
        localNameMap.put(localName, this);
        return this;
    }

    public IRI getId() {
        return id;
    }

    private Genus name(String name) {
        this.name = name;
        return this;
    }

    public String getName() {
        return name;
    }

    private Genus comment(String comment) {
        this.comment = comment;
        return this;
    }

    public String getComment() {
        return comment;
    }
}
