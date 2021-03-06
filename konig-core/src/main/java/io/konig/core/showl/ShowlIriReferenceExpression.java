package io.konig.core.showl;

/*
 * #%L
 * Konig Core
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


import java.util.Set;

import org.openrdf.model.URI;

import io.konig.core.OwlReasoner;
import io.konig.core.Vertex;

public class ShowlIriReferenceExpression implements ShowlExpression {

	private URI iriValue;
	private ShowlPropertyShape referencedBy;

	

	public ShowlIriReferenceExpression(URI iriValue, ShowlPropertyShape referencedBy) {
		this.iriValue = iriValue;
		this.referencedBy = referencedBy;
	}

	public URI getIriValue() {
		return iriValue;
	}

	/**
	 * The PropertyShape that contains the IRI reference.
	 */
	public ShowlPropertyShape getReferencedBy() {
		return referencedBy;
	}


	@Override
	public String displayValue() {
		return "<" + iriValue.stringValue() + ">";
	}

	@Override
	public void addDeclaredProperties(ShowlNodeShape sourceNodeShape, Set<ShowlPropertyShape> set) {
		
		// Do nothing

	}

	@Override
	public void addProperties(Set<ShowlPropertyShape> set) {
		// Do nothing
		
	}
	
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ShowlIriReferenceExpression(iriValue: <");
		builder.append(iriValue.stringValue());
		builder.append(">, referencedBy: ");
		builder.append(referencedBy.getPath());
		builder.append(')');
		return builder.toString();
	}

	@Override
	public URI valueType(OwlReasoner reasoner) {
		URI result = null;
		Vertex v = reasoner.getGraph().getVertex(iriValue);
		if (v != null) {
			result = reasoner.mostSpecificTypeOf(v);
		}
		return result;
	}

	@Override
	public ShowlIriReferenceExpression transform() {
		return this;
	}

}
