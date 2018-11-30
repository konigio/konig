package io.konig.core.showl;

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
import java.util.Set;

import org.openrdf.model.URI;

import io.konig.core.OwlReasoner;
import io.konig.core.vocab.Konig;

public class ShowlProperty {

	private URI predicate;
	private ShowlClass domain;
	private ShowlClass range;
	
	private Set<ShowlPropertyShape> propertyShapes = new HashSet<>();
	
	public ShowlProperty(URI predicate) {
		this.predicate = predicate;
	}

	public URI getPredicate() {
		return predicate;
	}

	/**
	 * Declare that a PropertyShape references the predicate. 
	 */
	public void addPropertyShape(ShowlPropertyShape p) {
		propertyShapes.add(p);
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
	public Set<URI> domainIncludes(OwlReasoner reasoner) {
		Set<URI> result = new HashSet<>();
		if (domain != null  && !Konig.Undefined.equals(domain)) {
			result.add(domain.getId());
		}
		addDomain(result, reasoner, propertyShapes);
		return result;
	}

	private void addDomain(Set<URI> result, OwlReasoner reasoner, Set<ShowlPropertyShape> set) {
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

	private void addConnectedProperties(ShowlProperty p, Set<ShowlProperty> result) {
		if (!result.contains(p)) {
			result.add(p);
			for (ShowlPropertyShape q : p.getPropertyShapes()) {
				ShowlPropertyGroup g = q.getGroup();
				if (g != null && g.size()>1) {
					for (ShowlPropertyShape r : g) {
						ShowlProperty s = r.getProperty();
						addConnectedProperties(s, result);
					}
				}
			}
		}
	}

	public String toString() {
		return "ShowlProperty(" + predicate.toString() + ")";
	}
	
}
