package com.example.beam.etl.schema;

import java.util.HashMap;
import java.util.Map;
import com.example.beam.etl.ex.GreekGod;
import com.example.beam.etl.rdf.IRI;

public enum GenderType {
    Female,
    Male;
    private static final Map<String, GenderType> genderCodeMap = new HashMap<String, GenderType>();
    private static final Map<String, GenderType> localNameMap = new HashMap<String, GenderType>();
    private IRI id;
    private GreekGod personifiedBy;
    private String name;
    private String genderCode;

    static {
        genderCodeMap.put("F", GenderType.Female);
        GenderType.Female.id("http://schema.org/", "Female").name("Female").genderCode("F").personifiedBy(GreekGod.Venus);
        genderCodeMap.put("M", GenderType.Male);
        GenderType.Male.id("http://schema.org/", "Male").name("Male").genderCode("M").personifiedBy(GreekGod.Mars);
    }

    public static GenderType findByGenderCode(String genderCode) {
        return genderCodeMap.get(genderCode);
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

    private GenderType personifiedBy(GreekGod personifiedBy) {
        this.personifiedBy = personifiedBy;
        return this;
    }

    public GreekGod getPersonifiedBy() {
        return personifiedBy;
    }

    private GenderType name(String name) {
        this.name = name;
        return this;
    }

    public String getName() {
        return name;
    }

    private GenderType genderCode(String genderCode) {
        this.genderCode = genderCode;
        return this;
    }

    public String getGenderCode() {
        return genderCode;
    }
}
