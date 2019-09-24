package io.konig.schemagen.jsonschema;

import java.util.ArrayList;
import java.util.Collections;

/*
 * #%L
 * Konig Schema Generator
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


import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.konig.core.Graph;
import io.konig.core.KonigException;
import io.konig.core.NamespaceManager;
import io.konig.core.OwlReasoner;
import io.konig.core.vocab.Konig;
import io.konig.schemagen.GeneratedMediaTypeTransformer;
import io.konig.schemagen.Generator;
import io.konig.schemagen.ShapeTransformer;
import io.konig.shacl.Constraint;
import io.konig.shacl.NodeKind;
import io.konig.shacl.NotConstraint;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;

public class JsonSchemaGenerator extends Generator {
	
	private boolean includeIdValue;
	private boolean includeNodeShape=true;
	private JsonSchemaNamer namer;
	private JsonSchemaTypeMapper typeMapper;
	private boolean additionalProperties;
	private OwlReasoner reasoner;

	/**
	 * For now, we hard-code a GeneratedMediaTypeTransformer.  In the future, the shape
	 * transformer really ought to be passed to the constructor.
	 */
	private ShapeTransformer shapeTransformer = new GeneratedMediaTypeTransformer("+json");
	
	public JsonSchemaGenerator(JsonSchemaNamer namer, NamespaceManager nsManager, JsonSchemaTypeMapper typeMapper) {
		this(namer, nsManager, typeMapper, false);
	}
	public JsonSchemaGenerator(JsonSchemaNamer namer, NamespaceManager nsManager, JsonSchemaTypeMapper typeMapper, boolean additionalProperties) {
		super(nsManager);
		this.namer = namer;
		this.typeMapper = typeMapper;
		this.additionalProperties = additionalProperties;
	}
	

	protected Set<String> enumList(PropertyConstraint property) {
		
		Set<String> set = super.enumList(property);
	
		if (set == null && reasoner!=null) {
			Resource rdfType = rdfType(property);
			if (rdfType!=null && reasoner.isEnumerationClass(rdfType)) {

				set = new HashSet<>();
				
				Graph graph = reasoner.getGraph();
				Set<URI> uriSet = graph.v(rdfType).in(RDF.TYPE).toUriSet();
				for (URI uri : uriSet) {
					set.add(uri.getLocalName());
				}
			}
			
		}
		return set;
	}
	

	private Resource rdfType(PropertyConstraint property) {
		if (property.getValueClass() != null) {
			return property.getValueClass();
		}
		if (property.getShape()!=null) {
			Resource result = property.getShape().getTargetClass();
			if (result != null) {
				return result;
			}
		}
		return property.getDatatype();
	}
	public OwlReasoner getReasoner() {
		return reasoner;
	}
	public void setReasoner(OwlReasoner reasoner) {
		this.reasoner = reasoner;
	}
	public boolean isIncludeIdValue() {
		return includeIdValue;
	}

	public void setIncludeIdValue(boolean includeIdValue) {
		this.includeIdValue = includeIdValue;
	}

	public JsonSchemaNamer getNamer() {
		return namer;
	}

	/**
	 * Generate a JSON Schema for a given SHACL Shape.
	 * @param shape The Shape for which a JSON Schema is to be generated
	 * @return The generated JSON Schema
	 */
	public ObjectNode generateJsonSchema(Shape shape) {
		
		Worker worker = new Worker();
		return worker.generateJsonSchema(shape);
	}
	
	private class Worker {

		private ObjectMapper mapper = new ObjectMapper();
		private Set<String> memory = new HashSet<>();
		private ObjectNode root;
		private ObjectNode definitions;
		private boolean ldLanguageExists=false;
		
		
		public ObjectNode generateJsonSchema(Shape shape) {
			
			String schemaId = namer.schemaId(shape);
			ObjectNode json = mapper.createObjectNode();
			if (root == null) {
				root = json;
			}
			if (memory.contains(schemaId)) {
				json.put("$ref", schemaId);
			} else {
				if (memory.isEmpty()) {
					json.put("$schema", "http://json-schema.org/draft-05/schema#");
				}
				
				memory.add(schemaId);
				if (includeIdValue) {
					json.put("$id", schemaId);
				}
				if (includeNodeShape) {
					json.put(JsonSchema.Extension.nodeShape, shape.getId().stringValue());
				}
				json.put("type", "object");
				
				putProperties(json, shape);
				addConstraint(shape.getOr(), json, "anyOf");
				addConstraint(shape.getAnd(), json, "allOf");
				addConstraint(shape.getXone(), json, "oneOf");
				addNot(shape, json);
			
				putRequired(json, shape);
			}
			return json;
		}

		private void putRequired(ObjectNode json, Shape shape) {
			List<PropertyConstraint> requiredList = requiredList(shape);
			if (!requiredList.isEmpty()) {
				ArrayNode array = mapper.createArrayNode();
				for (PropertyConstraint p : requiredList) {
					String fieldName = p.getPredicate().getLocalName();
					array.add(fieldName);
				}
				json.set("required", array);
			}
		}


		private List<PropertyConstraint> requiredList(Shape shape) {
			List<PropertyConstraint> list = new ArrayList<>();
			for (PropertyConstraint p : shape.getProperty()) {
				if (p.getMinCount() != null && p.getMinCount()>0 && p.getPredicate()!=null) {
					list.add(p);
				}
			}
			return list;
		}


		private void processJsonLdContext(ObjectNode json, Shape shape) {
			
			PropertyConstraint p = shape.getPropertyConstraint(Konig.ldContext);
			if (p != null) {
				putProperty(json, p);
				Shape valueShape = p.getShape();
				if (valueShape!=null) {
					ldLanguageExists = valueShape.getPropertyConstraint(Konig.language) != null;
				}
			}
			
		}



		private void addNot(Shape shape, ObjectNode json) {
			NotConstraint not = shape.getNot();
			if (not != null) {

				Shape s = not.getShape();
				
				ObjectNode node = null;
				String schemaId = namer.schemaId(s);
				if (memory.contains(schemaId)) {
					node = mapper.createObjectNode();
					node.put("$ref", schemaId);
				} else {
					node = generateJsonSchema(s);
				}
				
				json.set("not", node);
			}
			
		}
		
		
		private void addConstraint(Constraint constraint, ObjectNode json, String fieldName) {
			if (constraint != null) {
				List<Shape> list = constraint.getShapes();
				ArrayNode array = mapper.createArrayNode();
				json.set(fieldName, array);
				for (Shape s : list) {
					String schemaId = namer.schemaId(s);
					if (memory.contains(schemaId)) {
						ObjectNode node = mapper.createObjectNode();
						node.put("$ref", schemaId);
						array.add(node);
					} else {
						ObjectNode node = generateJsonSchema(s);
						array.add(node);
					}
				}
			}
			
		}





		private void putProperties(ObjectNode json, Shape shape) {

			
			boolean hasIdProperty =  (shape.getNodeKind() == NodeKind.IRI);
			
			List<PropertyConstraint> list = shape.getProperty();
			
			
			if (hasIdProperty  || !list.isEmpty()) {
				

				boolean languageExists = ldLanguageExists;
				
				ObjectNode properties = mapper.createObjectNode();
				json.set("properties", properties);

				processJsonLdContext(properties, shape);
				
				if (hasIdProperty) {
					addIdProperty(properties, shape);
				}
				
				json.put("additionalProperties", additionalProperties);
				for (PropertyConstraint constraint : list) {
					if (Konig.ldContext.equals(constraint.getPredicate())) {
						continue;
					}
					if (shapeTransformer != null) {
						constraint = shapeTransformer.transform(shape, constraint);
					}
					putProperty(properties, constraint);
				}
				
				ldLanguageExists = languageExists;
			}
			
			// TODO: list required fields.
			
		}

		private void addIdProperty(ObjectNode properties, Shape shape) {

			ObjectNode field = mapper.createObjectNode();
			properties.set("id", field);
			field.put("type", "string");
			
			if (shape.getTargetClass() != null) {
				StringBuilder builder = new StringBuilder();
				URI targetClass = shape.getTargetClass();
				builder.append("The IRI that identifies this ");
				builder.append(className(targetClass));
				
				field.put("description", builder.toString());
				field.put(JsonSchema.Extension.rdfType, targetClass.stringValue());
				if (reasoner!=null && reasoner.isEnumerationClass(targetClass)) {
					List<String> list = new ArrayList<>();
					
					Graph graph = reasoner.getGraph();
					Set<URI> uriSet = graph.v(targetClass).in(RDF.TYPE).toUriSet();
					for (URI uri : uriSet) {
						list.add(uri.getLocalName());
					}
					Collections.sort(list);
					ArrayNode array = mapper.createArrayNode();
					for (String value : list) {
						array.add(value);
					}
					field.set("enum", array);
				}
			}
			
		}

		private String className(URI targetClass) {
			return OWL.THING.equals(targetClass) ? "entity" : targetClass.getLocalName();
		}

		private void putProperty(ObjectNode properties, PropertyConstraint property) {
			
			
			URI propertyId = property.getPredicate();
			
			if (propertyId == null) {
				return;
			}
			
			String fieldName = fieldName(property);

			Integer maxCount = property.getMaxCount();
			
			ObjectNode field = mapper.createObjectNode();
			properties.set(fieldName, field);
			
			String doc = documentation(property);
			if (doc != null) {
				field.put("description", doc);
			}
			
			if (maxCount==null || maxCount>1) {
				field.put("type", "array");
				field.set("items", createType(fieldName, property, null));
			
			} else {
				createType(fieldName, property, field);
			}
			
			
		}
		
		private String fieldName(PropertyConstraint property) {

			URI propertyId = property.getPredicate();
			if (Konig.language.equals(propertyId)) {
				return "@language";
			}
			if (Konig.ldContext.equals(propertyId)) {
				return "@context";
			}
			return propertyId.getLocalName();
		}
		


		private ObjectNode createType(String fieldName, PropertyConstraint property, ObjectNode field) {
			
			
			ObjectNode object = (field == null) ? mapper.createObjectNode() : field;
			NodeKind nodeKind = property.getNodeKind();
			URI datatype = property.getDatatype();
			Resource valueShapeId = property.getShapeId();
			Resource rdfType = rdfType(property);
			
			Set<String> enumList = null;
			
			
			if (valueShapeId == null) {
				enumList = enumList(property);
			}
			String strictValue = strictValue(property);
			
			if (strictValue != null) {
				object.put("type", "string");
				ArrayNode array = mapper.createArrayNode();
				object.set("enum", array);
				array.add(strictValue);
				
			} else if (enumList != null) {
				object.put("type", "string");
				ArrayNode array = mapper.createArrayNode();
				object.set("enum", array);
				for (String value : enumList) {
					array.add(value);
				}
			} else if (nodeKind == NodeKind.IRI) {
				object.put("type", "string");
				object.put("format", "uri");
				
			} else if (RDF.LANGSTRING.equals(datatype)) {
				
				if (ldLanguageExists) {
					object.put("type", "string");
				} else {
				
					object.put("type", "object");
					ObjectNode properties = mapper.createObjectNode();
					object.set("properties", properties);
					object.put("additionalProperties", additionalProperties);
					
					ObjectNode value = mapper.createObjectNode();
					properties.set("@value", value);
					value.put("type", "string");
					
					ObjectNode language = mapper.createObjectNode();
					properties.set("@language", language);
					
					language.put("type", "string");
					
					ArrayNode array = mapper.createArrayNode();
					object.set("required", array);
					
					array.add("@value");
					array.add("@language");
				}
				
				
			} else if (datatype != null) {
				JsonSchemaDatatype jsonType = typeMapper.type(property);
				if (jsonType == null) {
					throw new KonigException("Datatype not supported: " + datatype.stringValue());
				}
				String typeName = jsonType.getTypeName();
				String format = jsonType.getFormat();
				Number minimum = jsonType.getMinimum();
				Number maximum = jsonType.getMaximum();
				Boolean exclusiveMaximum = jsonType.getExclusiveMaximum();
				Boolean exclusiveMinimum = jsonType.getExclusiveMinimum();
				Integer minLength = property.getMinLength();
				Integer maxLength = property.getMaxLength();

				if (field != null && format==null && minimum==null && maximum==null) {
					object.put("type", typeName);
				} else {

					object.put("type", typeName);
					if (format != null) {
						object.put("format", format);
					}
					if (minimum != null) {
						object.put("minimum", (Double)minimum);
						if (exclusiveMinimum != null) {
							object.put("exclusiveMinimum", (Boolean)exclusiveMinimum);
						}
					}
					if (maximum != null) {
						object.put("maximum", (Double)maximum);
						if (exclusiveMaximum != null) {
							object.put("exclusiveMaximum", (Boolean)exclusiveMaximum);
						}
					}
				}
				
				if (minLength != null)  {
					object.put("minLength", minLength.intValue());
				}
				if (maxLength != null) {
					object.put("maxLength",  maxLength.intValue());
				}
				
				
				
			} else if (valueShapeId != null) {
				Shape valueShape = property.getShape();
				String valueSchemaName = jsonSchemaLocalName(valueShape);
				
				object.put("$ref", "#/definitions/" + valueSchemaName);
				
				
				ObjectNode valueSchema = generateJsonSchema(valueShape);
				if (definitions == null) {
					definitions = mapper.createObjectNode();
					root.set("definitions", definitions);
				}
				// This is a bit of a hack to fix Issue #1177
				// TODO: Provide a better solution
				if (definitions.get(valueSchemaName) == null) {
					definitions.set(valueSchemaName, valueSchema);
				}
			}
			
			if (rdfType != null) {
				object.put(JsonSchema.Extension.rdfType, rdfType.stringValue());
			}
			
			return object;
		}

		private String jsonSchemaLocalName(Shape shape) {
			Resource shapeId = shape.getId();
			if (shapeId instanceof URI) {
				URI uri = (URI) shapeId;
				return uri.getLocalName();
			}
			String iri = namer.schemaId(shape);
			URI uri = new URIImpl(iri);
			return uri.getLocalName();
		}

	}
	

}
