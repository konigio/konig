package io.konig.schemagen.avro;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.openrdf.model.Namespace;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

import io.konig.core.Graph;
import io.konig.core.NamespaceManager;
import io.konig.core.Vertex;
import io.konig.core.impl.RdfUtil;
import io.konig.core.impl.TraversalImpl;
import io.konig.core.io.ResourceFile;
import io.konig.core.io.ResourceManager;
import io.konig.core.io.impl.ResourceFileImpl;
import io.konig.core.vocab.KOL;
import io.konig.core.vocab.SH;
import io.konig.schemagen.SchemaGeneratorException;
import io.konig.schemagen.avro.impl.SimpleAvroDatatypeMapper;
import io.konig.shacl.NodeKind;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;

public class AvroSchemaGenerator {
	public static final String AVRO_SCHEMA = "Avro-Schema";
	
	private AvroNamer namer;
	private AvroDatatypeMapper datatypeMapper = new SimpleAvroDatatypeMapper();
	private NamespaceManager nsManager;
	
	public AvroSchemaGenerator(AvroNamer namer, NamespaceManager nsManager) {
		this.namer = namer;
		this.nsManager = nsManager;
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
			
			ResourceFile result = ResourceFileImpl.create(schemaAddress, "application/json", entityBody);
			result.setProperty(AVRO_SCHEMA, avroName);
			return result;
		}
		
		
		
		return null;
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
		
		List<Vertex> propertyList = shapeVertex.asTraversal().out(SH.property).toVertexList();
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
			
			json.writeStringField("type", "array");
			json.writeFieldName("items");
			writeType(recordName, propertyVertex, property, json);
			
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
	
	private String documentation(PropertyConstraint p) {
		String doc = p.getDocumentation();
		if (doc != null) {
			doc = RdfUtil.normalize(doc);
		}
		
		
		if (doc == null) {
			doc = "";
		}
		StringBuilder builder = new StringBuilder(doc);
		
		addHasValueDocumentation(p, builder);
		addKnownValueDocumentation(p, builder);
		
		
		doc = builder.toString();
		return doc.length()==0 ? null : doc;
	}
	
	private void addKnownValueDocumentation(PropertyConstraint p, StringBuilder builder) {
		List<Value> possibleValues = p.getKnownValue();
		
		if (possibleValues !=null) {
			List<String> list = curieList(possibleValues);
			Set<Value> hasValue = p.getHasValue();
			if (hasValue != null) {
				for (Value v : hasValue) {
					String value = curie(v);
					list.remove(value);
				}
			}
			if (!list.isEmpty()) {
				beginClause(builder);
				builder.append("Possible values include (but are not limited to): ");
				writeList(builder, list);
			}
		}
		
	}
	

	private void addHasValueDocumentation(PropertyConstraint p, StringBuilder builder) {
		Set<Value> hasValue = p.getHasValue();
		
		if (hasValue != null && !hasValue.isEmpty()) {

			Integer maxCount = p.getMaxCount();
			if (maxCount == null || maxCount > 1) {
				beginClause(builder);
				
				builder.append("The set of values must include ");

				List<String> list = curieList(hasValue);
				if (list.size()>1) {
					builder.append(" all of ");
				}
				writeList(builder, list);
			}
			
			
		}
		
	}

	private void writeList(StringBuilder builder, List<String> list) {
		
		for (int i=0; i<list.size(); i++) {
			if (i>0) {
				if (i==list.size()-1) {
					builder.append(" and ");
				} else {
					builder.append(", ");
				}
			}
			String value = list.get(i);
			builder.append("'");
			builder.append(value);
			builder.append("'");
		}
		
	}

	private List<String> curieList(Collection<Value> source) {
		List<String> result = new ArrayList<>();
		for (Value value : source) {
			if (value instanceof URI) {
				result.add(curie((URI)value));
			} else {
				result.add(value.stringValue());
			}
		}
		Collections.sort(result);
		return result;
	}

	private void beginClause(StringBuilder builder) {
		
		if (builder.length()>0) {
			char c = builder.charAt(builder.length()-1);
			if (c != '.') {
				builder.append('.');
			}
			builder.append(' ');
		}
		
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
			AvroDatatype avroDatatype = datatypeMapper.toAvroDatatype(datatype, propertyVertex.getGraph());
			
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
			json.writeString(namer.toAvroFullName((URI)valueShapeId));
		}
		
	}

	private String strictValue(PropertyConstraint property) {
		
		Integer maxCount = property.getMaxCount();
		if (maxCount != null && maxCount==1) {
			
			Set<Value> valueSet = property.getHasValue();
			if (valueSet != null && valueSet.size()==1) {
				Value value = valueSet.iterator().next();
				if (value instanceof URI) {
					return curie((URI)value);
				}
				return value.stringValue();
			}
		}
		
		return null;
	}

	private List<String> enumList(PropertyConstraint property) {
		
		List<Value> valueList = property.getAllowedValues();
		if (valueList != null && !valueList.isEmpty()) {
			List<String> result = new ArrayList<>();
			for (Value value : valueList) {
				if (value instanceof URI) {
					// TODO: delegate to name the decision to use CURIEs
					URI uri = (URI) value;
					result.add(curie(uri));
				} else {
					result.add(value.stringValue());
				}
			}
			return result;
		}
		return null;
		
	}



	private String curie(Value value) {
		if (!(value instanceof URI)) {
			return value.stringValue();
		}
		URI uri = (URI) value;
		String result = null;
		if (nsManager == null) {
			result = uri.stringValue();
		} else {
			Namespace ns = nsManager.findByName(uri.getNamespace());
			if (ns != null) {
				StringBuilder builder = new StringBuilder();
				builder.append(ns.getPrefix());
				builder.append(':');
				builder.append(uri.getLocalName());
				result = builder.toString();
			} else {
				result = uri.stringValue();
			}
		}
		return result;
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
