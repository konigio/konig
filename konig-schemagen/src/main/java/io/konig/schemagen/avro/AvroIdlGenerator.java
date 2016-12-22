package io.konig.schemagen.avro;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;

import io.konig.core.Graph;
import io.konig.core.impl.MemoryGraph;
import io.konig.schemagen.SchemaGeneratorException;
import io.konig.schemagen.avro.impl.BasicIdlWriter;
import io.konig.schemagen.avro.impl.SimpleAvroDatatypeMapper;
import io.konig.shacl.NodeKind;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;

public class AvroIdlGenerator  {
	
	private AvroNamer namer;
	private AvroDatatypeMapper datatypeMapper = new SimpleAvroDatatypeMapper();
	private Graph graph;
	
	public AvroIdlGenerator(AvroNamer namer, Graph graph) {
		this.namer = namer;
		if (graph == null) {
			graph = new MemoryGraph();
		}
		this.graph = graph;
	}
	
	public void generateAll(Collection<Shape> collection) throws SchemaGeneratorException, IOException {
		for (Shape shape : collection) {
			
			Resource shapeId = shape.getId();
			if (shapeId instanceof URI) {
				URI shapeURI = (URI) shapeId;
				File idlFile = namer.idlFile(shapeURI);
				idlFile.getParentFile().mkdirs();
				
				FileWriter fileWriter = new FileWriter(idlFile);
				
				try {
					generateIDL(shape, new PrintWriter(fileWriter));
				} finally {
					close(fileWriter);
				}
				
			}
			
		}
	}

	private void close(Writer writer) {
		
		try {
			writer.close();
		} catch (Throwable ignore) {
			
		}
		
	}

	public void generateIDL(Shape shape, PrintWriter out) throws SchemaGeneratorException {
		Worker worker = new Worker(out);
		try {
			worker.generate(shape);
			out.flush();
		} catch (IOException e) {
			throw new SchemaGeneratorException("Failed to generate Avro IDL for " + shape.getId().stringValue());
		}
	}
	
	class Worker {
		private IdlWriter avro;
		private String namespace;
		public Worker(PrintWriter out) {
			avro = new BasicIdlWriter(out);
		}
		
		void generate(Shape shape) throws IOException {
			
			generateImportStatements(shape);
			
			Resource shapeId = shape.getId();
			if (shapeId instanceof URI) {
				URI shapeIRI = (URI) shapeId;
				namespace = shapeIRI.getNamespace();
				String avroNamespace = namer.toAvroNamespace(namespace);
				avro.writeNamespace(avroNamespace);
				
				String recordName = shapeIRI.getLocalName();
				avro.writeStartRecord(recordName);
				generateFields(shape);
				avro.writeEndRecord();
			}
		}

		private void generateImportStatements(Shape shape) throws IOException {
			
			List<String> importList = new ArrayList<>();
			List<PropertyConstraint> list = shape.getProperty();
			for (PropertyConstraint property : list) {
				
				Resource valueShapeId = property.getShapeId();
				if (valueShapeId instanceof URI) {
					
					File file = namer.idlFile((URI)valueShapeId);
					String fileName = file.getName();
					if (!importList.contains(fileName)) {
						importList.add(fileName);
					}
				}
			}
			
			Collections.sort(importList);
			for (String file : importList) {
				avro.writeImport(file);
			}
			
		}

		private void generateFields(Shape shape) throws IOException {
			
			List<PropertyConstraint> list = shape.getProperty();
			for (PropertyConstraint property : list) {
				generateField(shape, property);
			}
			
		}

		private void generateField(Shape shape, PropertyConstraint property) throws IOException {
			
			Integer maxCount = property.getMaxCount();
			Integer minCount = property.getMinCount();
			URI datatype = property.getDatatype();
			String fieldName = property.getPredicate().getLocalName();
			String comment = property.getDocumentation();
			Resource valueShapeId = property.getShapeId();
			URI valueShape = (valueShapeId instanceof URI) ? (URI) valueShapeId : null;
			NodeKind nodeKind = property.getNodeKind();
			
			if (comment != null) {
				avro.writeDocumentation(comment);
			}
			
			if (equals(maxCount, 1) && equals(minCount, 1)) {
				// Required field
				
				String type = avroType(datatype, valueShape, nodeKind);
				avro.writeField(type, fieldName);
				
			} else if (equals(maxCount, 1) && (minCount==null || equals(minCount, 0))) {
				
				String type = avroType(datatype, valueShape, nodeKind);
				avro.writeStartUnion();
				avro.writeNull();
				avro.writeType(type);
				avro.writeEndUnion(fieldName);
				
			} else if (maxCount==null) {
				
				String itemType = avroType(datatype, valueShape, nodeKind);
				
				if (minCount!=null && !equals(minCount, 0)) {
					avro.writeArrayField(itemType, fieldName);
				} else {
					avro.writeStartUnion();
					avro.writeNull();
					avro.writeArray(itemType);
					avro.writeEndUnion(fieldName);
				}
			}
			
			
		}
		
		private String avroType(URI datatype, URI valueShape, NodeKind nodeKind) throws IOException {
			if (datatype != null) {
				return datatypeMapper.toAvroDatatype(datatype).getTypeName();
			} else if (valueShape != null) {
				return toAvroName(valueShape);
			} else if (nodeKind == NodeKind.IRI) {
				return "string";
			}
			throw new SchemaGeneratorException("Failed to map to Avro type");
		}
		
		private String toAvroName(URI shapeId) {
			if (namespace.equals(shapeId.getNamespace())) {
				return shapeId.getLocalName();
			}
			return namer.toAvroFullName(shapeId);
		}
		
		private boolean equals(Integer a, int b) {
			return a!=null && a.equals(b);
		}
		
		
	}

}
