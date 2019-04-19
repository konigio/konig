package com.example.beam.etl.schema;

import com.example.beam.etl.rdf.IRI;

public enum GenderType {
    Female,
    Male;
    private IRI id;
    private String name;

    static {
        GenderType.Female.id("http://schema.org/", "Female").name("Female");
        GenderType.Male.id("http://schema.org/", "Male").name("Male");
    }

    private GenderType id(String namespace, String localName) {
        id = new IRI(namespace, localName);
        return this;
    }

    public IRI getId() {
        return id;
    }

    private GenderType name(String name) {
        this.name = name;
        return this;
    }

    public String getName() {
        return name;
    }
}
