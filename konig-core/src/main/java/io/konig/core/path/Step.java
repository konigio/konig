package io.konig.core.path;

import org.openrdf.model.URI;

import io.konig.core.Context;
import io.konig.core.NamespaceManager;
import io.konig.core.SPARQLBuilder;

/*
 * #%L
 * konig-core
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


import io.konig.core.TraversalException;
import io.konig.core.Traverser;

public interface Step {

	/**
	 * Get the predicate associated with this step or null if there is none.
	 */
	URI getPredicate();
	void traverse(Traverser traverser) throws TraversalException;
	void visit(SPARQLBuilder builder);
	void append(StringBuilder builder, NamespaceManager nsManager);
	
	String toString(Context context);
}