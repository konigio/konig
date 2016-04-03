package io.konig.services;

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


import org.openrdf.model.URI;

import io.konig.core.Graph;
import io.konig.core.Vertex;
import io.konig.shacl.Shape;

public interface GraphService {
	
	
	
	/**
	 * Add the state of the given vertex to a specified named graph.
	 * 
	 * @param namedGraph The URI that identifies the named graph to which the given vertex should be appended.
	 * @param vertex The vertex that is to be added to the named graph.
	 * @param VertexAlreadyExistsException The vertex already exists within the named graph
	 * @throws StorageException An internal error occurred while storing the state of the vertex.
	 */
	void post(URI namedGraph, Vertex vertex) throws VertexAlreadyExistsException, StorageException;
	
	/**
	 * Persist all the current state of the specified vertex, overwriting its previous state.
	 * @param vertex The vertex whose state is to be persisted.
	 * @throws StorageException An internal error occurred while storing the state of the vertex.
	 */
	void put(Vertex vertex) throws StorageException;
	
	/**
	 * Persist all the statements of a named graph, overwriting any statements that were previously stored 
	 * for that graph.
	 * 
	 * @param contentType The Content-Type of the graph.  
	 * @param graphName The URI that identifies the named graph
	 * @param graph The named graph whose statements will be persisted.
	 * @throws StorageException An internal error occurred while storing the graph.
	 */
	void put(URI graphName, Graph graph) throws StorageException;

	/**
	 * Persist a particular shape of a resource.  This method performs a complete replacement of the 
	 * the data shape with the current statements in the data store.
	 * 
	 * @param resource The resource being stored.
	 * @param shape The shape of the resource.
	 * @param source A graph containing statements about the resource to be stored.
	 */
	void put(URI resource, Shape shape, Graph source) throws StorageException;
	
	/**
	 * Get a specific shape of a given resource.
	 * @param resource The resource to be fetched
	 * @param shape The requested shape of the resource
	 * @param sink The graph into which the resource statements should be placed
	 * @return True if the resource shape was retrieved successfully, and false if the resource was not found.
	 * @throws StorageException An internal error occurred while fetching the resource.
	 */
	boolean get(URI resource, Shape shape, Graph sink) throws StorageException;
	
	boolean get(URI resource, Graph sink) throws StorageException;
	
}
