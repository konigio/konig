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
 * An interfaced used to create a hash for a BNode
 * @author Greg McFall
 *
 */
public interface BNodeHasher {

	/**
	 * Create a hash for a given BNode that is accessed via a given predicate.
	 * @param predicate The predicate through which the BNode is accessed
	 * @param object The BNode for which a hash value will be computed
	 * @return The hash value for the BNode
	 */
	String createHash(URI predicate, Vertex object);
}
