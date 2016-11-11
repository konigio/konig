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
	public static final URI Falsehood = new URIImpl("http://www.konig.io/ns/core/Falsehood");
	public static final URI KeyValue = new URIImpl("http://www.konig.io/ns/core/KeyValue");
	public static final URI KeyTerm = new URIImpl("http://www.konig.io/ns/core/KeyTerm");
	
	// Data generator terms
	public static final URI SyntheticGraphConstraints = new URIImpl("http://www.konig.io/ns/core/SyntheticGraphConstraints");
	public static final URI SyntheticClassConstraints = new URIImpl("http://www.konig.io/ns/core/SyntheticClassConstraints");
	public static final URI SyntheticShapeConstraints = new URIImpl("http://www.konig.io/ns/core/SyntheticShapeConstraints");
	public static final URI classConstraints = new URIImpl("http://www.konig.io/ns/core/classConstraints");
	public static final URI shapeConstraints = new URIImpl("http://www.konig.io/ns/core/shapeConstraints");
	public static final URI constrainedClass = new URIImpl("http://www.konig.io/ns/core/constrainedClass");
	public static final URI constrainedShape = new URIImpl("http://www.konig.io/ns/core/constrainedShape");
	public static final URI classInstanceCount = new URIImpl("http://www.konig.io/ns/core/classInstanceCount");
	public static final URI shapeInstanceCount = new URIImpl("http://www.konig.io/ns/core/shapeInstanceCount");
	
	// Generic Data Modeling

	public static final URI id = new URIImpl("http://www.konig.io/ns/core/id");
	public static final URI mediaTypeBaseName = new URIImpl("http://www.konig.io/ns/core/mediaTypeBaseName");
	public static final URI jsonSchemaRendition = new URIImpl("http://www.konig.io/ns/core/jsonSchemaRendition");
	public static final URI avroSchemaRendition = new URIImpl("http://www.konig.io/ns/core/avroSchemaRendition");
	public static final URI PreferredClass = new URIImpl("http://www.konig.io/ns/core/PreferredClass");
	
	// Fact Modeling

	public static final URI measure = new URIImpl("http://www.konig.io/ns/core/measure");
	public static final URI dimension = new URIImpl("http://www.konig.io/ns/core/dimension");
	public static final URI attribute = new URIImpl("http://www.konig.io/ns/core/attribute");
	public static final URI stereotype = new URIImpl("http://www.konig.io/ns/core/stereotype");
	public static final URI totalCount = new URIImpl("http://www.konig.io/ns/core/totalCount");
	
	

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
