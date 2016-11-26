package io.konig.spreadsheet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.text.MessageFormat;
import java.util.List;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.Ignore;
import org.junit.Test;
import org.openrdf.model.Literal;
import org.openrdf.model.Namespace;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.model.vocabulary.XMLSchema;

import io.konig.core.Graph;
import io.konig.core.NamespaceManager;
import io.konig.core.Vertex;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.MemoryNamespaceManager;
import io.konig.core.impl.RdfUtil;
import io.konig.core.pojo.SimplePojoFactory;
import io.konig.core.vocab.Konig;
import io.konig.core.vocab.OwlVocab;
import io.konig.core.vocab.SH;
import io.konig.core.vocab.Schema;
import io.konig.core.vocab.VANN;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;

public class WorkbookLoaderTest {
	
	@Ignore
	public void testEquivalentPath() throws Exception {

		InputStream input = getClass().getClassLoader().getResourceAsStream("analytics-model.xlsx");
		
		Workbook book = WorkbookFactory.create(input);
		Graph graph = new MemoryGraph();
		NamespaceManager nsManager = new MemoryNamespaceManager();
		
		WorkbookLoader loader = new WorkbookLoader(nsManager);
		
		loader.load(book, graph);

		URI shapeId = uri("http://example.com/shapes/v1/fact/SalesByCityShape");
		Vertex shapeVertex = graph.getVertex(shapeId);
		assertTrue(shapeVertex != null);
		
		SimplePojoFactory pojoFactory = new SimplePojoFactory();
		Shape shape = pojoFactory.create(shapeVertex, Shape.class);
		
		URI state = uri("http://example.com/ns/alias/state");
		PropertyConstraint p = shape.getPropertyConstraint(state);
		assertEquals("/city/containedInPlace", p.getEquivalentPath());

	}
	
	@Test
	public void testRollUpBy() throws Exception {

		InputStream input = getClass().getClassLoader().getResourceAsStream("analytics-model.xlsx");
		
		Workbook book = WorkbookFactory.create(input);
		Graph graph = new MemoryGraph();
		NamespaceManager nsManager = new MemoryNamespaceManager();
		
		WorkbookLoader loader = new WorkbookLoader(nsManager);
		
		loader.load(book, graph);
		
		URI shapeId = uri("http://example.com/shapes/v1/fact/SalesByCountryShape");
		Shape shape = loader.getShapeManager().getShapeById(shapeId);
		assertTrue(shape != null);
		
		List<PropertyConstraint> list = shape.getProperty();
		
		assertEquals(4, list.size());
		
		PropertyConstraint totalCount = shape.getPropertyConstraint(Konig.totalCount);
		assertTrue(totalCount != null);
		assertEquals(Konig.measure, totalCount.getStereotype());
		
		PropertyConstraint country = shape.getPropertyConstraint(uri("http://example.com/ns/alias/country"));
		assertTrue(country != null);
		assertEquals(Konig.dimension, country.getStereotype());
		assertTrue(country.getEquivalentPath() == null);
		assertEquals("/alias:country", country.getFromAggregationSource());
		
		PropertyConstraint continent = shape.getPropertyConstraint(uri("http://example.com/ns/alias/continent"));
		assertTrue(continent != null);
		assertEquals("/country/containedInPlace", continent.getEquivalentPath());
		assertEquals("/alias:continent", continent.getFromAggregationSource());
		
		PropertyConstraint timeInterval = shape.getPropertyConstraint(Konig.timeInterval);
		assertTrue(timeInterval != null);
		assertEquals(uri("http://example.com/shapes/v1/konig/WeekMonthYearShape"), timeInterval.getValueShapeId());
		
	}
	
	@Ignore
	public void testAggregationOf() throws Exception {
		InputStream input = getClass().getClassLoader().getResourceAsStream("analytics-model.xlsx");
		
		Workbook book = WorkbookFactory.create(input);
		Graph graph = new MemoryGraph();
		NamespaceManager nsManager = new MemoryNamespaceManager();
		
		WorkbookLoader loader = new WorkbookLoader(nsManager);
		
		loader.load(book, graph);

		
		URI shapeId = uri("http://example.com/shapes/v1/fact/SalesByCityShape");
		Vertex shapeVertex = graph.getVertex(shapeId);
		assertTrue(shapeVertex != null);
		
		SimplePojoFactory pojoFactory = new SimplePojoFactory();
		Shape shape = pojoFactory.create(shapeVertex, Shape.class);
		
		URI actual = shape.getAggregationOf();
		assertEquals(Schema.BuyAction, actual);
		
	}
	
