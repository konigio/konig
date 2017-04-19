package io.konig.schemagen.gcp;

import java.util.List;

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
				if (accept(enumId)) {
					
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


	private boolean accept(URI enumId) {
		List<Shape> shapeList = shapeManager.getShapesByTargetClass(enumId);
		for (Shape shape : shapeList) {
			List<DataSource> datasourceList = shape.getShapeDataSource();
			if (datasourceList != null) {
				for (DataSource datasource : datasourceList) {
					if (datasource instanceof GoogleBigQueryTable) {
						GoogleBigQueryTable bigquery = (GoogleBigQueryTable) datasource;
						if (bigquery.getExternalDataConfiguration() == null) {
							return false;
						}
					}
				}
			}
		}
		return true;
	}

}
