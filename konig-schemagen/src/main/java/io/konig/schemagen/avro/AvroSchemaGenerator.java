package io.konig.schemagen.avro;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.RDF;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.konig.core.KonigException;
import io.konig.core.NamespaceManager;
import io.konig.core.util.StringUtil;
import io.konig.core.vocab.SH;
import io.konig.schemagen.Generator;
import io.konig.schemagen.IriEnumStyle;
import io.konig.shacl.NodeKind;
import io.konig.shacl.OrConstraint;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;

public class AvroSchemaGenerator extends Generator {
	public static final String AVRO_SCHEMA = "Avro-Schema";
	public static final String USAGE_COUNT = "Usage-Count";
	
	private AvroNamer namer;
	private AvroDatatypeMapper datatypeMapper;
	private boolean embedValueShape = true;
	
	private static final Pattern enumSymbolPattern = Pattern.compile("[A-Za-z_][A-Za-z0-9_]*");
	
	
	
	public AvroSchemaGenerator(AvroDatatypeMapper datatypeMapper, AvroNamer namer, NamespaceManager nsManager) {
		super(nsManager);
		this.datatypeMapper = datatypeMapper;
		this.namer = namer;
		this.nsManager = nsManager;
		
		this.iriEnumStyle = IriEnumStyle.LOCAL;
	}
	
	
	public void generateAll(Collection<Shape> collection, File outDir) throws IOException, KonigException {
		outDir.mkdirs();
		
		ObjectMapper mapper = new ObjectMapper();
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
		
		for (Shape s : collection) {
			if (s.getOr() == null) {
				ObjectNode json = generateSchema(s);
				JsonNode nameNode = json.get("name");
				if (nameNode != null) {
					File outFile = new File(outDir, nameNode.asText() + ".avsc");
					mapper.writeValue(outFile, json);
				}
			}
		}
	}
	
	
	/**
	 * Get the configuration setting that determines whether the generator embeds nested
	 * schemas for related entities.
	 * @return True if the generator embeds nested schemas for related entities, and false
	 * if the generator merely provides a reference.
	 */
	public boolean isEmbedValueShape() {
		return embedValueShape;
	}


	/**
	 * Set the configuration property which determines whether the generator embeds nested
	 * schemas for related entities.
	 * @param embedValueShape A boolean value which, if true, directs the generator to embed
	 * nested schemas for related entities. If false, the generator will merely provide
	 * a named reference to the schema.
	 */
	public void setEmbedValueShape(boolean embedValueShape) {
		this.embedValueShape = embedValueShape;
	}

	@Override
	protected boolean validEnumValue(String text) {
		Matcher matcher = enumSymbolPattern.matcher(text);
		boolean result = matcher.matches();
		return result;
	}
	
	public ObjectNode generateSchema(Shape shape) throws KonigException {
		Worker worker = new Worker();

		Resource shapeId = shape.getId();
		if (shapeId instanceof URI) {
			URI shapeURI = (URI) shapeId;

			String avroName = namer.toAvroFullName(shapeURI);
			return worker.createSchema(shape, avroName);
		}
		
		throw new KonigException("Shape must have a URI id");
		
	}
	
	private class Worker {
		private JsonNodeFactory factory;
		private ObjectMapper mapper;
		private Set<String> memory;
		
		Worker() {
			factory = JsonNodeFactory.instance;
			mapper = new ObjectMapper();
			memory = new HashSet<>();
		}
		
		ObjectNode createSchema(Shape shape, String avroName) {
			memory.add(avroName);
			
			ObjectNode json = factory.objectNode();
			json.put("name",  avroName);
			json.put("type", "record");
			addFields(avroName, shape, json);
			return json;
		}

		private void addFields(String avroName, Shape shape, ObjectNode json)  {
			ArrayNode fieldArray = null;
			NodeKind nodeKind = shape.getNodeKind();
			URI targetClass = shape.getTargetClass();
			if (nodeKind != null) {
				fieldArray = mapper.createArrayNode();
				json.set("fields", fieldArray);
				
				ObjectNode idField = mapper.createObjectNode();
				fieldArray.add(idField);
				
				idField.put("name", "id");
				if (SH.IRI.equals(nodeKind)) {
					idField.put("type", "string");
				} else {
					ArrayNode typeValue = mapper.createArrayNode();
					idField.set("type", typeValue);
					typeValue.add("null");
					typeValue.add("string");
				}
				if (targetClass != null) {
					String doc = "A URI that identifies the " + targetClass.getLocalName();
					json.put("doc", doc);
				} else {
					json.put("doc", "A URI that identifies the resource");
				}
			}
			
			List<PropertyConstraint> propertyList = shape.getProperty();
			
			for (PropertyConstraint p : propertyList) {
				if (fieldArray == null) {
					fieldArray = mapper.createArrayNode();
					json.set("fields", fieldArray);
				}
				addField(shape, avroName, p, fieldArray);
			}
			
		}

