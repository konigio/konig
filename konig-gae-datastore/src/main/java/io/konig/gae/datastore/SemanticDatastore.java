package io.konig.gae.datastore;

/*
 * #%L
 * Konig GAE Datastore
 * %%
 * Copyright (C) 2015 - 2017 Gregory McFall
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

/**
 * A service for storing and retrieving entities.
 * @author Greg McFall
 *
 */
public interface SemanticDatastore {

	/**
	 * Store an entity using its canonical shape.
	 * The canonical shape includes all datatype properties and nested object properties for relationships
	 * to anonymous individuals (i.e. properties whose value is given by a BNode).
	 * @param entity A Vertex containing a description of the entity to be stored
	 */
	void put(Vertex entity);
	
	/**
	 * Load the description of an entity from the datastore.
	 * @param entityId The URI for the entity to be retrieved
	 * @param graph A graph into which the description of the entity will be loaded.
	 * @return A Vertex description of the entity
	 */
	Vertex getById(URI entityId, Graph graph);
}
