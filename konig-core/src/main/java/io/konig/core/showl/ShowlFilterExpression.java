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

public class ShowlFilterExpression implements ShowlExpression {

	private ShowlExpression value;
	

	public ShowlFilterExpression(ShowlExpression value) {
		this.value = value;
	}

	@Override
	public String displayValue() {
		StringBuilder builder = new StringBuilder();
		builder.append("Filter(");
		builder.append(value.displayValue());
		builder.append(")");
		return builder.toString();
	}
	
	public String toString() {
		return displayValue();
	}

	@Override
	public void addDeclaredProperties(ShowlNodeShape sourceNodeShape, Set<ShowlPropertyShape> set)
			throws ShowlProcessingException {
		value.addDeclaredProperties(sourceNodeShape, set);
	}

	@Override
	public void addProperties(Set<ShowlPropertyShape> set) {
		value.addProperties(set);
	}

	public ShowlExpression getValue() {
		return value;
	}

	@Override
	public URI valueType(OwlReasoner reasoner) {
		// It's not clear that this is the correct implementation.
		return value.valueType(reasoner);
	}

	@Override
	public ShowlFilterExpression transform() {
		return new ShowlFilterExpression(value.transform());
	}

}
