package io.konig.transform.sql.query;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;

import io.konig.core.Graph;
import io.konig.core.NamespaceManager;
import io.konig.core.PathFactory;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.MemoryNamespaceManager;
import io.konig.core.impl.RdfUtil;
import io.konig.core.util.IOUtil;
import io.konig.core.vocab.Schema;
import io.konig.gcp.datasource.BigQueryTableReference;
import io.konig.gcp.datasource.GcpShapeConfig;
import io.konig.gcp.datasource.GoogleBigQueryTable;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeManager;
import io.konig.shacl.ShapeNotFoundException;
import io.konig.shacl.impl.MemoryShapeManager;
import io.konig.shacl.io.ShapeLoader;
import io.konig.transform.MappedProperty;
import io.konig.transform.ShapePath;
import io.konig.transform.ShapeTransformException;
import io.konig.transform.TransformAttribute;
import io.konig.transform.TransformFrame;
import io.konig.transform.TransformFrameBuilder;

public class SqlFrameFactoryTest {
	
	private SqlFrameFactory factory = new SqlFrameFactory();
	private NamespaceManager nsManager = new MemoryNamespaceManager();
	private ShapeManager shapeManager = new MemoryShapeManager();
	private Graph graph = new MemoryGraph();
	
	@Before
	public void setUp() {
		GcpShapeConfig.init();
	}
	
	
	@Ignore
	public void testNestedStructure() throws Exception {
		load("SqlFrameFactoryTest/testNested.ttl");
		URI targetPersonShapeId = uri("http://example.com/shapes/TargetPersonShape");
		TransformFrame transformFrame = transformFrame(targetPersonShapeId);
		
		SqlFrame sqlFrame = factory.create(transformFrame);
		
		SqlAttribute name = sqlFrame.getAttribute(Schema.name);
		assertTrue(name != null);
		
		SqlAttribute address = sqlFrame.getAttribute(Schema.address);
		assertTrue(address != null);
		
		SqlFrame addressFrame = address.getEmbedded();
		assertTrue(addressFrame != null);
		
		SqlAttribute addressLocality = addressFrame.getAttribute(Schema.addressLocality);
		assertTrue(addressLocality != null);
		assertTrue(addressLocality.getMappedProperty() != null);
		assertEquals("city", addressLocality.getMappedProperty().getProperty().getPredicate().getLocalName());
		
		SqlAttribute addressRegion = addressFrame.getAttribute(Schema.addressRegion);
		assertTrue(addressRegion != null);
		
		
		
		
		
	}

	private TransformFrame transformFrame(URI shapeId) throws ShapeTransformException {
		
		TransformFrameBuilder frameBuilder = new TransformFrameBuilder(shapeManager, nsManager);
		Shape shape = shapeManager.getShapeById(shapeId);
		if (shape == null) {
			throw new ShapeNotFoundException(shapeId.stringValue());
		}
		return frameBuilder.create(shape);
	}

	private void load(String resource) throws IOException, RDFParseException, RDFHandlerException {
		InputStream input = getClass().getClassLoader().getResourceAsStream(resource);
		RdfUtil.loadTurtle(graph, nsManager, input, "");
		ShapeLoader shapeLoader = new ShapeLoader(shapeManager);
		shapeLoader.load(graph);
		
		IOUtil.close(input, resource);
	}

	@Ignore
	public void testSimpleAttribute() throws Exception {
		
		URI targetShapeId = uri("http://example.com/shape/target");
		URI sourceShapeId = uri("http://example.com/shape/source");
		
		URI moniker = uri("http://example.com/schema/moniker");
		URI phoneNumber = uri("http://example.com/schema/phoneNumber");
		
		Shape targetShape = new Shape(targetShapeId);
		Shape sourceShape = new Shape(sourceShapeId);

		TransformFrame frame = new TransformFrame(targetShape);
		
		GoogleBigQueryTable sourceTable = new GoogleBigQueryTable();
		sourceTable.setTableReference(new BigQueryTableReference("acme", "directory", "Person"));
		sourceShape.addShapeDataSource(sourceTable);
		
		PropertyConstraint nameTarget = new PropertyConstraint(Schema.name);
		PropertyConstraint nameSource = new PropertyConstraint(moniker);
		
		PropertyConstraint telephoneTarget = new PropertyConstraint(Schema.telephone);
		PropertyConstraint telephoneSource = new PropertyConstraint(phoneNumber);
		
		TransformAttribute attr = new TransformAttribute(nameTarget);
		
		MappedProperty m = new MappedProperty(new ShapePath("", sourceShape), nameSource);
		attr.add(m);
		frame.addAttribute(attr);
		
		attr = new TransformAttribute(telephoneTarget);
		m = new MappedProperty(new ShapePath("", sourceShape), telephoneSource);
		attr.add(m);
		frame.addAttribute(attr);
		
		SqlFrame s = factory.create(frame);
		
		List<SqlAttribute> list = s.getAttributes();
		assertEquals(2, list.size());
		
		SqlAttribute b = list.get(0);
		TableName tableName = b.getSourceTable();
		assertEquals("a", tableName.getAlias());
		assertEquals("directory.Person", tableName.getFullName());
		
		b = list.get(1);
		TableName otherTable = b.getSourceTable();
		assertEquals(tableName, otherTable);
	}

	private URI uri(String value) {
		return new URIImpl(value);
	}

}
