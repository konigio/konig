package io.konig.schemagen.sql;

import java.io.IOException;

import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;

import io.konig.core.impl.SimpleLocalNameService;
import io.konig.core.path.NamespaceMapAdapter;
import io.konig.formula.FormulaParser;
import io.konig.formula.ShapePropertyOracle;
import io.konig.rio.turtle.NamespaceMap;
import io.konig.schemagen.SchemaGeneratorTest;

public class AbstractRdbmsShapeGeneratorTest extends SchemaGeneratorTest {
	
	public static final String ALIAS = "http://example.com/ns/alias/";

	protected RdbmsShapeGenerator shapeGenerator;

	protected void load(String path) throws RDFParseException, RDFHandlerException, IOException {
		super.load(path);
		SimpleLocalNameService nameService = new SimpleLocalNameService();
		nameService.addAll(graph);
		NamespaceMap nsMap = new NamespaceMapAdapter(graph.getNamespaceManager());
		ShapePropertyOracle oracle = new ShapePropertyOracle();
		FormulaParser parser = new FormulaParser(oracle, nameService, nsMap);
		shapeGenerator =  new RdbmsShapeGenerator(parser, "(.*)Shape$","$1RdbmsShape",ALIAS);
	}

}
