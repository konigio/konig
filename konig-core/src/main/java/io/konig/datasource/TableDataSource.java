package io.konig.datasource;

import io.konig.core.project.ProjectFile;

/*
 * #%L
 * Konig Core
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


public interface TableDataSource {
	
	/**
	 * Get a String reference to the Table suitable for use in a SELECT statement.
	 */
	String getTableIdentifier();
	
	/**
	 * Get the name of the SQL dialect used by this TableDataSource.
	 */
	String getSqlDialect();
	
	/**
	 * An identifier for this TableDataSource that is unique across the set of all individuals
	 * of type TableDataSource.  This identifier may be composed of several parts each of which
	 * is separated by a colon (':').
	 */
	String getUniqueIdentifier();
	
	/**
	 * Returns a string suitable for use as the name of a file containing a DDL description.
	 * @return A file name which, when combined with the datasource type, is unique.
	 */
	String getDdlFileName();
	
	ProjectFile getDdlFile();
	
	void setDdlFile(ProjectFile ddlFile);
	
	ProjectFile getTransformFile();
	
	void setTransformFile(ProjectFile transformFile);

}
