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
import java.util.Iterator;

import org.openrdf.model.ValueFactory;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.turtle.TurtleParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.konig.core.ContextManager;
import io.konig.core.Edge;
import io.konig.core.Graph;
import io.konig.core.KonigValueFactory;
import io.konig.core.NamespaceManager;
import io.konig.core.io.CompositeRdfHandler;
import io.konig.core.io.JsonldParser;
import io.konig.core.io.ListRdfHandler;
import io.konig.core.io.NamespaceRDFHandler;
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

					String name = source.getName();
					if (name.endsWith(".ttl")) {
						loadTurtle(input);
					} else if (name.endsWith(".jsonld")) {
						loadJsonld(input);
					}
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
		TurtleParser parser = new TurtleParser();
		ShapeRdfHandler shapeHandler = new ShapeRdfHandler(shapeManager);
		RDFHandler listHandler = new ListRdfHandler(shapeHandler, shapeHandler);
		
		CompositeRdfHandler composite = new CompositeRdfHandler(listHandler);
		
		if (namespaceManager != null) {
			composite.add(new NamespaceRDFHandler(namespaceManager));
		}
		if (listener != null) {
			composite.add(listener);
		}
		parser.setRDFHandler(composite);
		try {
			parser.parse(input, "");
		} catch (RDFParseException | RDFHandlerException | IOException e) {
			throw new ShapeLoadException(e);
		}
	}

	public void loadJsonld(InputStream input) throws ShapeLoadException {
		
		load(input, null);
	}
	
	public void load(Graph graph) throws ShapeLoadException {
		RDFHandler handler = new ShapeRdfHandler(shapeManager);
		try {
			handler.startRDF();
			Iterator<Edge> sequence = graph.iterator();
			while (sequence.hasNext()) {
				Edge edge = sequence.next();
				handler.handleStatement(edge);
			}
			handler.endRDF();
		} catch (RDFHandlerException e) {
			throw new ShapeLoadException(e);
		}
	}
	
	public void load(InputStream input, RDFHandler handler) throws ShapeLoadException {
		RDFHandler rdfHandler = new ShapeRdfHandler(shapeManager);
		CompositeRdfHandler composite = new CompositeRdfHandler(rdfHandler);
		
		if (handler != null) {
			composite.add(handler);
		}
		if (listener != null) {
			composite.add(listener);
		}
		JsonldParser parser = new JsonldParser(contextManager, namespaceManager, valueFactory);
		parser.setRDFHandler(composite);
		try {
			parser.parse(input, "");
		} catch (RDFParseException | RDFHandlerException | IOException e) {
			throw new ShapeLoadException(e);
		}
	}

	public ContextManager getContextManager() {
		return contextManager;
	}

	public ShapeManager getShapeManager() {
		return shapeManager;
	}
	
	

}
