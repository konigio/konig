package io.konig.formula;

/*
 * #%L
 * Konig Core
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


import java.util.ArrayList;
import java.util.List;

import org.openrdf.model.URI;

import io.konig.core.Context;
import io.konig.core.Term;
import io.konig.core.impl.BasicContext;

public class FormulaBuilder {

	private List<DirectedPredicate> predicateList;
	
	public FormulaBuilder out(URI predicate) {
		predicateList().add(new DirectedPredicate(Direction.OUT, predicate));
		return this;
	}
	
	private List<DirectedPredicate> predicateList() {
		if (predicateList == null) {
			predicateList = new ArrayList<>();
		}
		return predicateList;
	}
	
	public FormulaBuilder in(URI predicate) {
		predicateList().add(new DirectedPredicate(Direction.IN, predicate));
		return this;
	}
	
	public void pop() {
		List<DirectedPredicate> list = predicateList();
		list.remove(list.size()-1);
	}
	
	public QuantifiedExpression build() {
		PathExpression path = new PathExpression();
		QuantifiedExpression formula = QuantifiedExpression.wrap(path);
		Context context = new BasicContext(null);
		formula.setContext(context);
		for (DirectedPredicate e : predicateList()) {
			URI predicate = e.getPredicate();
			context.add(new Term(predicate.getLocalName(), predicate.stringValue()));
			PathStep step = new DirectionStep(e.getDirection(), new LocalNameTerm(context, predicate.getLocalName()));
			path.add(step);
		}
		return formula;
	}
	

	private static class DirectedPredicate {
		private Direction direction;
		private URI predicate;
		public DirectedPredicate(Direction direction, URI predicate) {
			this.direction = direction;
			this.predicate = predicate;
		}
		public Direction getDirection() {
			return direction;
		}
		public URI getPredicate() {
			return predicate;
		}
		
		
	}

}
