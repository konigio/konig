package io.konig.services.impl;

/*
 * #%L
 * Konig Services
 * %%
 * Copyright (C) 2015 Gregory McFall
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
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;

import io.konig.core.ContentTypes;
import io.konig.core.Context;
import io.konig.core.ContextManager;
import io.konig.core.Edge;
import io.konig.core.Graph;
import io.konig.core.Vertex;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.io.GraphBuffer;
import io.konig.core.io.ResourceFile;
import io.konig.core.io.ResourceManager;
import io.konig.core.io.impl.ResourceFileImpl;
import io.konig.services.GraphService;
import io.konig.services.StorageException;
import io.konig.services.VertexAlreadyExistsException;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeManager;
import io.konig.shacl.transform.ReplaceTransform;

public class GraphServiceImpl implements GraphService {
	
	private Context context;
	private ContextManager contextManager;
	private ShapeManager shapeManager;
	private ResourceManager resourceManager;
	private GraphBuffer buffer = new GraphBuffer();

	public GraphServiceImpl(Context context, ContextManager contextManager, ResourceManager resourceManager, ShapeManager shapeManager) {
		this.context = context;
		this.contextManager = contextManager;
		this.resourceManager = resourceManager;
		this.shapeManager = shapeManager;
	}

	public void put(URI resource, Shape shape, Graph source) throws StorageException {
		
		try {
			ResourceFile file = resourceManager.get(resource.stringValue());
			
			MemoryGraph sink = new MemoryGraph();
			
			if (file != null) {
				buffer.readGraph(file.getEntityBody(), sink, contextManager);
			}
			Vertex sourceVertex = source.vertex(resource);
			Vertex target = sink.vertex(resource);
			
			ReplaceTransform replace = new ReplaceTransform(sourceVertex, target, shape);
			replace.execute();
			
			byte[] content = buffer.writeGraph(sink, context);
			if (file == null) {
				Properties properties = new Properties();
				properties.setProperty("Content-Type", ContentTypes.BINARY_GRAPH);
				properties.setProperty("Content-Location", resource.stringValue());
				
				file = new ResourceFileImpl(content, properties);
			} else {
				file = file.replaceContent(content);
			}
			resourceManager.put(file);
			
		} catch (IOException e) {
			throw new StorageException(e);
		}

	}

	

	public boolean get(URI resource, Shape shape, Graph sink)  throws StorageException {

		Set<String> memory = new HashSet<String>();
		return get(memory, resource, shape, sink);
		

	}

	private boolean get(Set<String> memory, URI resource, Shape shape, Graph sink) throws StorageException {
		
		if (memory.contains(resource.stringValue())) {
			return true;
		}
		boolean ok = false;
		try {
			ResourceFile file = resourceManager.get(resource.stringValue());
			if (file != null) {
				ok = true;
				buffer.readGraph(file.getEntityBody(), sink, contextManager);

				Vertex v = sink.vertex(resource);
				
				List<PropertyConstraint> list = shape.getProperty();
				for (PropertyConstraint p : list) {
					URI shapeId = p.getValueShapeId();
					if (shapeId != null) {
						Shape valueShape = p.getValueShape();
						if (valueShape == null) {
							valueShape = shapeManager.getShapeById(shapeId);
							if (valueShape == null) {
								throw new StorageException("Shape not found: " + shapeId);
							}
						}
						
						Set<Edge> out = v.outProperty(p.getPredicate());
						for (Edge e : out) {
							Value object = e.getObject();
							if (object instanceof URI) {
								URI objectId = (URI) object;
								if (!get(memory, objectId, valueShape, sink)) {
									// TODO: consider failing fast.
									// Should we add fail-fast as a configuration option on this service?
									return ok = false;
								}
							}
						}
						
					}
				}

			}
		} catch (IOException e) {
			throw new StorageException(e);
		}
		
		return ok;
		
	}

	public boolean get(URI resource, Graph sink) throws StorageException {
		try {
			ResourceFile file = resourceManager.get(resource.stringValue());
			if (file != null) {
				buffer.readGraph(file.getEntityBody(), sink, contextManager);
				return true;
			}
		} catch (IOException e) {
			throw new StorageException(e);
		}
		
		return false;
	}

	public void put(URI graphName, Graph graph) throws StorageException {
		
		try {
			byte[] data = buffer.writeGraph(graph, context);

			Properties properties = new Properties();
			properties.put(ResourceFile.CONTENT_TYPE, ContentTypes.BINARY_GRAPH);
			properties.put(ResourceFile.CONTENT_LOCATION, graphName.stringValue());
			
			ResourceFile file = new ResourceFileImpl(data, properties);
			
			resourceManager.put(file);
			
		} catch (IOException e) {
			throw new StorageException(e);
		}
		
	}

	public void put(Vertex vertex) throws StorageException {
		Resource id = vertex.getId();
		if (!(id instanceof URI)) {
			throw new StorageException("Vertex id must be a URI");
		}
		URI uri = (URI) id;
		
		Graph graph = new MemoryGraph();
		graph.add(vertex);

		// TODO: add incoming statements also.
		put(uri, graph);
		
		
	}

	public void post(URI namedGraph, Vertex vertex) throws VertexAlreadyExistsException, StorageException {
		
		MemoryGraph graph = new MemoryGraph();
		get(namedGraph, graph);
		
		Vertex prior = graph.getVertex(vertex.getId());
		if (prior != null) {
			throw new VertexAlreadyExistsException(vertex);
		}
		
		graph.add(vertex);
		
		put(namedGraph, graph);
		
	}

	

}
