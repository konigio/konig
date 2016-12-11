package io.konig.core;

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


import org.openrdf.model.Literal;
import org.openrdf.model.Namespace;
import org.openrdf.model.URI;
import org.openrdf.model.Value;


public class SPARQLBuilder {
	private StringBuilder builder;
	private NamespaceManager nsManager;
	private OwlReasoner owlReasoner;
	
	
	
	public SPARQLBuilder(NamespaceManager nsManager, OwlReasoner owlReasoner) {
		this.nsManager = nsManager;
		this.owlReasoner = owlReasoner;
		builder = new StringBuilder();
	}

	public void append(char c) {
		builder.append(c);
	}
	
	public void append(String s) {
		builder.append(s);
	}
	
	public void append(URI uri) {
		Namespace ns = nsManager.findByName(uri.getNamespace());
		if (ns == null) {
			builder.append(uri.stringValue());
		} else {
			builder.append(ns.getPrefix());
			builder.append(':');
			builder.append(uri.getLocalName());
		}
	}
	
	
	public void append(Value value) {
		if (value instanceof URI) {
			append((URI)value);
		} else if (value instanceof Literal) {
			Literal literal = (Literal) value;
			String text = value.toString();
			String language = literal.getLanguage();
			if (language != null) {
				builder.append('"');
				builder.append(text);
				builder.append('"');
				builder.append('@');
				builder.append(language);
			} else {
				URI datatype = literal.getDatatype();
				if (datatype == null || owlReasoner.isPlainLiteral(datatype)) {
					builder.append('"');
					builder.append(text);
					builder.append('"');
				} else if (owlReasoner.isIntegerDatatype(datatype)) {
				
					long integer = Long.parseLong(text);
					builder.append(integer);
				} else if (owlReasoner.isRealNumber(datatype)) {
					double number = Double.parseDouble(text);
					builder.append(number);
				} else if (owlReasoner.isBooleanType(datatype)) {
					boolean truth = "true".equalsIgnoreCase(text);
					builder.append(truth);
				} else {
					builder.append('"');
					builder.append(text);
					builder.append('"');
					builder.append('^');
					builder.append(datatype);
				}
			}
		} else {
			throw new KonigException("Blank Nodes not supported");
		}
	}

	public String toString() {
		return builder.toString();
	}
}
