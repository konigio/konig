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


import java.util.ArrayList;
import java.util.Collection;

import org.openrdf.model.URI;

import io.konig.core.Container;

public class MemoryContainer implements Container {
	private URI id;
	private ArrayList<URI> members;

	public MemoryContainer(URI id) {
		this.id = id;
	}

	@Override
	public URI getContainerId() {
		return id;
	}

	@Override
	public Collection<URI> members() {
		return members;
	}

	@Override
	public void add(URI member) {
		members.add(member);
		
	}

	@Override
	public void remove(URI member) {
		members.remove(member);
	}

	@Override
	public void reload() {
		
	}

}
