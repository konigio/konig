package io.konig.core.vocab;

/*
 * #%L
 * konig-core
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
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.model.vocabulary.XMLSchema;

import io.konig.core.Graph;


public class XSD {
	public static final URI length = new URIImpl("http://www.w3.org/2001/XMLSchema#length");
	public static final URI maxExclusive = new URIImpl("http://www.w3.org/2001/XMLSchema#maxExclusive");
	public static final URI maxInclusive = new URIImpl("http://www.w3.org/2001/XMLSchema#maxInclusive");
	public static final URI maxLength = new URIImpl("http://www.w3.org/2001/XMLSchema#maxLength");
	public static final URI minExclusive = new URIImpl("http://www.w3.org/2001/XMLSchema#minExclusive");
	public static final URI minInclusive = new URIImpl("http://www.w3.org/2001/XMLSchema#minInclusive");
	public static final URI minLength = new URIImpl("http://www.w3.org/2001/XMLSchema#minLength");
	public static final URI pattern = new URIImpl("http://www.w3.org/2001/XMLSchema#pattern");
	
	public static void addDatatypeHierarchy(Graph graph) {
		xmlSchemaDatatype(graph, XMLSchema.ANYURI,  XMLSchema.STRING);
		xmlSchemaDatatype(graph, XMLSchema.BASE64BINARY,  RDFS.LITERAL);
		xmlSchemaDatatype(graph, XMLSchema.BOOLEAN,  RDFS.LITERAL);
		xmlSchemaDatatype(graph, XMLSchema.BYTE,  XMLSchema.SHORT);
		xmlSchemaDatatype(graph, XMLSchema.DATE,  RDFS.LITERAL);
		xmlSchemaDatatype(graph, XMLSchema.DATETIME,  RDFS.LITERAL);
		xmlSchemaDatatype(graph, XMLSchema.DAYTIMEDURATION,  XMLSchema.DURATION);
		xmlSchemaDatatype(graph, XMLSchema.DECIMAL,  RDFS.LITERAL);
		xmlSchemaDatatype(graph, XMLSchema.DOUBLE,  RDFS.LITERAL);
		xmlSchemaDatatype(graph, XMLSchema.DURATION,  RDFS.LITERAL);
		xmlSchemaDatatype(graph, XMLSchema.FLOAT,  RDFS.LITERAL);
		xmlSchemaDatatype(graph, XMLSchema.GDAY,  RDFS.LITERAL);
		xmlSchemaDatatype(graph, XMLSchema.GMONTH,  RDFS.LITERAL);
		xmlSchemaDatatype(graph, XMLSchema.GMONTHDAY,  RDFS.LITERAL);
		xmlSchemaDatatype(graph, XMLSchema.GYEAR,  RDFS.LITERAL);
		xmlSchemaDatatype(graph, XMLSchema.GYEARMONTH,  RDFS.LITERAL);
		xmlSchemaDatatype(graph, XMLSchema.HEXBINARY,  RDFS.LITERAL);
		xmlSchemaDatatype(graph, XMLSchema.INT,  XMLSchema.LONG);
		xmlSchemaDatatype(graph, XMLSchema.INTEGER,  XMLSchema.DECIMAL);
		xmlSchemaDatatype(graph, RDF.LANGSTRING,  RDFS.LITERAL);
		xmlSchemaDatatype(graph, XMLSchema.LANGUAGE,  XMLSchema.TOKEN);
		xmlSchemaDatatype(graph, XMLSchema.LONG,  XMLSchema.INTEGER);
		xmlSchemaDatatype(graph, XMLSchema.NAME,  XMLSchema.TOKEN);
		xmlSchemaDatatype(graph, XMLSchema.NEGATIVE_INTEGER,  XMLSchema.NON_POSITIVE_INTEGER);
		xmlSchemaDatatype(graph, XMLSchema.NON_NEGATIVE_INTEGER,  XMLSchema.INTEGER);
		xmlSchemaDatatype(graph, XMLSchema.NON_POSITIVE_INTEGER,  XMLSchema.INTEGER);
		xmlSchemaDatatype(graph, XMLSchema.NORMALIZEDSTRING,  XMLSchema.STRING);
		xmlSchemaDatatype(graph, XMLSchema.POSITIVE_INTEGER,  XMLSchema.NON_NEGATIVE_INTEGER);
		xmlSchemaDatatype(graph, XMLSchema.SHORT,  XMLSchema.INT);
		xmlSchemaDatatype(graph, XMLSchema.STRING,  RDFS.LITERAL);
		xmlSchemaDatatype(graph, XMLSchema.TIME,  RDFS.LITERAL);
		xmlSchemaDatatype(graph, XMLSchema.UNSIGNED_BYTE,  XMLSchema.UNSIGNED_SHORT);
		xmlSchemaDatatype(graph, XMLSchema.UNSIGNED_INT,  XMLSchema.UNSIGNED_LONG);
		xmlSchemaDatatype(graph, XMLSchema.UNSIGNED_LONG,  XMLSchema.NON_NEGATIVE_INTEGER);
		xmlSchemaDatatype(graph, XMLSchema.UNSIGNED_SHORT,  XMLSchema.UNSIGNED_INT);
	}

	private static void xmlSchemaDatatype(Graph graph, URI datatype, URI subclassOf) {
		graph.edge(datatype, RDF.TYPE, RDFS.DATATYPE);
		graph.edge(datatype, RDFS.SUBCLASSOF, subclassOf);
		
	}

	
	
}
