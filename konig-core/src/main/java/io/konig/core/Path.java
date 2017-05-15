package io.konig.core;

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


import java.util.List;
import java.util.Set;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;

import io.konig.core.path.Step;

public interface Path {

	Context getContext();
	void setContext(Context context);
	
	
	Path out(URI predicate);
	Path in(URI predicate);
	Path has(URI predicate, Value value);
	Path copy();
	
	Set<Value> traverse(Vertex source);
	Set<Value> traverse(Traverser traverser);
	
	List<Step> asList();
	Value toValue();
	
	/**
	 * Get a sub-path
	 * @param start The initial index of the subpath.
	 * @param end The end of the subpath.
	 * @return The path consisting of steps from <code>start</code> through <code>end-1</code>
	 */
	Path subpath(int start, int end);
	
	/**
	 * Get a sub-path from a given index to the end of this Path.
	 * @param start
	 * @return Return the Path from the given index to the end of this Path
	 */
	Path subpath(int start);
	
	Step remove(int index);
	
	int length();
	
	void visit(SPARQLBuilder builder);
}