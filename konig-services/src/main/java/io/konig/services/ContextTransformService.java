package io.konig.services;

/*
 * #%L
 * Konig Services
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


import io.konig.core.Context;

/**
 * A service that transforms JSON-LD contexts.
 * @author Greg McFall
 *
 */
public interface ContextTransformService {

	
	
	/**
	 * Append simple terms from some source context to a target context.
	 * @param source The source context that supplies terms that will be added to the target
	 * @param target The target context into which simple terms will be injected.
	 */
	void append(Context source, Context target);

}
