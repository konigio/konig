package io.konig.schemagen.jsonschema.doc;

/*
 * #%L
 * Konig Schema Generator
 * %%
 * Copyright (C) 2015 - 2019 Gregory McFall
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


import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.StringTokenizer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.konig.core.io.PrettyPrintWriter;

public class JsonSchemaDocumentationGenerator {

	public JsonSchemaDocumentationGenerator() {
	}
	
	public void write(Writer out, ObjectNode schema) throws IOException {
		Worker worker = new Worker(schema, new PrettyPrintWriter(out));
		worker.object(schema, null, null, false);
		worker.flush();
	}
	
	private class Worker {
		private PrettyPrintWriter out;
		private ObjectNode root;

		public Worker(ObjectNode root, PrettyPrintWriter out) {
			this.root = root;
			this.out = out;
		}
		
		private void flush() {
			out.flush();
		}
		
		private void object(ObjectNode schema, ObjectNode schemaRef, String accessorName, boolean moreFields) throws IOException {
			out.print('{');
			comment(required(schema, schemaRef, accessorName), null, description(schema, schemaRef));
			out.pushIndent();
			 
			ObjectNode properties = properties(schema, schemaRef);
			if (properties != null) {
				Iterator<String> fieldNames = properties.fieldNames();
				while (fieldNames.hasNext()) {
					String fieldName = fieldNames.next();
					ObjectNode fieldSchema = (ObjectNode) properties.get(fieldName);
					ObjectNode fieldSchemaRef = resolveReference(fieldSchema);
					out.indent();
					quote(fieldName);
					out.print(": ");
	
					value(schema, schemaRef, fieldSchema, fieldSchemaRef, fieldName, fieldNames.hasNext(), true);
					
				}
			}
			
			out.popIndent();
			out.indent();
			out.print('}');
			if (moreFields) {
				out.println(',');
			} else {
				out.println();
			}
			
				
		}
		


		private void value(
				ObjectNode schema, ObjectNode schemaRef,
				ObjectNode fieldSchema, ObjectNode fieldSchemaRef,
				String fieldName, 
				boolean hasNext,
				boolean withComment
		) throws IOException {

			String type = type(fieldSchema, fieldSchemaRef);
			switch (type) {
			
			case "object" :
				object(fieldSchema, fieldSchemaRef, fieldName, hasNext);
				break;
				
			case "array" :
				array(schema, schemaRef, fieldSchema, fieldSchemaRef, fieldName, hasNext);
				break;
				
			default :
				out.print(type);
				comma(hasNext);
				if (withComment) {
					comment(required(schema, schemaRef, fieldName), format(fieldSchema, fieldSchemaRef), description(fieldSchema, fieldSchemaRef));
				}
				break;
				
			}
			
		}

		private void comma(boolean hasNext) {
			if (hasNext) {
				out.print(",");
			} else {
				out.print(" ");
			}
			
		}

		private void array(
				ObjectNode schema, ObjectNode schemaRef,
				ObjectNode fieldSchema, ObjectNode fieldSchemaRef,
				String fieldName, 
				boolean moreFields
		) throws IOException {
			ObjectNode items = (ObjectNode) get(fieldSchema, fieldSchemaRef, "items");
			if (items == null) {
				throw new IllegalArgumentException("Array 'items' not defined for " + fieldName);
			}
			ObjectNode itemsRef = resolveReference(items);
			out.println('[');
			out.pushIndent();
			out.indent();
			value(schema, schemaRef, items, itemsRef, fieldName, false, false);
			comment(required(schema, schemaRef, fieldName), format(items, itemsRef), description(fieldSchema, fieldSchemaRef));
			out.popIndent();
			out.indent();
			out.print(']');
			if (moreFields) {
				out.println(',');
			} else {
				out.println();
			}
			
		}

		private String type(ObjectNode schema, ObjectNode schemaRef) {
			String value = stringValue(schema, schemaRef, "type");
			if (value == null) {
				throw new IllegalArgumentException("type is not defined");
			}
			return value;
		}

		private ObjectNode resolveReference(ObjectNode schema) {
			JsonNode node = schema.get("$ref");
			if (node==null) {
				return null;
			}
			String ref = node.asText();
			if (ref != null) {
				return resolve(ref);
			}
			return null;
		}

		private void quote(String fieldName) {
			out.print('"');
			out.print(fieldName);
			out.print('"');
			
		}

		private ObjectNode properties(ObjectNode schema, ObjectNode schemaRef) {
			JsonNode properties = get(schema, schemaRef, "properties");
			
			if (properties instanceof ObjectNode) {
				return (ObjectNode) properties;
			}
			return null;
		}


		//  "$ref" : "#/definitions/LinkedDataContextShape"
		private ObjectNode resolve(String ref) {
			if (!ref.startsWith("#/")) {
				throw new IllegalArgumentException("Unexpected reference: " + ref);
			}
			StringTokenizer sequence = new StringTokenizer(ref.substring(2), "/");
			ObjectNode node = root;
			while (sequence.hasMoreTokens()) {
				String fieldName = sequence.nextToken();
				JsonNode value = node.get(fieldName);
				if (value instanceof ObjectNode) {
					node = (ObjectNode) value;
				} else {
					throw new IllegalArgumentException("Reference is not an ObjectNode: " + ref);
				}
			}
			return node;
		}

		private void comment(boolean required, String format, String description) {
			
			
			if (required || format!=null || description!=null) {
				out.print(" -- ");
				if (required || format!=null) {
					String comma = "";
					out.print('(');
					if (required) {
						out.print("Required");
						comma = ", ";
					}
					if (format!=null) {
						out.print(comma);
						out.print(format);
					}
					out.print(") ");
				}
				if (description != null) {
					out.print(description);
				}
			}
			
			out.println();
			
		}

		private String format(ObjectNode node, ObjectNode nodeRef) {
			String format = stringValue(node, nodeRef, "format");
			if (format != null) {
				switch (format) {
				
				case "date" :
					return "ISO8601/RFC-3339 date";
					
				case "date-time" :
					return "ISO8601/RFC-3339 date-time";
				
				case "uri" :
				case "uri-reference" :
					return "URI Reference";
					
				case "iri" :
				case "iri-reference" :
					return "IRI Reference";
					
				}
			}
			
			return null;
		}

		private boolean required(ObjectNode schema, ObjectNode schemaRef, String fieldName) {
			if (fieldName==null) {
				return false;
			}
			ArrayNode array = (ArrayNode) get(schema, schemaRef, "required");
			if (array != null) {
				for (int i=0; i<array.size(); i++) {
					if (fieldName.equals(array.get(i).asText())) {
						return true;
					}
				}
			}
			return false;
		}

		private JsonNode get(ObjectNode schema, ObjectNode schemaRef, String fieldName) {
			
			JsonNode node = schema==null ? null : schema.get(fieldName);
			if (node == null && schemaRef!=null) {
				node = schemaRef.get(fieldName);
			}
			return node;
		}

		private String description(ObjectNode node, ObjectNode nodeRef) {
			return stringValue(node, nodeRef, "description"); 
		}

		private String stringValue(ObjectNode node, ObjectNode nodeRef, String fieldName) {
			if (node == null) {
				return null;
			}
			JsonNode value = node.get(fieldName);
			if (value == null && nodeRef!=null) {
				value = nodeRef.get(fieldName);
			}
			if (value == null) {
				return null;
			}
			
			String text =  value.asText().trim();
			return text.length()==0 ? null : text;
		}

		
		
	}

}
