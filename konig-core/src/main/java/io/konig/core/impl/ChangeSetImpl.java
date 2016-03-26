package io.konig.core.impl;

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


import org.openrdf.model.Resource;

import io.konig.core.ChangeSet;
import io.konig.core.Graph;
import io.konig.core.Vertex;
import io.konig.core.vocab.KCS;

public class ChangeSetImpl implements ChangeSet {
	private Vertex self;
	private Graph main;
	private Vertex reference;
	private Vertex add;
	private Vertex remove;
	private Vertex source;
	private Vertex target;

	public ChangeSetImpl(Graph main) {
		this.main = main;
		self = main.vertex();
	}
	
	public ChangeSetImpl(Vertex v) {
		self = v;
		main = v.getGraph();
	}
	
	public ChangeSetImpl(Resource id) {
		main = new MemoryGraph();
		self = main.vertex(id);
	}

	@Override
	public Vertex asVertex() {
		return self;
	}

	@Override
	public Resource getId() {
		return self.getId();
	}

	@Override
	public Vertex assertPriorState() {
		if (reference == null) {
			reference = main.vertex();
			reference.assertNamedGraph();
			main.edge(getId(), KCS.reference, reference.getId());
		}
		return reference;
	}

	@Override
	public Vertex assertAddition() {
		if (add == null) {
			add = main.vertex();
			add.assertNamedGraph();
			main.edge(getId(), KCS.add, add.getId());
		}
		return add;
	}

	@Override
	public Vertex assertRemoval() {
		if (remove == null) {
			remove = main.vertex();
			remove.assertNamedGraph();
			main.edge(getId(), KCS.remove, remove.getId());
		}
		return remove;
	}

	@Override
	public Vertex getReference() {
		if (reference == null) {
			reference = self.asTraversal().firstVertex(KCS.reference);
		}
		return reference;
	}

	@Override
	public Vertex getAddition() {
		if (add == null) {
			add = self.asTraversal().firstVertex(KCS.add);
		}
		return add;
	}

	@Override
	public Vertex getRemoval() {
		if (remove == null) {
			remove = self.asTraversal().firstVertex(KCS.remove);
		}
		return remove;
	}

	@Override
	public Vertex getSource() {
		if (source == null) {
			source = self.asTraversal().firstVertex(KCS.source);
		}
		return source;
	}

	@Override
	public Vertex getTarget() {
		if (target == null) {
			target = self.asTraversal().firstVertex(KCS.target);
		}
		return target;
	}



}
