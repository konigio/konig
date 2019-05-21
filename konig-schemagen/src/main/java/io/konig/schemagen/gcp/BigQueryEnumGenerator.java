package io.konig.schemagen.gcp;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	
	private static Logger logger =LoggerFactory.getLogger(BigQueryEnumGenerator.class);
	
	private ShapeManager shapeManager;
	private boolean failOnCardinalityViolation = true;
	
	public BigQueryEnumGenerator(ShapeManager shapeManager) {
		this.shapeManager = shapeManager;
	}

	public boolean isFailOnCardinalityViolation() {
		return failOnCardinalityViolation;
	}

	public void setFailOnCardinalityViolation(boolean failOnCardinalityViolation) {
		this.failOnCardinalityViolation = failOnCardinalityViolation;
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

	class TypeSelector implements ValueSelector {
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
			
			String comma = "";
			Value first = null;
			for (Value v : options) {
				msg.append(comma);
				msg.append('"');
				msg.append(v.stringValue());
				msg.append('"');
				
				if (first == null) {
					first = v;
					comma = ", ";
				}
			}
			if (failOnCardinalityViolation) {
				throw new KonigException(msg.toString());
			}
			msg.append(".  Using \"");
			msg.append(first.stringValue());
			msg.append('"');
			return first;
		}
		
	}
}
