package io.konig.formula;

import java.io.StringWriter;

/*
 * #%L
 * Konig Core
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


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.openrdf.model.URI;

import io.konig.core.io.PrettyPrintWriter;

public class PathExpression extends AbstractFormula implements PrimaryExpression {
	
	private List<PathStep> stepList = new ArrayList<>();
	
	public static PathExpressionBuilder builder() {
		return new PathExpressionBuilder();
	}
	
	public void add(PathStep step) {
		stepList.add(step);
	}
	
	public List<PathStep> getStepList() {
		return stepList;
	}

	@Override
	public void print(PrettyPrintWriter out) {
		
		if (!stepList.isEmpty()) {
			Iterator<PathStep> sequence = stepList.iterator();
			if (!stepList.isEmpty()) {
				String dollar = "$";
				PathStep first = stepList.get(0);
				if (first instanceof DirectionStep) {
					DirectionStep dirStep = (DirectionStep) first;
					if (dirStep.getTerm() instanceof VariableTerm) {
						dollar = "";
					}
				}
				out.print(dollar);
			}
			while (sequence.hasNext()) {
				PathStep step = sequence.next();
				step.print(out);
			}
		}

	}
	
	public String simpleText() {
		StringWriter writer = new StringWriter();
		PrettyPrintWriter pretty = new PrettyPrintWriter(writer);
		pretty.setSuppressContext(true);
		print(pretty);
		pretty.close();
		return writer.toString();
	}

	@Override
	public void dispatch(FormulaVisitor visitor) {

		visitor.enter(this);
		for (PathStep s : stepList) {
			s.dispatch(visitor);
		}
		visitor.exit(this);
		
	}
	
	public static class PathExpressionBuilder {
		private PathExpression path;
		
		private PathExpressionBuilder() {
			path = new PathExpression();
		}
		
		public PathExpressionBuilder out(URI predicate) {
			PathStep step = new DirectionStep(Direction.OUT, new FullyQualifiedIri(predicate));
			path.add(step);
			return this;
		}
		
		public PathExpression build() {
			return path;
		}
	}

	public static PathExpression toPathExpression(QuantifiedExpression formula) {
		if (formula == null) {
			return null;
		}
		PrimaryExpression primary = formula.asPrimaryExpression();
		
		return primary instanceof PathExpression ? (PathExpression) primary : null;
	}

	public DirectionStep directionStepAfter(int i) {
		for (int j=i+1; j<stepList.size(); j++) {
			PathStep step = stepList.get(j);
			if (step instanceof DirectionStep) {
				return (DirectionStep) step;
			}
		}
		return null;
	}

	public DirectionStep directionStepBefore(int i) {
		for (int j=i-1; j>=0; j--) {
			PathStep step = stepList.get(j);
			if (step instanceof DirectionStep) {
				return (DirectionStep) step;
			}
		}
		return null;
	}

}
