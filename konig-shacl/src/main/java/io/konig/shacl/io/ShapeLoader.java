package io.konig.shacl.io;

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

import org.openrdf.model.ValueFactory;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.turtle.TurtleParser;

import io.konig.core.ContextManager;
import io.konig.core.KonigValueFactory;
import io.konig.core.NamespaceManager;
import io.konig.core.io.CompositeRdfHandler;
import io.konig.core.io.JsonldParser;
import io.konig.core.io.ListRdfHandler;
import io.konig.shacl.ShapeManager;

public class ShapeLoader {

	private ContextManager contextManager;
	private ShapeManager shapeManager;
	private NamespaceManager namespaceManager;
	private ValueFactory valueFactory;
	
	
	
	public ShapeLoader(ContextManager contextManager, ShapeManager shapeManager) {
		this(contextManager, shapeManager, null, null);
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
	
	public void loadTurtle(InputStream input) throws ShapeLoadException {
		TurtleParser parser = new TurtleParser();
		ShapeRdfHandler shapeHandler = new ShapeRdfHandler(shapeManager);
		ListRdfHandler listHandler = new ListRdfHandler(shapeHandler, shapeHandler);
		parser.setRDFHandler(listHandler);
		try {
			parser.parse(input, "");
		} catch (RDFParseException | RDFHandlerException | IOException e) {
			throw new ShapeLoadException(e);
		}
	}

	public void loadJsonld(InputStream input) throws ShapeLoadException {
		load(input, null);
	}
	
	public void load(InputStream input, RDFHandler handler) throws ShapeLoadException {
		RDFHandler rdfHandler = new ShapeRdfHandler(shapeManager);
		if (handler != null) {
			CompositeRdfHandler composite = new CompositeRdfHandler();
			composite.add(rdfHandler);
			composite.add(handler);
			handler = composite;
		}
		JsonldParser parser = new JsonldParser(contextManager, namespaceManager, valueFactory);
		parser.setRDFHandler(rdfHandler);
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
