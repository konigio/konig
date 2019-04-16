package com.example.beam.etl.ex;

import com.example.beam.etl.rdf.IRI;

public enum GreekGod {
    Mars,
    Venus;
    private IRI id;
    private String name;

    static {
        GreekGod.Mars.id("http://example.com/ns/core/", "Mars").name("Mars");
        GreekGod.Venus.id("http://example.com/ns/core/", "Venus").name("Venus");
    }

    private GreekGod id(String namespace, String localName) {
        id = new IRI(namespace, localName);
        return this;
    }

    public IRI getId() {
        return id;
    }

    private GreekGod name(String name) {
        this.name = name;
        return this;
    }

    public String getName() {
        return name;
    }
}
