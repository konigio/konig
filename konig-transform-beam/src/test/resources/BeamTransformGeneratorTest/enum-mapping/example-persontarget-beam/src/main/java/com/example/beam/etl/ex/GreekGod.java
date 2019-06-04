package com.example.beam.etl.ex;

import java.util.HashMap;
import java.util.Map;
import com.example.beam.etl.rdf.IRI;

public enum GreekGod {
    Mars,
    Venus;
    private static final Map<String, GreekGod> localNameMap = new HashMap<String, GreekGod>();
    private IRI id;
    private String name;

    static {
        GreekGod.Mars.id("http://example.com/ns/core/", "Mars").name("Mars");
        GreekGod.Venus.id("http://example.com/ns/core/", "Venus").name("Venus");
    }

    public static GreekGod findByLocalName(String localName) {
        return localNameMap.get(localName);
    }

    private GreekGod id(String namespace, String localName) {
        id = new IRI(namespace, localName);
        localNameMap.put(localName, this);
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
