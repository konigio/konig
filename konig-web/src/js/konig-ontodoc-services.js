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
function StaticOntologyService() {
	
}

StaticOntologyService.prototype.getOntologyGraph = function() {
	return {
		  "@context" : {
			    "as" : "http://www.w3.org/ns/activitystreams#",
			    "cnt" : "http://schema.pearson.com/ns/content/",
			    "dc" : "http://purl.org/dc/terms/",
			    "konig" : "http://www.konig.io/ns/core/",
			    "org" : "http://www.w3.org/ns/org#",
			    "owl" : "http://www.w3.org/2002/07/owl#",
			    "prov" : "http://www.w3.org/ns/prov#",
			    "rdf" : "http://www.w3.org/1999/02/22-rdf-syntax-ns#",
			    "rdfs" : "http://www.w3.org/2000/01/rdf-schema#",
			    "schema" : "http://schema.org/",
			    "sh" : "http://www.w3.org/ns/shacl#",
			    "sys" : "http://schema.pearson.com/ns/system/",
			    "vann" : "http://purl.org/vocab/vann/",
			    "vs" : "http://www.w3.org/2003/06/sw-vocab-status/ns#",
			    "xas" : "http://schema.pearson.com/ns/activity/",
			    "xowl" : "http://schema.pearson.com/ns/xowl/",
			    "xprov" : "http://schema.pearson.com/ns/xprov/",
			    "xsd" : "http://www.w3.org/2001/XMLSchema#"
			  },
			  "@graph" : [ {
			    "@id" : "dc:",
			    "@type" : "owl:Ontology",
			    "vann:preferredNamespacePrefix" : {
			      "@value" : "dc"
			    },
			    "rdfs:label" : {
			      "@value" : "Dublin Core"
			    },
			    "rdfs:comment" : {
			      "@value" : "A general-purpose vocabulary for resource description including terms like author, title, etc."
			    }
			  }, {
			    "@id" : "org:",
			    "@type" : "owl:Ontology",
			    "vann:preferredNamespacePrefix" : {
			      "@value" : "org"
			    },
			    "rdfs:label" : {
			      "@value" : "W3C Organization Ontology"
			    },
			    "rdfs:comment" : {
			      "@value" : "A vocabulary for describing organization structures and roles"
			    }
			  }, {
			    "@id" : "sys:",
			    "@type" : "owl:Ontology",
			    "vann:preferredNamespacePrefix" : {
			      "@value" : "sys"
			    },
			    "rdfs:label" : {
			      "@value" : "Pearson Systems"
			    },
			    "rdfs:comment" : {
			      "@value" : "\r\n  \tControlled vocabularies for the various platforms and systems at Pearson.\r\n  "
			    }
			  }, {
			    "@id" : "schema:",
			    "@type" : "owl:Ontology",
			    "vann:preferredNamespacePrefix" : {
			      "@value" : "schema"
			    },
			    "rdfs:label" : {
			      "@value" : "Schema.org"
			    },
			    "rdfs:comment" : {
			      "@value" : "\r\n  \tA vocabulary developed by Google, Microsoft, Yahoo and others through a community \r\n  \tprocess to promote structured data on the Internet, on web pages, in email messages, and beyond.\r\n  "
			    }
			  }, {
			    "@id" : "xas:",
			    "@type" : "owl:Ontology",
			    "vann:preferredNamespacePrefix" : {
			      "@value" : "xas"
			    },
			    "rdfs:label" : {
			      "@value" : "Pearson Activity Streams"
			    },
			    "rdfs:comment" : {
			      "@value" : "Pearson's extension of the W3C Activity vocabulary"
			    }
			  }, {
			    "@id" : "xprov:",
			    "@type" : "owl:Ontology",
			    "vann:preferredNamespacePrefix" : {
			      "@value" : "xprov"
			    },
			    "rdfs:label" : {
			      "@value" : "Pearson Provenance"
			    },
			    "rdfs:comment" : {
			      "@value" : "\r\n  \tAn extension of the W3C Provenance Ontology\r\n  "
			    }
			  }, {
			    "@id" : "xowl:",
			    "@type" : "owl:Ontology",
			    "vann:preferredNamespacePrefix" : {
			      "@value" : "xowl"
			    },
			    "rdfs:label" : {
			      "@value" : "Pearson Ontology Language"
			    },
			    "rdfs:comment" : {
			      "@value" : "\r\n  \tProvides terms that extend OWL and SHACL.\r\n  "
			    }
			  }, {
			    "@id" : "cnt:",
			    "@type" : "owl:Ontology",
			    "vann:preferredNamespacePrefix" : {
			      "@value" : "cnt"
			    },
			    "rdfs:label" : {
			      "@value" : "Pearson Content"
			    },
			    "rdfs:comment" : {
			      "@value" : "\r\n  \tA vocabulary for describing learning resources.\r\n  "
			    }
			  }, {
			    "@id" : "as:",
			    "@type" : "owl:Ontology",
			    "vann:preferredNamespacePrefix" : {
			      "@value" : "as"
			    },
			    "rdfs:label" : {
			      "@value" : "W3C Activity Streams"
			    },
			    "rdfs:comment" : {
			      "@value" : "This vocabulary describes activities that people, groups, or software agents perform."
			    }
			  }, {
			    "@id" : "prov:",
			    "@type" : "owl:Ontology",
			    "vann:preferredNamespacePrefix" : {
			      "@value" : "prov"
			    },
			    "rdfs:label" : {
			      "@value" : "W3C Provenance Ontology"
			    },
			    "rdfs:comment" : {
			      "@value" : "A vocabulary for describing the provenance of resources -- i.e. the activities that produce resources"
			    }
			  }, {
			    "@id" : "as:Activity",
			    "@type" : "owl:Class",
			    "rdfs:comment" : {
			      "@value" : "\r\n\t\tIn general, an Activity encapsulates information about actions that have occurred, are in the process of occurring,\r\n\t\tor may occur in the future.  At Pearson, we almost always use Activity entities to describe actions that have already occurred."
			    }
			  }, {
			    "@id" : "sys:CitationGenerator",
			    "@type" : "owl:Class",
			    "rdfs:subClassOf" : {
			      "@id" : "schema:SoftwareApplication"
			    },
			    "rdfs:comment" : {
			      "@value" : "The class of software applications that can generate citations"
			    }
			  }, {
			    "@id" : "cnt:CitationSource",
			    "@type" : "owl:Class",
			    "rdfs:subClassOf" : {
			      "@id" : "schema:CreativeWork"
			    },
			    "rdfs:comment" : {
			      "@value" : "The class of things that may by referenced by a Citation"
			    }
			  }, {
			    "@id" : "sys:CiteASource",
			    "@type" : "owl:Class",
			    "rdfs:subClassOf" : [ {
			      "@id" : "sys:CitationGenerator"
			    }, {
			      "@id" : "sys:PearsonWriter"
			    } ],
			    "rdfs:comment" : {
			      "@value" : "The component of PearsonWriter responsible for implementing the 'Cite a source' feature"
			    }
			  }, {
			    "@id" : "schema:CreativeWork",
			    "@type" : "owl:Class",
			    "rdfs:comment" : {
			      "@value" : "The most generic kind of creative work including learning resources"
			    }
			  }, {
			    "@id" : "sys:DesktopApplication",
			    "@type" : "owl:Class",
			    "rdfs:subClassOf" : {
			      "@id" : "schema:SoftwareApplication"
			    },
			    "rdfs:comment" : {
			      "@value" : "\r\n\t\tThe class of all desktop software applications such as Microsoft Word, \r\n\t\tPhotoshop, etc.\r\n\t"
			    }
			  }, {
			    "@id" : "sys:FindASource",
			    "@type" : "owl:Class",
			    "rdfs:subClassOf" : [ {
			      "@id" : "sys:PearsonWriter"
			    }, {
			      "@id" : "sys:CitationGenerator"
			    } ],
			    "rdfs:comment" : {
			      "@value" : "The component of PearsonWriter responsible for implementing the 'Find a source' feature"
			    }
			  }, {
			    "@id" : "xas:Load",
			    "@type" : "owl:Class",
			    "rdfs:subClassOf" : {
			      "@id" : "as:Activity"
			    },
			    "rdfs:comment" : {
			      "@value" : "The action of loading some resource into a browser or mobile device"
			    }
			  }, {
			    "@id" : "xas:Login",
			    "@type" : "owl:Class",
			    "rdfs:comment" : {
			      "@value" : "Describes the action of logging into some system"
			    }
			  }, {
			    "@id" : "xas:LoginPage",
			    "@type" : "owl:Class",
			    "rdfs:subClassOf" : {
			      "@id" : "schema:WebPage"
			    }
			  }, {
			    "@id" : "schema:MobileApplication",
			    "@type" : "owl:Class",
			    "rdfs:subClassOf" : {
			      "@id" : "schema:SoftwareApplication"
			    },
			    "rdfs:comment" : {
			      "@value" : "A software application designed specifically to work well on a mobile device"
			    }
			  }, {
			    "@id" : "sys:PearsonWriter",
			    "@type" : "owl:Class",
			    "rdfs:subClassOf" : {
			      "@id" : "schema:SoftwareApplication"
			    },
			    "rdfs:comment" : {
			      "@value" : "The class of all software applications that power PearsonWriter"
			    }
			  }, {
			    "@id" : "sys:PearsonWriterWordPlugin",
			    "@type" : "owl:Class",
			    "rdfs:subClassOf" : [ {
			      "@id" : "sys:PearsonWriter"
			    }, {
			      "@id" : "sys:DesktopApplication"
			    } ],
			    "rdfs:comment" : {
			      "@value" : "\r\n\t\tThe PearsonWriter plugin for Microsoft Word. This application allows users to\r\n\t\tauthor content in Word and push it to PearsonWriter in the cloud.\r\n\t"
			    }
			  }, {
			    "@id" : "schema:SoftwareApplication",
			    "@type" : "owl:Class",
			    "rdfs:comment" : {
			      "@value" : "A software application"
			    }
			  }, {
			    "@id" : "xas:Unload",
			    "@type" : "owl:Class",
			    "rdfs:subClassOf" : {
			      "@id" : "as:Activity"
			    },
			    "rdfs:comment" : {
			      "@value" : "The action of unloading some resource from a browser or mobile device"
			    }
			  }, {
			    "@id" : "schema:WebApplication",
			    "@type" : "owl:Class",
			    "rdfs:subClassOf" : {
			      "@id" : "schema:SoftwareApplication"
			    },
			    "rdfs:comment" : {
			      "@value" : "A web-based application"
			    }
			  }, {
			    "@id" : "as:actor",
			    "@type" : "owl:ObjectProperty",
			    "rdfs:comment" : {
			      "@value" : "The person who performed the action"
			    },
			    "rdfs:domain" : {
			      "@id" : "as:Activity"
			    },
			    "rdfs:range" : {
			      "@id" : "as:Actor"
			    }
			  }, {
			    "@id" : "xprov:generator",
			    "@type" : "owl:ObjectProperty",
			    "rdfs:subPropertyOf" : {
			      "@id" : "prov:wasInfluencedBy"
			    },
			    "rdfs:domain" : {
			      "@id" : "prov:Activity"
			    },
			    "rdfs:range" : {
			      "@id" : "prov:Agent"
			    },
			    "rdfs:comment" : {
			      "@value" : "The individual person, group, or thing that was responsible for generating the \r\n\t\toutput of this Activity"
			    }
			  }, {
			    "@id" : "http://schema.pearson.com/shapes/v1/cnt/Citation",
			    "@type" : "sh:Shape",
			    "sh:targetClass" : {
			      "@id" : "cnt:Citation"
			    },
			    "sh:property" : [ {
			      "sh:predicate" : {
			        "@id" : "xowl:guid"
			      },
			      "rdfs:comment" : {
			        "@value" : "A globally unique identifier for this Citation entity"
			      },
			      "sh:datatype" : {
			        "@id" : "xsd:string"
			      },
			      "sh:minCount" : {
			        "@value" : "1",
			        "@type" : "xsd:integer"
			      },
			      "sh:maxCount" : {
			        "@value" : "1",
			        "@type" : "xsd:integer"
			      }
			    }, {
			      "sh:predicate" : {
			        "@id" : "prov:wasGeneratedBy"
			      },
			      "rdfs:comment" : {
			        "@value" : "Provenance information which describes the process that generated this Citation entity"
			      },
			      "sh:valueShape" : {
			        "@id" : "http://schema.pearson.com/shapes/v1/cnt/CitationProvenance"
			      },
			      "sh:minCount" : {
			        "@value" : "1",
			        "@type" : "xsd:integer"
			      },
			      "sh:maxCount" : {
			        "@value" : "1",
			        "@type" : "xsd:integer"
			      }
			    }, {
			      "sh:predicate" : {
			        "@id" : "cnt:source"
			      },
			      "rdfs:comment" : {
			        "@value" : "The work that is being cited"
			      },
			      "sh:valueShape" : {
			        "@id" : "http://schema.pearson.com/shapes/v1/cnt/CitationSource"
			      },
			      "sh:minCount" : {
			        "@value" : "1",
			        "@type" : "xsd:integer"
			      }
			    }, {
			      "sh:predicate" : {
			        "@id" : "schema:partOf"
			      },
			      "rdfs:comment" : {
			        "@value" : "A container (such as a Project) within which this citation is found"
			      },
			      "sh:nodeKind" : {
			        "@id" : "sh:IRI"
			      },
			      "sh:minCount" : {
			        "@value" : "1",
			        "@type" : "xsd:integer"
			      },
			      "sh:maxCount" : {
			        "@value" : "1",
			        "@type" : "xsd:integer"
			      }
			    }, {
			      "sh:predicate" : {
			        "@id" : "rdf:type"
			      },
			      "rdfs:comment" : {
			        "@value" : "Specifies the type of this entity"
			      },
			      "sh:nodeKind" : {
			        "@id" : "sh:IRI"
			      },
			      "sh:minCount" : {
			        "@value" : "1",
			        "@type" : "xsd:integer"
			      },
			      "sh:hasValue" : {
			        "@id" : "cnt:Citation"
			      }
			    } ],
			    "konig:avroSchemaRendition" : {
			      "@id" : "http://schema.pearson.com/shapes/v1/cnt/Citation/avro"
			    },
			    "konig:jsonSchemaRendition" : {
			      "@id" : "http://schema.pearson.com/shapes/v1/cnt/Citation/jsonschema"
			    },
			    "konig:mediaTypeBaseName" : {
			      "@value" : "application/vnd.pearson.v1.cnt.citation"
			    }
			  }, {
			    "@id" : "http://schema.pearson.com/shapes/v1/cnt/CitationProvenance",
			    "@type" : "sh:Shape",
			    "sh:targetClass" : {
			      "@id" : "prov:Activity"
			    },
			    "sh:property" : {
			      "sh:predicate" : {
			        "@id" : "xprov:generator"
			      },
			      "rdfs:comment" : {
			        "@value" : "The system that generated the Citation message"
			      },
			      "sh:nodeKind" : {
			        "@id" : "sh:IRI"
			      },
			      "sh:class" : {
			        "@id" : "sys:CitationGenerator"
			      },
			      "sh:minCount" : {
			        "@value" : "1",
			        "@type" : "xsd:integer"
			      },
			      "sh:maxCount" : {
			        "@value" : "1",
			        "@type" : "xsd:integer"
			      }
			    },
			    "konig:avroSchemaRendition" : {
			      "@id" : "http://schema.pearson.com/shapes/v1/cnt/CitationProvenance/avro"
			    },
			    "konig:jsonSchemaRendition" : {
			      "@id" : "http://schema.pearson.com/shapes/v1/cnt/CitationProvenance/jsonschema"
			    },
			    "konig:mediaTypeBaseName" : {
			      "@value" : "application/vnd.pearson.v1.cnt.citationprovenance"
			    }
			  }, {
			    "@id" : "http://schema.pearson.com/shapes/v1/cnt/CitationSource",
			    "@type" : "sh:Shape",
			    "sh:targetClass" : {
			      "@id" : "cnt:CitationSource"
			    },
			    "sh:property" : [ {
			      "sh:predicate" : {
			        "@id" : "konig:id"
			      },
			      "rdfs:comment" : {
			        "@value" : "A URI that identifies the source"
			      },
			      "sh:nodeKind" : {
			        "@id" : "sh:IRI"
			      },
			      "sh:minCount" : {
			        "@value" : "1",
			        "@type" : "xsd:integer"
			      },
			      "sh:maxCount" : {
			        "@value" : "1",
			        "@type" : "xsd:integer"
			      }
			    }, {
			      "sh:predicate" : {
			        "@id" : "rdf:type"
			      },
			      "rdfs:comment" : {
			        "@value" : "The type of source that was cited."
			      },
			      "sh:nodeKind" : {
			        "@id" : "sh:IRI"
			      },
			      "sh:minCount" : {
			        "@value" : "1",
			        "@type" : "xsd:integer"
			      }
			    }, {
			      "sh:predicate" : {
			        "@id" : "schema:name"
			      },
			      "rdfs:comment" : {
			        "@value" : "The name or title of the source"
			      },
			      "sh:datatype" : {
			        "@id" : "xsd:string"
			      },
			      "sh:minCount" : {
			        "@value" : "1",
			        "@type" : "xsd:integer"
			      },
			      "sh:maxCount" : {
			        "@value" : "1",
			        "@type" : "xsd:integer"
			      }
			    } ],
			    "konig:avroSchemaRendition" : {
			      "@id" : "http://schema.pearson.com/shapes/v1/cnt/CitationSource/avro"
			    },
			    "konig:jsonSchemaRendition" : {
			      "@id" : "http://schema.pearson.com/shapes/v1/cnt/CitationSource/jsonschema"
			    },
			    "konig:mediaTypeBaseName" : {
			      "@value" : "application/vnd.pearson.v1.cnt.citationsource"
			    }
			  }, {
			    "@id" : "http://schema.pearson.com/shapes/v1/xas/CreateCitation",
			    "@type" : "sh:Shape",
			    "sh:targetClass" : {
			      "@id" : "cnt:Citation"
			    },
			    "sh:property" : [ {
			      "sh:predicate" : {
			        "@id" : "as:object"
			      },
			      "rdfs:comment" : {
			        "@value" : "The Citation object that was created"
			      },
			      "sh:valueShape" : {
			        "@id" : "http://schema.pearson.com/shapes/v1/cnt/Citation"
			      },
			      "sh:minCount" : {
			        "@value" : "1",
			        "@type" : "xsd:integer"
			      },
			      "sh:maxCount" : {
			        "@value" : "1",
			        "@type" : "xsd:integer"
			      }
			    }, {
			      "sh:predicate" : {
			        "@id" : "xowl:guid"
			      },
			      "rdfs:comment" : {
			        "@value" : "A globally unique identifier for this Citation activity"
			      },
			      "sh:datatype" : {
			        "@id" : "xsd:string"
			      },
			      "sh:minCount" : {
			        "@value" : "1",
			        "@type" : "xsd:integer"
			      },
			      "sh:maxCount" : {
			        "@value" : "1",
			        "@type" : "xsd:integer"
			      }
			    }, {
			      "sh:predicate" : {
			        "@id" : "as:instrument"
			      },
			      "rdfs:comment" : {
			        "@value" : "The software application used to perform the action"
			      },
			      "sh:valueShape" : {
			        "@id" : "http://schema.pearson.com/shapes/v1/schema/SoftwareApplication"
			      },
			      "sh:minCount" : {
			        "@value" : "1",
			        "@type" : "xsd:integer"
			      },
			      "sh:maxCount" : {
			        "@value" : "1",
			        "@type" : "xsd:integer"
			      }
			    }, {
			      "sh:predicate" : {
			        "@id" : "as:actor"
			      },
			      "rdfs:comment" : {
			        "@value" : "The person who performed the Citation action"
			      },
			      "sh:valueShape" : {
			        "@id" : "http://schema.pearson.com/shapes/v1/schema/Person"
			      },
			      "sh:minCount" : {
			        "@value" : "1",
			        "@type" : "xsd:integer"
			      },
			      "sh:maxCount" : {
			        "@value" : "1",
			        "@type" : "xsd:integer"
			      }
			    }, {
			      "sh:predicate" : {
			        "@id" : "rdf:type"
			      },
			      "rdfs:comment" : {
			        "@value" : "The type of activity that occurred."
			      },
			      "sh:nodeKind" : {
			        "@id" : "sh:IRI"
			      },
			      "sh:minCount" : {
			        "@value" : "1",
			        "@type" : "xsd:integer"
			      },
			      "sh:hasValue" : [ {
			        "@id" : "as:Create"
			      }, {
			        "@id" : "xas:CreateCitation"
			      } ]
			    }, {
			      "sh:predicate" : {
			        "@id" : "as:location"
			      },
			      "rdfs:comment" : {
			        "@value" : "\r\n\t\t\tThe logical location(s) where the application occurred. \r\n\t\t\tIn principle, a login activity should not have a location.  We include a location in the login activity\r\n\t\t\tso that we can capture the user's institutional affiliation.  It is likely that the location will be\r\n\t    removed from the schema in the future.\r\n\t\t"
			      },
			      "sh:valueShape" : {
			        "@id" : "http://schema.pearson.com/shapes/v1/owl/Thing"
			      },
			      "sh:minCount" : {
			        "@value" : "1",
			        "@type" : "xsd:integer"
			      }
			    }, {
			      "sh:predicate" : {
			        "@id" : "as:eventTime"
			      },
			      "rdfs:comment" : {
			        "@value" : "The time at which the login action occurred, as recorded on the server-side"
			      },
			      "sh:datatype" : {
			        "@id" : "xsd:dateTime"
			      },
			      "sh:minCount" : {
			        "@value" : "1",
			        "@type" : "xsd:integer"
			      },
			      "sh:maxCount" : {
			        "@value" : "1",
			        "@type" : "xsd:integer"
			      }
			    }, {
			      "sh:predicate" : {
			        "@id" : "as:target"
			      },
			      "rdfs:comment" : {
			        "@value" : "The web page to which the user agent was redirected after login."
			      },
			      "sh:valueShape" : {
			        "@id" : "http://schema.pearson.com/shapes/v1/schema/WebPage"
			      },
			      "sh:minCount" : {
			        "@value" : "0",
			        "@type" : "xsd:integer"
			      },
			      "sh:maxCount" : {
			        "@value" : "1",
			        "@type" : "xsd:integer"
			      }
			    }, {
			      "sh:predicate" : {
			        "@id" : "prov:wasGeneratedBy"
			      },
			      "rdfs:comment" : {
			        "@value" : "Provenance information which describes the process that generated this Activity record"
			      },
			      "sh:valueShape" : {
			        "@id" : "http://schema.pearson.com/shapes/v1/xas/CreateCitationProvenance"
			      },
			      "sh:minCount" : {
			        "@value" : "1",
			        "@type" : "xsd:integer"
			      },
			      "sh:maxCount" : {
			        "@value" : "1",
			        "@type" : "xsd:integer"
			      }
			    } ],
			    "konig:avroSchemaRendition" : {
			      "@id" : "http://schema.pearson.com/shapes/v1/xas/CreateCitation/avro"
			    },
			    "konig:jsonSchemaRendition" : {
			      "@id" : "http://schema.pearson.com/shapes/v1/xas/CreateCitation/jsonschema"
			    },
			    "konig:mediaTypeBaseName" : {
			      "@value" : "application/vnd.pearson.v1.xas.createcitation"
			    }
			  }, {
			    "@id" : "http://schema.pearson.com/shapes/v1/xas/CreateCitationProvenance",
			    "@type" : "sh:Shape",
			    "sh:targetClass" : {
			      "@id" : "prov:Activity"
			    },
			    "sh:property" : [ {
			      "sh:predicate" : {
			        "@id" : "xprov:generator"
			      },
			      "rdfs:comment" : {
			        "@value" : "The system that generated the Citation message"
			      },
			      "sh:nodeKind" : {
			        "@id" : "sh:IRI"
			      },
			      "sh:minCount" : {
			        "@value" : "1",
			        "@type" : "xsd:integer"
			      },
			      "sh:maxCount" : {
			        "@value" : "1",
			        "@type" : "xsd:integer"
			      }
			    }, {
			      "sh:predicate" : {
			        "@id" : "xas:generatedMediaType"
			      },
			      "rdfs:comment" : {
			        "@value" : "The media type of the Citation message"
			      },
			      "sh:datatype" : {
			        "@id" : "xsd:string"
			      },
			      "sh:minCount" : {
			        "@value" : "1",
			        "@type" : "xsd:integer"
			      },
			      "sh:maxCount" : {
			        "@value" : "1",
			        "@type" : "xsd:integer"
			      },
			      "sh:hasValue" : {
			        "@value" : "application/vnd.pearson.v1.xas.createcitation+json"
			      }
			    } ],
			    "konig:avroSchemaRendition" : {
			      "@id" : "http://schema.pearson.com/shapes/v1/xas/CreateCitationProvenance/avro"
			    },
			    "konig:jsonSchemaRendition" : {
			      "@id" : "http://schema.pearson.com/shapes/v1/xas/CreateCitationProvenance/jsonschema"
			    },
			    "konig:mediaTypeBaseName" : {
			      "@value" : "application/vnd.pearson.v1.xas.createcitationprovenance"
			    }
			  }, {
			    "@id" : "http://schema.pearson.com/shapes/v1/schema/CreativeWork",
			    "@type" : "sh:Shape",
			    "sh:targetClass" : {
			      "@id" : "schema:CreativeWork"
			    },
			    "sh:property" : [ {
			      "sh:predicate" : {
			        "@id" : "schema:name"
			      },
			      "sh:datatype" : {
			        "@id" : "xsd:string"
			      },
			      "rdfs:comment" : {
			        "@value" : "A human-friendly name or title for the resource"
			      }
			    }, {
			      "sh:predicate" : {
			        "@id" : "xowl:guid"
			      },
			      "sh:datatype" : {
			        "@id" : "xsd:string"
			      },
			      "rdfs:comment" : {
			        "@value" : "A globally unique identifier for the resource"
			      },
			      "sh:minCount" : {
			        "@value" : "1",
			        "@type" : "xsd:integer"
			      },
			      "sh:maxCount" : {
			        "@value" : "1",
			        "@type" : "xsd:integer"
			      }
			    }, {
			      "sh:predicate" : {
			        "@id" : "konig:id"
			      },
			      "rdfs:comment" : {
			        "@value" : "The URI of the resource"
			      },
			      "sh:nodeKind" : {
			        "@id" : "sh:IRI"
			      },
			      "sh:minCount" : {
			        "@value" : "1",
			        "@type" : "xsd:integer"
			      },
			      "sh:maxCount" : {
			        "@value" : "1",
			        "@type" : "xsd:integer"
			      }
			    } ],
			    "konig:avroSchemaRendition" : {
			      "@id" : "http://schema.pearson.com/shapes/v1/schema/CreativeWork/avro"
			    },
			    "konig:jsonSchemaRendition" : {
			      "@id" : "http://schema.pearson.com/shapes/v1/schema/CreativeWork/jsonschema"
			    },
			    "konig:mediaTypeBaseName" : {
			      "@value" : "application/vnd.pearson.v1.schema.creativework"
			    }
			  }, {
			    "@id" : "http://schema.pearson.com/shapes/v1/xas/Load",
			    "@type" : "sh:Shape",
			    "sh:targetClass" : {
			      "@id" : "xas:Load"
			    },
			    "sh:property" : [ {
			      "sh:predicate" : {
			        "@id" : "as:instrumentType"
			      },
			      "rdfs:comment" : {
			        "@value" : "The type of software application used to perform the action"
			      },
			      "sh:nodeKind" : {
			        "@id" : "sh:IRI"
			      },
			      "sh:minCount" : {
			        "@value" : "1",
			        "@type" : "xsd:integer"
			      }
			    }, {
			      "sh:predicate" : {
			        "@id" : "prov:wasGeneratedBy"
			      },
			      "rdfs:comment" : {
			        "@value" : "Provenance information which describes the process that generated this Activity record"
			      },
			      "sh:valueShape" : {
			        "@id" : "http://schema.pearson.com/shapes/v1/xas/LoadProvenance"
			      },
			      "sh:minCount" : {
			        "@value" : "1",
			        "@type" : "xsd:integer"
			      },
			      "sh:maxCount" : {
			        "@value" : "1",
			        "@type" : "xsd:integer"
			      }
			    }, {
			      "sh:predicate" : {
			        "@id" : "as:eventTime"
			      },
			      "rdfs:comment" : {
			        "@value" : "The time at which the load action occurred, as recorded on the client-side"
			      },
			      "sh:datatype" : {
			        "@id" : "xsd:dateTime"
			      },
			      "sh:minCount" : {
			        "@value" : "1",
			        "@type" : "xsd:integer"
			      },
			      "sh:maxCount" : {
			        "@value" : "1",
			        "@type" : "xsd:integer"
			      }
			    }, {
			      "sh:predicate" : {
			        "@id" : "xowl:guid"
			      },
			      "rdfs:comment" : {
			        "@value" : "A globally unique identifier for this Load activity"
			      },
			      "sh:datatype" : {
			        "@id" : "xsd:string"
			      },
			      "sh:minCount" : {
			        "@value" : "1",
			        "@type" : "xsd:integer"
			      },
			      "sh:maxCount" : {
			        "@value" : "1",
			        "@type" : "xsd:integer"
			      }
			    }, {
			      "sh:predicate" : {
			        "@id" : "rdf:type"
			      },
			      "rdfs:comment" : {
			        "@value" : "\r\n\t\t\tThe type of activity that occurred. Multiple values are allowed so that we can support other vocabularies.  \r\n\t\t\tThe set of values MUST include 'xas:Load'.\r\n\t\t"
			      },
			      "sh:nodeKind" : {
			        "@id" : "sh:IRI"
			      },
			      "sh:minCount" : {
			        "@value" : "1",
			        "@type" : "xsd:integer"
			      },
			      "sh:hasValue" : {
			        "@id" : "xas:Load"
			      }
			    }, {
			      "sh:predicate" : {
			        "@id" : "as:location"
			      },
			      "rdfs:comment" : {
			        "@value" : "\r\n\t\t\tThe logical location(s) where the activity occurred.  This could include, for example, the\r\n\t\t\tcourse section and/or educational institution within which the activity occurred.\r\n\t\t"
			      },
			      "sh:valueShape" : {
			        "@id" : "http://schema.pearson.com/shapes/v1/owl/Thing"
			      },
			      "sh:minCount" : {
			        "@value" : "1",
			        "@type" : "xsd:integer"
			      }
			    }, {
			      "sh:predicate" : {
			        "@id" : "as:object"
			      },
			      "rdfs:comment" : {
			        "@value" : "The resource that was loaded into the browser or mobile device"
			      },
			      "sh:valueShape" : {
			        "@id" : "http://schema.pearson.com/shapes/v2/xas/LoginPage"
			      },
			      "sh:minCount" : {
			        "@value" : "1",
			        "@type" : "xsd:integer"
			      },
			      "sh:maxCount" : {
			        "@value" : "1",
			        "@type" : "xsd:integer"
			      }
			    }, {
			      "sh:predicate" : {
			        "@id" : "as:actor"
			      },
			      "rdfs:comment" : {
			        "@value" : "The person who performed the Load action"
			      },
			      "sh:valueShape" : {
			        "@id" : "http://schema.pearson.com/shapes/v1/schema/Person"
			      },
			      "sh:minCount" : {
			        "@value" : "1",
			        "@type" : "xsd:integer"
			      },
			      "sh:maxCount" : {
			        "@value" : "1",
			        "@type" : "xsd:integer"
			      }
			    } ],
			    "konig:avroSchemaRendition" : {
			      "@id" : "http://schema.pearson.com/shapes/v1/xas/Load/avro"
			    },
			    "konig:jsonSchemaRendition" : {
			      "@id" : "http://schema.pearson.com/shapes/v1/xas/Load/jsonschema"
			    },
			    "konig:mediaTypeBaseName" : {
			      "@value" : "application/vnd.pearson.v1.xas.load"
			    }
			  }, {
			    "@id" : "http://schema.pearson.com/shapes/v1/xas/LoadProvenance",
			    "@type" : "sh:Shape",
			    "sh:targetClass" : {
			      "@id" : "prov:Activity"
			    },
			    "sh:property" : [ {
			      "sh:predicate" : {
			        "@id" : "xas:generatedMediaType"
			      },
			      "rdfs:comment" : {
			        "@value" : "The media type of the Login message"
			      },
			      "sh:datatype" : {
			        "@id" : "xsd:string"
			      },
			      "sh:minCount" : {
			        "@value" : "1",
			        "@type" : "xsd:integer"
			      },
			      "sh:maxCount" : {
			        "@value" : "1",
			        "@type" : "xsd:integer"
			      },
			      "sh:hasValue" : {
			        "@value" : "application/vnd.pearson.v1.xas.load+json"
			      }
			    }, {
			      "sh:predicate" : {
			        "@id" : "xprov:generator"
			      },
			      "rdfs:comment" : {
			        "@value" : "The system that generated the Login message"
			      },
			      "sh:nodeKind" : {
			        "@id" : "sh:IRI"
			      },
			      "sh:minCount" : {
			        "@value" : "1",
			        "@type" : "xsd:integer"
			      },
			      "sh:maxCount" : {
			        "@value" : "1",
			        "@type" : "xsd:integer"
			      }
			    } ],
			    "konig:avroSchemaRendition" : {
			      "@id" : "http://schema.pearson.com/shapes/v1/xas/LoadProvenance/avro"
			    },
			    "konig:jsonSchemaRendition" : {
			      "@id" : "http://schema.pearson.com/shapes/v1/xas/LoadProvenance/jsonschema"
			    },
			    "konig:mediaTypeBaseName" : {
			      "@value" : "application/vnd.pearson.v1.xas.loadprovenance"
			    }
			  }, {
			    "@id" : "http://schema.pearson.com/shapes/v1/xas/Login",
			    "@type" : "sh:Shape",
			    "sh:targetClass" : {
			      "@id" : "xas:Login"
			    },
			    "sh:property" : [ {
			      "sh:predicate" : {
			        "@id" : "as:eventTime"
			      },
			      "rdfs:comment" : {
			        "@value" : "The time at which the login action occurred, as recorded on the server-side"
			      },
			      "sh:datatype" : {
			        "@id" : "xsd:dateTime"
			      },
			      "sh:minCount" : {
			        "@value" : "1",
			        "@type" : "xsd:integer"
			      },
			      "sh:maxCount" : {
			        "@value" : "1",
			        "@type" : "xsd:integer"
			      }
			    }, {
			      "sh:predicate" : {
			        "@id" : "xowl:guid"
			      },
			      "rdfs:comment" : {
			        "@value" : "A globally unique identifier for this Login activity"
			      },
			      "sh:datatype" : {
			        "@id" : "xsd:string"
			      },
			      "sh:minCount" : {
			        "@value" : "1",
			        "@type" : "xsd:integer"
			      },
			      "sh:maxCount" : {
			        "@value" : "1",
			        "@type" : "xsd:integer"
			      }
			    }, {
			      "sh:predicate" : {
			        "@id" : "as:location"
			      },
			      "rdfs:comment" : {
			        "@value" : "\r\n\t\t\tThe logical location(s) where the login activity occurred. \r\n\t\t\tIn principle, a login activity should not have a location.  We include a location in the login activity\r\n\t\t\tso that we can capture the user's institutional affiliation.  It is likely that the location will be\r\n\t    removed from the schema in the future.\r\n\t\t"
			      },
			      "sh:valueShape" : {
			        "@id" : "http://schema.pearson.com/shapes/v1/owl/Thing"
			      },
			      "sh:minCount" : {
			        "@value" : "1",
			        "@type" : "xsd:integer"
			      }
			    }, {
			      "sh:predicate" : {
			        "@id" : "as:object"
			      },
			      "rdfs:comment" : {
			        "@value" : "The WebPage where the login activity occurred"
			      },
			      "sh:valueShape" : {
			        "@id" : "http://schema.pearson.com/shapes/v2/xas/LoginPage"
			      },
			      "sh:minCount" : {
			        "@value" : "1",
			        "@type" : "xsd:integer"
			      },
			      "sh:maxCount" : {
			        "@value" : "1",
			        "@type" : "xsd:integer"
			      }
			    }, {
			      "sh:predicate" : {
			        "@id" : "rdf:type"
			      },
			      "rdfs:comment" : {
			        "@value" : "\r\n\t\t\tThe type of activity that occurred. Multiple values are allowed so that we can support other vocabularies.  \r\n\t\t\tThe set of values MUST include 'xas:Login'.\r\n\t\t"
			      },
			      "sh:nodeKind" : {
			        "@id" : "sh:IRI"
			      },
			      "sh:minCount" : {
			        "@value" : "1",
			        "@type" : "xsd:integer"
			      },
			      "sh:hasValue" : {
			        "@id" : "xas:Login"
			      }
			    }, {
			      "sh:predicate" : {
			        "@id" : "as:target"
			      },
			      "rdfs:comment" : {
			        "@value" : "The web page to which the user agent was redirected after login."
			      },
			      "sh:valueShape" : {
			        "@id" : "http://schema.pearson.com/shapes/v1/schema/WebPage"
			      },
			      "sh:minCount" : {
			        "@value" : "0",
			        "@type" : "xsd:integer"
			      },
			      "sh:maxCount" : {
			        "@value" : "1",
			        "@type" : "xsd:integer"
			      }
			    }, {
			      "sh:predicate" : {
			        "@id" : "as:actor"
			      },
			      "rdfs:comment" : {
			        "@value" : "The person who performed the login action"
			      },
			      "sh:valueShape" : {
			        "@id" : "http://schema.pearson.com/shapes/v1/schema/Person"
			      },
			      "sh:minCount" : {
			        "@value" : "1",
			        "@type" : "xsd:integer"
			      },
			      "sh:maxCount" : {
			        "@value" : "1",
			        "@type" : "xsd:integer"
			      }
			    }, {
			      "sh:predicate" : {
			        "@id" : "as:instrumentType"
			      },
			      "rdfs:comment" : {
			        "@value" : "The type of software application used to perform the action"
			      },
			      "sh:nodeKind" : {
			        "@id" : "sh:IRI"
			      },
			      "sh:class" : {
			        "@id" : "schema:SoftwareApplication"
			      },
			      "sh:minCount" : {
			        "@value" : "1",
			        "@type" : "xsd:integer"
			      }
			    }, {
			      "sh:predicate" : {
			        "@id" : "prov:wasGeneratedBy"
			      },
			      "rdfs:comment" : {
			        "@value" : "Provenance information which describes the process that generated this Activity record"
			      },
			      "sh:valueShape" : {
			        "@id" : "http://schema.pearson.com/shapes/v1/xas/LoginProvenance"
			      },
			      "sh:minCount" : {
			        "@value" : "1",
			        "@type" : "xsd:integer"
			      },
			      "sh:maxCount" : {
			        "@value" : "1",
			        "@type" : "xsd:integer"
			      }
			    } ],
			    "konig:avroSchemaRendition" : {
			      "@id" : "http://schema.pearson.com/shapes/v1/xas/Login/avro"
			    },
			    "konig:jsonSchemaRendition" : {
			      "@id" : "http://schema.pearson.com/shapes/v1/xas/Login/jsonschema"
			    },
			    "konig:mediaTypeBaseName" : {
			      "@value" : "application/vnd.pearson.v1.xas.login"
			    }
			  }, {
			    "@id" : "http://schema.pearson.com/shapes/v2/xas/LoginPage",
			    "@type" : "sh:Shape",
			    "sh:targetClass" : {
			      "@id" : "xas:LoginPage"
			    },
			    "sh:property" : [ {
			      "sh:predicate" : {
			        "@id" : "rdf:type"
			      },
			      "sh:hasValue" : {
			        "@id" : "xas:LoginPage"
			      },
			      "sh:nodeKind" : {
			        "@id" : "sh:IRI"
			      },
			      "sh:minCount" : {
			        "@value" : "1",
			        "@type" : "xsd:integer"
			      }
			    }, {
			      "sh:predicate" : {
			        "@id" : "konig:id"
			      },
			      "rdfs:comment" : {
			        "@value" : "The address of the login page"
			      },
			      "sh:nodeKind" : {
			        "@id" : "sh:IRI"
			      },
			      "sh:minCount" : {
			        "@value" : "1",
			        "@type" : "xsd:integer"
			      },
			      "sh:maxCount" : {
			        "@value" : "1",
			        "@type" : "xsd:integer"
			      }
			    } ],
			    "konig:avroSchemaRendition" : {
			      "@id" : "http://schema.pearson.com/shapes/v2/xas/LoginPage/avro"
			    },
			    "konig:jsonSchemaRendition" : {
			      "@id" : "http://schema.pearson.com/shapes/v2/xas/LoginPage/jsonschema"
			    },
			    "konig:mediaTypeBaseName" : {
			      "@value" : "application/vnd.pearson.v2.xas.loginpage"
			    }
			  }, {
			    "@id" : "http://schema.pearson.com/shapes/v1/xas/LoginProvenance",
			    "@type" : "sh:Shape",
			    "sh:targetClass" : {
			      "@id" : "prov:Activity"
			    },
			    "sh:property" : [ {
			      "sh:predicate" : {
			        "@id" : "xas:generatedMediaType"
			      },
			      "rdfs:comment" : {
			        "@value" : "The media type of the Login message"
			      },
			      "sh:datatype" : {
			        "@id" : "xsd:string"
			      },
			      "sh:minCount" : {
			        "@value" : "1",
			        "@type" : "xsd:integer"
			      },
			      "sh:maxCount" : {
			        "@value" : "1",
			        "@type" : "xsd:integer"
			      },
			      "sh:hasValue" : {
			        "@value" : "application/vnd.pearson.v1.xas.login+json"
			      }
			    }, {
			      "sh:predicate" : {
			        "@id" : "xprov:generator"
			      },
			      "rdfs:comment" : {
			        "@value" : "The system that generated the Login message"
			      },
			      "sh:nodeKind" : {
			        "@id" : "sh:IRI"
			      },
			      "sh:minCount" : {
			        "@value" : "1",
			        "@type" : "xsd:integer"
			      },
			      "sh:maxCount" : {
			        "@value" : "1",
			        "@type" : "xsd:integer"
			      }
			    } ],
			    "konig:avroSchemaRendition" : {
			      "@id" : "http://schema.pearson.com/shapes/v1/xas/LoginProvenance/avro"
			    },
			    "konig:jsonSchemaRendition" : {
			      "@id" : "http://schema.pearson.com/shapes/v1/xas/LoginProvenance/jsonschema"
			    },
			    "konig:mediaTypeBaseName" : {
			      "@value" : "application/vnd.pearson.v1.xas.loginprovenance"
			    }
			  }, {
			    "@id" : "http://schema.pearson.com/shapes/v1/org/Membership",
			    "@type" : "sh:Shape",
			    "sh:targetClass" : {
			      "@id" : "org:Membership"
			    },
			    "sh:property" : [ {
			      "sh:predicate" : {
			        "@id" : "org:organization"
			      },
			      "sh:nodeKind" : {
			        "@id" : "sh:IRI"
			      },
			      "sh:minCount" : {
			        "@value" : "1",
			        "@type" : "xsd:integer"
			      },
			      "sh:maxCount" : {
			        "@value" : "1",
			        "@type" : "xsd:integer"
			      }
			    }, {
			      "sh:predicate" : {
			        "@id" : "org:role"
			      },
			      "sh:nodeKind" : {
			        "@id" : "sh:IRI"
			      },
			      "sh:minCount" : {
			        "@value" : "1",
			        "@type" : "xsd:integer"
			      },
			      "sh:maxCount" : {
			        "@value" : "1",
			        "@type" : "xsd:integer"
			      }
			    } ],
			    "konig:avroSchemaRendition" : {
			      "@id" : "http://schema.pearson.com/shapes/v1/org/Membership/avro"
			    },
			    "konig:jsonSchemaRendition" : {
			      "@id" : "http://schema.pearson.com/shapes/v1/org/Membership/jsonschema"
			    },
			    "konig:mediaTypeBaseName" : {
			      "@value" : "application/vnd.pearson.v1.org.membership"
			    }
			  }, {
			    "@id" : "http://schema.pearson.com/shapes/v1/schema/Person",
			    "@type" : "sh:Shape",
			    "sh:targetClass" : {
			      "@id" : "schema:Person"
			    },
			    "sh:property" : [ {
			      "sh:predicate" : {
			        "@id" : "org:hasMember"
			      },
			      "sh:valueShape" : {
			        "@id" : "http://schema.pearson.com/shapes/v1/org/Membership"
			      },
			      "sh:minCount" : {
			        "@value" : "1",
			        "@type" : "xsd:integer"
			      },
			      "sh:maxCount" : {
			        "@value" : "1",
			        "@type" : "xsd:integer"
			      }
			    }, {
			      "sh:predicate" : {
			        "@id" : "konig:id"
			      },
			      "sh:nodeKind" : {
			        "@id" : "sh:IRI"
			      },
			      "sh:minCount" : {
			        "@value" : "1",
			        "@type" : "xsd:integer"
			      },
			      "sh:maxCount" : {
			        "@value" : "1",
			        "@type" : "xsd:integer"
			      }
			    } ],
			    "konig:avroSchemaRendition" : {
			      "@id" : "http://schema.pearson.com/shapes/v1/schema/Person/avro"
			    },
			    "konig:jsonSchemaRendition" : {
			      "@id" : "http://schema.pearson.com/shapes/v1/schema/Person/jsonschema"
			    },
			    "konig:mediaTypeBaseName" : {
			      "@value" : "application/vnd.pearson.v1.schema.person"
			    }
			  }, {
			    "@id" : "http://schema.pearson.com/shapes/v1/schema/SoftwareApplication",
			    "@type" : "sh:Shape",
			    "sh:targetClass" : {
			      "@id" : "schema:SoftwareApplication"
			    },
			    "sh:property" : {
			      "sh:predicate" : {
			        "@id" : "rdf:type"
			      },
			      "sh:nodeKind" : {
			        "@id" : "sh:IRI"
			      },
			      "sh:minCount" : {
			        "@value" : "1",
			        "@type" : "xsd:integer"
			      }
			    },
			    "konig:avroSchemaRendition" : {
			      "@id" : "http://schema.pearson.com/shapes/v1/schema/SoftwareApplication/avro"
			    },
			    "konig:jsonSchemaRendition" : {
			      "@id" : "http://schema.pearson.com/shapes/v1/schema/SoftwareApplication/jsonschema"
			    },
			    "konig:mediaTypeBaseName" : {
			      "@value" : "application/vnd.pearson.v1.schema.softwareapplication"
			    }
			  }, {
			    "@id" : "http://schema.pearson.com/shapes/v1/owl/Thing",
			    "@type" : "sh:Shape",
			    "sh:targetClass" : {
			      "@id" : "owl:Thing"
			    },
			    "sh:property" : [ {
			      "sh:predicate" : {
			        "@id" : "rdf:type"
			      },
			      "sh:nodeKind" : {
			        "@id" : "sh:IRI"
			      },
			      "sh:minCount" : {
			        "@value" : "1",
			        "@type" : "xsd:integer"
			      }
			    }, {
			      "sh:predicate" : {
			        "@id" : "konig:id"
			      },
			      "sh:nodeKind" : {
			        "@id" : "sh:IRI"
			      },
			      "sh:minCount" : {
			        "@value" : "1",
			        "@type" : "xsd:integer"
			      },
			      "sh:maxCount" : {
			        "@value" : "1",
			        "@type" : "xsd:integer"
			      }
			    }, {
			      "sh:predicate" : {
			        "@id" : "konig:id"
			      },
			      "sh:nodeKind" : {
			        "@id" : "sh:IRI"
			      },
			      "sh:minCount" : {
			        "@value" : "1",
			        "@type" : "xsd:integer"
			      },
			      "sh:maxCount" : {
			        "@value" : "1",
			        "@type" : "xsd:integer"
			      }
			    } ],
			    "konig:avroSchemaRendition" : {
			      "@id" : "http://schema.pearson.com/shapes/v1/owl/Thing/avro"
			    },
			    "konig:jsonSchemaRendition" : {
			      "@id" : "http://schema.pearson.com/shapes/v1/owl/Thing/jsonschema"
			    },
			    "konig:mediaTypeBaseName" : {
			      "@value" : "application/vnd.pearson.v1.owl.thing"
			    }
			  }, {
			    "@id" : "http://schema.pearson.com/shapes/v1/schema/WebPage",
			    "@type" : "sh:Shape",
			    "sh:targetClass" : {
			      "@id" : "schema:WebPage"
			    },
			    "sh:property" : {
			      "sh:predicate" : {
			        "@id" : "konig:id"
			      },
			      "rdfs:comment" : {
			        "@value" : "The address of the WebPage"
			      },
			      "sh:nodeKind" : {
			        "@id" : "sh:IRI"
			      },
			      "sh:minCount" : {
			        "@value" : "1",
			        "@type" : "xsd:integer"
			      },
			      "sh:maxCount" : {
			        "@value" : "1",
			        "@type" : "xsd:integer"
			      }
			    },
			    "konig:avroSchemaRendition" : {
			      "@id" : "http://schema.pearson.com/shapes/v1/schema/WebPage/avro"
			    },
			    "konig:jsonSchemaRendition" : {
			      "@id" : "http://schema.pearson.com/shapes/v1/schema/WebPage/jsonschema"
			    },
			    "konig:mediaTypeBaseName" : {
			      "@value" : "application/vnd.pearson.v1.schema.webpage"
			    }
			  } ]
			};
}


konig.buildOntodoc(new StaticOntologyService());
	
});
