package io.konig.shacl;

/*
 * #%L
 * Konig Core
 * %%
 * Copyright (C) 2015 - 2018 Gregory McFall
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

import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

import io.konig.core.OwlReasoner;
import io.konig.formula.DirectedStep;
import io.konig.formula.Direction;
import io.konig.formula.PathExpression;
import io.konig.formula.PathStep;

/**
 * A utility that converts a {@link PathExpression} to an equivalent Shape
 * @author Greg McFall
 *
 */
public class PathShapeGenerator {
	
	private OwlReasoner reasoner;

	public PathShapeGenerator(OwlReasoner reasoner) {
		this.reasoner = reasoner;
	}

	public Shape toShape(String baseName, PathExpression path) {
		Shape shape = null;
		
		List<PathStep> list = path.getStepList();
		
		for (int i=0; i<list.size(); i++) {
			PathStep step = list.get(i);
			if (step instanceof DirectedStep) {
				DirectedStep dir = (DirectedStep) step;
				URI predicate = dir.getTerm().getIri();
				
				
				
				switch (dir.getDirection()) {
				case IN : {
					Set<URI> domainIncludes = reasoner.domainIncludes(predicate);
					
					InversePath inversePath = new InversePath(predicate);
					PropertyConstraint p = new PropertyConstraint(inversePath);
					
					if (domainIncludes.size()==1) {
						URI valueClass = domainIncludes.iterator().next();
						p.setValueClass(valueClass);
					}
					break;
				}
					
				case OUT :
					break;
					
				}
				
			}
		}
		
		return shape;
	}


	private URI shapeId(String baseName, PathExpression path, int index) {
		StringBuilder builder = new StringBuilder(baseName);
		builder.append('_');
		PathStep first = path.getStepList().get(index);
		
		if (first instanceof DirectedStep) {
			DirectedStep directed = (DirectedStep) first;
			if (directed.getDirection() == Direction.IN) {
				builder.append("_");
			}
			builder.append(directed.getTerm().getIri().getLocalName());
		}
		
		return new URIImpl(builder.toString());
	}
}
