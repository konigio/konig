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

import io.konig.shacl.Shape;

/**
 * A utility that identifies the Datastore Entity type for a given owl:Class
 * @author Greg McFall
 *
 */
public interface EntityNamer {

	/**
	 * Get the name of the Google Datastore entity that stores entities of a given type.
	 * @param owlClass  The URI for the OWL Class of interest
	 * @return The name of Google Datastore entity that stores instances of the given OWL Class
	 */
	String entityName(URI owlClass);
}
