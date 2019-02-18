package io.konig.spreadsheet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Set;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.DCTERMS;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.model.vocabulary.SKOS;
import org.openrdf.model.vocabulary.XMLSchema;

import io.konig.core.Graph;
import io.konig.core.NamespaceManager;
import io.konig.core.Vertex;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.MemoryNamespaceManager;
import io.konig.core.util.IriTemplate;
import io.konig.core.vocab.Konig;
import io.konig.core.vocab.Schema;
import io.konig.core.vocab.VANN;
import io.konig.datasource.DataSource;
import io.konig.gcp.datasource.GcpShapeConfig;
import io.konig.gcp.datasource.GoogleBigQueryTable;
import io.konig.shacl.AndConstraint;
import io.konig.shacl.OrConstraint;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeManager;
import io.konig.shacl.XoneConstraint;
import io.konig.shacl.impl.MemoryShapeManager;

public class WorkbookProcessorImplTest {
	private static final URI Stable = new URIImpl("http://example.com/ns/status/Stable");
	private static final URI Customer = new URIImpl("http://example.com/ns/subject/Customer");
	private NamespaceManager nsManager = new MemoryNamespaceManager();
	private Graph graph = new MemoryGraph(nsManager);
	private ShapeManager shapeManager = new MemoryShapeManager();
	private WorkbookProcessorImpl processor = new WorkbookProcessorImpl(graph, shapeManager, null);
	
	@Before
	public void setUp() {
		GcpShapeConfig.init();
	}
	
	@Test
	public void testShape() throws Exception {

		
		File file = new File("src/test/resources/workbook-shape.xlsx");
		process(file);
		
		URI personEdwShapeId = uri("http://example.com/shape/PersonEdwShape");
		Shape personEdwShape = shapeManager.getShapeById(personEdwShapeId);
		assertTrue(personEdwShape != null);
		
		assertEquals("Person structure used in the Enterprise Data Warehouse", personEdwShape.getComment());
		assertEquals(Schema.Person, personEdwShape.getTargetClass());
		assertEquals(1, personEdwShape.getShapeDataSource().size());
		DataSource datasource = personEdwShape.getShapeDataSource().get(0);
		assertTrue(datasource instanceof GoogleBigQueryTable);
		
		IriTemplate template = personEdwShape.getIriTemplate();
		assertTrue(template != null);
		assertEquals("http://example.com/person/{uid}", template.getText());
		
		Set<Shape> derivedFrom = personEdwShape.getExplicitDerivedFrom();
		assertEquals(1, derivedFrom.size());
		
		Shape personStageShape = derivedFrom.iterator().next();
		assertEquals(uri("http://example.com/shape/PersonStageShape"), personStageShape.getId());
		OrConstraint orConstraint = personEdwShape.getOr();
		assertTrue(orConstraint != null);
		assertEquals(2, orConstraint.getShapes().size());
		
		Shape personOneShape = orConstraint.getShapes().get(0);
		assertEquals(uri("http://example.com/shape/PersonOneShape"), personOneShape.getId());
		
		Shape personTwoShape = orConstraint.getShapes().get(1);
		assertEquals(uri("http://example.com/shape/PersonTwoShape"), personTwoShape.getId());
		
		XoneConstraint xone = personEdwShape.getXone();
		assertTrue(xone != null);
		assertEquals(2, xone.getShapes().size());
		
		Shape personThreeShape = xone.getShapes().get(0);
		assertEquals(uri("http://example.com/shape/PersonThreeShape"), personThreeShape.getId());
		
		Shape personFourShape = xone.getShapes().get(1);
		assertEquals(uri("http://example.com/shape/PersonFourShape"), personFourShape.getId());

		AndConstraint and = personEdwShape.getAnd();
		assertTrue(xone != null);
		assertEquals(2, xone.getShapes().size());
		
		Shape personFiveShape = and.getShapes().get(0);
		assertEquals(uri("http://example.com/shape/PersonFiveShape"), personFiveShape.getId());
		
		Shape personSixShape = and.getShapes().get(1);
		assertEquals(uri("http://example.com/shape/PersonSixShape"), personSixShape.getId());
		assertEquals(Stable, personEdwShape.getTermStatus());
		
		Set<URI> broader = personEdwShape.getBroader();
		assertTrue(broader.contains(Customer));
		
				
	}
	@Ignore
	public void testIndividual() throws Exception {

		File file = new File("src/test/resources/workbook-individual.xlsx");
		process(file);
		URI hasYChromosome = uri("http://example.com/ns/core/hasYChromosome");
		URI personifiedBy = uri("http://example.com/ns/core/personifiedBy");
		URI mars = uri("http://example.com/ns/core/Mars");
		Vertex Male = graph.getVertex(Schema.Male);
		assertTrue(Male != null);
		assertValue("Male", Male.getValue(Schema.name));
		assertValue("The male gender", Male.getValue(RDFS.COMMENT));
		assertIri(Schema.GenderType, Male.getURI(RDF.TYPE));
		assertValue("mars", Male.getValue(DCTERMS.IDENTIFIER));
		assertIri(Schema.GenderType, Male.getURI(RDF.TYPE));
		assertIri(Stable, Male.getURI(Konig.termStatus));
		assertIri(mars, Male.getURI(personifiedBy));
		
		Value maleChromosome = Male.getValue(hasYChromosome);
		assertTrue(maleChromosome != null);
		assertTrue(maleChromosome instanceof Literal);
		Literal literal = (Literal) maleChromosome;
		assertEquals(XMLSchema.BOOLEAN, literal.getDatatype());
		assertEquals(true, literal.booleanValue());
		
		// TODO: test deferred action for custom properties
	}
	
