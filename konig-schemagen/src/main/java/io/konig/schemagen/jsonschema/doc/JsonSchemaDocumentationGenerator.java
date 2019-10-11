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
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;

import org.openrdf.model.impl.URIImpl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
		worker.object(schema, null, null, null, false);
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
		private String lineSeparator;
		private Map<String,Boolean> recursiveMap = new HashMap<>();
		private Deque<String> caseNumberStack = new ArrayDeque();
		private ObjectMapper mapper;
		
		public Worker(ObjectNode root, PrettyPrintWriter out) {
			this.root = root;
			this.out = out;
			lineSeparator = System.lineSeparator();
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
		
		private void object(ObjectNode schema, ObjectNode schemaRef, String accessorName, String comment, boolean moreFields) throws IOException {
			
			Boolean isRecursive = isRecursive(schema);
			
			if (comment == null) {
				comment = comment(required(schema, schemaRef, accessorName), null, description(schema, schemaRef), null, null);
			}
			comment = objectComment(isRecursive, comment, schema, schemaRef);
			out.print('{');
			out.print(comment);
			
			out.pushIndent();
			 
			if (!Boolean.TRUE.equals(isRecursive)) {
				emitProperties(schema, schemaRef);
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
		


		private String objectComment(Boolean recursive, String comment, ObjectNode node, ObjectNode nodeRef) {
			if (isRecursive(node, nodeRef)) {
				String ref = ref(node);
				StringBuilder builder = new StringBuilder();
				if (comment != null) {
					comment = comment.trim();
					if (comment.isEmpty()) {
						builder.append(" -- ");
					} else {
						builder.append(comment);
						if (comment.endsWith(".")) {
							builder.append(' ');
						} else {
							builder.append(". ");
						}
					}
				}
				String localName = localName(ref);
				if (Boolean.TRUE.equals(recursive)) {
					builder.append("Repeat the '");
					builder.append(localName);
					builder.append("' structure here...");
					
				} else {
					builder.append("This is a recursive structure named '");
					builder.append(localName);
					builder.append("'.");
				}
				builder.append(lineSeparator);
				
				comment = builder.toString();
				
			}
			return comment;
		}

		private String localName(String ref) {
			if ("#".equals(ref)) {
				JsonNode nodeShape = root.get("nodeShape");
				ref = nodeShape==null ? "/RootNode" : nodeShape.asText();
			}
			int slash = ref.lastIndexOf('/');
			if (slash < 0) {
				return ref;
			}
			return ref.substring(slash+1);
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
	
					value(
							schema, schemaRef, 
							fieldSchema, 	fieldSchemaRef, 
							fieldName, 
							null,
							fieldNames.hasNext(), true);
					
				}
			}
			
			logicalConstraint(schema, schemaRef, "oneOf", "Must match exactly one of the following {0} cases");
			logicalConstraint(schema, schemaRef, "anyOf", "Must match one or more of the following {0} cases");
			logicalConstraint(schema, schemaRef, "not", "Must NOT match any of the following {0} cases");
			
			
		}

		private void logicalConstraint(ObjectNode schema, ObjectNode schemaRef, String fieldName, String pattern) throws IOException {
			
			ArrayNode constraint = (ArrayNode) get(schema, schemaRef, fieldName);
			JsonNode allOf = get(schema, schemaRef, "allOf");
			if (allOf instanceof ArrayNode) {
				ArrayNode allOfArray = (ArrayNode)allOf;
				Iterator<JsonNode> sequence = allOfArray.elements();
				while (sequence.hasNext()) {
					ObjectNode next = (ObjectNode) sequence.next();
					ObjectNode nextRef = resolveReference(next);
					JsonNode node = get(next, nextRef, fieldName);
					if (node instanceof ArrayNode) {
						ArrayNode array = (ArrayNode) node;
						logicalConstraint(array, pattern);
					}
				}
			} 
			logicalConstraint(constraint, pattern);
			
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
			separator(separatorLength);
			
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
			caseNumberStack.pop();
		}

		private String caseName(int index, JsonNode nodeShape) {
			index++;
			
			String caseNumber = caseNumber(index);
			if (nodeShape == null) {
				return "CASE " + caseNumber;
			}
			String localName = shapeLocalName(nodeShape.asText().trim());
			if (localName.endsWith("Shape")) {
				localName = localName.substring(0, localName.length()-5);
			}
			localName = StringUtil.label(localName);
			return MessageFormat.format("CASE {0} ... {1}", caseNumber, localName);
		}

		private String caseNumber(int index) {

			String text = Integer.toString(index);
			if (!caseNumberStack.isEmpty()) {
				text = caseNumberStack.peek() + "." + text;
			}
			caseNumberStack.push(text);
			return text;
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
				String comment, 
				boolean hasNext,
				boolean withComment
		) throws IOException {

			String type = type(fieldSchema, fieldSchemaRef);
			switch (type) {
			
			case "object" :
				object(fieldSchema, fieldSchemaRef, fieldName, comment, hasNext);
				break;
				
			case "array" :
				array(schema, schemaRef, fieldSchema, fieldSchemaRef, fieldName, hasNext);
				break;
				
			default :
				out.print(type);
				comma(hasNext);
				if (withComment) {
					comment = comment(
							required(schema, schemaRef, fieldName), 
							format(fieldSchema, fieldSchemaRef), 
							description(fieldSchema, fieldSchemaRef), 
							enumList(fieldSchema, fieldSchemaRef),
							enumType(fieldSchema, fieldSchemaRef)
						);
				}
				if (comment != null) {
					out.print(comment);
				} else {
					out.println();
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
			String comment = comment(
					required(schema, schemaRef, fieldName), 
					format(items, itemsRef), 
					description(fieldSchema, fieldSchemaRef), 
					enumList(items, itemsRef),
					enumType(items, itemsRef)
			);
			value(schema, schemaRef, items, itemsRef, fieldName, comment, false, false);
			
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
			JsonNode allOf = get(schema, schemaRef, "allOf");
			
			if (properties instanceof ObjectNode && allOf==null) {
				return (ObjectNode) properties;
			}
			if (allOf instanceof ArrayNode) {
				return merge((ObjectNode) properties, (ArrayNode)allOf);
			}
			return null;
		}


		private ObjectNode merge(ObjectNode properties, ArrayNode array) {
			ObjectMapper mapper = mapper();
			ObjectNode result = mapper.createObjectNode();
			if (properties != null) {
				copyFields(properties, result);
			}
			if (array!=null) {
				Iterator<JsonNode> sequence = array.iterator();
				while (sequence.hasNext()) {
					JsonNode node = sequence.next();
					
					JsonNode ref = node.get("$ref");
					if (ref != null) {
						node = resolve(ref.asText());
					}
					
					JsonNode elementProperties = node.get("properties");
					if (elementProperties instanceof ObjectNode) {
						copyFields((ObjectNode)elementProperties, result);
					}
					
				}
			}
			
			return result;
		}

		private void copyFields(ObjectNode properties, ObjectNode result) {

			Iterator<Entry<String,JsonNode>> sequence = properties.fields();
			while (sequence.hasNext()) {
				Entry<String,JsonNode> e = sequence.next();
				String fieldName = e.getKey();
				JsonNode fieldValue = e.getValue();
				result.set(fieldName, fieldValue);
			}
			
		}

		private ObjectMapper mapper() {
			if (mapper == null) {
				mapper = new ObjectMapper();
			}
			return mapper;
		}

		//  "$ref" : "#/definitions/LinkedDataContextShape"
		private ObjectNode resolve(String ref) {
			if ("#".equals(ref)) {
				return root;
			}
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

		private String comment(boolean required, String format, String description, ArrayNode enumList, String enumType) {
			
			StringBuilder builder = new StringBuilder();
			
			if (required || format!=null || description!=null) {
				builder.append(" -- ");
				if (required || format!=null) {
					String comma = "";
					builder.append('(');
					if (required) {
						builder.append("Required");
						comma = ", ";
					}
					if (format!=null) {
						builder.append(comma);
						builder.append(format);
					}
					builder.append(") ");
				}
				if (description != null) {
					builder.append(description);
				}
				if (enumList!=null && enumList.size()>0) {
					enumComment(builder, description, enumList, enumType);
				}
			}
			
			builder.append(lineSeparator);
			
			return builder.toString();
			
		}

		private void enumComment(StringBuilder builder, String description, ArrayNode enumArray, String enumType) {
			if (description!=null && !description.endsWith(".")) {
				builder.append('.');
			}
			builder.append(" ");
			if (enumArray.size()==1) {
				builder.append("The value must be \"");
				builder.append(enumArray.get(0).asText());
				builder.append("\".");
			} else if (enumArray.size()<=10) {
				builder.append("The value must be one of: ");
				String comma = "";
				for (JsonNode value : enumArray) {
					builder.append(comma);
					comma = ", ";
					builder.append(value.asText());
				}
			} else {
				int footnoteNumber = ++footnoteCount;
				if (enumType != null) {
					builder.append("The value must be a member of the ");
					builder.append(enumType);
					builder.append(" enumeration.  ");
				}
				builder.append("See [");
				builder.append(footnoteNumber);
				builder.append("] below for the complete list of possible values.");
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
		


		private Boolean isRecursive(ObjectNode schema) {

			if (schema != null) {
				
				String ref = ref(schema);
				
				if (ref != null) {
					return recursiveMap.get(ref);
				}
			}
			
			return null;
		}

		private String ref(ObjectNode schema) {

			JsonNode refNode = schema.get("$ref");
			if (refNode != null) {
				return refNode.asText();
			}
			if (schema == root) {
				return "#";
			}
			return null;
		}

		private boolean isRecursive(ObjectNode node, ObjectNode nodeRef) {
			if (node == null) {
				return false;
			}
			

			String ref = ref(node);
			if (ref == null) {
				return false;
			}
			Boolean truth = recursiveMap.get(ref);
			if (truth == null) {
				truth = isRecursive(ref, node, nodeRef);
				recursiveMap.put(ref, truth);
			}
			
			return truth;
			
		}
		private boolean isRecursive(String ref, ObjectNode node, ObjectNode nodeRef) {
			
			
			ObjectNode properties = properties(node, nodeRef);
			if (properties != null) {
				Iterator<String> fieldNames = properties.fieldNames();
				while (fieldNames.hasNext()) {
					String fieldName = fieldNames.next();
					ObjectNode fieldSchema = (ObjectNode) properties.get(fieldName);
					
					
					JsonNode fieldRefNode = fieldSchema.get("$ref");
					if (fieldRefNode==null) {

						JsonNode items = fieldSchema.get("items");
						if (items != null) {
							fieldRefNode = items.get("$ref");
							if (fieldRefNode == null) {
								continue;
							}
						} else {
							continue;
						}
					}
					String fieldRef = fieldRefNode.asText();
					if (fieldRef.equals(ref)) {
						return true;
					}

					ObjectNode fieldSchemaRef = resolveReference(fieldSchema);
				
					if (isRecursive(ref, fieldSchema, fieldSchemaRef)) {
						return true;
					}
					
				}
			}

			return false;
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
