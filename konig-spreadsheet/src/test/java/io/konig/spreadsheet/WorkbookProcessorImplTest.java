package io.konig.spreadsheet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Iterator;
import java.util.Set;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.DCTERMS;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.model.vocabulary.SKOS;
import org.openrdf.model.vocabulary.XMLSchema;

import io.konig.cadl.Attribute;
import io.konig.cadl.Cube;
import io.konig.cadl.CubeManager;
import io.konig.cadl.Dimension;
import io.konig.cadl.Level;
import io.konig.cadl.Measure;
import io.konig.cadl.Variable;
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
import io.konig.formula.QuantifiedExpression;
import io.konig.gcp.datasource.GcpShapeConfig;
import io.konig.gcp.datasource.GoogleBigQueryTable;
import io.konig.shacl.AndConstraint;
import io.konig.shacl.NodeKind;
import io.konig.shacl.OrConstraint;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.RelationshipDegree;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeManager;
import io.konig.shacl.XoneConstraint;
import io.konig.shacl.impl.MemoryShapeManager;

public class WorkbookProcessorImplTest {
	private static final Integer ONE = new Integer(1);
	private static final Integer ZERO = new Integer(0);

	private static final URI Harvard = new URIImpl("http://example.com/ns/core/Harvard");
	private static final URI Stable = new URIImpl("http://example.com/ns/status/Stable");
	private static final URI Customer = new URIImpl("http://example.com/ns/subject/Customer");
	private static final URI organizationShapeId = new URIImpl("http://example.com/shape/OrganizationShape");
	private static final URI Sensitive = new URIImpl("http://example.com/ns/security/Sensitive");
	private NamespaceManager nsManager = new MemoryNamespaceManager();
	private Graph graph = new MemoryGraph(nsManager);
	private ShapeManager shapeManager = new MemoryShapeManager();
	private WorkbookProcessorImpl processor = new WorkbookProcessorImpl(graph, shapeManager, null);
	
	@Before
	public void setUp() {
		GcpShapeConfig.init();
	}
	
	@Test
	public void testCube() throws Exception {

		
		File file = new File("src/test/resources/workbook-cube.xlsx");
		process(file);
		
		URI cubeId = uri("http://example.com/ns/cube/OpportunityRevenueCube");
		CubeManager cubeManager = processor.service(CubeManager.class);
		Cube cube = cubeManager.findById(cubeId);
		
		assertTrue(cube != null);
		
		Variable source = cube.getSource();
		URI sourceId = uri("http://example.com/ns/cube/OpportunityRevenueCube/source/?opportunity");
		URI Opportunity = uri("http://example.com/ns/crm/Opportunity");
		assertEquals(sourceId, source.getId());
		assertEquals(Opportunity, source.getValueType());
		
		assertEquals(2, cube.getDimension().size());
		Iterator<Dimension> dimensions = cube.getDimension().iterator();
		Dimension timeDim = dimensions.next();
		URI timeDimId = uri("http://example.com/ns/cube/OpportunityRevenueCube/dimension/timeDim");
		assertEquals(timeDimId, timeDim.getId());
		assertEquals(3, timeDim.getLevel().size());
		
		Iterator<Level> levels = timeDim.getLevel().iterator();
		Level dayLevel = levels.next();
		
		URI dayLevelId = uri("http://example.com/ns/cube/OpportunityRevenueCube/dimension/timeDim/level/day");
		assertEquals(dayLevelId, dayLevel.getId());
		
		QuantifiedExpression formula = dayLevel.getFormula();
		assertTrue(formula != null);
		assertEquals("DATE_TRUNC(DAY, ?opportunity.dateCreated)", formula.toSimpleString());
		
		Dimension accountDim = dimensions.next();
		URI accountDimId = uri("http://example.com/ns/cube/OpportunityRevenueCube/dimension/accountDim");
		assertEquals(accountDimId, accountDim.getId());
		
		assertEquals(3, accountDim.getLevel().size());
		levels = accountDim.getLevel().iterator();
		Level accountLevel = levels.next();
		assertEquals(2, accountLevel.getAttribute().size());
		
		Iterator<Attribute> attributes = accountLevel.getAttribute().iterator();
		Attribute idAttr = attributes.next();
		URI accountId = uri("http://example.com/ns/cube/OpportunityRevenueCube/dimension/accountDim/level/account/attribute/id");
		assertEquals(accountId, idAttr.getId());
		
		Measure measure = cube.getMeasure().iterator().next();
		URI measureId = uri("http://example.com/ns/cube/OpportunityRevenueCube/measure/expectedRevenue");
		assertEquals(measureId, measure.getId());
		
		formula = measure.getFormula();
		assertEquals("SUM(?opportunity.salePrice)", formula.toSimpleString());
		
		
		
	}
	