	@Ignore
	public void testIn() throws Exception {

		InputStream input = getClass().getClassLoader().getResourceAsStream("analytics-model.xlsx");
		
		Workbook book = WorkbookFactory.create(input);
		Graph graph = new MemoryGraph();
		NamespaceManager nsManager = new MemoryNamespaceManager();
		
		WorkbookLoader loader = new WorkbookLoader(nsManager);
		
		loader.load(book, graph);

		
		URI shapeId = uri("http://example.com/shapes/v1/konig/WeekMonthYearShape");
		Vertex shapeVertex = graph.getVertex(shapeId);
		assertTrue(shapeVertex != null);
		
		SimplePojoFactory pojoFactory = new SimplePojoFactory();
		Shape shape = pojoFactory.create(shapeVertex, Shape.class);
		
		PropertyConstraint p = shape.getPropertyConstraint(Konig.durationUnit);
		assertTrue(p!=null);
		
		List<Value> list = p.getIn();
		assertTrue(list != null);
		assertEquals(Konig.Week, list.get(0));
		assertEquals(Konig.Month, list.get(1));
		assertEquals(Konig.Year, list.get(2));
	}
	
	@Ignore
	public void testStereotype() throws Exception {

		
		InputStream input = getClass().getClassLoader().getResourceAsStream("analytics-model.xlsx");
		
		Workbook book = WorkbookFactory.create(input);
		Graph graph = new MemoryGraph();
		NamespaceManager nsManager = new MemoryNamespaceManager();
		
		WorkbookLoader loader = new WorkbookLoader(nsManager);
		
		loader.load(book, graph);
		
		URI shapeId = uri("http://example.com/shapes/v1/fact/SalesByCityShape");
		Vertex shapeVertex = graph.getVertex(shapeId);
		assertTrue(shapeVertex != null);
		
		SimplePojoFactory pojoFactory = new SimplePojoFactory();
		Shape shape = pojoFactory.create(shapeVertex, Shape.class);
		
		PropertyConstraint totalCount = shape.getPropertyConstraint(Konig.totalCount);
		assertTrue(totalCount!=null);
		assertEquals(Konig.measure, totalCount.getStereotype());
		
		URI cityId = uri("http://example.com/ns/alias/city");
		PropertyConstraint city = shape.getPropertyConstraint(cityId);
		assertTrue(city != null);
		assertEquals(Konig.dimension, city.getStereotype());
		
//		RdfUtil.prettyPrintTurtle(nsManager, graph, new OutputStreamWriter(System.out));
		
	}

	@Ignore
	public void test() throws Exception {
		
		InputStream input = getClass().getClassLoader().getResourceAsStream("test.xlsx");
		
		Workbook book = WorkbookFactory.create(input);
		Graph graph = new MemoryGraph();
		NamespaceManager nsManager = new MemoryNamespaceManager();
		
		WorkbookLoader loader = new WorkbookLoader(nsManager);
		
		loader.load(book, graph);

		
		checkOntologySheet(graph);
		checkNamespaces(nsManager);
		checkClasses(graph);
		checkProperties(graph);
		checkIndividuals(graph);
		checkShapes(graph);
		checkPropertyConstraints(graph);
		
		
	}


	private void checkPropertyConstraints(Graph graph) {
		
		Vertex shape = graph.getVertex(uri("http://example.com/shapes/v1/schema/Person"));
		assertTrue(shape!=null);
		
		Vertex id = propertyConstraint(shape, Konig.id);
		assertTrue(id == null);
		
		Vertex givenName = propertyConstraint(shape, Schema.givenName);
		assertValue(givenName, SH.datatype, XMLSchema.STRING);
		assertInt(givenName, SH.minCount, 0);
		assertInt(givenName, SH.maxCount, 1);
		
		Vertex familyName = propertyConstraint(shape, Schema.familyName);
		assertInt(familyName, SH.minCount, 1);
		
		Vertex address = propertyConstraint(shape, Schema.address);
		assertValue(address, SH.valueShape, uri("http://example.com/shapes/v1/schema/Address"));
		
		Vertex worksFor = propertyConstraint(shape, uri("http://schema.org/worksFor"));
		assertValue(worksFor, SH.valueClass, Schema.Organization);
		assertValue(worksFor, SH.nodeKind, SH.IRI);
	}
	
	
	private Vertex propertyConstraint(Vertex shape, URI predicate) {
		return shape.asTraversal().out(SH.property).hasValue(SH.predicate, predicate).firstVertex();
	}
	
