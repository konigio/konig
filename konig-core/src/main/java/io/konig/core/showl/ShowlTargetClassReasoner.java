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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.konig.core.OwlReasoner;
import io.konig.core.vocab.Konig;
import io.konig.formula.Direction;
import io.konig.formula.DirectionStep;
import io.konig.formula.PathExpression;
import io.konig.formula.PathStep;
import io.konig.formula.PrimaryExpression;
import io.konig.formula.QuantifiedExpression;
import io.konig.shacl.PropertyConstraint;

public class ShowlTargetClassReasoner {
	
	private static final Logger logger = LoggerFactory.getLogger(ShowlTargetClassReasoner.class);


	private ShowlService service;
	
	
	public ShowlTargetClassReasoner(ShowlService service) {
		this.service = service;
	}

	public void inferTargetClass() {
		ShowlClass undefined = service.produceShowlClass(Konig.Undefined);
		for (ShowlNodeShape node : undefined.getTargetClassOf()) {
			inferTargetClassOf(node);
		}
	}

	private void inferTargetClassOf(ShowlNodeShape node) {
		
		DomainReasoner domainReasoner = new DomainReasoner(service.getOwlReasoner());
		// TODO: handle derived properties separately.
				Set<ShowlPropertyShape> allProperties = node.allOutwardProperties();
				for (ShowlPropertyShape p : allProperties) {
					
					if (p.isNestedAccordingToFormula()) {
						continue;
					}
					
					ShowlProperty property = p.getProperty();
					if (property != null) {
						if (property.getDomain() != null) {
							domainReasoner.require(property.getDomain().getId());
						} else {
							domainReasoner.domainIncludes(property.domainIncludes(service));
						}
					}
				}
				
				Set<URI> candidates = domainReasoner.getRequiredClasses();
				
				if (candidates.isEmpty()) {
					candidates = inferTargetClassFromFormula(node, domainReasoner);
				}
				
				if (candidates.size()==1) {
					URI owlClass = candidates.iterator().next();
					replaceOwlClass(node, owlClass);
					if (logger.isTraceEnabled()) {
						logger.trace("inferTargetClass: Set {} as target class of {}", owlClass.getLocalName(), node.getPath());
					}
				} else {
					
				
					
					if (logger.isWarnEnabled()) {
						if (candidates.isEmpty()) {
							candidates = domainReasoner.getAllClasses();
						}
						if (candidates.isEmpty()) {
							logger.warn("No candidates found for target class of " + node.getPath());
						} else {
							StringBuilder builder = new StringBuilder();
							builder.append("Target class at " + node.getPath() + " is ambiguous.  Candidates include\n");
							for (URI c : candidates) {
								builder.append("  ");
								builder.append(c.getLocalName());
								builder.append('\n');
							}
							logger.warn(builder.toString());
						
						}
					}
				}
		
	}
	@SuppressWarnings("unchecked")
	private <T> Set<T> setOf(T... elements) {
		Set<T> result = new HashSet<T>();
		for (T e : elements) {
			result.add(e);
		}
		return result;
	}

	private Set<URI> inferTargetClassFromFormula(ShowlNodeShape node, DomainReasoner domainReasoner) {


		ShowlPropertyShape accessor = node.getAccessor();
		if (accessor != null) {
			
			ShowlClass owlClass = accessor.getProperty().getRange();
			if (owlClass != null) {
				return setOf(owlClass.getId());
			}
			
			ShowlPropertyShape peer = accessor.getPeer();
			if (peer != null) {
				owlClass = peer.getProperty().getRange();
				if (owlClass != null) {
					return setOf(owlClass.getId());
				}
			}
		}
		
		boolean updated = false;
		for (ShowlPropertyShape p : node.getProperties()) {
			
			PropertyConstraint constraint = p.getPropertyConstraint();
			if (constraint != null) {
				QuantifiedExpression formula = constraint.getFormula();
				if (formula != null) {
					PrimaryExpression primary = formula.asPrimaryExpression();
					if (primary instanceof PathExpression) {
						PathExpression path = (PathExpression) primary;
						List<PathStep> stepList = path.getStepList();
						if (stepList.size() == 1) {
							PathStep step = stepList.get(0);
							if (step instanceof DirectionStep) {
								DirectionStep dirStep = (DirectionStep) step;
								if (dirStep.getDirection() == Direction.OUT) {
									URI predicate = dirStep.getTerm().getIri();
									ShowlProperty property = service.produceProperty(predicate);
									if (property != null) {
										Set<URI> domainIncludes = property.domainIncludes(service);
										
										domainReasoner.domainIncludes(domainIncludes);
										updated = true;
										
									}
								}
							}
						}
					}
				}
			}
		}
		
		return updated ? domainReasoner.getRequiredClasses() : Collections.emptySet();
		
	}
		

	private void replaceOwlClass(ShowlNodeShape node, URI owlClassId) {
		node.getOwlClass().getTargetClassOf().remove(node);
		
		ShowlClass newClass = service.produceShowlClass(owlClassId);
		node.setOwlClass(newClass);
		newClass.addTargetClassOf(node);
		
		if (logger.isDebugEnabled()) {
			logger.debug("Set OWL Class of " + node.getPath() + " as " + "<" + owlClassId.stringValue() + ">");
		}
		
	}

	
	

	private class DomainReasoner  {
		private OwlReasoner reasoner;
		private Set<ModalClass> candidates = new HashSet<>();
		
		
		public DomainReasoner(OwlReasoner reasoner) {
			this.reasoner = reasoner;
		}
		
		public void require(URI owlClass) {
			candidates.add(new ModalClass(owlClass, true));
		}

		public void domainIncludes(Set<URI> domainIncludes) {
			
			boolean required = domainIncludes.size()==1;
			for (URI domain : domainIncludes) {
				URI matched = null;
				for (ModalClass modal : candidates) {
					URI candidate = modal.getOwlClass();
					if (reasoner.isSubClassOf(domain, candidate)) {
						// The domain is more narrow than the candidate
						// so replace the candidate with the domain.
						
						modal.update(domain, required);
						matched = domain;
					} else if (reasoner.isSubClassOf(candidate, domain)) {
						// The existing candidate is more narrow than the domain
						// so keep the existing candidate
						
						modal.update(candidate, required);
						matched = candidate;
					}
				}
				if (matched==null) {
					candidates.add(new ModalClass(domain, required));
				}
			}
		}
		
		public Set<URI> getRequiredClasses() {
			Set<URI> result = new HashSet<>();
			for (ModalClass modal : candidates) {
				if (modal.isRequired()) {
					result.add(modal.getOwlClass());
				}
			}
			return result;
		}
		
		public Set<URI> getAllClasses() {
			Set<URI> result = new HashSet<>();
			for (ModalClass modal : candidates) {
				result.add(modal.getOwlClass());
			}
			return result;
		}
	}
	
	/**
	 * A structure that decorates the URI for an OWL class with
	 * a flag that specifies whether or not the class is required.
	 *
	 */
	private class ModalClass {
		private boolean required;
		private URI owlClass;
		public ModalClass(URI owlClass, boolean required) {
			this.required = required;
			this.owlClass = owlClass;
		}
		public boolean isRequired() {
			return required;
		}
		public URI getOwlClass() {
			return owlClass;
		}
		
		public void update(URI owlClass, boolean required) {
			this.owlClass = owlClass;
			if (required) {
				this.required = true;
			}
		}
		
		
	}

}
