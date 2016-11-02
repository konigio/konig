package io.konig.core.vocab;

/*
 * #%L
 * Konig Core
 * %%
 * Copyright (C) 2015 - 2016 Gregory McFall
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


import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

public class Konig {
	
	public static final String NAMESPACE = "http://www.konig.io/ns/core/";
	
	// ChangeSet terms
	public static final URI Dictum = new URIImpl("http://www.konig.io/ns/core/Dictum");
	public static final URI Falsity = new URIImpl("http://www.konig.io/ns/core/Falsity");
	public static final URI KeyValue = new URIImpl("http://www.konig.io/ns/core/KeyValue");
	public static final URI KeyTerm = new URIImpl("http://www.konig.io/ns/core/KeyTerm");
	
	// Data generator terms
	public static final URI DataGeneratorConfig = new URIImpl("http://www.konig.io/ns/core/DataGeneratorConfig");
	public static final URI ShapeConfig = new URIImpl("http://www.konig.io/ns/core/ShapeConfig");
	public static final URI generate = new URIImpl("http://www.konig.io/ns/core/generate");
	public static final URI targetShape = new URIImpl("http://www.konig.io/ns/core/targetShape");
	public static final URI shapeCount = new URIImpl("http://www.konig.io/ns/core/shapeCount");
	
	// Konig Ontology Language terms

	public static final URI knownValue = new URIImpl("http://www.konig.io/ns/core/knownValue");
	public static final URI id = new URIImpl("http://www.konig.io/ns/core/id");
	public static final URI mediaTypeBaseName = new URIImpl("http://www.konig.io/ns/core/mediaTypeBaseName");
	public static final URI jsonSchemaRendition = new URIImpl("http://www.konig.io/ns/core/jsonSchemaRendition");
	public static final URI avroSchemaRendition = new URIImpl("http://www.konig.io/ns/core/avroSchemaRendition");
	public static final URI PreferredClass = new URIImpl("http://www.konig.io/ns/core/PreferredClass");
	public static final URI localKey = new URIImpl("http://www.konig.io/ns/core/localKey");

	public static final URI equivalentRelationalShape = new URIImpl("http://www.konig.io/ns/core/equivalentRelationalShape");
	
	// Project Management terms

	public static final URI GooglePubSubTopic = new URIImpl("http://www.konig.io/ns/core/GooglePubSubTopic");
	public static final URI projectName = new URIImpl("http://www.konig.io/ns/core/projectName");
	public static final URI topicName = new URIImpl("http://www.konig.io/ns/core/topicName");
	public static final URI acceptsShape = new URIImpl("http://www.konig.io/ns/core/acceptsShape");
	public static final URI jsonTemplate = new URIImpl("http://www.konig.io/ns/core/jsonTemplate");
	public static final URI topicProject = new URIImpl("http://www.konig.io/ns/core/topicProject");
	public static final URI projectTopic = new URIImpl("http://www.konig.io/ns/core/projectTopic");
	public static final URI datasetTemplate = new URIImpl("http://www.konig.io/ns/core/datasetTemplate");

	
	// Google Cloud Platform terms

	public static final URI GoogleCloudProject = new URIImpl("http://www.konig.io/ns/core/GoogleCloudProject");
	public static final URI BigQueryTable = new URIImpl("http://www.konig.io/ns/core/BigQueryTable");
	public static final URI tableShape = new URIImpl("http://www.konig.io/ns/core/tableShape");
}
