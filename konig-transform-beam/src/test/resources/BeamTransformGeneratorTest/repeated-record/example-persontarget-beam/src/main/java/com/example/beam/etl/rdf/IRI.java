package com.example.beam.etl.rdf;

public class IRI {
    private String namespace;
    private String localName;

    public IRI(String namespace, String localName) {
        this.namespace = namespace;
        this.localName = localName;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getLocalName() {
        return localName;
    }

    public String stringValue() {
        return (namespace + localName);
    }

    public String toString() {
        return stringValue();
    }
}
