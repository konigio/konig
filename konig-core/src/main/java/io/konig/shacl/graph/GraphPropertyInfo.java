package io.konig.shacl.graph;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.openrdf.model.URI;

import io.konig.core.OwlReasoner;

public class GraphPropertyInfo {

	private URI predicate;
	private Set<GraphPropertyShape> directPropertyShapes = new HashSet<>();
	private Set<GraphPropertyShape> indirectPropertyShapes = new HashSet<>();
	
	public GraphPropertyInfo(URI predicate) {
		this.predicate = predicate;
	}

	public URI getPredicate() {
		return predicate;
	}

	/**
	 * Declare that a PropertyShape references the predicate directly via a Predicate Path. 
	 */
	public void addDirectPropertyShape(GraphPropertyShape p) {
		directPropertyShapes.add(p);
	}
	
	/**
	 * Get the set of PropertyShapes that reference the predicate directly via a Predicate Path
	 * @return
	 */
	public Set<GraphPropertyShape> getDirectPropertyShapes() {
		return directPropertyShapes;
	}
	
	/**
	 * Declare that a PropertyShape references the predicate indirectly via a formula that contains
	 * a PathExpression with the predicate as the initial outbound step.
	 */
	public void addIndirectPropertyShape(GraphPropertyShape p) {
		indirectPropertyShapes.add(p);
	}

	/**
	 * Get the set of PropertyShapes that reference the predicate indirectly via a formula that contains
	 * a PathExpression with the predicate as the initial outbound step.
	 * @return
	 */
	public Set<GraphPropertyShape> getIndirectPropertyShapes() {
		return indirectPropertyShapes;
	}

	/**
	 * Get the set of OWL Classes that are known to be in the domain of
	 * the property. Subclasses are excluded.
	 */
	public Set<URI> domainIncludes(OwlReasoner reasoner) {
		Set<URI> result = new HashSet<>();
		addDomain(result, reasoner, directPropertyShapes);
		addDomain(result, reasoner, indirectPropertyShapes);
		return result;
	}

	private void addDomain(Set<URI> result, OwlReasoner reasoner, Set<GraphPropertyShape> set) {
		outer : for (GraphPropertyShape p : set) {
			URI owlClass = p.getDeclaringShape().getOwlClass().getOwlClassId();
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
			result.add(owlClass);
		}
		
	}
	
	
}
