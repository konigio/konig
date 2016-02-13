/*
 * #%L
 * konig-web
 * %%
 * Copyright (C) 2015 Gregory McFall
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
$(document).ready(function(){
	

/**
 * A mock implementation of the OntologyService.  Used for testing
 */
function MockOntologyService() {
	
}

MockOntologyService.prototype.getOntologyGraph = function() {
	return {
		  "@context" : {
			    "bibo" : "http://purl.org/ontology/bibo/",
			    "dc" : "http://purl.org/dc/terms/",
			    "dcat" : "http://www.w3.org/ns/dcat#",
			    "foaf" : "http://xmlns.com/foaf/0.1/",
			    "owl" : "http://www.w3.org/2002/07/owl#",
			    "rdf" : "http://www.w3.org/1999/02/22-rdf-syntax-ns#",
			    "rdfs" : "http://www.w3.org/2000/01/rdf-schema#",
			    "schema" : "http://schema.org/",
			    "vann" : "http://purl.org/vocab/vann/",
			    "void" : "http://rdfs.org/ns/void#",
			    "xsd" : "http://www.w3.org/2001/XMLSchema#"
			  },
			  "@graph" : [ {
			    "@id" : "schema:",
			    "@type" : "owl:Ontology",
			    "rdfs:comment" : {
			      "@value" : "An ontology for structured data on the Internet sponsored by Google, Microsoft, Yahoo and Yandex."
			    },
			    "vann:preferredNamespacePrefix" : {
			      "@value" : "schema"
			    }
			  }, {
			    "@id" : "owl:Ontology"
			  }, {
			    "@id" : "schema:DataType",
			    "@type" : "rdfs:Class",
			    "rdfs:subClassOf" : [ {
			      "@id" : "rdfs:Datatype"
			    }, {
			      "@id" : "rdfs:Class"
			    } ],
			    "rdfs:label" : {
			      "@value" : "DataType"
			    },
			    "rdfs:comment" : {
			      "@value" : "The basic data types such as Integers, Strings, etc."
			    }
			  }, {
			    "@id" : "rdfs:Datatype"
			  }, {
			    "@id" : "schema:Date",
			    "@type" : [ "rdfs:Class", "schema:DataType" ],
			    "rdfs:subClassOf" : {
			      "@id" : "xsd:date"
			    },
			    "rdfs:label" : {
			      "@value" : "Date"
			    },
			    "rdfs:comment" : {
			      "@value" : "A date value in <a href='http://en.wikipedia.org/wiki/ISO_8601'>ISO 8601 date format</a>."
			    }
			  }, {
			    "@id" : "xsd:date"
			  }, {
			    "@id" : "schema:Text",
			    "@type" : [ "schema:DataType", "rdfs:Class" ],
			    "rdfs:subClassOf" : {
			      "@id" : "xsd:string"
			    },
			    "rdfs:label" : {
			      "@value" : "Text"
			    },
			    "rdfs:comment" : {
			      "@value" : "Data type: Text."
			    }
			  }, {
			    "@id" : "xsd:string"
			  }, {
			    "@id" : "schema:Thing",
			    "@type" : "rdfs:Class",
			    "rdfs:label" : {
			      "@value" : "Thing"
			    },
			    "rdfs:comment" : {
			      "@value" : "The most generic type of item."
			    }
			  }, {
			    "@id" : "rdfs:Class"
			  }, {
			    "@id" : "schema:Person",
			    "@type" : "rdfs:Class",
			    "rdfs:label" : {
			      "@value" : "Person"
			    },
			    "rdfs:comment" : {
			      "@value" : "A person (alive, dead, undead, or fictional)."
			    },
			    "rdfs:subClassOf" : {
			      "@id" : "schema:Thing"
			    },
			    "owl:equivalentClass" : {
			      "@id" : "foaf:Person"
			    },
			    "dc:source" : {
			      "@id" : "http://www.w3.org/wiki/WebSchemas/SchemaDotOrgSources#source_rNews"
			    }
			  }, {
			    "@id" : "foaf:Person"
			  }, {
			    "@id" : "http://www.w3.org/wiki/WebSchemas/SchemaDotOrgSources#source_rNews"
			  }, {
			    "@id" : "schema:ContactPoint",
			    "@type" : "rdfs:Class",
			    "rdfs:label" : {
			      "@value" : "ContactPoint"
			    },
			    "rdfs:comment" : {
			      "@value" : "A contact point&#x2014;for example, a Customer Complaints department."
			    },
			    "rdfs:subClassOf" : {
			      "@id" : "schema:StructuredValue"
			    }
			  }, {
			    "@id" : "schema:StructuredValue"
			  }, {
			    "@id" : "schema:subOrganization",
			    "@type" : "rdf:Property",
			    "rdfs:label" : {
			      "@value" : "subOrganization"
			    },
			    "rdfs:comment" : {
			      "@value" : "A relationship between two organizations where the first includes the second, e.g., as a subsidiary. See also: the more specific 'department' property."
			    },
			    "schema:domainIncludes" : {
			      "@id" : "schema:Organization"
			    },
			    "schema:rangeIncludes" : {
			      "@id" : "schema:Organization"
			    },
			    "schema:inverseOf" : {
			      "@id" : "schema:parentOrganization"
			    }
			  }, {
			    "@id" : "rdf:Property"
			  }, {
			    "@id" : "schema:Organization",
			    "@type" : "rdfs:Class",
			    "rdfs:label" : {
			      "@value" : "Organization"
			    },
			    "rdfs:comment" : {
			      "@value" : "An organization such as a school, NGO, corporation, club, etc."
			    },
			    "rdfs:subClassOf" : {
			      "@id" : "schema:Thing"
			    }
			  }, {
			    "@id" : "schema:parentOrganization",
			    "@type" : "rdf:Property",
			    "rdfs:label" : {
			      "@value" : "parentOrganization"
			    },
			    "rdfs:comment" : {
			      "@value" : "The larger organization that this organization is a branch of, if any."
			    },
			    "schema:domainIncludes" : {
			      "@id" : "schema:Organization"
			    },
			    "schema:rangeIncludes" : {
			      "@id" : "schema:Organization"
			    },
			    "schema:inverseOf" : {
			      "@id" : "schema:subOrganization"
			    }
			  }, {
			    "@id" : "schema:telephone",
			    "@type" : "rdf:Property",
			    "rdfs:label" : {
			      "@value" : "telephone"
			    },
			    "rdfs:comment" : {
			      "@value" : "The telephone number."
			    },
			    "schema:domainIncludes" : [ {
			      "@id" : "schema:Person"
			    }, {
			      "@id" : "schema:Place"
			    }, {
			      "@id" : "schema:ContactPoint"
			    }, {
			      "@id" : "schema:Organization"
			    } ],
			    "schema:rangeIncludes" : {
			      "@id" : "schema:Text"
			    }
			  }, {
			    "@id" : "schema:Place"
			  }, {
			    "@id" : "schema:contactPoint",
			    "@type" : "rdf:Property",
			    "rdfs:label" : {
			      "@value" : "contactPoint"
			    },
			    "rdfs:comment" : {
			      "@value" : "A contact point for a person or organization."
			    },
			    "schema:domainIncludes" : [ {
			      "@id" : "schema:Organization"
			    }, {
			      "@id" : "schema:Person"
			    } ],
			    "schema:rangeIncludes" : {
			      "@id" : "schema:ContactPoint"
			    }
			  }, {
			    "@id" : "schema:email",
			    "@type" : "rdf:Property",
			    "rdfs:label" : {
			      "@value" : "email"
			    },
			    "rdfs:comment" : {
			      "@value" : "Email address."
			    },
			    "schema:domainIncludes" : [ {
			      "@id" : "schema:Organization"
			    }, {
			      "@id" : "schema:Person"
			    }, {
			      "@id" : "schema:ContactPoint"
			    } ],
			    "schema:rangeIncludes" : {
			      "@id" : "schema:Text"
			    }
			  }, {
			    "@id" : "schema:contactType",
			    "@type" : "rdf:Property",
			    "rdfs:label" : {
			      "@value" : "contactType"
			    },
			    "rdfs:comment" : {
			      "@value" : "A person or organization can have different contact points, for different purposes. For example, a sales contact point, a PR contact point and so on. This property is used to specify the kind of contact point."
			    },
			    "schema:domainIncludes" : {
			      "@id" : "schema:ContactPoint"
			    },
			    "schema:rangeIncludes" : {
			      "@id" : "schema:Text"
			    }
			  }, {
			    "@id" : "schema:worksFor",
			    "@type" : "rdf:Property",
			    "rdfs:label" : {
			      "@value" : "worksFor"
			    },
			    "rdfs:comment" : {
			      "@value" : "Organizations that the person works for."
			    },
			    "schema:domainIncludes" : {
			      "@id" : "schema:Person"
			    },
			    "schema:rangeIncludes" : {
			      "@id" : "schema:Organization"
			    }
			  }, {
			    "@id" : "schema:parent",
			    "@type" : "rdf:Property",
			    "rdfs:label" : {
			      "@value" : "parent"
			    },
			    "rdfs:comment" : {
			      "@value" : "A parent of this person."
			    },
			    "schema:domainIncludes" : {
			      "@id" : "schema:Person"
			    },
			    "schema:rangeIncludes" : {
			      "@id" : "schema:Person"
			    }
			  }, {
			    "@id" : "schema:givenName",
			    "@type" : "rdf:Property",
			    "rdfs:label" : {
			      "@value" : "givenName"
			    },
			    "rdfs:comment" : {
			      "@value" : "Given name. In the U.S., the first name of a Person. This can be used along with familyName instead of the name property."
			    },
			    "schema:domainIncludes" : {
			      "@id" : "schema:Person"
			    },
			    "schema:rangeIncludes" : {
			      "@id" : "schema:Text"
			    }
			  }, {
			    "@id" : "schema:familyName",
			    "@type" : "rdf:Property",
			    "rdfs:label" : {
			      "@value" : "familyName"
			    },
			    "rdfs:comment" : {
			      "@value" : "Family name. In the U.S., the last name of an Person. This can be used along with givenName instead of the name property."
			    },
			    "schema:domainIncludes" : {
			      "@id" : "schema:Person"
			    },
			    "schema:rangeIncludes" : {
			      "@id" : "schema:Text"
			    }
			  }, {
			    "@id" : "schema:children",
			    "@type" : "rdf:Property",
			    "rdfs:label" : {
			      "@value" : "children"
			    },
			    "rdfs:comment" : {
			      "@value" : "A child of the person."
			    },
			    "schema:domainIncludes" : {
			      "@id" : "schema:Person"
			    },
			    "schema:rangeIncludes" : {
			      "@id" : "schema:Person"
			    }
			  }, {
			    "@id" : "schema:birthDate",
			    "@type" : "rdf:Property",
			    "rdfs:label" : {
			      "@value" : "birthDate"
			    },
			    "rdfs:comment" : {
			      "@value" : "Date of birth."
			    },
			    "schema:domainIncludes" : {
			      "@id" : "schema:Person"
			    },
			    "schema:rangeIncludes" : {
			      "@id" : "schema:Date"
			    }
			  }, {
			    "@id" : "schema:PostalAddress",
			    "@type" : "rdfs:Class",
			    "rdfs:label" : {
			      "@value" : "PostalAddress"
			    },
			    "rdfs:comment" : {
			      "@value" : "The mailing address."
			    },
			    "rdfs:subClassOf" : {
			      "@id" : "schema:ContactPoint"
			    }
			  }, {
			    "@id" : "schema:member",
			    "@type" : "rdf:Property",
			    "rdfs:label" : {
			      "@value" : "member"
			    },
			    "rdfs:comment" : {
			      "@value" : "A member of an Organization or a ProgramMembership. Organizations can be members of organizations; ProgramMembership is typically for individuals."
			    },
			    "schema:inverseOf" : {
			      "@id" : "schema:memberOf"
			    },
			    "schema:domainIncludes" : [ {
			      "@id" : "schema:ProgramMembership"
			    }, {
			      "@id" : "schema:Organization"
			    } ],
			    "schema:rangeIncludes" : [ {
			      "@id" : "schema:Organization"
			    }, {
			      "@id" : "schema:Person"
			    } ]
			  }, {
			    "@id" : "schema:memberOf",
			    "@type" : "rdf:Property",
			    "rdfs:label" : {
			      "@value" : "memberOf"
			    },
			    "rdfs:comment" : {
			      "@value" : "An Organization (or ProgramMembership) to which this Person or Organization belongs."
			    },
			    "schema:inverseOf" : {
			      "@id" : "schema:member"
			    },
			    "schema:domainIncludes" : [ {
			      "@id" : "schema:Organization"
			    }, {
			      "@id" : "schema:Person"
			    } ],
			    "schema:rangeIncludes" : [ {
			      "@id" : "schema:ProgramMembership"
			    }, {
			      "@id" : "schema:Organization"
			    } ]
			  }, {
			    "@id" : "schema:ProgramMembership"
			  }, {
			    "@id" : "schema:streetAddress",
			    "@type" : "rdf:Property",
			    "rdfs:label" : {
			      "@value" : "streetAddress"
			    },
			    "rdfs:comment" : {
			      "@value" : "The street address. For example, 1600 Amphitheatre Pkwy."
			    },
			    "schema:domainIncludes" : {
			      "@id" : "schema:PostalAddress"
			    },
			    "schema:rangeIncludes" : {
			      "@id" : "schema:Text"
			    }
			  }, {
			    "@id" : "schema:addressCountry",
			    "@type" : "rdf:Property",
			    "rdfs:label" : {
			      "@value" : "addressCountry"
			    },
			    "rdfs:comment" : {
			      "@value" : "The country. For example, USA. You can also provide the two-letter <a href='http://en.wikipedia.org/wiki/ISO_3166-1'>ISO 3166-1 alpha-2 country code</a>."
			    },
			    "schema:domainIncludes" : [ {
			      "@id" : "schema:GeoCoordinates"
			    }, {
			      "@id" : "schema:GeoShape"
			    }, {
			      "@id" : "schema:PostalAddress"
			    } ],
			    "schema:rangeIncludes" : [ {
			      "@id" : "schema:Text"
			    }, {
			      "@id" : "schema:Country"
			    } ]
			  }, {
			    "@id" : "schema:GeoCoordinates"
			  }, {
			    "@id" : "schema:GeoShape"
			  }, {
			    "@id" : "schema:Country"
			  }, {
			    "@id" : "schema:addressLocality",
			    "@type" : "rdf:Property",
			    "rdfs:label" : {
			      "@value" : "addressLocality"
			    },
			    "rdfs:comment" : {
			      "@value" : "The locality. For example, Mountain View."
			    },
			    "schema:domainIncludes" : {
			      "@id" : "schema:PostalAddress"
			    },
			    "schema:rangeIncludes" : {
			      "@id" : "schema:Text"
			    }
			  }, {
			    "@id" : "schema:addressRegion",
			    "@type" : "rdf:Property",
			    "rdfs:label" : {
			      "@value" : "addressRegion"
			    },
			    "rdfs:comment" : {
			      "@value" : "The region. For example, CA."
			    },
			    "schema:domainIncludes" : {
			      "@id" : "schema:PostalAddress"
			    },
			    "schema:rangeIncludes" : {
			      "@id" : "schema:Text"
			    }
			  }, {
			    "@id" : "schema:postalCode",
			    "@type" : "rdf:Property",
			    "rdfs:label" : {
			      "@value" : "postalCode"
			    },
			    "rdfs:comment" : {
			      "@value" : "The postal code. For example, 94043."
			    },
			    "schema:domainIncludes" : [ {
			      "@id" : "schema:GeoShape"
			    }, {
			      "@id" : "schema:GeoCoordinates"
			    }, {
			      "@id" : "schema:PostalAddress"
			    } ],
			    "schema:rangeIncludes" : {
			      "@id" : "schema:Text"
			    }
			  }, {
			    "@id" : "schema:postOfficeBoxNumber",
			    "@type" : "rdf:Property",
			    "rdfs:label" : {
			      "@value" : "postOfficeBoxNumber"
			    },
			    "rdfs:comment" : {
			      "@value" : "The post office box number for PO box addresses."
			    },
			    "schema:domainIncludes" : {
			      "@id" : "schema:PostalAddress"
			    },
			    "schema:rangeIncludes" : {
			      "@id" : "schema:Text"
			    }
			  }, {
			    "@id" : "schema:affiliation",
			    "@type" : "rdf:Property",
			    "rdfs:label" : {
			      "@value" : "affiliation"
			    },
			    "rdfs:comment" : {
			      "@value" : "An organization that this person is affiliated with. For example, a school/university, a club, or a team."
			    },
			    "rdfs:subPropertyOf" : {
			      "@id" : "schema:memberOf"
			    },
			    "schema:domainIncludes" : {
			      "@id" : "schema:Person"
			    },
			    "schema:rangeIncludes" : {
			      "@id" : "schema:Organization"
			    }
			  } ]
			};
}


konig.buildOntodoc(new MockOntologyService());
	
});
