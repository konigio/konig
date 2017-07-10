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


import org.openrdf.model.URI;
import org.openrdf.model.Value;

import io.konig.core.io.PrettyPrintWriter;
import io.konig.shacl.PropertyConstraint;

public class SourceProperty extends PropertyNode<SourceShape> {
	
	private TargetProperty match;
	private int pathIndex;
	private boolean isDerived;
	
	// (predicate, value) pair is used only for a "has constraint" from an equivalent Path.
	private URI predicate;
	private Value value;

	public SourceProperty(PropertyConstraint propertyConstraint) {
		this(propertyConstraint, -1);
	}

	public SourceProperty(PropertyConstraint propertyConstraint, int pathIndex) {
		super(propertyConstraint);
		this.pathIndex = pathIndex;
	}
	
	public SourceProperty(PropertyConstraint propertyConstraint, int pathIndex, URI predicate, Value value) {
		this(propertyConstraint, pathIndex);
		this.predicate = predicate;
		this.value = value;
	}

	public TargetProperty getMatch() {
		return match;
	}

	public void setMatch(TargetProperty match) {
		this.match = match;
	}

	@Override
	public int getPathIndex() {
		return pathIndex;
	}

	@Override
	protected void printLocalFields(PrettyPrintWriter out) {
		out.field("hasMatch", match!=null);
		out.field("pathIndex", pathIndex);
		out.field("predicate", predicate);
		out.field("value", value);
		out.field("isDerived", isDerived);
		
	}
	
	@Override
	public URI getPredicate() {
		return predicate==null ? super.getPredicate() : predicate;
	}

	public Value getValue() {
		return value;
	}

	public boolean isDerived() {
		return isDerived;
	}

	@Override
	public void setDerived(boolean isDerived) {
		this.isDerived = isDerived;
	}
	


}
