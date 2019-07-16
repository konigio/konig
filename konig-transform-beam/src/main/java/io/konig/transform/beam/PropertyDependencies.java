package io.konig.transform.beam;

/*
 * #%L
 * Konig Transform Beam
 * %%
 * Copyright (C) 2015 - 2019 Gregory McFall
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


import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.openrdf.model.URI;

import io.konig.core.showl.ShowlDirectPropertyShape;
import io.konig.core.showl.ShowlPropertyShape;

public class PropertyDependencies implements Comparable<PropertyDependencies>{
	
	private ShowlDirectPropertyShape targetProperty;
	private String targetPropertyPath;
	private Set<ShowlPropertyShape> dependsOn;
	private Set<String> pathDependsOn;
	
	@SuppressWarnings("unchecked")
	public PropertyDependencies(ShowlDirectPropertyShape targetProperty, Set<ShowlPropertyShape> dependsOn) {
		this.targetProperty = targetProperty;
		this.dependsOn = dependsOn;
		targetPropertyPath = targetProperty.getPath();
		
		if (dependsOn.isEmpty()) {
			pathDependsOn = Collections.EMPTY_SET;
		} else {
			pathDependsOn = new HashSet<>();
			
			for (ShowlPropertyShape p : dependsOn) {
				addPaths(p);
			}	
		}
		
	}
	
	

	private void addPaths(ShowlPropertyShape p) {
		
		while (p != null) {
			pathDependsOn.add(p.getPath());
			p = p.getDeclaringShape().getAccessor();
		}
		
	}



	public ShowlDirectPropertyShape getTargetProperty() {
		return targetProperty;
	}



	public Set<ShowlPropertyShape> getDependsOn() {
		return dependsOn;
	}



	@Override
	public int compareTo(PropertyDependencies other) {
		if (this == other) {
			return 0;
		}
		if (other.pathDependsOn.contains(targetPropertyPath)) {
			return -1;
		}
		if (pathDependsOn.contains(other.targetPropertyPath)) {
			return 1;
		}
		URI thisPredicate = targetProperty.getPredicate();
		URI otherPredicate = other.targetProperty.getPredicate();
		
		
		return thisPredicate.getLocalName().compareTo(otherPredicate.getLocalName());
	}
	
}
