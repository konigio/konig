package io.konig.schemagen.avro;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

import io.konig.core.Graph;
import io.konig.core.NamespaceManager;
import io.konig.core.OwlReasoner;
import io.konig.core.Vertex;
import io.konig.core.impl.RdfUtil;
import io.konig.core.impl.TraversalImpl;
import io.konig.core.io.ResourceFile;
import io.konig.core.io.ResourceManager;
import io.konig.core.io.impl.ResourceFileImpl;
import io.konig.core.vocab.KOL;
import io.konig.core.vocab.SH;
import io.konig.schemagen.GeneratedMediaTypeTransformer;
import io.konig.schemagen.Generator;
import io.konig.schemagen.IriEnumStyle;
import io.konig.schemagen.SchemaGeneratorException;
import io.konig.schemagen.ShapeTransformer;
import io.konig.schemagen.avro.impl.SimpleAvroDatatypeMapper;
import io.konig.shacl.NodeKind;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;

public class AvroSchemaGenerator extends Generator {
	public static final String AVRO_SCHEMA = "Avro-Schema";
	public static final String USAGE_COUNT = "Usage-Count";
	
	private AvroNamer namer;
	private AvroDatatypeMapper datatypeMapper;
	private boolean embedValueShape = true;
	
	private static final Pattern enumSymbolPattern = Pattern.compile("[A-Za-z_][A-Za-z0-9_]*");
	
	/**
	 * For now, we hard-code a GeneratedMediaTypeTransformer.  In the future, the shape
	 * transformer really ought to be passed to the constructor.
	 */
	private ShapeTransformer shapeTransformer = new GeneratedMediaTypeTransformer("+avro");
	
