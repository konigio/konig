package io.konig.core.delta;

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


import org.openrdf.model.Value;
import org.openrdf.model.vocabulary.RDF;

import io.konig.core.Edge;
import io.konig.core.Graph;
import io.konig.core.vocab.CS;

/**
 * A processor that applies a ChangeSet to a source graph.
 * @author Greg McFall
 *
 */
public class ChangeSetProcessor {

	


	public void applyChanges(Graph source, Graph changeSet, Graph target) {
		// First copy all edges from source to target
		for (Edge e : source) {
			target.edge(e);
		}
		
		// Now edit the target in accordance with the changeSet
		for (Edge e : changeSet) {
			
			Value value = e.getAnnotation(RDF.TYPE);
			
			
			if (e.matches(CS.Falsity, value)) {
				target.remove(e);
			} else if (e.matches(CS.Dictum, value)) {
				target.edge(e);
			}
		}
		
	}

}