	@Ignore
	public void testLabels() throws Exception {

		
		File file = new File("src/test/resources/workbook-labels.xlsx");
		process(file);
		
		String arabicValue = "\u0627\u0644\u0627\u0633\u0645 \u0627\u0644\u0645\u0639\u0637\u0649";
		
		assertLabel(graph, Schema.givenName, "Given Name", "en");
		assertLabel(graph, Schema.givenName, "Pr\u00E9nom", "fr");
		assertLabel(graph, Schema.givenName, arabicValue, "ar");
		
				
	}

	private void assertLabel(Graph graph, URI subject, String label, String language) {
		Literal literal = new LiteralImpl(label, language);
		assertTrue(graph.contains(subject, RDFS.LABEL, literal));
	}
	
	@Ignore
	public void testTriples() throws Exception {

		
		File file = new File("src/test/resources/workbook-triples.xlsx");
		process(file);
		
		URI tolkienId = uri("http://example.com/ns/core/Tolkien");
		
		Vertex book = graph.getVertex(uri("http://example.com/ns/core/LordOfTheRings"));
		assertTrue(book != null);
		assertIri(Schema.Book, book.getURI(RDF.TYPE));
		assertValue("Lord of the Rings", book.getValue(Schema.name));
		
		Value numberOfPages = book.getValue(Schema.numberOfPages);
		assertTrue(numberOfPages instanceof Literal);
		Literal numberOfPagesLiteral = (Literal) numberOfPages;
		assertEquals(XMLSchema.INT, numberOfPagesLiteral.getDatatype());
		assertEquals(1216, numberOfPagesLiteral.intValue());
		
		Vertex tolkien = book.getVertex(Schema.author);
		assertTrue(tolkien != null);
		assertEquals(tolkienId, tolkien.getId());
		assertValue("J.R.R. Tolkien", tolkien.getValue(Schema.name));	
				
	}
	
	@Ignore
	public void testPropertyConstraint() throws Exception {

		
		File file = new File("src/test/resources/workbook-property-constraints.xlsx");
		process(file);
		
		URI personShapeId = uri("http://example.com/shape/PersonShape");
		Shape personShape = shapeManager.getShapeById(personShapeId);
		
		assertTrue(personShape != null);
		assertEquals(Schema.Person, personShape.getTargetClass());
		assertEquals(NodeKind.IRI, personShape.getNodeKind());
		
		PropertyConstraint givenName = personShape.getPropertyConstraint(Schema.givenName);
		
		
		assertTrue(givenName != null);
		assertEquals("The Person's given name.  In the U.S., the first name.", givenName.getComment());
		assertEquals(XMLSchema.STRING, givenName.getDatatype());
		assertEquals(ONE, givenName.getMinCount());
		assertEquals(ONE, givenName.getMaxCount());
		assertTrue(givenName.getQualifiedSecurityClassification().contains(Sensitive));
		assertEquals(Stable, givenName.getTermStatus());
		assertEquals(Boolean.TRUE, givenName.getUniqueLang());
		assertEquals(ONE, givenName.getMinLength());
		assertEquals(new Integer(64), givenName.getMaxLength());
		
		PropertyConstraint name = personShape.getDerivedPropertyByPredicate(Schema.name);
		assertTrue(name != null);
		QuantifiedExpression formula = name.getFormula();
		assertTrue(formula != null);
		String formulaText = formula.toSimpleString();
		assertEquals("CONCAT(givenName, \" \", familyName)", formulaText);
		
		PropertyConstraint alumniOf = personShape.getPropertyConstraint(Schema.alumniOf);
		assertTrue(alumniOf != null);
		assertEquals(NodeKind.IRI, alumniOf.getNodeKind());
		assertEquals(null, alumniOf.getMaxCount());
		assertEquals(Schema.Organization, alumniOf.getValueClass());
		assertEquals(2, alumniOf.getIn().size());
		assertTrue(alumniOf.getIn().contains(Harvard));
		assertEquals(RelationshipDegree.ManyToMany, alumniOf.getRelationshipDegree());
		assertEquals(organizationShapeId, alumniOf.getPreferredTabularShape());
		
		PropertyConstraint age = personShape.getPropertyConstraint(uri("http://example.com/ns/core/age"));
		assertTrue(age != null);
		assertEquals(new Double(0), age.getMinInclusive());
		assertEquals(new Double(120), age.getMaxInclusive());		

		PropertyConstraint weight = personShape.getPropertyConstraint(uri("http://example.com/ns/core/weight"));
		assertTrue(weight != null);
		assertEquals(new Double(0.5), weight.getMinExclusive());
		assertEquals(new Double(500.01), weight.getMaxExclusive());
		
		PropertyConstraint gender = personShape.getPropertyConstraint(Schema.gender);
		Shape enumShape = gender.getShape();
		assertTrue(enumShape != null);
		assertEquals(uri("http://example.com/shape/EnumerationShape"), enumShape.getId());
		assertEquals(Schema.GenderType, gender.getValueClass());
		
		
				
	}
	
	@Ignore
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
		assertIri(Sensitive, givenName.getURI(Konig.securityClassification));
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
