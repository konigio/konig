package io.konig.schemagen.gcp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.model.vocabulary.XMLSchema;

import io.konig.core.Graph;
import io.konig.core.NamespaceManager;
import io.konig.core.OwlReasoner;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.MemoryNamespaceManager;
import io.konig.core.vocab.Schema;
import io.konig.datasource.DataSource;
import io.konig.gcp.datasource.BigQueryTableReference;
import io.konig.gcp.datasource.GoogleBigQueryTable;
import io.konig.shacl.NodeKind;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeBuilder;
import io.konig.shacl.ShapeManager;
import io.konig.shacl.ShapeNamer;
import io.konig.shacl.ShapeVisitor;
import io.konig.shacl.SimpleShapeNamer;
import io.konig.shacl.impl.MemoryShapeManager;
import io.konig.shacl.impl.ShapeManagerShapeVistor;

public class BigQueryEnumShapeGeneratorTest {
	
	private Graph graph = new MemoryGraph();
	private OwlReasoner reasoner = new OwlReasoner(graph);
	private NamespaceManager nsManager = MemoryNamespaceManager.getDefaultInstance();
	private DatasetMapper datasetMapper = new SimpleDatasetMapper("example");
	private BigQueryTableMapper tableMapper = new LocalNameTableMapper();
	private ShapeNamer shapeNamer = new SimpleShapeNamer(nsManager, "http://example.com/shapes/", "BigQuery", "Shape");
	private ShapeManager shapeManager = new MemoryShapeManager();
	private ShapeVisitor shapeVisitor = new ShapeManagerShapeVistor(shapeManager);
	private BigQueryEnumShapeGenerator generator = new BigQueryEnumShapeGenerator(
			datasetMapper, tableMapper, shapeNamer, shapeManager, shapeVisitor);
	
	@Test
	public void testPlace() {
		URI usa = uri("http://example.com/place/us");
		URI china = uri("http://example.com/place/cn");
		
		graph.edge(Schema.Place, RDFS.SUBCLASSOF, Schema.Enumeration);
		graph.edge(Schema.Country, RDFS.SUBCLASSOF, Schema.Enumeration);
		graph.edge(Schema.Country, RDFS.SUBCLASSOF, Schema.Place);
		graph.edge(usa, RDF.TYPE, Schema.Country);
		graph.edge(usa, Schema.name, literal("United States of America"));
		graph.edge(china, RDF.TYPE, Schema.Country);
		graph.edge(china, Schema.name, literal("China"));
		
		
		GoogleBigQueryTable datasource = new GoogleBigQueryTable();
		datasource.setTableReference(new BigQueryTableReference("{gcpProjectId}", "example", "Place"));
		ShapeBuilder builder = new ShapeBuilder(shapeManager);
		URI placeShapeId = uri("http://example.com/shapes/MyPlaceShape");
		builder
			.beginShape(placeShapeId)
				.targetClass(Schema.Place)
				.nodeKind(NodeKind.IRI)
				.datasource(datasource)
				.beginProperty(Schema.name)
					.datatype(XMLSchema.STRING)
					.maxCount(1)
				.endProperty()
				.beginProperty(RDF.TYPE)
					.nodeKind(NodeKind.IRI)
					.maxCount(1)
				.endProperty()
			.endShape();

		generator.generateAll(reasoner);
		
		List<Shape> countryShapeList = shapeManager.getShapesByTargetClass(Schema.Country);
		assertEquals(0, countryShapeList.size());
		
		List<Shape> placeShapeList = shapeManager.getShapesByTargetClass(Schema.Place);
		assertEquals(1, placeShapeList.size());
		assertEquals(placeShapeId, placeShapeList.get(0).getId());
	}

	@Test
	public void testGenderType() {
		
		graph.edge(Schema.GenderType, RDFS.SUBCLASSOF, Schema.Enumeration);
		graph.edge(Schema.Male, RDF.TYPE, Schema.GenderType);
		graph.edge(Schema.Male, Schema.name, literal("Male"));
		
		generator.generateAll(reasoner);
		
		URI shapeId = uri("http://example.com/shapes/schema/BigQueryGenderTypeShape");
		Shape shape = shapeManager.getShapeById(shapeId);
		assertTrue(shape != null);
		
		List<DataSource> datasourceList = shape.getShapeDataSource();
		assertTrue(datasourceList != null);
		assertEquals(1, datasourceList.size());
		
		DataSource datasource = datasourceList.get(0);
		assertTrue(datasource instanceof GoogleBigQueryTable);
		GoogleBigQueryTable table = (GoogleBigQueryTable) datasource;
		BigQueryTableReference ref = table.getTableReference();
		assertTrue(ref != null);
		assertEquals(ref.getDatasetId(), "example");
		assertEquals(ref.getTableId(), "GenderType");
	}



	private URI uri(String value) {
		return new URIImpl(value);
	}



	private Value literal(String value) {
		return new LiteralImpl(value);
	}
	
}
