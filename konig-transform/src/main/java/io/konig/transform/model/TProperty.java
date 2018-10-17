package io.konig.transform.model;

/*
 * #%L
 * Konig Transform
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


import java.util.HashSet;
import java.util.Set;

public class TProperty {

	private TClass subjectClass;
	private TClass rangeClass;
	private TPropertyShape targetProperty;
	private TExpression valueExpression;
	
	private Set<TPropertyShape> candidateSourceProperties = new HashSet<>();

	public TProperty() {
	}

	public TPropertyShape getTargetProperty() {
		return targetProperty;
	}

	public void setTargetProperty(TPropertyShape targetProperty) {
		this.targetProperty = targetProperty;
		targetProperty.setPropertyGroup(this);
	}

	public TExpression getValueExpression() {
		return valueExpression;
	}

	public Set<TPropertyShape> getCandidateSourceProperties() {
		return candidateSourceProperties;
	}

	public void setValueExpression(TExpression valueExpression) {
		this.valueExpression = valueExpression;
	}

	public TClass getSubjectClass() {
		return subjectClass;
	}

	public void setSubjectClass(TClass subjectClass) {
		this.subjectClass = subjectClass;
	}

	public TClass getRangeClass() {
		return rangeClass;
	}

	public void setRangeClass(TClass rangeClass) {
		this.rangeClass = rangeClass;
	}

	public void addCandidateSourceProperty(TPropertyShape p) {
		candidateSourceProperties.add(p);
		p.setPropertyGroup(this);
	}
	
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("TProperty[");
		String comma = "";
		if (targetProperty !=null) {
			builder.append("targetProperty: ");
			builder.append(targetProperty.getPredicate().getLocalName());
			comma = ", ";
		}
		if (valueExpression != null) {
			builder.append(comma);
			builder.append("valueExpression: ");
			builder.append(valueExpression.toString());
			comma = ", ";
		} else if (!candidateSourceProperties.isEmpty()) {
			builder.append(comma);
			builder.append("candidateSourceProperties: (");
			String space = "";
			for (TPropertyShape c : candidateSourceProperties) {
				builder.append(space);
				space = " ";
				builder.append(c.getPredicate().getLocalName());
			}
			builder.append(")");
		}
		
		return builder.toString();
	}
}
