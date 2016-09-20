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


import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;

public interface Edge extends Statement, Cloneable {

	/**
	 * Get a property of this edge.
	 * @param predicate The predicate that identifies the property whose value is to be returned.
	 * @return The value of the specified property
	 */
	Value getProperty(URI predicate);
	
	/**
	 * Set a property on this edge
	 * @param predicate The predicate that identifies the property to be set
	 * @param value The value of the property to be set.
	 */
	void setProperty(URI predicate, Value value);
	
	Value removeProperty(URI predicate);

}
