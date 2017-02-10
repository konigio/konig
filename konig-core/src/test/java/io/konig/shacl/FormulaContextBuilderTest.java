package io.konig.shacl;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Before;
import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;

import io.konig.core.Context;
import io.konig.core.Graph;
import io.konig.core.NameMap;
import io.konig.core.Term;
import io.konig.core.Term.Kind;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.MemoryNamespaceManager;
import io.konig.core.impl.RdfUtil;
import io.konig.core.vocab.Schema;
import io.konig.formula.Expression;
import io.konig.shacl.impl.MemoryShapeManager;
import io.konig.shacl.io.ShapeLoader;

public class FormulaContextBuilderTest {
	
	private Graph graph;
	private ShapeManager shapeManager;
	private NameMap nameMap;
	private FormulaContextBuilder contextBuilder;
	
	@Before
	public void setUp() {
		graph = new MemoryGraph();
		graph.setNamespaceManager(new MemoryNamespaceManager());
		shapeManager = new MemoryShapeManager();
	}

	@Test
	public void testLocalName() throws Exception {
		
		load("FormulaContextBuilderTest/testLocalName.ttl");
		
		URI shapeId = uri("http://example.com/shape/RootedPersonShape");
		Shape shape = shapeManager.getShapeById(shapeId);
		contextBuilder.visit(shape);
		
		Expression formula = shape.getPropertyConstraint(Schema.birthPlace).getFormula();
	
		Context context = formula.getContext();
		Term term = context.getTerm("schema");
		assertTrue(term != null);
		assertEquals("http://schema.org/", term.getId());
		assertEquals(Kind.NAMESPACE, term.getKind());
		Term parent = context.getTerm("parent");
		assertTrue(parent != null);
		assertEquals("schema:parent", parent.getId());
		Term birthPlace = context.getTerm("birthPlace");
		assertEquals("schema:birthPlace", birthPlace.getId());
		
		
		
	}

	@Test
	public void testCurie() throws Exception {
		
		load("FormulaContextBuilderTest/testCurie.ttl");
		
		URI shapeId = uri("http://example.com/shape/RootedPersonShape");
		Shape shape = shapeManager.getShapeById(shapeId);
		contextBuilder.visit(shape);
		
		Expression formula = shape.getPropertyConstraint(Schema.birthPlace).getFormula();
		
		Context context = formula.getContext();
		Term term = context.getTerm("schema");
		assertTrue(term != null);
		assertEquals("http://schema.org/", term.getId());
		assertEquals(Kind.NAMESPACE, term.getKind());
		
		
	}
	
	private URI uri(String value) {
		return new URIImpl(value);
	}

	private void load(String resource) throws RDFParseException, RDFHandlerException, IOException {
		
		InputStream input = getClass().getClassLoader().getResourceAsStream(resource);
		RdfUtil.loadTurtle(graph, input, "");
		ShapeLoader shapeLoader = new ShapeLoader(shapeManager);
		shapeLoader.load(graph);
		nameMap = new NameMap();
		nameMap.addAll(graph);
		contextBuilder = new FormulaContextBuilder(graph.getNamespaceManager(), nameMap);
	}

}
