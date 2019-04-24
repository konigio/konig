package io.konig.core.pojo;

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


/**
 * An interface used to test whether an object description can be embedded in 
 * the description file of some entity, or must be emitted separated.
 * @author Greg McFall
 *
 */
public interface ConditionalEmbeddable {

	/**
	 * Returns true if the description of this object can be embedded in 
	 * the RDF file for a resource related to this object.
	 * If false, the expected behavior is that the system will store this object's
	 * description in its own RDF file.
	 */
	boolean isEmbeddabled();
}
