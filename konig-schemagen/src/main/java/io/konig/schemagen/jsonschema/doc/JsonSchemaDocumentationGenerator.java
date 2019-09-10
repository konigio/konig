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
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.openrdf.model.impl.URIImpl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.konig.core.io.PrettyPrintWriter;
import io.konig.core.util.StringUtil;
import io.konig.schemagen.jsonschema.JsonSchema;

public class JsonSchemaDocumentationGenerator {

	public JsonSchemaDocumentationGenerator() {
	}
	
	public void write(Writer out, ObjectNode schema) throws IOException {
		Worker worker = new Worker(schema, new PrettyPrintWriter(out));
		worker.object(schema, null, null, false);
		worker.footnotes();
		worker.flush();
	}
	
	private static class EnumInfo {
		private int footnoteNumber;
		private String enumType;
		private ArrayNode enumArray;
		public EnumInfo(int footnoteNumber, String enumType, ArrayNode enumArray) {
			this.footnoteNumber = footnoteNumber;
			this.enumType = enumType;
			this.enumArray = enumArray;
		}
		public int getFootnoteNumber() {
			return footnoteNumber;
		}
		public String getEnumType() {
			return enumType;
		}
		public ArrayNode getEnumArray() {
			return enumArray;
		}
		
		
	}
	
	private class Worker {
		private PrettyPrintWriter out;
		private ObjectNode root;
		private List<EnumInfo> enumList;
		private int footnoteCount=0;

		public Worker(ObjectNode root, PrettyPrintWriter out) {
			this.root = root;
			this.out = out;
		}
		
		public void footnotes() {
			if (enumList!=null) {
				for (EnumInfo info : enumList) {
					int number = info.getFootnoteNumber();
					ArrayNode enumArray = info.getEnumArray();
					String enumType = info.getEnumType();
					out.println();
					out.print('[');
					out.print(number);
					out.print("] Members of the ");
					if (enumType != null) {
						out.print(enumType);
						out.print(' ');
					}
					out.println("enumeration include:");
					out.pushIndent();
					for (JsonNode node : enumArray) {
						out.indent();
						out.println(node.asText());
					}
					
					out.popIndent();
				}
			}
			
		}

		private void flush() {
			out.flush();
		}
		
		private void object(ObjectNode schema, ObjectNode schemaRef, String accessorName, boolean moreFields) throws IOException {
			out.print('{');
			comment(required(schema, schemaRef, accessorName), null, description(schema, schemaRef), null, null);
			out.pushIndent();
			 
			emitProperties(schema, schemaRef);
			
			out.popIndent();
			out.indent();
			out.print('}');
			if (moreFields) {
				out.println(',');
			} else {
				out.println();
			}
			
				
		}
		


		private ArrayNode enumList(ObjectNode schema, ObjectNode schemaRef) {
			return (ArrayNode) get(schema, schemaRef, "enum");
		}

		private void emitProperties(ObjectNode schema, ObjectNode schemaRef) throws IOException {
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
			
			logicalConstraint((ArrayNode)get(schema, schemaRef, "oneOf"), "Must match exactly one of the following {0} cases");
			logicalConstraint((ArrayNode)get(schema, schemaRef, "anyOf"), "Must match one or more of the following {0} cases");
			logicalConstraint((ArrayNode)get(schema, schemaRef, "allOf"), "Must match all of the following {0} cases");
			logicalConstraint((ArrayNode)get(schema, schemaRef, "not"), "Must NOT match any of the following {0} cases");
			
		}

		private void logicalConstraint(ArrayNode list, String pattern) throws IOException {
			if (list == null) {
				return;
			}
			
			
			String statement = MessageFormat.format(pattern, list.size());
			int separatorLength = statement.length();
			
			separator(separatorLength);
			out.indent();
			out.println(statement);
			
			for (int i=0; i<list.size(); i++) {
				ObjectNode schema = (ObjectNode) list.get(i);
				ObjectNode schemaRef = resolveReference(schema);
				
				logicalCase(i, schema, schemaRef, separatorLength);
			}
			
		}

		private void logicalCase(int index, ObjectNode schema, ObjectNode schemaRef, int separatorLength) throws IOException {
			
			String caseName = caseName(index, get(schema, schemaRef, JsonSchema.Extension.nodeShape) ); 
			out.indent();
			out.println(caseName);
			emitProperties(schema, schemaRef);
			separator(separatorLength);
		}

		private String caseName(int index, JsonNode nodeShape) {
			index++;
			if (nodeShape == null) {
				return "CASE " + index;
			}
			String localName = shapeLocalName(nodeShape.asText().trim());
			if (localName.endsWith("Shape")) {
				localName = localName.substring(0, localName.length()-5);
			}
			localName = StringUtil.label(localName);
			return MessageFormat.format("CASE {0} ... {1}", index, localName);
		}

		private String shapeLocalName(String text) {
			int begin = text.lastIndexOf('/');
			return text.substring(begin+1);
		}


		private void separator(int separatorLength) {
			
			out.indent();
			for (int i=0; i<separatorLength; i++) {
				out.print('_');
			}
			out.println();
			
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
					comment(
							required(schema, schemaRef, fieldName), 
							format(fieldSchema, fieldSchemaRef), 
							description(fieldSchema, fieldSchemaRef), 
							enumList(fieldSchema, fieldSchemaRef),
							enumType(fieldSchema, fieldSchemaRef)
						);
				}
				break;
				
			}
			
		}

		private String enumType(ObjectNode node, ObjectNode nodeRef) {
			JsonNode value = get(node, nodeRef, JsonSchema.Extension.rdfType);
			if (value != null) {
				URIImpl uri = new URIImpl(value.asText());
				return uri.getLocalName();
			}
			return null;
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
			comment(
					required(schema, schemaRef, fieldName), 
					format(items, itemsRef), 
					description(fieldSchema, fieldSchemaRef), 
					enumList(items, itemsRef),
					enumType(items, itemsRef)
			);
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

		private void comment(boolean required, String format, String description, ArrayNode enumList, String enumType) {
			
			
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
				if (enumList!=null && enumList.size()>0) {
					enumComment(description, enumList, enumType);
				}
			}
			
			out.println();
			
		}

		private void enumComment(String description, ArrayNode enumArray, String enumType) {
			if (description!=null && !description.endsWith(".")) {
				out.print('.');
			}
			out.print(" ");
			if (enumArray.size()==1) {
				out.print("The value must be \"");
				out.print(enumArray.get(0).asText());
				out.print("\".");
			} else if (enumArray.size()<=10) {
				out.print("The value must be one of: ");
				String comma = "";
				for (JsonNode value : enumArray) {
					out.print(comma);
					comma = ", ";
					out.print(value.asText());
				}
			} else {
				int footnoteNumber = ++footnoteCount;
				if (enumType != null) {
					out.print("The value must be a member of the ");
					out.print(enumType);
					out.print(" enumeration.  ");
				}
				out.print("See [");
				out.print(footnoteNumber);
				out.print("] below for the complete list of possible values.");
				EnumInfo info = new EnumInfo(footnoteNumber, enumType, enumArray);
				if (enumList == null) {
					enumList = new ArrayList<>();
				}
				enumList.add(info);
			}
			
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
