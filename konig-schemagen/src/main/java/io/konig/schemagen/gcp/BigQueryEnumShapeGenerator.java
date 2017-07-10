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


import java.util.List;
import java.util.Set;

import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;

import io.konig.core.Graph;
import io.konig.core.OwlReasoner;
import io.konig.core.Vertex;
import io.konig.core.vocab.Schema;
import io.konig.datasource.DataSource;
import io.konig.gcp.datasource.BigQueryTableReference;
import io.konig.gcp.datasource.GoogleBigQueryTable;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeManager;
import io.konig.shacl.ShapeNamer;
import io.konig.shacl.ShapeVisitor;

public class BigQueryEnumShapeGenerator {
	
	private DatasetMapper datasetMapper;
	private BigQueryTableMapper tableMapper;
	private ShapeNamer shapeNamer;
	private ShapeManager shapeManager;
	private ShapeVisitor shapeVisitor;
	
	public BigQueryEnumShapeGenerator(DatasetMapper datasetMapper, BigQueryTableMapper tableMapper,
			ShapeNamer shapeNamer, ShapeManager shapeManager, ShapeVisitor shapeVisitor) {
		this.datasetMapper = datasetMapper;
		this.tableMapper = tableMapper;
		this.shapeNamer = shapeNamer;
		this.shapeManager = shapeManager;
		this.shapeVisitor = shapeVisitor;
	}


	public void generateAll(OwlReasoner reasoner) {
		ShapeGenerator shapeGenerator = new ShapeGenerator(reasoner);
		Graph graph = reasoner.getGraph();
		
		List<Vertex> enumList = graph.v(Schema.Enumeration).in(RDFS.SUBCLASSOF).toVertexList();
		for (Vertex v : enumList) {
			if (v.getId() instanceof URI) {
				URI enumId = (URI) v.getId();
				if (accept(enumId, reasoner)) {
					
					List<Vertex> individuals = v.asTraversal().in(RDF.TYPE).toVertexList();
					if (!individuals.isEmpty()) {
						Shape shape = shapeGenerator.generateShape(individuals);
						URI shapeId = shapeNamer.shapeName(enumId);
						shape.setTargetClass(enumId);
						
						shape.setId(shapeId);
						
						String tableId = tableMapper.tableForClass(v);
						String datasetId = datasetMapper.datasetForClass(v);
						
						GoogleBigQueryTable table = new GoogleBigQueryTable();
						BigQueryTableReference tableReference = new BigQueryTableReference("{gcpProjectId}", datasetId, tableId);
						table.setTableReference(tableReference);
						shape.addShapeDataSource(table);
						shapeVisitor.visit(shape);
					}
					
				}
			}
		}
		
	}


	private boolean accept(URI enumId, OwlReasoner reasoner) {
		Set<URI> types = reasoner.superClasses(enumId);
		types.remove(Schema.Enumeration);
		types.add(enumId);
		
		for (Shape shape : shapeManager.listShapes()) {
			URI enumType = shape.getTargetClass();
			if (types.contains(enumType)) {
				List<DataSource> datasourceList = shape.getShapeDataSource();
				if (datasourceList != null) {
					for (DataSource datasource : datasourceList) {
						if (datasource instanceof GoogleBigQueryTable) {
							GoogleBigQueryTable bigquery = (GoogleBigQueryTable) datasource;
							if (bigquery.getExternalDataConfiguration() == null) {
								if (enumType.equals(enumId)) {
									return false;
								} else {
									PropertyConstraint p = shape.getPropertyConstraint(RDF.TYPE);
									if (p != null) {
										return false;
									}
								}
							}
						}
					}
				}
			}
			
		}
		
		
		return true;
	}

}
