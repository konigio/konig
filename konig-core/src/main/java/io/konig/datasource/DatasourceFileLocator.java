package io.konig.datasource;

/*
 * #%L
 * Konig Core
 * %%
 * Copyright (C) 2015 - 2018 Gregory McFall
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

/**
 * An interface for locating a file associated with a given DataSource.
 * Different implementations of this interface can be used to locate different
 * types of files.
 * 
 * @author Greg McFall
 *
 */
public interface DatasourceFileLocator {

	/**
	 * Locate a file associated with a given DataSource.
	 * @param ds
	 * @return The file associated with the DataSource, or null if no appropriate file is found.
	 */
	public File locateFile(DataSource ds);
}
