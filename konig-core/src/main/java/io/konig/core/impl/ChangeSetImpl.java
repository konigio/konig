package io.konig.core.impl;

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


import java.util.LinkedHashSet;
import java.util.Set;

import io.konig.core.ChangeSet;
import io.konig.core.Trail;

public class ChangeSetImpl implements ChangeSet {
	
	private Set<Trail> additions = new LinkedHashSet<>();
	private Set<Trail> removals = new LinkedHashSet<>();

	@Override
	public void add(Trail addition) {
		additions.add(addition);
	}

	@Override
	public void remove(Trail removal) {
		removals.add(removal);
	}

	@Override
	public Set<Trail> getAdditions() {
		return additions;
	}

	@Override
	public Set<Trail> getRemovals() {
		return removals;
	}
	
	

}
