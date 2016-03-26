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


import io.konig.core.ChangeSet;
import io.konig.core.Graph;

public interface ChangeService {

	/**
	 * Apply a ChangeSet to a source Graph to produce a new revision of the Graph.
	 * @param source The Graph that will be mutated by the ChangeSet.
	 * @param delta The ChangeSet that encapsulates additions and removals.
	 */
	void apply(Graph source, ChangeSet delta);
	
	/**
	 * Undo a set of changes to a target Graph to restore it to the 
	 * previous state.   
	 * @param target  The Graph that will be mutated by undoing the supplied ChangeSet
	 * @param delta The ChangeSet that will be undone.
	 */
	void undo(Graph target, ChangeSet delta);
}
