package io.konig.formula;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;

import io.konig.core.Context;
import io.konig.core.KonigException;
import io.konig.core.Path;
import io.konig.core.Term;
import io.konig.core.path.HasStep;
import io.konig.core.path.HasStep.PredicateValuePair;
import io.konig.core.path.InStep;
import io.konig.core.path.OutStep;
import io.konig.core.path.Step;
import io.konig.core.path.VertexStep;

public class Path2FormulaTranslator {

	private static final Path2FormulaTranslator INSTANCE = new Path2FormulaTranslator();
	
	public static Path2FormulaTranslator getInstance() {
		return INSTANCE;
	}
	
	public QuantifiedExpression toQuantifiedExpression(Path path) {		
		PathExpression pathExpr = toPathExpression(path);
		UnaryExpression unary = new UnaryExpression(null, pathExpr);
		MultiplicativeExpression mult = new MultiplicativeExpression(unary);
		GeneralAdditiveExpression add = new GeneralAdditiveExpression(mult);
		
		BinaryRelationalExpression binary = new BinaryRelationalExpression(null, add, null);
		ConditionalAndExpression and = new ConditionalAndExpression();
		and.add(binary);
		QuantifiedExpression quantified = new QuantifiedExpression();
		quantified.setContext(path.getContext());
		quantified.add(and);
	
		return quantified;
	}
	

	private PathExpression toPathExpression(Path path) {
		List<Step> stepList = path.asList();
		Context context = path.getContext();
		
		PathExpression e = new PathExpression();
		for (Step step : stepList) {
			e.add(toPathStep(step, context));
		}
		
		return e;
	}

	private PathStep toPathStep(Step step, Context context) {
		PathStep result = null;
		if (step instanceof OutStep) {
			result = directionStep(Direction.OUT, ((OutStep)step).getPredicate(), context);
		} else if (step instanceof InStep) {
			result = directionStep(Direction.IN, ((InStep)step).getPredicate(), context);
		} else if (step instanceof HasStep) {
			result = hasStep((HasStep) step, context);
		} else if (step instanceof VertexStep) {
			throw new KonigException("VertexStep is not supported");
		}
		return result;
	}



	private HasPathStep hasStep(HasStep step, Context context) {
		
		Map<URI,PredicateObjectList> map = new HashMap<>();
		
		List<PredicateObjectList> constraints = new ArrayList<>();
		for (PredicateValuePair pair : step.getPairList()) {
			URI predicate = pair.getPredicate();
			Value value = pair.getValue();
			Expression valueExpression = toExpression(value, context);
			
			PredicateObjectList pol = map.get(predicate);
			if (pol == null) {
				PathTerm predicateValue = iriValue(predicate, context);
				List<Expression> expressionList = new ArrayList<>();
				ObjectList objectList = new ObjectList(expressionList);
				expressionList.add(valueExpression);
				
				PathExpression path = new PathExpression();
				path.add(new DirectionStep(Direction.OUT, predicateValue));
				
				pol = new PredicateObjectList(path, objectList);
				constraints.add(pol);
				map.put(predicate, pol);
			} else {
				pol.getObjectList().getExpressions().add(valueExpression);
			}
		}
		HasPathStep result =  new HasPathStep(constraints);
		return result;
	}

	private Expression toExpression(Value value, Context context) {
		PrimaryExpression primary = toPrimaryExpression(value, context);
		UnaryExpression unary = new UnaryExpression(null, primary);
		MultiplicativeExpression mult = new MultiplicativeExpression(unary);
		GeneralAdditiveExpression add = new GeneralAdditiveExpression(mult);
		
		BinaryRelationalExpression binary = new BinaryRelationalExpression(null, add, null);
		ConditionalAndExpression and = new ConditionalAndExpression();
		and.add(binary);
		Expression result = new BasicExpression();
		result.setContext(context);
		result.add(and);
	
		return result;
	}

	private PrimaryExpression toPrimaryExpression(Value value, Context context) {
		if (value instanceof URI) {
			return iriValue((URI)value, context);
		} else if (value instanceof Literal) {
			return new LiteralFormula((Literal)value);
		}
		throw new KonigException("PrimaryExpression for BNode not supported");
	}

	private PathTerm iriValue(URI predicate, Context context) {
		if (context == null) {
			return new FullyQualifiedIri(predicate);
		}
		String predicateValue = predicate.stringValue();
		String localName = predicate.getLocalName();
		Term term = context.getTerm(localName);
		if (term != null && predicateValue.equals(term.getId())) {
			return new LocalNameTerm(context, localName);
		} else {
		
			String namespace = predicate.getNamespace();
			Context inverse = context.inverse();
			term = inverse.getTerm(namespace);
			if (term != null) {
				return new CurieValue(context, term.getKey(), localName);
			} else {
				return new FullyQualifiedIri(predicate);
			}
		}
		
	}

	private PathStep directionStep(Direction direction, URI predicate, Context context) {
		
		PathTerm pathTerm = iriValue(predicate, context);
		return new DirectionStep(direction, pathTerm);
	}

}
