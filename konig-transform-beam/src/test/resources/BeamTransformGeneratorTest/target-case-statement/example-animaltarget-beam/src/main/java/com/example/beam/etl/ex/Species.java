package com.example.beam.etl.ex;

import java.util.HashMap;
import java.util.Map;
import com.example.beam.etl.rdf.IRI;

public enum Species {
    PanPaniscus,
    PanTroglodytes,
    PongoAbelii,
    PongoPygmaeus;
    private static final Map<String, Species> localNameMap = new HashMap<String, Species>();
    private IRI id;
    private String name;
    private String comment;

    static {
        Species.PanPaniscus.id("http://example.com/ns/core/", "PanPaniscus").name("Pan paniscus").comment("Bonobo");
        Species.PanTroglodytes.id("http://example.com/ns/core/", "PanTroglodytes").name("Pan troglodytes").comment("Common Chimpanzee");
        Species.PongoAbelii.id("http://example.com/ns/core/", "PongoAbelii").name("Pongo abelii").name("Pongo tapanuliensis").comment("Sumatran Orangutan").comment("Tapanuli Orangutan");
        Species.PongoPygmaeus.id("http://example.com/ns/core/", "PongoPygmaeus").name("Pongo pygmaeus").comment("Bornean Orangutan");
    }

    public static Species findByLocalName(String localName) {
        return localNameMap.get(localName);
    }

    private Species id(String namespace, String localName) {
        id = new IRI(namespace, localName);
        localNameMap.put(localName, this);
        return this;
    }

    public IRI getId() {
        return id;
    }

    private Species name(String name) {
        this.name = name;
        return this;
    }

    public String getName() {
        return name;
    }

    private Species comment(String comment) {
        this.comment = comment;
        return this;
    }

    public String getComment() {
        return comment;
    }
}