	private void assertInt(Vertex v, URI predicate, int value) {
		assertEquals(literal(v.getValue(predicate)).intValue(), value);
	}
	
	private Literal literal(Value value) {
		return (Literal) value;
	}


	private void checkShapes(Graph graph) {
		Vertex v = graph.getVertex(uri("http://example.com/shapes/v1/schema/Person"));
		assertTrue(v!=null);
		
		assertValue(v, RDF.TYPE, SH.Shape);
		assertValue(v, RDFS.COMMENT, "A light-weight data shape for a person.");
		assertValue(v, SH.targetClass, Schema.Person);
		assertValue(v, SH.nodeKind, SH.IRI);
		assertValue(v, Konig.mediaTypeBaseName, "application/vnd.example.v1.schema.person");
		
		
	}


	private void checkIndividuals(Graph graph) {

		URI OrderStatus = uri("http://schema.org/OrderStatus");
		Vertex v = graph.getVertex(uri("http://schema.org/OrderPaymentDue"));
		assertTrue(v!=null);
		
		assertValue(v, RDF.TYPE, Schema.Enumeration);
		assertValue(v, RDF.TYPE, OrderStatus);
		assertValue(v, RDFS.COMMENT, "Payment is due");
		assertValue(v, RDFS.LABEL, "Payment Due");
		
		
	}


	private void checkProperties(Graph graph) {
		

		Vertex v = graph.getVertex(Schema.givenName);
		assertTrue(v!=null);
		
		assertValue(v, RDFS.LABEL, "Given Name");
		assertValue(v, RDFS.COMMENT, "The person's given name. In the U.S., the first name of a Person. "
				+ "This can be used along with familyName instead of the name property.");
		
		assertValue(v, RDF.TYPE, RDF.PROPERTY);
		assertValue(v, RDF.TYPE, OWL.DATATYPEPROPERTY);
		assertValue(v, RDFS.DOMAIN, Schema.Person);
		assertValue(v, RDFS.RANGE, XMLSchema.STRING);
		
		v = graph.getVertex(Schema.address);
		assertValue(v, RDF.TYPE, OWL.OBJECTPROPERTY);
		
		
	}


	private void checkClasses(Graph graph) {
		
		Vertex v = graph.getVertex(Schema.Person);
		assertTrue(v!=null);
		
		assertValue(v, RDF.TYPE, OWL.CLASS);
		assertValue(v, RDFS.LABEL, "Person");
		assertValue(v, RDFS.COMMENT, "Any person (alive, dead, undead or fictional).");
		assertValue(v, RDFS.SUBCLASSOF, Schema.Thing);
		
		
	}


	private void checkNamespaces(NamespaceManager nsManager) {
		
		Namespace ns = nsManager.findByPrefix("owl");
		assertEquals("owl", ns.getPrefix());
		assertEquals(OWL.NAMESPACE, ns.getName());
		
		
	}


	private void checkOntologySheet(Graph graph) {
		
		Vertex v = graph.getVertex(uri(OWL.NAMESPACE));
		assertTrue(v != null);
		
		assertValue(v, RDF.TYPE, OWL.ONTOLOGY);
		assertValue(v, VANN.preferredNamespacePrefix, "owl");
		assertValue(v, RDFS.LABEL, "OWL");
		assertValue(v, RDFS.COMMENT, "Web Ontology Language");
		
	}
	


	private void assertValue(Vertex subject, URI predicate, String object) {
		Literal value = new LiteralImpl(object);
		assertValue(subject, predicate, value);
	}


	private void assertValue(Vertex subject, URI predicate, Value object) {
		
		List<Value> list = subject.asTraversal().out(predicate).toValueList();
		
		for (Value value : list) {
			if (value.equals(object)) {
				return;
			}
		}
		
		String message = MessageFormat.format("Triple not found: <{0}> <{1}> \"{2}\"", subject.getId().stringValue(), predicate.getLocalName(), object.stringValue());
		
		fail(message);
		
	}


	private URI uri(String text) {
		return new URIImpl(text);
	}

}
