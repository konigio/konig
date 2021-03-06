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

public class ShowlTeleportExpression implements ShowlExpression {
	
	private ShowlNodeShape focusNode;
	private ShowlExpression delegate;
	

	public ShowlTeleportExpression(ShowlNodeShape focusNode, ShowlExpression delegate) {
		this.focusNode = focusNode;
		this.delegate = delegate;
	}

	public ShowlNodeShape getFocusNode() {
		return focusNode;
	}

	public ShowlExpression getDelegate() {
		return delegate;
	}

	@Override
	public ShowlExpression transform() {
		return new ShowlTeleportExpression(focusNode, delegate.transform());
	}

	@Override
	public String displayValue() {
		return delegate.displayValue();
	}

	@Override
	public void addDeclaredProperties(ShowlNodeShape sourceNodeShape, Set<ShowlPropertyShape> set)
			throws ShowlProcessingException {
		
		delegate.addDeclaredProperties(sourceNodeShape, set);
		
	}

	@Override
	public void addProperties(Set<ShowlPropertyShape> set) {
		
		delegate.addProperties(set);
		
	}

	@Override
	public URI valueType(OwlReasoner reasoner) {
		return delegate.valueType(reasoner);
	}

	@Override
	public String toString() {
		return displayValue();
	}

}
