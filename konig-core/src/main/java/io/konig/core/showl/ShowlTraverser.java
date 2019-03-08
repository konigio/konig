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


import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openrdf.model.URI;

import io.konig.core.Graph;
import io.konig.core.OwlReasoner;
import io.konig.formula.DirectionStep;
import io.konig.formula.PathExpression;
import io.konig.formula.PathStep;
import io.konig.formula.PathTerm;
import io.konig.formula.PrimaryExpression;
import io.konig.formula.QuantifiedExpression;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;

public class ShowlTraverser {
	
	private ShowlManager manager;


	public ShowlTraverser(ShowlManager manager) {
		this.manager = manager;
	}



	public ShowlClass owlClass(URI owlClassId) {
		
		ShowlClass owlClass = manager.findClassById(owlClassId);
		if (owlClass == null) {
			owlClass = manager.produceOwlClass(owlClassId);
			for (Shape shape : manager.getShapeManager().listShapes()) {
				ShowlNodeShape node = new ShowlNodeShape(null, shape, owlClass);
				manager.addIdProperty(node);
			}
		}
			
		return owlClass;
	}
	
	public Set<ShowlProperty> traverse(URI variable, URI owlClass, QuantifiedExpression formula) {
		PrimaryExpression primary = formula.asPrimaryExpression();
		
		if (primary instanceof PathExpression) {
			return traversePath(variable, owlClass, (PathExpression) primary);
		}
		return Collections.emptySet();
	}


	private Set<ShowlProperty> traversePath(URI variable, URI owlClassId, PathExpression path) {
		if (variable!=null && owlClassId !=null && path!=null) {
			List<PathStep> list = path.getStepList();
			PathStep first = list.get(0);
			
			if (first instanceof DirectionStep) {
				DirectionStep dirStep = (DirectionStep) first;
				PathTerm term = dirStep.getTerm();
				
				if (term.getIri().getLocalName().equals(variable.getLocalName())) {
					ShowlClass owlClass = owlClass(owlClassId);
					Set<ShowlProperty> propertySet = null;
					for (int i=1; i<list.size(); i++) {
						PathStep step = list.get(i);
						
						if (step instanceof DirectionStep) {
							dirStep = (DirectionStep) step;
							URI predicate = dirStep.getTerm().getIri();
							
							switch (dirStep.getDirection()) {
							case OUT :
								propertySet = propertySet==null ? out(owlClass, predicate) : out(propertySet, predicate);
								break;
								
							case IN:
								throw new RuntimeException("In steps not supported yet");
							}
						}
					}
					return propertySet == null ? Collections.emptySet() : propertySet;
				}
			}
		}
		
		
		
		return Collections.emptySet();
	}

	public Set<ShowlProperty> out(Set<ShowlProperty> propertySet, URI outPredicate) {
		
		Set<URI> memory = new HashSet<>();
		Set<ShowlClass> classes = new HashSet<>();
		
		for (ShowlProperty p : propertySet) {
			addRangeIncludes(memory, classes, p);
		}

		Set<ShowlProperty> result = new HashSet<>();
		for (ShowlClass owlClass : classes) {
			Set<ShowlProperty> p = out(owlClass, outPredicate);
			result.addAll(p);
		}

		return result;
	}


	public Set<ShowlProperty> out(Set<ShowlProperty> propertySet, String localName) {
		
		Set<URI> memory = new HashSet<>();
		Set<ShowlClass> classes = new HashSet<>();
		
		for (ShowlProperty p : propertySet) {
			addRangeIncludes(memory, classes, p);
		}

		Set<ShowlProperty> result = new HashSet<>();
		for (ShowlClass owlClass : classes) {
			Set<ShowlProperty> p = out(owlClass, localName);
			result.addAll(p);
		}

		return result;
	}

	

	private void addRangeIncludes(Set<URI> memory, Set<ShowlClass> classes, ShowlProperty p) {
		URI predicate = p.getPredicate();
		if (!memory.contains(predicate)) {
			memory.add(predicate);

			ShowlClass range = p.getRange();
			if (range != null) {
				classes.add(range);
			} else {
				for (URI owlClassId  : p.rangeIncludes(manager.getReasoner()) ) {
					classes.add(manager.produceOwlClass(owlClassId));
				}
			}
			
		}
		
	}
	
