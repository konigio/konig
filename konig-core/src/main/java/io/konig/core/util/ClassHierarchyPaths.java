package io.konig.core.util;

/*
 * #%L
 * Konig Core
 * %%
 * Copyright (C) 2015 - 2017 Gregory McFall
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
import java.util.List;
import java.util.Set;

import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDFS;

import io.konig.core.KonigException;
import io.konig.core.Vertex;
import io.konig.core.vocab.Schema;

/**
 * A list of paths starting at some target OWL class and traversing all ancestors
 * in the subsumption hierarchy, up to but excluding owl:Thing and schema:Thing.
 * 
 * @author Greg McFall
 *
 */
public class ClassHierarchyPaths extends ArrayList<List<URI>> {
	private static final long serialVersionUID = 1L;
	
	public ClassHierarchyPaths(Vertex targetClass) {
		build(targetClass);
	}

	private void build(Vertex targetClass) {
		if (targetClass.getId() instanceof URI) {

			List<URI> list = new ArrayList<>();

			URI classId = (URI) targetClass.getId();
			list.add(classId);
			add(list);
			
			addSuperClasses(list, targetClass);
		}
	}

	private void addSuperClasses(List<URI> list, Vertex targetClass) {
		
		Set<URI> superSet = targetClass.asTraversal().out(RDFS.SUBCLASSOF).toUriSet();
		superSet.remove(OWL.THING);
		superSet.remove(Schema.Thing);
		
		if (!superSet.isEmpty()) {
			
			boolean multiple = superSet.size()>1;
			if (multiple) {
				remove(list);
			}
			for (URI superId : superSet) {
				List<URI> sink = multiple ? new ArrayList<>(list) : list;
				sink.add(superId);
				if (multiple) {
					add(sink);
				}
				Vertex superVertex = targetClass.getGraph().getVertex(superId);
				if (superVertex == null) {
					throw new KonigException("Vertex not found: " + superId.stringValue());
				}
				addSuperClasses(sink, superVertex);
			}
		}
	}

}
