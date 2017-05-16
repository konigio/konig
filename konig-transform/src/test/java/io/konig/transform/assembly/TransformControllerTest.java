package io.konig.transform.assembly;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;

import io.konig.core.Graph;
import io.konig.core.NamespaceManager;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.MemoryNamespaceManager;
import io.konig.core.impl.RdfUtil;
import io.konig.core.util.TurtleElements;
import io.konig.core.vocab.Schema;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeManager;
import io.konig.shacl.impl.MemoryShapeManager;
import io.konig.transform.rule.ExactMatchPropertyRule;
import io.konig.transform.rule.PropertyRule;
import io.konig.transform.rule.RenamePropertyRule;
import io.konig.transform.rule.ShapeRule;

public class TransformControllerTest {

	private NamespaceManager nsManager = new MemoryNamespaceManager();
	private Graph graph = new MemoryGraph(nsManager);
	private ShapeManager shapeManager = new MemoryShapeManager();
	private Blackboard board = new Blackboard();
	private TransformController controller = new TransformController();
	

	@Test
	public void testFlattenedField() throws Exception {

		load("src/test/resources/TransformControllerTest/flattened-field");
		URI shapeId = iri("http://example.com/shapes/BqPersonShape");
		assemble(shapeId);

		ShapeRule shapeRule = board.getShapeRule();
		assertTrue(shapeRule != null);
		
		List<PropertyRule> propertyList = shapeRule.getPropertyRules();
		assertEquals(1, propertyList.size());
		
		PropertyRule rule = propertyList.get(0);

		assertEquals(Schema.address, rule.getFocusPredicate());
		
		ShapeRule nestedRule = rule.getNestedRule();
		assertTrue(nestedRule != null);
		
		propertyList = nestedRule.getPropertyRules();
		assertEquals(1, propertyList.size());
		rule = propertyList.get(0);
		assertEquals(Schema.postalCode, rule.getFocusPredicate());
		
	
	}
	
	@Ignore
	public void testExactMatchProperty() throws Exception {

		load("src/test/resources/TransformControllerTest/field-exact-match");
		URI shapeId = iri("http://example.com/shapes/BqPersonShape");
		assemble(shapeId);

		ShapeRule shapeRule = board.getShapeRule();
		assertTrue(shapeRule != null);
		
		List<PropertyRule> propertyList = shapeRule.getPropertyRules();
		assertEquals(1, propertyList.size());
		
		PropertyRule rule = propertyList.get(0);
		assertTrue(rule instanceof ExactMatchPropertyRule);

		assertEquals(Schema.givenName, rule.getFocusPredicate());
	
	}

	@Ignore
	public void testRenameProperty() throws Exception {
		load("src/test/resources/TransformControllerTest/renameProperty");
		URI shapeId = iri("http://example.com/shapes/BqPersonShape");
		assemble(shapeId);
		
		ShapeRule shapeRule = board.getShapeRule();
		assertTrue(shapeRule != null);
		
		List<PropertyRule> propertyList = shapeRule.getPropertyRules();
		assertEquals(1, propertyList.size());
		
		PropertyRule rule = shapeRule.propertyRule(Schema.givenName);
		assertTrue(rule != null);
		assertTrue(rule instanceof RenamePropertyRule);
		RenamePropertyRule renameRule = (RenamePropertyRule) rule;
		assertEquals(0, renameRule.getPathIndex());
		assertEquals("first_name", renameRule.getSourceProperty().getPredicate().getLocalName());
		
	}

	private void assemble(URI shapeId) throws Exception {
		
		Shape shape = shapeManager.getShapeById(shapeId);
		if (shape == null) {
			throw new Exception("Shape not found: " + TurtleElements.iri(shapeId));
		}
		board.setGraph(graph);
		board.setShapeManager(shapeManager);
		board.setFocusShape(shape);
		
		controller.assemble(board);
		
		
	}

	private URI iri(String value) {
		return new URIImpl(value);
	}
	
	private void load(String path) throws RDFParseException, RDFHandlerException, IOException {
		File dir = new File(path);
		RdfUtil.loadTurtle(dir, graph, shapeManager);
	}

}
