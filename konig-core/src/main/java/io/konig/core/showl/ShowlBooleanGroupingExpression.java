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


import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.XMLSchema;

import io.konig.core.OwlReasoner;

abstract public class ShowlBooleanGroupingExpression implements ShowlExpression, ShowlStatement {
	
	private List<ShowlExpression> operands;
	
	
	protected ShowlBooleanGroupingExpression(List<ShowlExpression> operands) {
		this.operands = operands;
	}


	abstract public ShowlBooleanOperator getOperator();
	
	abstract protected ShowlBooleanGroupingExpression create(List<ShowlExpression> operands);

	@Override
	public ShowlExpression transform() {
		List<ShowlExpression> list = new ArrayList<ShowlExpression>();
		for (ShowlExpression e : operands) {
			list.add(e.transform());
		}
		return create(list);
	}

	@Override
	public String displayValue() {
		StringBuilder builder = new StringBuilder();
		builder.append('(');
		String operator = null;
		for (ShowlExpression e : operands) {
			if (operator != null) {
				builder.append(operator);
			} else {
				operator = getOperator().name();
			}
			builder.append(e.displayValue());
			
		}
		builder.append(')');
		return builder.toString();
	}

	@Override
	public void addDeclaredProperties(ShowlNodeShape sourceNodeShape, Set<ShowlPropertyShape> set)
			throws ShowlProcessingException {
		
		for (ShowlExpression e : operands) {
			e.addDeclaredProperties(sourceNodeShape, set);
		}

	}
	
	

	public List<ShowlExpression> getOperands() {
		return operands;
	}


	@Override
	public void addProperties(Set<ShowlPropertyShape> set) {

		for (ShowlExpression e : operands) {
			e.addProperties(set);
		}

	}

	@Override
	public URI valueType(OwlReasoner reasoner) {
		return XMLSchema.BOOLEAN;
	}

}
