package io.konig.pojo.io;

/*
 * #%L
 * Konig Schema Generator
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


import io.konig.core.Vertex;

public interface PojoFactory {

	/**
	 * Create a POJO of a given type from data contained in a given vertex.
	 * @param v The vertex containing data for the POJO
	 * @param type The Java Class of the POJO to be created.
	 * @return A POJO of the given type containing data from the given vertex.
	 */
	public <T> T create(Vertex v, Class<T> type) throws ParseException;
}