	@Ignore
	public void testProperty() throws Exception {

		File file = new File("src/test/resources/workbook-property.xlsx");
		process(file);
		
		Vertex givenName = graph.getVertex(Schema.givenName);
		assertTrue(givenName != null);
		
		Set<Value> type = givenName.getValueSet(RDF.TYPE);
		assertTrue(type.contains(RDF.PROPERTY));
		assertTrue(type.contains(OWL.DATATYPEPROPERTY));
		assertValue("Given Name", givenName.getValue(RDFS.LABEL));
		assertValue("Given name. In the U.S., the first name of a Person.", givenName.getValue(RDFS.COMMENT));
		assertIri(Schema.Person, givenName.getURI(RDFS.DOMAIN));
		assertIri(XMLSchema.STRING, givenName.getURI(RDFS.RANGE));
		assertIri(Schema.name, givenName.getURI(RDFS.SUBPROPERTYOF));

		assertIri(uri("http://example.com/ns/status/stable"), givenName.getURI(Konig.termStatus));
		assertIri(uri("http://example.com/ns/security/Sensitive"), givenName.getURI(Konig.securityClassification));
		Vertex Person = graph.getVertex(Schema.Person);
		
		int relationshipDegree = Person.asTraversal()
			.out(RDFS.SUBCLASSOF)
			.hasValue(OWL.ONPROPERTY, Schema.givenName)
			.hasValue(Konig.relationshipDegree, Konig.ManyToOne)
			.toValueList().size();
		
		assertEquals(1, relationshipDegree);
		
		Vertex hasPart = graph.getVertex(Schema.hasPart);
		assertTrue(hasPart != null);
		
		
		Vertex CreativeWork = graph.getVertex(Schema.CreativeWork);
		assertTrue(CreativeWork.getValueSet(RDF.TYPE).contains(OWL.CLASS));
		
		assertIri(Schema.isPartOf, hasPart.getURI(OWL.INVERSEOF));
		
		Set<Value> hasPartRange = hasPart.getValueSet(Schema.rangeIncludes);
		assertEquals(2, hasPartRange.size());
		assertTrue(hasPartRange.contains(Schema.CreativeWork));
		assertTrue(hasPartRange.contains(Schema.Trip));
		
		
	}

	
	@Ignore
	public void testClass() throws Exception {

		File file = new File("src/test/resources/workbook-class.xlsx");
		process(file);
		
		Vertex v = graph.getVertex(Schema.EducationalOrganization);
		assertTrue(v != null);
		assertValue("An organizational organization", v.getValue(RDFS.COMMENT));
		assertIri(Schema.Organization, v.getURI(RDFS.SUBCLASSOF));
		assertIri(uri("http://example.com/ns/subject/Customer"), v.getURI(SKOS.BROADER));
		assertIri(Stable, v.getURI(Konig.termStatus));
		
		Value iriTemplateValue = v.getValue(Konig.iriTemplate);
		assertTrue(iriTemplateValue != null);
		
		String iriTemplateText = iriTemplateValue.stringValue();
		IriTemplate template = new IriTemplate(iriTemplateText);
		assertEquals("http://example.com/org/{uid}", template.getText());
		
	}

	private void assertValue(String expected, Value actual) {
		assertTrue(actual != null);
		assertEquals(expected, actual.stringValue());
		
	}private void assertIri(URI expected, URI actual) {
		assertTrue(actual != null);
		assertEquals(expected, actual);
		
	}

	@Ignore
	public void testOntology() throws Exception {
		
		File file = new File("src/test/resources/ontologies-test.xlsx");
		process(file);
		
		URI namespaceId = uri("http://example.com/ns/core/");
		
		Vertex v = graph.getVertex(namespaceId);
		assertTrue(v != null);

		Value prefix = v.getValue(VANN.preferredNamespacePrefix);
		assertTrue(prefix!=null);
		assertEquals("ex", prefix.stringValue());
		
		Value comment = v.getValue(RDFS.COMMENT);
		assertTrue(comment != null);
		assertEquals("A dummy ontology for testing purposes", comment.stringValue());
		
		Value label = v.getValue(RDFS.LABEL);
		assertTrue(label != null);
		assertEquals("Example Ontology", label.stringValue());
		
		URI imported = v.getURI(OWL.IMPORTS);
		assertTrue(imported != null);
		assertEquals("http://schema.org/", imported.stringValue());
		
	}
	
	@Ignore
	public void testSubproperty() throws Exception {

		File file = new File("src/test/resources/subproperty.xlsx");
		process(file);

		assertTrue(graph.contains(Schema.taxID, RDFS.SUBPROPERTYOF, Schema.identifier));
	}
	
	private void process(File file) throws SpreadsheetException {

		processor.process(file);
		processor.executeDeferredActions();
		
	}

	private URI uri(String value) {
		return new URIImpl(value);
	}

}
