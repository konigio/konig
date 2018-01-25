package io.konig.cadl.parser;

/*
 * #%L
 * konig-datacube
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

import io.konig.cadl.model.DataCube;
import io.konig.cadl.model.Datatype;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class CADLParserTest {

    private CADLParser parser;

    @Before
    public void setUp() throws IOException {
        InputStream is = this.getClass().getResourceAsStream("/cubes/simple.cadl");
        Reader reader = new InputStreamReader(is);
        CharStream input = CharStreams.fromReader(reader);
        CADLLexer lexer = new CADLLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        parser = new CADLParser(tokens);
    }

    @Test
    public void testParser() throws Exception {
        String expected = "(base (namespace PREFIX ex : http://example.org/) (namespace PREFIX schema : http://schema.org/) (cube CUBE foo { (dimension DIMENSION a (level LEVEL a1 ATTRIBUTES { (attribute name ( (datatype string) ) UNIQUE) }) (level LEVEL a2 ATTRIBUTES { (attribute name ( (datatype string) ) UNIQUE ,) (attribute category ( (datatype string) )) }) (rollup a2 ROLL-UP to a1)) (measure MEASURE b ( (datatype real) )) }))";
//        parser.setErrorHandler();
        ParseTree tree = parser.base();
        assertEquals(expected, tree.toStringTree(parser));
    }

    @Test
    public void testListener() {
        ParseTree tree = parser.base();
        ParseTreeWalker walker = new ParseTreeWalker();
        DataCubeListener listener = new DataCubeListener();
        walker.walk(listener, tree);

        DataCube cube = listener.getDataCube();
        assertNotNull(cube);
        assertEquals("foo", cube.getName());
        assertEquals(1, cube.getDimensions().size());
        assertEquals("a", cube.getDimensions().get(0).getName());
        assertEquals("a1", cube.getDimensions().get(0).getLevels().get(0).getName());
        assertEquals("a2", cube.getDimensions().get(0).getLevels().get(1).getName());

        // check that a2 ROLL-UP to a1 was correctly parsed
        assertEquals("a1", cube.getDimensions().get(0).getLevels().get(1).getRollUps().get(0).getName());

        // check that MEASURE b was correctly parsed
        assertEquals(1, cube.getMeasures().size());
        assertEquals("b", cube.getMeasures().get(0).getName());
        assertEquals(Datatype.real, cube.getMeasures().get(0).getDatatype());
    }
}
