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

import java.util.Set;

public interface ChangeSet {
	
	/**
	 * Record an edge was added.
	 * @param addition The edge that was added.
	 */
	void add(Trail addition);
	
	/**
	 * Record an edge that was removed
	 * @param removal The Edge that was removed.
	 */
	void remove(Trail removal);
	
	/**
	 * Get the set of edges that were added.
	 * @return
	 */
	Set<Trail> getAdditions();
	
	/**
	 * Get the set of edges that were removed.
	 * @return
	 */
	Set<Trail> getRemovals();

}
