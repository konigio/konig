package io.konig.core.io;

/*
 * #%L
 * Konig Core
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


import java.io.File;

import org.openrdf.model.URI;

/**
 * A utility for getting the file that holds the description of a given resource.
 * @author Greg McFall
 *
 */
public interface FileGetter {

	/**
	 * Get the File where the description of a given resource is stored.
	 * @param resourceId  The URI for the resource
	 * @return The File where a description of the resource is to be stored.
	 */
	File getFile(URI resourceId);
}
