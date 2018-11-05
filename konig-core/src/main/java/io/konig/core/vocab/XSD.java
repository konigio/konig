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
		graph.edge(XMLSchema.ANYURI, RDFS.SUBCLASSOF, XMLSchema.STRING);
		graph.edge(XMLSchema.BASE64BINARY, RDFS.SUBCLASSOF, RDFS.DATATYPE);
		graph.edge(XMLSchema.BOOLEAN, RDFS.SUBCLASSOF, RDFS.DATATYPE);
		graph.edge(XMLSchema.BYTE, RDFS.SUBCLASSOF, XMLSchema.SHORT);
		graph.edge(XMLSchema.DATE, RDFS.SUBCLASSOF, RDFS.DATATYPE);
		graph.edge(XMLSchema.DATETIME, RDFS.SUBCLASSOF, RDFS.DATATYPE);
		graph.edge(XMLSchema.DAYTIMEDURATION, RDFS.SUBCLASSOF, XMLSchema.DURATION);
		graph.edge(XMLSchema.DECIMAL, RDFS.SUBCLASSOF, RDFS.DATATYPE);
		graph.edge(XMLSchema.DOUBLE, RDFS.SUBCLASSOF, RDFS.DATATYPE);
		graph.edge(XMLSchema.DURATION, RDFS.SUBCLASSOF, RDFS.DATATYPE);
		graph.edge(XMLSchema.FLOAT, RDFS.SUBCLASSOF, RDFS.DATATYPE);
		graph.edge(XMLSchema.GDAY, RDFS.SUBCLASSOF, RDFS.DATATYPE);
		graph.edge(XMLSchema.GMONTH, RDFS.SUBCLASSOF, RDFS.DATATYPE);
		graph.edge(XMLSchema.GMONTHDAY, RDFS.SUBCLASSOF, RDFS.DATATYPE);
		graph.edge(XMLSchema.GYEAR, RDFS.SUBCLASSOF, RDFS.DATATYPE);
		graph.edge(XMLSchema.GYEARMONTH, RDFS.SUBCLASSOF, RDFS.DATATYPE);
		graph.edge(XMLSchema.HEXBINARY, RDFS.SUBCLASSOF, RDFS.DATATYPE);
		graph.edge(XMLSchema.INT, RDFS.SUBCLASSOF, XMLSchema.LONG);
		graph.edge(XMLSchema.INTEGER, RDFS.SUBCLASSOF, XMLSchema.DECIMAL);
		graph.edge(XMLSchema.LANGUAGE, RDFS.SUBCLASSOF, XMLSchema.TOKEN);
		graph.edge(XMLSchema.LONG, RDFS.SUBCLASSOF, XMLSchema.INTEGER);
		graph.edge(XMLSchema.NAME, RDFS.SUBCLASSOF, XMLSchema.TOKEN);
		graph.edge(XMLSchema.NEGATIVE_INTEGER, RDFS.SUBCLASSOF, XMLSchema.NON_POSITIVE_INTEGER);
		graph.edge(XMLSchema.NON_NEGATIVE_INTEGER, RDFS.SUBCLASSOF, XMLSchema.INTEGER);
		graph.edge(XMLSchema.NON_POSITIVE_INTEGER, RDFS.SUBCLASSOF, XMLSchema.INTEGER);
		graph.edge(XMLSchema.NORMALIZEDSTRING, RDFS.SUBCLASSOF, XMLSchema.STRING);
		graph.edge(XMLSchema.POSITIVE_INTEGER, RDFS.SUBCLASSOF, XMLSchema.NON_NEGATIVE_INTEGER);
		graph.edge(XMLSchema.SHORT, RDFS.SUBCLASSOF, XMLSchema.INT);
		graph.edge(XMLSchema.STRING, RDFS.SUBCLASSOF, RDFS.DATATYPE);
		graph.edge(XMLSchema.TIME, RDFS.SUBCLASSOF, RDFS.DATATYPE);
		graph.edge(XMLSchema.UNSIGNED_BYTE, RDFS.SUBCLASSOF, XMLSchema.UNSIGNED_SHORT);
		graph.edge(XMLSchema.UNSIGNED_INT, RDFS.SUBCLASSOF, XMLSchema.UNSIGNED_LONG);
		graph.edge(XMLSchema.UNSIGNED_LONG, RDFS.SUBCLASSOF, XMLSchema.NON_NEGATIVE_INTEGER);
		graph.edge(XMLSchema.UNSIGNED_SHORT, RDFS.SUBCLASSOF, XMLSchema.UNSIGNED_INT);
	}

	
	
}
