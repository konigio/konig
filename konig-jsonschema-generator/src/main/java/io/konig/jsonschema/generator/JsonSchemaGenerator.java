package io.konig.jsonschema.generator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.RDF;

import io.konig.core.KonigException;
import io.konig.core.NamespaceManager;
import io.konig.generator.common.Generator;
import io.konig.jsonschema.model.JsonSchema;
import io.konig.jsonschema.model.JsonSchema.PropertyMap;
import io.konig.jsonschema.model.JsonSchemaDatatype;
import io.konig.shacl.AndConstraint;
import io.konig.shacl.NodeKind;
import io.konig.shacl.OrConstraint;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;

public class JsonSchemaGenerator extends Generator {
	
	private JsonSchemaNamer namer;
	private JsonSchemaTypeMapper typeMapper;
	
	public JsonSchemaGenerator(NamespaceManager nsManager, JsonSchemaNamer namer, JsonSchemaTypeMapper typeMapper) {
		super(nsManager);
		this.namer = namer;
		this.typeMapper = typeMapper;
	}
	
	public JsonSchema asJsonSchema(Shape shape) throws JsonSchemaGeneratorException {
		Worker worker = new Worker();
		return worker.asJsonSchema(shape);
	}

	
	public JsonSchemaNamer getNamer() {
		return namer;
	}

	public JsonSchemaTypeMapper getTypeMapper() {
		return typeMapper;
	}


	private class Worker {
		Map<Shape,JsonSchema> schemaMap = new HashMap<>();
		
		private JsonSchema asJsonSchema(Shape shape) throws JsonSchemaGeneratorException {
			JsonSchema schema = schemaMap.get(shape);
			if (schema == null) {
				schema = new JsonSchema();
				schemaMap.put(shape, schema);
				if (namer != null) {
					String id = namer.schemaId(shape);
					schema.setId(id);
				}
				schema.setType("object");
				putProperties(schema, shape);
				putConstraints(schema, shape);
			}
			return schema;
		}

		private void putConstraints(JsonSchema schema, Shape shape) throws JsonSchemaGeneratorException {
			putOrConstraints(schema, shape);
			putAndConstraints(schema, shape);
			
		}

		private void putAndConstraints(JsonSchema schema, Shape shape) throws JsonSchemaGeneratorException {
			AndConstraint constraint = shape.getAnd();
			if (constraint != null && !constraint.getShapes().isEmpty()) {
				List<JsonSchema> list = new ArrayList<>();
				for (Shape s : constraint.getShapes()) {
					list.add(asJsonSchema(s));
				}
				schema.setAllOf(list);
			}
		}

		private void putOrConstraints(JsonSchema schema, Shape shape) throws JsonSchemaGeneratorException {
			OrConstraint constraint = shape.getOr();
			if (constraint != null && !constraint.getShapes().isEmpty()) {
				List<JsonSchema> list = new ArrayList<>();
				for (Shape s : constraint.getShapes()) {
					list.add(asJsonSchema(s));
				}
				schema.setAnyOf(list);
			}
			
		}

		private void putProperties(JsonSchema schema, Shape shape) throws JsonSchemaGeneratorException {
			List<PropertyConstraint> list = shape.getProperty();
			if (list != null && !list.isEmpty()) {
				PropertyMap properties = new PropertyMap();
				schema.setProperties(properties);
				
				for (PropertyConstraint p : list) {
					putProperty(properties, p);
				}
			}
			
		}

		private void putProperty(PropertyMap properties, PropertyConstraint property) throws JsonSchemaGeneratorException {
			URI propertyId = property.getPredicate();
			
			String fieldName = propertyId.getLocalName();

			Integer maxCount = property.getMaxCount();
			
			JsonSchema fieldSchema = createType(property);
			
			
			if (maxCount==null || maxCount>1) {
				JsonSchema arraySchema = new JsonSchema();
				arraySchema.setType("array");
				arraySchema.setItems(fieldSchema);
				fieldSchema = arraySchema;
			}

			String doc = documentation(property);
			if (doc != null) {
				fieldSchema.setDescription(doc);
			}
			properties.put(fieldName, fieldSchema);
			
		}
		
		private JsonSchema createType(PropertyConstraint property) throws JsonSchemaGeneratorException {
			
			JsonSchema result = null;
			
			NodeKind nodeKind = property.getNodeKind();
			URI datatype = property.getDatatype();
			Resource valueShapeId = property.getShapeId();
			
			Set<String> enumList = null;
			
			
			if (valueShapeId == null) {
				enumList = enumList(property);
			}
			String strictValue = strictValue(property);
			
			if (strictValue != null) {
				result = new JsonSchema();
				result.setType("string");
				result.addEnum(strictValue);
				
			} else if (enumList != null) {
				result = new JsonSchema();
				result.setType("string");
				for (String enumValue : enumList) {
					result.addEnum(enumValue);
				}
			} else if (nodeKind == NodeKind.IRI) {
				result = JsonSchemaDatatype.URI;
				
			} else if (RDF.LANGSTRING.equals(datatype)) {
				result = JsonSchemaDatatype.LANGSTRING;
				
			} else if (datatype != null) {
				result = typeMapper.type(property);
				if (result == null) {
					throw new KonigException("Datatype not supported: " + datatype.stringValue());
				}
				
				
			} else if (valueShapeId != null) {
				result =  asJsonSchema(property.getShape());
			}
			
			return result;
		}
		
	}

}
