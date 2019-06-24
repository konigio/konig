package io.konig.core.showl;

import java.util.ArrayList;
import java.util.Collections;

import org.openrdf.model.URI;

@SuppressWarnings("serial")
public class ShowlPredicatePath extends ArrayList<URI> {
	
	private ShowlNodeShape root;

	public ShowlPredicatePath(ShowlNodeShape root) {
		this.root = root;
	}
	
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(root.getPath());
		for (URI predicate : this) {
			builder.append('.');
			builder.append(predicate.getLocalName());
		}
		
		return builder.toString();
	}
	
	public URI getLast() {
		return get(size()-1);
	}


	public static ShowlPredicatePath forProperty(ShowlPropertyShape p) {
		ShowlPredicatePath result = new ShowlPredicatePath(p.getRootNode());
		while (p != null) {
			result.add(p.getPredicate());
			p = p.getDeclaringShape().getAccessor();
		}
		Collections.reverse(result);
		return result;
	}
	
}
