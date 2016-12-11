package io.konig.schemagen.gcp;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

import io.konig.core.Graph;
import io.konig.core.KonigException;
import io.konig.core.OwlReasoner;
import io.konig.core.Vertex;
import io.konig.core.vocab.Schema;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeManager;
import io.konig.shacl.io.json.JsonWriter;
import io.konig.shacl.io.json.ValueSelector;

public class BigQueryEnumGenerator {
	
	private ShapeManager shapeManager;
	
	public BigQueryEnumGenerator(ShapeManager shapeManager) {
		this.shapeManager = shapeManager;
	}

	public void generate(Graph graph, DataFileMapper dataFileMapper) throws IOException {
		List<Vertex> enumClassList = graph.v(Schema.Enumeration).in(RDFS.SUBCLASSOF).toVertexList();
		for (Vertex owlClass : enumClassList) {
			generate(owlClass, dataFileMapper);
		}
	}

	private void generate(Vertex owlClass, DataFileMapper dataFileMapper) throws IOException {
		
		Resource id = owlClass.getId();
		if (id instanceof URI) {
			final URI enumClassId = (URI) id;

			List<Vertex> list = owlClass.asTraversal().in(RDF.TYPE).toVertexList();
			
			if (!list.isEmpty()) {

				File file = dataFileMapper.fileForEnumRecords(owlClass);
				if (file != null) {
					
					Shape shape = getBigQueryTableShape(enumClassId);
					if (shape != null) {

						FileWriter out = new FileWriter(file);
						
						JsonFactory factory = new JsonFactory();
						JsonGenerator generator = factory.createGenerator(out);
						JsonWriter jsonWriter = new JsonWriter(generator);
						TypeSelector selector = new TypeSelector(enumClassId, new OwlReasoner(owlClass.getGraph()));
						jsonWriter.setValueSelector(selector);
						
						try {

							for (Vertex v : list) {
								selector.setVertex(v);
								jsonWriter.write(shape, v);
								generator.flush();
								out.write('\n');
							}

						} finally {
							out.close();
						}
						
					}
					
					
				}
			}
			
		}
		
		
	}

	private Shape getBigQueryTableShape(URI enumClassId) {

		List<Shape> shapeList = shapeManager.getShapesByTargetClass(enumClassId);
		for (Shape shape : shapeList) {
			if (shape.getBigQueryTableId() != null) {
				return shape;
			}
		}
		return null;
	}

	static class TypeSelector implements ValueSelector {
		private OwlReasoner reasoner;
		private Vertex vertex;
		private URI type;


		public TypeSelector(URI type, OwlReasoner reasoner) {
			this.type = type;
			this.reasoner = reasoner;
		}

		public Vertex getVertex() {
			return vertex;
		}

		public void setVertex(Vertex vertex) {
			this.vertex = vertex;
		}


		@Override
		public Value select(Vertex subject, URI predicate, Set<Value> options) {
			if (vertex == subject && predicate.equals(RDF.TYPE)) {
				Set<URI> set = new HashSet<>();
				for (Value value : options) {
					if (value instanceof URI) {
						set.add((URI)value);
					}
				}
				return reasoner.mostSpecificType(set, type);
			}
			
			StringBuilder msg = new StringBuilder();
			msg.append("Expected single value for ");
			msg.append(predicate.getLocalName());
			msg.append(" property of ");
			msg.append(subject.getId().stringValue());
			msg.append(" but found ");
			for (Value v : options) {
				msg.append(v.stringValue());
				msg.append(' ');
			}
			throw new KonigException(msg.toString());
		}
		
	}
}
