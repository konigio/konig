package io.konig.core.showl;

/*
 * #%L
 * Konig Core
 * %%
 * Copyright (C) 2015 - 2019 Gregory McFall
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


import io.konig.datasource.DataSource;
import io.konig.shacl.Shape;

public interface ShowlNodeShapeService {

	
	ShowlNodeShape createNodeShape(Shape shape) throws ShowlProcessingException;
	ShowlNodeShape createNodeShape(Shape shape, DataSource ds) throws ShowlProcessingException;

	ShowlNodeShape createShowlNodeShape(ShowlPropertyShape accessor, Shape shape, ShowlClass owlClass);
	
	/**
	 * Get a ShowlNodeShape instance that describes the structure of a given Enumeration class.
	 * This method will return the same instance for a given class.  The instance will be
	 * created upon the first request.
	 * @param enumClass
	 * @return 
	 */
	Shape enumNodeShape(ShowlClass enumClass) throws ShowlProcessingException;
}
