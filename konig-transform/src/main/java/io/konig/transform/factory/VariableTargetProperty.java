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

import io.konig.core.KonigException;
import io.konig.core.io.PrettyPrintWriter;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;

public class VariableTargetProperty extends TargetProperty {
	private Set<SourceShape> candidateSourceShape = new HashSet<>();
	private SourceShape preferredSourceShape;

	public VariableTargetProperty(PropertyConstraint propertyConstraint) {
		super(propertyConstraint);
	}

	@Override
	public SourceProperty getPreferredMatch() {
		return null;
	}

	@Override
	public void setPreferredMatch(SourceProperty preferredMatch) {
		throw new KonigException("Cannot set a SourceProperty as the preferredMatch of a VariableTargetProperty");
	}
	
	public void addCandidateSourceShape(SourceShape source) {
		candidateSourceShape.add(source);
	}

	public Set<SourceShape> getCandidateSourceShape() {
		return candidateSourceShape;
	}

	
	public boolean isDirectProperty() {
		return false;
	}
	

	public SourceShape getPreferredSourceShape() {
		return preferredSourceShape;
	}

	public void setPreferredSourceShape(SourceShape preferredSourceShape) {
		this.preferredSourceShape = preferredSourceShape;
	}

	@Override
	public int getPathIndex() {
		return -1;
	}
	
	@Override
	protected void printLocalFields(PrettyPrintWriter out) {
		super.printLocalFields(out);
		if (!candidateSourceShape.isEmpty()) {
			out.beginArray("candidateSourceShape");
			for (SourceShape source : candidateSourceShape) {
				out.beginObject(source);
				Shape shape = source.getShape();
				out.beginObjectField("shape", shape);
				out.field("id", source.getShape().getId());
				out.endObjectField(shape);
				out.endObject();
			}
			out.endArray("candidateSourceShape");
		}
		
		if (preferredSourceShape != null) {
			out.field("preferredSourceShape", preferredSourceShape);
		}
	}

	@Override
	public int totalPropertyCount() {
		return 1;
	}

	@Override
	public int mappedPropertyCount() {
		return preferredSourceShape==null ? 0 : 1;
	}

}
