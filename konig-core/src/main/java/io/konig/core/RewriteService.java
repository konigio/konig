package io.konig.core;

/*
 * #%L
 * konig-core
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


public interface RewriteService {
	
	/**
	 * Convert a canonical IRI to the local IRI for the same resource in the local server.
	 * @param iri The canonical IRI that is to be converted.
	 * @return The converted IRI
	 */
	String toLocal(String iri);
	
	/**
	 * Convert a local IRI for a resource on the local server to its canonical IRI.
	 * @param iri The local IRI for a resource on the local server.
	 * @return The canonical IRI for given IRI.
	 */
	String fromLocal(String iri);

}
