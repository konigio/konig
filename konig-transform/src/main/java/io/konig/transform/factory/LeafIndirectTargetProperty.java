package io.konig.transform.factory;

/*
 * #%L
 * Konig Transform
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


import io.konig.shacl.PropertyConstraint;

public class LeafIndirectTargetProperty extends IndirectTargetProperty {

	private SharedSourceProperty preferredMatch;
	
	public LeafIndirectTargetProperty(PropertyConstraint propertyConstraint, int pathIndex, SharedSourceProperty preferredMatch) {
		super(propertyConstraint, pathIndex);
		this.preferredMatch = preferredMatch;
	}

	@Override
	public SourceProperty getPreferredMatch() {
		return preferredMatch.get();
	}

	@Override
	public void setPreferredMatch(SourceProperty preferredMatch) {
		this.preferredMatch.set(preferredMatch);
		preferredMatch.setMatch(this);
	}

}
