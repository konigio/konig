package io.konig.shacl.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

/*
 * #%L
 * konig-shacl
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


import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.turtle.TurtleParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.konig.core.ContextManager;
import io.konig.core.Graph;
import io.konig.core.KonigValueFactory;
import io.konig.core.NamespaceManager;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.io.GraphLoadHandler;
import io.konig.core.io.JsonldLoader;
import io.konig.core.pojo.PojoContext;
import io.konig.core.pojo.PojoListener;
import io.konig.core.pojo.SimplePojoFactory;
import io.konig.core.vocab.SH;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeManager;

public class ShapeLoader {

	private static final Logger logger = LoggerFactory.getLogger(ShapeLoader.class);
	
	private ContextManager contextManager;
	private ShapeManager shapeManager;
	private NamespaceManager namespaceManager;
	private ValueFactory valueFactory;
	private RDFHandler listener;
	
	
	
	public ShapeLoader(ContextManager contextManager, ShapeManager shapeManager) {
		this(contextManager, shapeManager, null, null);
	}
	
	public ShapeLoader(ShapeManager shapeManager) {
		this(null, shapeManager, null, null);
	}
	

	public RDFHandler getListener() {
		return listener;
	}



	public void setListener(RDFHandler listener) {
		this.listener = listener;
	}



	public ShapeLoader(ContextManager contextManager, ShapeManager shapeManager, NamespaceManager namespaceManager) {
		this(contextManager, shapeManager, namespaceManager, null);
	}

	public ShapeLoader(ContextManager contextManager, ShapeManager shapeManager, NamespaceManager namespaceManager, ValueFactory valueFactory) {
		if (valueFactory == null) {
			valueFactory = new KonigValueFactory();
		}
		this.contextManager = contextManager;
		this.shapeManager = shapeManager;
		this.namespaceManager = namespaceManager;
		this.valueFactory = valueFactory;
	}
	
	public void loadAll(File source) throws ShapeLoadException {
		
		if (source.isDirectory()) {
			File[] kids = source.listFiles();
			for (File file : kids) {
				loadAll(file);
			}
		} else {
			try {
				FileInputStream input = new FileInputStream(source);
				try {

					URI fileContext = new URIImpl("file://localhost/" + source.getAbsolutePath());
					String name = source.getName();
					if (name.endsWith(".ttl")) {
						loadTurtle(input, fileContext);
					} else if (name.endsWith(".jsonld")) {
						loadJsonld(input);
					}
				} catch (Throwable oops) {
					throw new ShapeLoadException("Failed to load " + source.getName(), oops);
				} finally {
					close(input);
				}
			} catch (FileNotFoundException e) {
				throw new ShapeLoadException(e);
			}
		}
	}
	
	private void close(FileInputStream input) {
		try {
			input.close();
		} catch (IOException e) {
			logger.warn("Failed to close file input stream", e);
		}
		
	}

	public void loadTurtle(InputStream input) throws ShapeLoadException {
		loadTurtle(input, null);
	}
	
	public void loadTurtle(InputStream input, URI context) throws ShapeLoadException {
		
		Graph graph = new MemoryGraph();
		
		GraphLoadHandler handler = new GraphLoadHandler(graph);
		handler.setQuadContext(context);
		TurtleParser turtle  = new TurtleParser();
		turtle.setRDFHandler(handler);
		try {
			turtle.parse(input, "");
		} catch (Throwable oops) {
			throw new ShapeLoadException(oops);
		}
		
		load(graph);
		
		
	}

	public void loadJsonld(InputStream input) throws ShapeLoadException {
		Graph graph = new MemoryGraph();
		JsonldLoader loader = new JsonldLoader();
		try {
			loader.load(input, graph, contextManager, namespaceManager);
			load(graph);
		} catch (RDFParseException | RDFHandlerException | IOException e) {
			
			throw new ShapeLoadException(e);
		}
	}
	
	public void load(Graph graph) throws ShapeLoadException {
		
		SimplePojoFactory factory = new SimplePojoFactory();
		PojoContext context = new PojoContext();
		context.setListener(new PojoListener() {

			@Override
			public void map(Resource id, Object pojo) {
				if ((pojo instanceof Shape) && (id instanceof URI)) {
					shapeManager.addShape((Shape)pojo);
				}
				
			}
		
			
		});
		context.mapClass(SH.Shape, Shape.class);
		factory.createAll(graph, context);
		
	}
	

	public ContextManager getContextManager() {
		return contextManager;
	}

	public ShapeManager getShapeManager() {
		return shapeManager;
	}
	
	

}
