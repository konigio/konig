package io.konig.core.util;

/*
 * #%L
 * Konig Core
 * %%
 * Copyright (C) 2015 - 2017 Gregory McFall
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


import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.datatypes.XMLDatatypeUtil;
import org.openrdf.model.util.Literals;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.rio.turtle.TurtleUtil;

import io.konig.core.Context;
import io.konig.core.Term;

public class TurtleElements {
	
	public static String iri(Context context, URI resource) {
		
		String iriValue = resource.stringValue();
		
		if (context != null) {
			Context inverse = context.inverse();
			Term term = inverse.getTerm(iriValue);
			if (term != null) {
				return term.getKey();
			}
			
			term = inverse.getTerm(resource.getNamespace());
			if (term != null) {
				StringBuilder builder = new StringBuilder();
				builder.append(term.getKey());
				builder.append(':');
				builder.append(resource.getLocalName());
				return builder.toString();
			}
		}
		
		StringBuilder builder = new StringBuilder();
		builder.append('<');
		builder.append(iriValue);
		builder.append('>');
		return builder.toString();
		
	}
	
	public static String bnode(BNode bnode) {
		StringBuilder builder = new StringBuilder();
		builder.append("_:");
		builder.append(bnode.getID());
		return builder.toString();
	}
	
	public static String literal(Context context, Literal literal) {

		String label = literal.getLabel();
		URI datatype = literal.getDatatype();
		
		if (XMLSchema.INTEGER.equals(datatype) || XMLSchema.DECIMAL.equals(datatype)
				|| XMLSchema.DOUBLE.equals(datatype) || XMLSchema.BOOLEAN.equals(datatype))
		{
			try {
				return XMLDatatypeUtil.normalize(label, datatype);
			}
			catch (IllegalArgumentException e) {
				// not a valid numeric typed literal. ignore error and write
				// as
				// quoted string instead.
			}
		}

		StringBuilder builder = new StringBuilder();
		if (label.indexOf('\n') != -1 || label.indexOf('\r') != -1 || label.indexOf('\t') != -1) {
			// Write label as long string
			builder.append("\"\"\"");
			builder.append(TurtleUtil.encodeLongString(label));
			builder.append("\"\"\"");
		}
		else {
			// Write label as normal string
			builder.append("\"");
			builder.append(TurtleUtil.encodeString(label));
			builder.append("\"");
		}

		if (Literals.isLanguageLiteral(literal)) {
			// Append the literal's language
			builder.append("@");
			builder.append(literal.getLanguage());
		}
		else if (!XMLSchema.STRING.equals(datatype) ) {
			// Append the literal's datatype (possibly written as an abbreviated
			// URI)
			builder.append("^^");
			builder.append(iri(context, datatype));
		}
		
		return builder.toString();
	}
	
	public static String value(Context context, Value value) {
		if (value instanceof URI) {
			return iri(context, (URI)value);
		}
		if (value instanceof BNode) {
			return bnode((BNode)value);
		}
		return literal(context, (Literal) value);
	}

	public static String iri(Resource id) {
		StringBuilder builder = new StringBuilder();
		if (id == null) {
			return "null";
		}
		if (id instanceof URI) {
			builder.append('<');
			builder.append(id.stringValue());
			builder.append('>');
		} else {
			builder.append("_:");
			builder.append(id.stringValue());
		}
		return builder.toString();
	}
	
	
}