		private void addField(Shape shape, String avroName, PropertyConstraint p, ArrayNode fieldArray) {
			URI predicate = p.getPredicate();
			String fieldName = predicate.getLocalName();
			
			ObjectNode field = mapper.createObjectNode();
			fieldArray.add(field);
			
			field.put("name", fieldName);
			Integer maxCount = p.getMaxCount();
			Integer minCount = p.getMinCount();
			String doc = documentation(p);
			if (doc != null) {
				field.put("doc", doc);
			}
			
			if (maxCount==null || maxCount>1) {
				ObjectNode typeValue = mapper.createObjectNode();
				field.set("type", typeValue);
				typeValue.put("type", "array");
				JsonNode itemType = type(avroName, p);
				typeValue.set("items", itemType);
				
			} else if (minCount==null || minCount==0) {
				JsonNode typeValue = type(avroName, p);
				field.set("type", typeValue);
				
			} else {
				field.set("type", type(avroName, p));
			}
			
			
		}

		private JsonNode type(String avroName, PropertyConstraint p) {
			
			NodeKind nodeKind = p.getNodeKind();
			
			Set<String> enumList = enumList(p);
			URI datatype = p.getDatatype();
			Shape valueShape = p.getValueShape();
			
			String strictValue = strictValue(p);
			
			Integer minCount = p.getMinCount();
			
			boolean optional = minCount == null || minCount==0;
			
			if (strictValue != null) {
				ObjectNode typeValue = mapper.createObjectNode();
				typeValue.put("type", "enum");
				typeValue.put("name", namer.enumName(avroName, p));
				ArrayNode symbolList = mapper.createArrayNode();
				typeValue.set("symbols", symbolList);
				symbolList.add(strictValue);
				
				return typeValue;
			} else if (enumList != null) {
				ObjectNode typeValue = mapper.createObjectNode();
				typeValue.put("type", "enum");
				typeValue.put("name", namer.enumName(avroName, p));
				ArrayNode symbolList = mapper.createArrayNode();
				typeValue.set("symbols", symbolList);
				for (String value : enumList) {
					symbolList.add(value);
				}
				return typeValue;
				
			} else if (nodeKind == NodeKind.IRI) {
				return factory.textNode("string");
			} else if (RDF.LANGSTRING.equals(datatype)) {
				ObjectNode typeValue = factory.objectNode();
				typeValue.put("name", extendedValueName(p));
				typeValue.put("name", "record");
				ArrayNode array = factory.arrayNode();
				typeValue.set("fields", array);
				ObjectNode valueField = factory.objectNode();
				array.add(valueField);
				valueField.put("name", "value");
				valueField.put("type", "string");
				
				ObjectNode langField = factory.objectNode();
				array.add(langField);
				langField.put("name", "language");
				langField.put("type", "string");
				
				return typeValue;
				
			} if (datatype != null) {
				AvroDatatype avroDatatype = datatypeMapper.toAvroDatatype(datatype);
				if (avroDatatype == null) {
					throw new KonigException("AvroDatatype not found " + datatype);
				}
				String typeName = avroDatatype.getTypeName();
				String logicalType = avroDatatype.getLogicalType();
				
				if (logicalType == null) {
					return factory.textNode(typeName);
				} else {
					ObjectNode typeValue = factory.objectNode();
					typeValue.put("type", typeName);
					typeValue.put("logicalType", logicalType);
					return typeValue;
				}
			} else if (valueShape != null) {
				
				OrConstraint orConstraint = valueShape.getOr();
				if (orConstraint == null) {
					Resource id = valueShape.getId();
					String shapeName = null;
					if (id instanceof URI) {
						shapeName = namer.toAvroFullName((URI)id);
					} else {
						shapeName = namer.valueShapeName(avroName, p);
					}
					
					if (memory.contains(shapeName)) {
						return factory.textNode(shapeName);
					}

					
					return createSchema(valueShape, shapeName);
				} else {
					ArrayNode array = mapper.createArrayNode();
					
					if (optional) {
						array.addNull();
					}
					List<Shape> shapeList = orConstraint.getShapes();
					
					for (Shape s : shapeList) {
						Resource id = s.getId();
						String shapeName = null;
						if (id instanceof URI) {
							shapeName = namer.toAvroFullName((URI)id);
							
						} else {
							throw new KonigException("Anonymous shape not permitted in sh:or constraint: " 
									+ avroName + "." + p.getPredicate().getLocalName());
						}
						
						if (memory.contains(shapeName)) {
							array.add(shapeName);
						} else {
							array.add(createSchema(s, shapeName));
						}
					}
					
					return array;
				}
				
				
				
				
			}
			
			
			return null;
		}
	}


	private String extendedValueName(PropertyConstraint property) {
		URI predicate = property.getPredicate();
		StringBuilder builder = new StringBuilder();
		builder.append(StringUtil.capitalize(predicate.getLocalName()));
		builder.append("Value");
		
		return builder.toString();
	}

}
