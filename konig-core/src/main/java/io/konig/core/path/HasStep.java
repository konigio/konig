package io.konig.core.path;

/*
 * #%L
 * Konig Core
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

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;

import io.konig.core.Graph;
import io.konig.core.TraversalException;

public class HasStep implements Step {

	private URI predicate;
	private Value value;
	
	public HasStep(URI predicate, Value value) {
		this.predicate = predicate;
		this.value = value;
	}

	@Override
	public void traverse(Traverser traverser) throws TraversalException {

		Graph graph = traverser.getGraph();
		Set<Value> source = traverser.getSource();
		
		for (Value s : source) {
			if (s instanceof Resource) {
				Resource subject = (Resource) s;
				if (graph.contains(subject, predicate, value)) {
					traverser.addResult(s);
				}
			}
		}

	}

}
