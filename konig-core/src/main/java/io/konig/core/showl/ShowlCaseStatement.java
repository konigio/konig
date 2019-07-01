package io.konig.core.showl;

import java.util.ArrayList;

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


import java.util.List;
import java.util.Set;

import org.openrdf.model.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.konig.core.OwlReasoner;

public class ShowlCaseStatement implements ShowlExpression {

	private static Logger logger = LoggerFactory.getLogger(ShowlCaseStatement.class);
	private ShowlExpression caseCondition;
	private List<ShowlWhenThenClause> whenThenList;
	private ShowlExpression elseClause;
	

	public ShowlCaseStatement(ShowlExpression caseCondition, List<ShowlWhenThenClause> whenThenList,
			ShowlExpression elseClause) {
		this.caseCondition = caseCondition;
		this.whenThenList = whenThenList;
		this.elseClause = elseClause;
	}

	public ShowlExpression getCaseCondition() {
		return caseCondition;
	}

	public List<ShowlWhenThenClause> getWhenThenList() {
		return whenThenList;
	}

	public ShowlExpression getElseClause() {
		return elseClause;
	}

	@Override
	public String displayValue() {
		StringBuilder builder = new StringBuilder();
		builder.append("CASE ");
		
		for (ShowlWhenThenClause whenThen : whenThenList) {
			builder.append(whenThen);
			builder.append(' ');
		}
		builder.append("END");
		
		return builder.toString();
	}
	

	@Override
	public void addDeclaredProperties(ShowlNodeShape sourceNodeShape, Set<ShowlPropertyShape> set)
			throws ShowlProcessingException {
		
		if (caseCondition!=null) {
			caseCondition.addDeclaredProperties(sourceNodeShape, set);
		}
		for (ShowlWhenThenClause whenThen : whenThenList) {
			whenThen.addDeclaredProperties(sourceNodeShape, set);
		}
		if (elseClause!=null) {
			elseClause.addDeclaredProperties(sourceNodeShape, set);
		}

	}

	@Override
	public void addProperties(Set<ShowlPropertyShape> set) {
		
		if (caseCondition!=null) {
			caseCondition.addProperties(set);
		}
		for (ShowlWhenThenClause whenThen : whenThenList) {
			whenThen.addProperties(set);
		}
		if (elseClause!=null) {
			elseClause.addProperties(set);
		}

	}

	@Override
	public String toString() {
		return displayValue();
	}

	@Override
	public URI valueType(OwlReasoner reasoner) {
		URI result = null;
		for (ShowlWhenThenClause c : whenThenList) {
			URI type = c.getThen().valueType(reasoner);
			result = (URI) reasoner.leastCommonSuperClass(result, type);
		}
		return result;
	}

	@Override
	public ShowlCaseStatement transform() {
		
		ShowlCaseStatement result =  new ShowlCaseStatement(ShowlUtil.transform(caseCondition), transform(whenThenList), ShowlUtil.transform(elseClause));
		if (logger.isTraceEnabled()) {
			logger.trace("transform: from...\n {}", displayValue());
			logger.trace("transform: to...\n{}", result.displayValue());
		}
		return result;
	}

	private List<ShowlWhenThenClause> transform(List<ShowlWhenThenClause> list) {
		List<ShowlWhenThenClause> result = new ArrayList<>();
		for (ShowlWhenThenClause clause : list) {
			result.add(clause.transform());
		}
		return result;
	}
}
