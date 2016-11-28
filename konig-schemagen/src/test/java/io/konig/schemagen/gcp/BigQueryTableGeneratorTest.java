package io.konig.schemagen.gcp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Ignore;
import org.junit.Test;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.DC;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.model.vocabulary.XMLSchema;

import io.konig.core.NamespaceManager;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.MemoryNamespaceManager;
import io.konig.core.vocab.Schema;
import io.konig.schemagen.SimpleShapeNamer;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeManager;
import io.konig.shacl.impl.MemoryShapeManager;

public class BigQueryTableGeneratorTest {

	@Ignore
	public void testGenerateEnumTables() {
		
		NamespaceManager nsManager = new MemoryNamespaceManager();
		nsManager.add("schema", "http://schema.org/");
		
		MemoryGraph graph = new MemoryGraph();
		graph.edge(Schema.GenderType, RDFS.SUBCLASSOF, Schema.Enumeration);
		graph.edge(Schema.Male, RDF.TYPE, Schema.GenderType);
		graph.edge(Schema.Female, RDF.TYPE, Schema.GenderType);
		
		ShapeManager shapeManager = new MemoryShapeManager();
		BigQueryTableGenerator generator = new BigQueryTableGenerator()
			.setShapeManager(shapeManager)
			.setShapeNamer(new SimpleShapeNamer(nsManager, "http://example.com/shapes/v1/"))
			.setTableMapper(new LocalNameTableMapper());
		
		GoogleCloudManager gcpManager = new MemoryGoogleCloudManager()
			.setShapeManager(shapeManager)
			.setProjectMapper(new SimpleProjectMapper("myproject"))
			.setDatasetMapper(new SimpleDatasetMapper("mydataset"));
		
		URI shapeId = uri("http://example.com/shapes/v1/schema/GenderType");
		
		generator.generateEnumTables(graph, gcpManager);
		
		Shape shape = shapeManager.getShapeById(shapeId);
		assertTrue(shape != null);

		assertProperty(shape, Schema.name, XMLSchema.STRING, 1, 1);
		
		GoogleCloudProject project = gcpManager.getProjectById("myproject");
		assertTrue(project != null);
		
		BigQueryDataset dataset = project.findProjectDataset("mydataset");
		assertTrue(dataset!=null);
		
		BigQueryTable table = dataset.findDatasetTable("GenderType");
		assertTrue(table != null);
		assertEquals(shapeId, table.getTableShape());
		
	}

	@Test
	public void testGenerateEnumTablesOptionalIdentifier() {
		
		NamespaceManager nsManager = new MemoryNamespaceManager();
		nsManager.add("schema", "http://schema.org/");
		
		MemoryGraph graph = new MemoryGraph();
		graph.edge(Schema.GenderType, RDFS.SUBCLASSOF, Schema.Enumeration);
		graph.edge(Schema.Male, RDF.TYPE, Schema.GenderType);
		graph.edge(Schema.Female, RDF.TYPE, Schema.GenderType);
		graph.edge(Schema.Female, DC.IDENTIFIER, literal("F"));
		
		ShapeManager shapeManager = new MemoryShapeManager();
		BigQueryTableGenerator generator = new BigQueryTableGenerator()
			.setShapeManager(shapeManager)
			.setShapeNamer(new SimpleShapeNamer(nsManager, "http://example.com/shapes/v1/"))
			.setTableMapper(new LocalNameTableMapper());
		
		GoogleCloudManager gcpManager = new MemoryGoogleCloudManager()
			.setShapeManager(shapeManager)
			.setProjectMapper(new SimpleProjectMapper("myproject"))
			.setDatasetMapper(new SimpleDatasetMapper("mydataset"));
		
		URI shapeId = uri("http://example.com/shapes/v1/schema/GenderType");
		
		generator.generateEnumTables(graph, gcpManager);
		
		Shape shape = shapeManager.getShapeById(shapeId);
		assertTrue(shape != null);
		
		assertProperty(shape, Schema.name, XMLSchema.STRING, 1, 1);
		assertProperty(shape, DC.IDENTIFIER, XMLSchema.STRING, 0, 1);
		
		
		
	}
	
	@Ignore
	public void testGenerateEnumTablesRequiredIdentifier() {
		
		NamespaceManager nsManager = new MemoryNamespaceManager();
		nsManager.add("schema", "http://schema.org/");
		
		MemoryGraph graph = new MemoryGraph();
		graph.edge(Schema.GenderType, RDFS.SUBCLASSOF, Schema.Enumeration);
		graph.edge(Schema.Male, RDF.TYPE, Schema.GenderType);
		graph.edge(Schema.Female, RDF.TYPE, Schema.GenderType);
		graph.edge(Schema.Female, DC.IDENTIFIER, literal("F"));
		graph.edge(Schema.Male, DC.IDENTIFIER, literal("M"));
		
		ShapeManager shapeManager = new MemoryShapeManager();
		BigQueryTableGenerator generator = new BigQueryTableGenerator()
			.setShapeManager(shapeManager)
			.setShapeNamer(new SimpleShapeNamer(nsManager, "http://example.com/shapes/v1/"))
			.setTableMapper(new LocalNameTableMapper());
		
		GoogleCloudManager gcpManager = new MemoryGoogleCloudManager()
			.setShapeManager(shapeManager)
			.setProjectMapper(new SimpleProjectMapper("myproject"))
			.setDatasetMapper(new SimpleDatasetMapper("mydataset"));
		
		URI shapeId = uri("http://example.com/shapes/v1/schema/GenderType");
		
		generator.generateEnumTables(graph, gcpManager);
		
		Shape shape = shapeManager.getShapeById(shapeId);
		assertTrue(shape != null);
		
		assertProperty(shape, Schema.name, XMLSchema.STRING, 1, 1);
		assertProperty(shape, DC.IDENTIFIER, XMLSchema.STRING, 1, 1);
		
	}
	
	private void assertProperty(Shape shape, URI predicate, URI datatype, Integer minCount, Integer maxCount) {
		
		PropertyConstraint p = shape.getPropertyConstraint(predicate);
		assertTrue(p != null);
		assertEquals(datatype, p.getDatatype());
		assertEquals(minCount, p.getMinCount());
		assertEquals(maxCount, p.getMaxCount());
		
		
	}
	
	private Literal literal(String value) {
		return new LiteralImpl(value);
	}

	private URI uri(String value) {
		return new URIImpl(value);
	}

}
