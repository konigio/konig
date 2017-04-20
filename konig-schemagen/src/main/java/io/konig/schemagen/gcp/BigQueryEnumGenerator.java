package io.konig.schemagen.gcp;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
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
import io.konig.core.KonigException;
import io.konig.core.NamespaceManager;
import io.konig.core.OwlReasoner;
import io.konig.core.Vertex;
import io.konig.core.vocab.Konig;
import io.konig.core.vocab.Schema;
import io.konig.datasource.DataSource;
import io.konig.gcp.datasource.GoogleBigQueryTable;
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
		
		OwlReasoner reasoner = new OwlReasoner(owlClass.getGraph());
		
		Resource id = owlClass.getId();
		if (id instanceof URI) {
			final URI enumClassId = (URI) id;

			Shape shape = getBigQueryTableShape(enumClassId);

			
			if (shape != null) {
		
				
				List<Vertex> list = null;
				
				if (shape.getPropertyConstraint(RDF.TYPE)==null) {
					list = owlClass.asTraversal().in(RDF.TYPE).toVertexList();
				} else {
					Set<URI> superClasses = reasoner.superClasses(enumClassId);
					superClasses.remove(Schema.Enumeration);
					list = owlClass.asTraversal().union(superClasses).in(RDF.TYPE).distinct().toVertexList();
				}
				
				setIdFormat(reasoner, shape, list);
				
				if (!list.isEmpty()) {
	
					File file = dataFileMapper.fileForEnumRecords(owlClass);
					if (file != null) {
						
						file.getParentFile().mkdirs();

						FileWriter out = new FileWriter(file);
						
						JsonFactory factory = new JsonFactory();
						JsonGenerator generator = factory.createGenerator(out);
						JsonWriter jsonWriter = new JsonWriter(reasoner, generator);
						TypeSelector selector = new TypeSelector(enumClassId, reasoner);
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

	private void setIdFormat(OwlReasoner reasoner, Shape shape, List<Vertex> list) {
		
		if (shape.getIdFormat() == null) {
			NamespaceManager nsManager = reasoner.getGraph().getNamespaceManager();
			if (nsManager == null) {
				shape.setIdFormat(Konig.FullyQualifiedIri);
			} else {

				for (Vertex v : list) {
					Resource id = v.getId();
					if (id instanceof URI) {
						URI uri = (URI) id;
						Namespace ns = nsManager.findByName(uri.getNamespace());
						if (ns == null) {
							shape.setIdFormat(Konig.FullyQualifiedIri);
							return;
						}
					}
				}
				shape.setIdFormat(Konig.LocalName);
			}
		}
		
	}

	private Shape getBigQueryTableShape(URI enumClassId) {

		List<Shape> shapeList = shapeManager.getShapesByTargetClass(enumClassId);
		for (Shape shape : shapeList) {
			List<DataSource> datasourceList = shape.getShapeDataSource();
			if (datasourceList != null) {
				for (DataSource source : datasourceList) {
					if (source instanceof GoogleBigQueryTable) {
						GoogleBigQueryTable table = (GoogleBigQueryTable) source;
						if (table.getExternalDataConfiguration() == null) {
							return shape;
						}
					}
				}
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