	public AvroSchemaGenerator(AvroDatatypeMapper datatypeMapper, AvroNamer namer, NamespaceManager nsManager) {
		super(nsManager);
		this.datatypeMapper = datatypeMapper;
		this.namer = namer;
		this.nsManager = nsManager;
		
		this.iriEnumStyle = IriEnumStyle.NONE;
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



	public void generateAll(Graph graph, ResourceManager resourceManager) throws IOException {
		
		List<Vertex> shapeList = graph.v(SH.Shape).in(RDF.TYPE).toVertexList();
		for (Vertex v : shapeList) {
			ResourceFile file = generateSchema(v);
			if (file != null) {
				resourceManager.put(file);
			}
		}
	}


	/**
	 * Generate an Avro schema for a given SHACL shape.
	 * @param shape The Shape for which an Avro schema shall be generated
	 * @return A ResourceFile that encapsulates a description of the Avro schema.
	 * @throws IOException
	 */
	public ResourceFile generateSchema(Vertex shape) throws IOException {
		
		Resource id = shape.getId();
		
		if (id instanceof URI) {
			URI uri = (URI) id;
			StringWriter writer = new StringWriter();
			JsonFactory factory = new JsonFactory();
			JsonGenerator json = factory.createGenerator(writer);
			json.useDefaultPrettyPrinter();
			
			
			String avroName = doGenerateSchema(uri, shape, json);
			json.flush();

			String entityBody = writer.toString();
			String schemaAddress = namer.toAvroSchemaURI(uri.stringValue());
			int usageCount = shape.asTraversal().in(SH.valueShape).toVertexList().size();
			
			ResourceFile result = ResourceFileImpl.create(schemaAddress, "application/json", entityBody);
			result.setProperty(AVRO_SCHEMA, avroName);
			result.setProperty(USAGE_COUNT, Integer.toString(usageCount));
			
			
			URI schemaId = new URIImpl(result.getContentLocation());
			shape.getGraph().edge(id, KOL.avroSchemaRendition, schemaId);
			
			return result;
		}
		
		
		
		return null;
	}



	@Override
	protected boolean validEnumValue(String text) {
		Matcher matcher = enumSymbolPattern.matcher(text);
		boolean result = matcher.matches();
		return result;
	}
	private String doGenerateSchema(URI uri, Vertex shape, JsonGenerator json) throws IOException {
				
		String avroName = namer.toAvroFullName(uri);
		
		json.writeStartObject();
		json.writeStringField("name", avroName);
		json.writeStringField("type", "record");
		
		generateFields(avroName, shape, json);
		
		json.writeEndObject();
		
		return avroName;
		
		
	}


	private void generateFields(String recordName, Vertex shapeVertex, JsonGenerator json) throws IOException {
		
		
		Shape shape = new Shape();
		
		Value scopeClass = shapeVertex.getValue(SH.scopeClass);
		if (scopeClass instanceof URI) {
			shape.setScopeClass((URI) scopeClass);
		}
		
		List<Vertex> propertyList = shapeVertex.asTraversal().out(SH.property).distinct().toVertexList();
		boolean fieldStart = false;
		for (Vertex p : propertyList) {
			if (!fieldStart) {
				json.writeArrayFieldStart("fields");
				fieldStart = true;
			}
			writeField(shape, recordName, p, json);
		}
		
		if (fieldStart) {
			json.writeEndArray();
		}
		
	}


	private void writeField(Shape shape, String recordName, Vertex propertyVertex, JsonGenerator json) throws IOException {

		
		PropertyConstraint property = asPropertyConstraint(shape, propertyVertex);
		if (property == null) {
			return;
		}
		URI predicate = property.getPredicate();
		
		String fieldName = predicate.getLocalName();

		json.writeStartObject();
		json.writeStringField("name", fieldName);
		Integer maxCount = property.getMaxCount();
		Integer minCount = property.getMinCount();
		String doc = documentation(property);
		if (doc != null) {
			json.writeStringField("doc", doc);
		}
		
		if (maxCount == null || maxCount>1) {
			
			json.writeObjectFieldStart("type");
			json.writeStringField("type", "array");
			json.writeFieldName("items");
			writeType(recordName, propertyVertex, property, json);
			json.writeEndObject();
			
		} else if (minCount == null || minCount==0) {

			json.writeFieldName("type");
			json.writeStartArray();
			json.writeString("null");
			writeType(recordName, propertyVertex, property, json);
			json.writeEndArray();
			
		} else {
			json.writeFieldName("type");
			writeType(recordName, propertyVertex, property, json);
		}

		json.writeEndObject();
		
	}
	


	private void writeType(String recordName, Vertex propertyVertex, PropertyConstraint property, JsonGenerator json) throws IOException {
		
		List<String> enumList = null;

		NodeKind nodeKind = property.getNodeKind();
		URI datatype = property.getDatatype();
		Resource valueShapeId = property.getValueShapeId();
		
		if (valueShapeId == null) {
			enumList = enumList(property);
			
		}
		
		String strictValue = strictValue(property);
		
		if (strictValue != null) {
			json.writeStartObject();
			json.writeStringField("type", "enum");
			json.writeStringField("name", namer.enumName(recordName, property, propertyVertex));
			json.writeFieldName("symbols");
			json.writeStartArray();
			json.writeString(strictValue);
			json.writeEndArray();
			json.writeEndObject();
		} else if (enumList != null) {
			json.writeStartObject();
			json.writeStringField("type", "enum");
			json.writeStringField("name", namer.enumName(recordName, property, propertyVertex));
			json.writeFieldName("symbols");
			json.writeStartArray();
			for (String value : enumList) {
				json.writeString(value);
			}
			json.writeEndArray();
			json.writeEndObject();
		} else if (nodeKind == NodeKind.IRI) {
			json.writeString("string");
		} else if (datatype != null) {
			AvroDatatype avroDatatype = datatypeMapper.toAvroDatatype(datatype);
			
			if (avroDatatype == null) {
				throw new IOException("AvroDatatype not found: " + datatype);
			}
			
			String typeName = avroDatatype.getTypeName();
			String logicalType = avroDatatype.getLogicalType();
			
			if (logicalType == null) {
				json.writeString(typeName);
			} else {
				json.writeStartObject();
				json.writeStringField("type", typeName);
				json.writeStringField("logicalType", logicalType);
				json.writeEndObject();
			}
			
			
		} else if (valueShapeId instanceof URI) {
			URI valueShapeURI = (URI) valueShapeId;
			
			if (embedValueShape) {
				Vertex embeddedShape = propertyVertex.getGraph().getVertex(valueShapeURI);
				doGenerateSchema(valueShapeURI, embeddedShape, json);
				
			} else {
				json.writeString(namer.toAvroFullName((URI)valueShapeId));
			}
		}
		
	}




	private PropertyConstraint asPropertyConstraint(Shape shape, Vertex propertyVertex) {

		URI predicate = uri(propertyVertex, SH.predicate);
		if (predicate == null) {
			throw new SchemaGeneratorException("Missing predicate for PropertyConstraint");
		}

		PropertyConstraint p = new PropertyConstraint(predicate);
		p.setDatatype( uri(propertyVertex, SH.datatype) );
		p.setNodeKind( NodeKind.fromURI(uri(propertyVertex, SH.nodeKind)) );
		p.setMinCount(intValue(propertyVertex, SH.minCount));
		p.setMaxCount(intValue(propertyVertex, SH.maxCount));
		p.setDirectValueType(uri(propertyVertex, SH.directType));
		p.setValueClass(uri(propertyVertex, SH.valueClass));
		
		String description = description(propertyVertex, predicate);
		
		List<Value> hasValueList = propertyVertex.asTraversal().out(SH.hasValue).toValueList();
		List<Value> allowedValues = allowedValues(propertyVertex);
		
		
		if (description != null) {
			p.setDocumentation(description);
		}
		
		for (Value value : hasValueList) {
			p.addHasValue(value);
		}
		if (allowedValues != null) {
			for (Value value : allowedValues) {
				p.addAllowedValue(value);
			}
		}
		p.setValueShapeId(uri(propertyVertex, SH.valueShape));

		addKnownValues(shape, propertyVertex, p);
		
		if (shapeTransformer != null) {
			p = shapeTransformer.transform(shape, p);
		}
		
		return p;
		
	}
	
	
	private void addKnownValues(Shape shape, Vertex propertyVertex, PropertyConstraint p) {
		
		if (p.getValueShapeId() != null) {
			return;
		}
		
		Graph graph = propertyVertex.getGraph();
		
		
		List<Value> allowed = p.getAllowedValues();
		if (allowed!=null && !allowed.isEmpty()) {
			return;
		}
		
		List<Vertex> typeList = null;
		URI predicate = p.getPredicate();
		if (RDF.TYPE.equals(predicate)) {
			URI scopeClass = shape.getScopeClass();
			if (scopeClass != null) {
				Vertex scopeVertex = graph.vertex(scopeClass);
				typeList = RdfUtil.subtypeList(scopeVertex);
				typeList.add(scopeVertex);
				for (Vertex v : typeList) {
					p.addKnownValue(v.getId());
				}
			}
			
			
			
		} else {
		

			
			if (KOL.id.equals(p.getPredicate())) {

				URI scopeClass = shape.getScopeClass();
				if (scopeClass != null) {
					Vertex scopeVertex = graph.vertex(scopeClass);
					typeList = RdfUtil.subtypeList(scopeVertex);
					typeList.add(scopeVertex);
				}
			}
			
			if (typeList == null || typeList.isEmpty()) {
				typeList = propertyVertex.asTraversal().out(SH.valueClass).toVertexList();
			}
			
			if (!typeList.isEmpty()) {
				List<Vertex> subtypes = RdfUtil.listSubtypes(typeList);
				typeList.addAll(subtypes);
				
			}
			
			if (typeList.isEmpty()) {
				typeList = propertyVertex.asTraversal().out(SH.directType).toVertexList();
			} 
			
			if (typeList.isEmpty()) {
				typeList = propertyVertex.asTraversal().out(SH.valueShape).out(SH.scopeClass).distinct().toVertexList();
				List<Vertex> subtypes = RdfUtil.listSubtypes(typeList);
				typeList.addAll(subtypes);
			}
			
			if (typeList.isEmpty()) {
				typeList = graph.v(p.getPredicate()).out(RDFS.RANGE).toVertexList();
			}
			
			TraversalImpl t = new TraversalImpl(graph, typeList);
			
			List<Vertex> list = t.in(RDF.TYPE).distinct().toVertexList();
			for (Vertex v : list) {
				p.addKnownValue(v.getId());
			}
		}
	}


	private String description(Vertex propertyVertex, URI predicate) {
		String result = RdfUtil.getDescription(propertyVertex);
		if (result == null) {
			Vertex v = propertyVertex.getGraph().getVertex(predicate);
			result = RdfUtil.getDescription(v);
		}
		return result;
	}

	private List<Value> allowedValues(Vertex propertyVertex) {
		Vertex v = propertyVertex.asTraversal().firstVertex(SH.in);
		return (v==null) ? null : v.asList();
	}

	private URI uri(Vertex propertyVertex, URI predicate) {
		Value v = value(propertyVertex, predicate);
		return v instanceof URI ? (URI)v : null;
	}

	private Integer intValue(Vertex propertyVertex, URI predicate) {
		Value value = value(propertyVertex, predicate);
		
		return value==null ? null : new Integer(value.stringValue());
	}

	private Value value(Vertex v, URI predicate) {
		return v.asTraversal().firstValue(predicate);
	}
	
	

}
