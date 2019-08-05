package io.konig.core.showl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openrdf.model.URI;

@SuppressWarnings("serial")
public class SynsetProperty extends ArrayList<ShowlPropertyShape> {
	private SynsetNode valueNode;
	private List<URI> predicates = new ArrayList<>();
	
	@Override
	public boolean add(ShowlPropertyShape p) {
		if (!contains(p)) {
			addPredicate(p.getPredicate());
			return super.add(p);
		}
		return false;
	}
	
	public List<URI> getPredicates() {
		return predicates;
	}
	
	public String localNames() {
		Set<String> set = new HashSet<>();
		for (ShowlPropertyShape p : this) {
			set.add(p.getPredicate().getLocalName());
		}
		
		List<String> list = new ArrayList<>(set);
		Collections.sort(list);
		
		StringBuilder builder = new StringBuilder();
		String comma = "[";
		for (String text : list) {
			builder.append(comma);
			comma = ", ";
			builder.append(text);
		}
		builder.append(']');
		
		return builder.toString();
	}
	
	public ShowlDirectPropertyShape direct() {
		for (ShowlPropertyShape p : this) {
			if (p instanceof ShowlDirectPropertyShape) {
				return (ShowlDirectPropertyShape) p;
			}
		}
		return null;
	}
	
	public ShowlOutwardPropertyShape outFormula() {
		for (ShowlPropertyShape p : this) {
			if (p instanceof ShowlOutwardPropertyShape && 
					p.getFormula()!=null) {
				return (ShowlOutwardPropertyShape) p;
			}
		}
		return null;
	}
	

	public ShowlPropertyShape select() {
		ShowlPropertyShape p = direct();
		if (p == null) {
			p = outFormula();
			if (p == null && !isEmpty()) {
				p = get(0);
			}
		}
		
		return p;
	}

	/**
	 * Add the given predicate to the list of predicates, sorted by localName and then namespace.
	 */
	private void addPredicate(URI predicate) {
		if (!predicates.contains(predicate)) {
			String localName = predicate.getLocalName();
			for (int i=0; i<predicates.size(); i++) {
				URI p = predicates.get(i);
				int compare = localName.compareTo(p.getLocalName());
				if (compare == 0) {
					String namespace = predicate.getNamespace();
					if (namespace.compareTo(p.getNamespace()) > 0) {
						predicates.add(i+1, predicate);
						return;
					}
					
				} else if (compare > 0) {
					predicates.add(i+1, predicate);
					return;
				}
			}
			predicates.add(predicate);
		}
		
	}

	public SynsetNode getValueNode() {
		return valueNode;
	}

	public void setValueNode(SynsetNode valueNode) {
		this.valueNode = valueNode;
	}
	

}
