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

/**
 * An expression which asserts that a mapping has been delegated to another expression.
 * This concept was introduced originally to handle mappings of repeated fields and so the delegate
 * is typically a ShowlArrayExpression.  However, the ShowlDelegationExpression may find other uses in the future.
 * @author Greg McFall
 *
 */
public class ShowlDelegationExpression implements ShowlExpression {
	

	private static final ShowlDelegationExpression instance = new ShowlDelegationExpression();
	
	public static ShowlDelegationExpression getInstance() {
		return instance;
	}
	
	private ShowlDelegationExpression() {
		
	}
	
	@Override
	public String displayValue() {
		
		return "DelegationExpression";
	}

	@Override
	public void addDeclaredProperties(ShowlNodeShape sourceNodeShape, Set<ShowlPropertyShape> set)
			throws ShowlProcessingException {
		// Do nothing.  Mappings will be handled by the delegate.

	}

	@Override
	public void addProperties(Set<ShowlPropertyShape> set) {
		// Do nothing.  Mappings will be handled by the delegate.

	}

	@Override
	public URI valueType(OwlReasoner reasoner) {
		return null;
	}

}
