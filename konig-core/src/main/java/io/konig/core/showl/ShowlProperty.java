package io.konig.core.showl;

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

	
	public String toString() {
		return "ShowlProperty(" + predicate.toString() + ")";
	}
	
}
