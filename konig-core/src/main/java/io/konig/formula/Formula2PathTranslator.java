package io.konig.formula;

import java.util.List;

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


import org.openrdf.model.URI;
import org.openrdf.model.Value;

import io.konig.core.Path;
import io.konig.core.path.HasStep;
import io.konig.core.path.InStep;
import io.konig.core.path.OutStep;
import io.konig.core.path.PathImpl;
import io.konig.core.path.Step;

public class Formula2PathTranslator {
	
	private static final Formula2PathTranslator INSTANCE = new Formula2PathTranslator();
	
	public static Formula2PathTranslator getInstance() {
		return INSTANCE;
	}
	
	public Path toPath(Formula formula) {
		
		if (formula instanceof Expression) {
			return fromExpression((Expression)formula);
		}
		
		return null;
	}

	private Path fromExpression(Expression e) {
		PathImpl result = null;
		
		PrimaryExpression pe = e.asPrimaryExpression();
		if (pe instanceof PathExpression) {
			PathExpression p = (PathExpression) pe;
			result = new PathImpl();
			result.setContext(e.getContext());
			for (PathStep s : p.getStepList()) {
				Step step = step(s);
				if (step == null) {
					return null;
				}
				step = step(s);
				if (step == null) {
					return null;
				} else {
					result.add(step(s));
				}
			}
			
		}
		return result;
	}

	private Step step(PathStep s) {
		if (s instanceof DirectionStep) {
			return directionStep((DirectionStep)s);
		} else if (s instanceof HasPathStep) {
			return hasStep((HasPathStep)s);
		}
		return null;
	}

	private HasStep hasStep(HasPathStep s) {
		HasStep has = new HasStep();
		for (PredicateObjectList pol : s.getConstraints()) {
			URI predicate = pol.getVerb().getIri();
			for (Expression e : pol.getObjectList().getExpressions()) {
				PrimaryExpression primary = e.asPrimaryExpression();
				if (primary == null) {
					return null;
				}
				
				Value value = null;
				if (primary instanceof LiteralFormula) {
					LiteralFormula literal = (LiteralFormula) primary;
					value = literal.getLiteral();
				} else if (primary instanceof PathExpression) {
					PathExpression path = (PathExpression) primary;
					List<PathStep> stepList = path.getStepList();
					if (stepList.size()==1) {
						PathStep pathStep = stepList.get(0);
						if (pathStep instanceof DirectionStep) {
							DirectionStep dirStep = (DirectionStep) pathStep;
							if (dirStep.getDirection() == Direction.OUT) {
								PathTerm term = dirStep.getTerm();
								if (term instanceof IriValue) {
									value = term.getIri();
								}
							}
						}
					}
				} else if (primary instanceof IriValue) {
					IriValue iriValue = (IriValue) primary;
					value = iriValue.getIri();
				}
				
				if (value != null) {
					has.add(predicate, value);
				} else {
					return null;
				}
			}
			
		}
		return has;
	}

	private Step directionStep(DirectionStep s) {
		PathTerm term = s.getTerm();
		if (term instanceof VariableTerm) {
			return null;
		}
		switch (s.getDirection()) {
		case IN : return inStep(s);
		case OUT: return outStep(s);
		}
		return null;
	}

	private Step outStep(DirectionStep s) {
		
		URI predicate = predicate(s);
		if (predicate == null) {
			return null;
		}
		return new OutStep(predicate);
	}

	private Step inStep(DirectionStep s) {
		URI predicate = predicate(s);
		if (predicate == null) {
			return null;
		}
		return new InStep(predicate);
	}

	private URI predicate(DirectionStep s) {
		return s.getTerm().getIri();
	}

}
