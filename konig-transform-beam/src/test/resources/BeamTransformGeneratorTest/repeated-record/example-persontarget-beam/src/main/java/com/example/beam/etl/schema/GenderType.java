package com.example.beam.etl.schema;

import java.util.HashMap;
import java.util.Map;
import com.example.beam.etl.rdf.IRI;

public enum GenderType {
    Female,
    Male;
    private static final Map<String, GenderType> nameMap = new HashMap<String, GenderType>();
    private static final Map<String, GenderType> localNameMap = new HashMap<String, GenderType>();
    private IRI id;
    private String name;

    static {
        nameMap.put("Female", GenderType.Female);
        GenderType.Female.id("http://schema.org/", "Female").name("Female");
        nameMap.put("Male", GenderType.Male);
        GenderType.Male.id("http://schema.org/", "Male").name("Male");
    }

    public static GenderType findByName(String name) {
        return nameMap.get(name);
    }

    public static GenderType findByLocalName(String localName) {
        return localNameMap.get(localName);
    }

    private GenderType id(String namespace, String localName) {
        id = new IRI(namespace, localName);
        localNameMap.put(localName, this);
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
