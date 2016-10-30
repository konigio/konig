package io.konig.core.delta;

/*
 * #%L
 * konig-core
 * %%
 * Copyright (C) 2015 - 2016 Gregory McFall
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


import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.openrdf.model.Resource;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.turtle.TurtleParser;

import io.konig.core.Graph;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.io.ContextValueFactory;
import io.konig.core.io.GraphLoadHandler;


/**
 * 
 * @author Greg McFall
 *
 */
public class FileChangeManager {

	public void applyChanges(File sourceDir, File targetDir, FileChangeHandler handler) 
		throws IOException, RDFParseException, RDFHandlerException {
		
		Graph targetGraph = new MemoryGraph();
		loadAll(targetDir, targetGraph);
	}

	private void loadAll(File file, Graph graph) throws IOException, RDFParseException, RDFHandlerException {
		
		if (file.isDirectory()) {
			File[] kids = file.listFiles();
			for (File child : kids) {
				loadAll(child, graph);
			}
		} else {
			FileReader input = new FileReader(file);
			ContextValueFactory valueFactory = new ContextValueFactory(fileResource(file));
			TurtleParser parser = new TurtleParser(valueFactory);
			GraphLoadHandler handler = new GraphLoadHandler(graph);
			parser.setRDFHandler(handler);
			parser.parse(input, "");
		}
	}

	private Resource fileResource(File file) {
		String path = file.getAbsolutePath().replace('\\', '/');
		StringBuilder builder = new StringBuilder();
		builder.append("file://localhost/");
		builder.append(path);
		
		return new URIImpl(builder.toString());
	}
}
