package io.konig.schemagen;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.util.List;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

import io.konig.core.Graph;
import io.konig.core.NamespaceManager;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.MemoryNamespaceManager;
import io.konig.schemagen.sql.RdbmsShapeGenerator;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeManager;
import io.konig.shacl.impl.MemoryShapeManager;
import io.konig.shacl.io.ShapeLoader;
import io.konig.spreadsheet.WorkbookLoader;

public class RdbmsShapeGeneratorTest {
	
	@Test
	public void testValidateLocalNames() throws Exception {

		InputStream input = getClass().getClassLoader().getResourceAsStream("rdbms/rdbmsshapegenerator.xlsx");
		Workbook book = WorkbookFactory.create(input);
		Graph graph = new MemoryGraph();
		NamespaceManager nsManager = new MemoryNamespaceManager();
		graph.setNamespaceManager(nsManager);
		
		WorkbookLoader loader = new WorkbookLoader(nsManager);
		loader.load(book, graph);
		input.close();
		
		URI shapeId = uri("http://example.com/shapes/TargetPersonShape");
		
		ShapeManager s = new MemoryShapeManager();
		
		ShapeLoader shapeLoader = new ShapeLoader(s);
		shapeLoader.load(graph);
		
		
		Shape shape = s.getShapeById(shapeId);
		RdbmsShapeGenerator.validateLocalNames(shape);

		assertTrue(shape!=null);
	
		
	}
	
	private URI uri(String text) {
		return new URIImpl(text);
	}


}
