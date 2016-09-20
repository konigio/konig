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


import java.util.List;

import org.openrdf.model.URI;

import io.konig.core.Edge;
import io.konig.core.Vertex;

/**
 * An interface used to extract a key for a BNode suitable for use in an index.
 * The key must uniquely identify the BNode within the context(s) from which it is accessed.
 * @author Greg McFall
 *
 */
public interface KeyFactory {

	
	List<Edge> createKey(Vertex subject, URI predicate, Vertex object);
	
}
