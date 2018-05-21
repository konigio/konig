package io.konig.schemagen.sql;

/*
 * #%L
 * Konig Schema Generator
 * %%
 * Copyright (C) 2015 - 2018 Gregory McFall
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


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
