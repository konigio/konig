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
import io.konig.parser.BaseParser;
import io.konig.parser.ParseException;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.io.IOException;
import java.io.Reader;

public class DataCubeParser extends BaseParser {

    /**
     * Create a new CADLParser.
     *
     * @param lookAheadLimit The maximum number of characters that this parser can lookahead while parsing.
     */
    public DataCubeParser(int lookAheadLimit) {
        super(lookAheadLimit);
    }

    /**
     * Create new CADLParser with default lookAheadLimit = 100
     */
    public DataCubeParser() {
        super(100);
    }

    public DataCube parse(Reader reader) throws IOException {

        CharStream input = CharStreams.fromReader(reader);
        CADLLexer lexer = new CADLLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        CADLParser parser = new CADLParser(tokens);

        ParseTree tree = parser.base();
        ParseTreeWalker walker = new ParseTreeWalker();
        DataCubeListener listener = new DataCubeListener();
        walker.walk(listener, tree);

        return listener.getDataCube();
    }
}
