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


import java.util.List;
import java.util.Set;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;

import io.konig.core.Edge;
import io.konig.core.Graph;
import io.konig.core.KonigException;
import io.konig.core.Path;
import io.konig.core.Traverser;
import io.konig.core.Vertex;

public class DataInjector {

	
	public void inject(Vertex subject, Path path, Value value) {
		
		Graph graph = subject.getGraph();
		Resource subjectId = subject.getId();
		
		List<Step> stepList = path.asList();
		int lastStep = stepList.size()-1;
		
		for (int i=0; i<=lastStep; i++) {
			Step step = stepList.get(i);
			if (step instanceof OutStep) {
				OutStep outStep = (OutStep) step;
				URI predicate = outStep.getPredicate();
				Value object = subject.getValue(predicate);
				if (i == lastStep) {
					if (object == null) {
						graph.edge(subject.getId(), predicate, value);
					} else if (!value.equals(object)) {

						StringBuilder message = new StringBuilder();
						message.append("Cannot inject path value ");
						message.append(subjectId.stringValue());
						message.append(path.toString());
						message.append(' ');
						message.append(value.toString());
						message.append(". Conflicting value found: " + object.toString());
						
						throw new KonigException(message.toString());
					}
				} else {
					if (object instanceof Resource) {
						subject = graph.getVertex((Resource)object);
					} else {
						Resource id = subject.getId();
						subject = graph.vertex();
						graph.edge(id, predicate, subject.getId());
					}
				}

				
			} else {
				throw new KonigException("Unsupported step type: " + step.getClass().getSimpleName());
			}
		}
		
		
	}

	

}