	public Set<ShowlProperty> out(ShowlClass owlClass, URI outPredicate) {
		Set<ShowlProperty> result = new HashSet<>();
		
		for (ShowlProperty p : owlClass.getDomainOf()) {
			if (p.getPredicate().equals(outPredicate)) {
				result.add(p);
			}
		}
		if (!result.isEmpty()) {
			return result;
		}
		
		
		// No property found.  Try to construct it.
		for (ShowlNodeShape node : owlClass.getTargetClassOf()) {
			Shape shape = node.getShape();
			for (PropertyConstraint p : shape.getProperty()) {
				URI predicate = p.getPredicate();
				if (predicate != null && predicate.equals(outPredicate)) {
					ShowlProperty property = produceProperty(predicate, owlClass);
					result.add(property);
					ShowlDirectPropertyShape ps = new ShowlDirectPropertyShape(node, property, p);
					node.addProperty(ps);
				}
			}
		}
		
		for (ShowlNodeShape node : owlClass.getTargetClassOf()) {
			Shape shape = node.getShape();
			for (PropertyConstraint p : shape.getDerivedProperty()) {
				URI predicate = p.getPredicate();
				if (predicate != null) {
					ShowlProperty property = produceProperty(predicate, owlClass);
					result.add(property);
					ShowlFormulaPropertyShape ps = new ShowlFormulaPropertyShape(node, property, p);
					node.addDerivedProperty(ps);
				}
			}
		}
		
		if (result.isEmpty()) {
			// We still did not find any values.
			
			ShowlProperty property = produceProperty(outPredicate, owlClass);
			result.add(property);
		}
		
		return result;
	}

	public Set<ShowlProperty> out(ShowlClass owlClass, String localName) {
		
		Set<ShowlProperty> result = new HashSet<>();
		
		for (ShowlProperty p : owlClass.getDomainOf()) {
			if (p.getPredicate().getLocalName().equals(localName)) {
				result.add(p);
			}
		}
		if (!result.isEmpty()) {
			return result;
		}
		
		
		// No property found.  Try to construct it.
		for (ShowlNodeShape node : owlClass.getTargetClassOf()) {
			Shape shape = node.getShape();
			for (PropertyConstraint p : shape.getProperty()) {
				URI predicate = p.getPredicate();
				if (predicate != null && predicate.getLocalName().equals(localName)) {
					ShowlProperty property = produceProperty(predicate, owlClass);
					result.add(property);
					ShowlDirectPropertyShape ps = new ShowlDirectPropertyShape(node, property, p);
					node.addProperty(ps);
				}
			}
		}
		
		for (ShowlNodeShape node : owlClass.getTargetClassOf()) {
			Shape shape = node.getShape();
			for (PropertyConstraint p : shape.getDerivedProperty()) {
				URI predicate = p.getPredicate();
				if (predicate != null) {
					ShowlProperty property = produceProperty(predicate, owlClass);
					result.add(property);
					ShowlFormulaPropertyShape ps = new ShowlFormulaPropertyShape(node, property, p);
					node.addDerivedProperty(ps);
				}
			}
		}
		
		if (result.isEmpty()) {
			// We still did not find any values.
			// As a last resort, scan the graph.
			OwlReasoner owlReasoner = manager.getReasoner();
			Graph graph = owlReasoner.getGraph();
			Set<URI> terms = graph.lookupLocalName(localName);
			for (URI term : terms) {
				if (owlReasoner.isProperty(term)) {
					ShowlProperty property = produceProperty(term, owlClass);
					result.add(property);
				}
			}
		}
		
		return result;
	}

	private ShowlProperty produceProperty(URI term, ShowlClass owlClass) {

		ShowlProperty p = manager.produceShowlProperty(term);
		owlClass.addDomainOf(p);
		
		return p;
	}


}
