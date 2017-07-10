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


import java.util.HashSet;
import java.util.Set;

import io.konig.core.io.PrettyPrintWriter;
import io.konig.shacl.PropertyConstraint;

abstract public class TargetProperty extends PropertyNode<TargetShape> {

	private Set<SourceProperty> matchSet = new HashSet<>();
	private boolean isNull;
	
	public TargetProperty(PropertyConstraint propertyConstraint) {
		super(propertyConstraint);
	}
	
	public void addMatch(SourceProperty node) {
		matchSet.add(node);
		node.setMatch(this);
	}
	
	public void removeMatch(SourceProperty node) {
		matchSet.remove(node);
		node.setMatch(null);
	}
	
	public Set<SourceProperty> getMatches() {
		return matchSet;
	}

	abstract public SourceProperty getPreferredMatch();

	abstract public void setPreferredMatch(SourceProperty preferredMatch);
	
	abstract public int totalPropertyCount();
	
	abstract public int mappedPropertyCount();


	@Override
	protected void printLocalFields(PrettyPrintWriter out) {
		out.field("hasPreferredMatch", getPreferredMatch()!=null);
		out.field("isNull", isNull);
	}

	public boolean isNull() {
		return isNull;
	}

	public void setNull(boolean isNull) {
		this.isNull = isNull;
	}

	
}
