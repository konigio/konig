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


import org.openrdf.model.URI;

import io.konig.core.Vertex;

/**
 * An factory that creates a BNodeKey for a given BNode.
 * @author Greg McFall
 *
 */
public interface BNodeKeyFactory {
	
	/**
	 * Create a key for a given BNode that is the object of some statement.
	 * @param predicate The predicate of the statement
	 * @param object The BNode whose key is to be created.
	 * @return The key for the given BNode which is accessed via the given predicate
	 */
	BNodeKey createKey(URI predicate, Vertex object);
	

}
