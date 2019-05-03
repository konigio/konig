package io.konig.shacl.io;

import java.io.File;

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

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.turtle.TurtleParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.konig.core.ContextManager;
import io.konig.core.Graph;
import io.konig.core.KonigException;
import io.konig.core.KonigValueFactory;
import io.konig.core.NamespaceManager;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.RdfUtil;
import io.konig.core.io.GraphLoadHandler;
import io.konig.core.io.JsonldLoader;
import io.konig.core.pojo.PojoContext;
import io.konig.core.pojo.PojoListener;
import io.konig.core.pojo.SimplePojoFactory;
import io.konig.core.pojo.impl.BasicPojoHandler;
import io.konig.core.pojo.impl.PojoInfo;
import io.konig.core.vocab.SH;
import io.konig.datasource.DataSource;
import io.konig.datasource.DataSourceManager;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeManager;

public class ShapeLoader {

	private static final Logger logger = LoggerFactory.getLogger(ShapeLoader.class);
	
	public static final PojoContext CONTEXT = new PojoContext();
	static {
		CONTEXT.mapClass(SH.Shape, Shape.class);
	}
	
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

	public RDFHandler getListener() {
		return listener;
	}



	public void setListener(RDFHandler listener) {
		this.listener = listener;
	}


	
	public void loadAll(File source) throws ShapeLoadException {
		Graph graph = new MemoryGraph();
		try {
			RdfUtil.loadTurtle(source, graph, namespaceManager);
		} catch (RDFParseException | RDFHandlerException | IOException e1) {
			throw new ShapeLoadException(e1);
		}
		
		load(graph);
	}
	

	public void loadTurtle(InputStream input) throws ShapeLoadException {
		loadTurtle(input, null);
	}
	
	public void loadTurtle(InputStream input, URI context) throws ShapeLoadException {
		
		Graph graph = new MemoryGraph();
		graph.setNamespaceManager(namespaceManager);
		
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
		
		PojoContext context = new PojoContext(CONTEXT);

		context.putPojoHandler(Shape.class, new ShapePojoHandler(shapeManager));
		context.putPojoHandler(DataSource.class, new MasterDataSourcePojoHandler());
		context.setListener(new PojoListener() {

			@Override
			public void map(Resource id, Object pojo) {
				if ((pojo instanceof Shape) && (id instanceof URI)) {
					shapeManager.addShape((Shape)pojo);
				}
				
			}
		
			
		});
		SimplePojoFactory factory = new SimplePojoFactory(context);
		factory.createAll(graph);
		
	}
	

	public ContextManager getContextManager() {
		return contextManager;
	}

	public ShapeManager getShapeManager() {
		return shapeManager;
	}
	
	private static class ShapePojoHandler extends BasicPojoHandler {

		private ShapeManager shapeManager;
		
		public ShapePojoHandler(ShapeManager shapeManager) {
			super(Shape.class);
			this.shapeManager = shapeManager;
		}

		protected Object newInstance(PojoInfo pojoInfo) throws KonigException {
			Resource id = pojoInfo.getVertex().getId();
			Shape shape = shapeManager.getShapeById(id);
			if (shape != null) {
				return shape;
			}
			
			DataSource ds = DataSourceManager.getInstance().findDataSourceById(id);
			if (ds != null) {
				return ds;
			}
			return super.newInstance(pojoInfo);
		}
		
	}
	
	
	

}
