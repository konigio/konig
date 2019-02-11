package io.konig.core.showl;

import java.util.Collections;

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


import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.openrdf.model.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.konig.core.KonigException;
import io.konig.core.OwlReasoner;
import io.konig.core.impl.RdfUtil;
import io.konig.core.vocab.Konig;
import io.konig.formula.Direction;
import io.konig.formula.DirectionStep;
import io.konig.formula.PathExpression;
import io.konig.formula.PathStep;
import io.konig.formula.PrimaryExpression;
import io.konig.formula.QuantifiedExpression;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;

public class ShowlProperty {
	
	private static final Logger logger = LoggerFactory.getLogger(ShowlProperty.class);

	private URI predicate;
	private ShowlClass domain;
	private ShowlClass range;
	private Set<ShowlProperty> inverses;
	
	private Set<ShowlPropertyShape> propertyShapes = new HashSet<>();
	
	public ShowlProperty(URI predicate) {
		this.predicate = predicate;
//		if (logger.isTraceEnabled()) {
//			logger.trace("new ShowlProperty({})", predicate.getLocalName());
//		}
	}

	public URI getPredicate() {
		return predicate;
	}

	/**
	 * Declare that a PropertyShape references the predicate. 
	 */
	public void addPropertyShape(ShowlPropertyShape p) {
		if (p.getDirection() == Direction.IN) {
			throw new KonigException("addPropertyShape: Inward property not allowed: " + p.getPath());
		}
		propertyShapes.add(p);
//		if (logger.isTraceEnabled()) {
//			logger.trace("addPropertyShape({})", p.getPath());
//		}
	}
	
	/**
	 * Get the set of PropertyShapes that reference the predicate directly via a Predicate Path
	 * @return
	 */
	public Set<ShowlPropertyShape> getPropertyShapes() {
		return propertyShapes;
	}
	

	/**
	 * Get the set of OWL Classes that are known to be in the domain of
	 * the property. Subclasses are excluded.
	 */
	public Set<URI> domainIncludes(ShowlManager manager) {
		Set<URI> result = new HashSet<>();
		if (domain != null  && !Konig.Undefined.equals(domain.getId())) {
			result.add(domain.getId());
		}
		Set<URI> equivalent = new HashSet<>();
		equivalent.add(predicate);
		addDomain(equivalent, result, manager, propertyShapes);
		return result;
	}
	
	/**
	 * Get the set of OWL Classes that are known to be in the range of the property. Subclasses are excluded.
	 */
	public Set<URI> rangeIncludes(OwlReasoner reasoner) {
		Set<URI> result = new HashSet<>();
		if (range !=null && !Konig.Undefined.equals(range.getId())) {
			result.add(range.getId());
		}
		addRange(result, reasoner, propertyShapes);
		return result;
	}

	private void addRange(Set<URI> result, OwlReasoner reasoner, Set<ShowlPropertyShape> set) {
		outer: for (ShowlPropertyShape p : set) {
			PropertyConstraint constraint = p.getPropertyConstraint();
			if (constraint != null) {
				URI valueClass = RdfUtil.uri(constraint.getValueClass());
				if (valueClass == null) {
					Shape valueShape = constraint.getShape();
					if (valueShape != null) {
						valueClass = valueShape.getTargetClass();
					}
				}
				if (valueClass != null && !Konig.Undefined.equals(valueClass)) {
					Iterator<URI> sequence = result.iterator();
					while (sequence.hasNext()) {
						URI other = sequence.next();
						if (reasoner.isSubClassOf(valueClass, other)) {
							continue outer;
						} else if (reasoner.isSubClassOf(other, valueClass)) {
							sequence.remove();
							break;
						}
					}
					result.add(valueClass);
				}
			}
		}
		
	}

	private void addDomain(Set<URI> equivalentProperty, Set<URI> result, ShowlManager manager, Set<ShowlPropertyShape> set) {
		OwlReasoner reasoner = manager.getReasoner();
		outer : for (ShowlPropertyShape p : set) {
			URI owlClass = p.getDeclaringShape().getOwlClass().getId();
			Iterator<URI> sequence = result.iterator();
			while (sequence.hasNext()) {
				URI other = sequence.next();
				if (reasoner.isSubClassOf(owlClass, other)) {
					continue outer;
				} else if (reasoner.isSubClassOf(other, owlClass)) {
					sequence.remove();
					break;
				}
			}
			if (!Konig.Undefined.equals(owlClass)) {
				result.add(owlClass);
			}
		

		}
	
		
	}
	
	public ShowlClass inferDomain(ShowlManager manager) {
		if (domain != null) {
			return domain;
		}
		Set<URI> domainIncludes = domainIncludes(manager);
		
		return domainIncludes.size()==1 ? 
				manager.produceOwlClass(domainIncludes.iterator().next()) : 
				manager.produceOwlClass(Konig.Undefined);
	}


	public ShowlClass inferRange(ShowlManager manager) {
		if (range != null) {
			return range;
		}
		Set<URI> rangeIncludes = rangeIncludes(manager.getReasoner());
	
		return rangeIncludes.size()==1 ?
			manager.produceOwlClass(rangeIncludes.iterator().next()) :
			manager.produceOwlClass(Konig.Undefined);
	}

	public ShowlClass getDomain() {
		return domain;
	}

	public void setDomain(ShowlClass domain) {
		this.domain = domain;
		if (domain != null) {
			domain.addDomainOf(this);
		}
	}

	public ShowlClass getRange() {
		return range;
	}

	public void setRange(ShowlClass range) {
		this.range = range;
		if (range != null) {
			range.addRangeOf(this);
		}
	}
	

	/**
	 * Get the transitive closure consisting of this Property plus all other
	 * Properties reachable via Property Groups.
	 * @return
	 */
	public Set<ShowlProperty> getConnectedProperties() {
		Set<ShowlProperty> result = new HashSet<>();
		addConnectedProperties(this, result);
		return result;
	}

	
	public Set<ShowlProperty> getInverses() {
		return inverses==null ? Collections.emptySet() : inverses;
	}

	public void addInverse(ShowlProperty inverseProperty) {
		if (inverseProperty != null  && (inverses==null || !inverses.contains(inverseProperty))) {
			
			if (inverses == null) {
				inverses = new HashSet<>();
			}
			inverses.add(inverseProperty);
			inverseProperty.addInverse(this);
			
			logger.trace("addInverse: {}...{}", predicate.getLocalName(), inverseProperty.getPredicate().getLocalName());
		}
	}

	private void addConnectedProperties(ShowlProperty p, Set<ShowlProperty> result) {
		if (!result.contains(p)) {
			result.add(p);
			for (ShowlPropertyShape q : p.getPropertyShapes()) {
				ShowlPropertyShape peer = q.getPeer();
				ShowlProperty peerProperty = peer.getProperty();
				addConnectedProperties(peerProperty, result);
			}
		}
	}

	public String toString() {
		return "ShowlProperty(" + predicate.toString() + ")";
	}
	
}
