package io.konig.schemagen.java;

import java.util.Collections;
import java.util.List;

import org.openrdf.model.URI;

import io.konig.schemagen.maven.FilterPart;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;

public class Filter {

	List<FilterPart> parts;
	

	public Filter(List<FilterPart> parts) {
		if (parts == null) {
			parts = Collections.emptyList();
		}
		this.parts = parts;
	}

	public List<FilterPart> getParts() {
		return parts;
	}

	public void setParts(List<FilterPart> parts) {
		this.parts = parts;
	}
	
	public boolean acceptNamespace(String namespace) {
		boolean result = true;
			
		for (FilterPart part : parts) {
			result = part.acceptNamespace(namespace);
		}
		return result;
	}
	
	public boolean acceptIndividual(String classId) {
		String namespace = namespace(classId);
		return acceptNamespace(namespace);
	}
	
	public boolean acceptShape(Shape shape) {
		for (PropertyConstraint p : shape.getProperty()) {
			URI predicate = p.getPredicate();
			if (acceptIndividual(predicate.stringValue())) {
				return true;
			}
		}
		return false;
	}

	private String namespace(String stringValue) {
		int slash = stringValue.lastIndexOf('/');
		int hash = stringValue.lastIndexOf('#');
		int colon = stringValue.lastIndexOf(':');
		
		int mark = Math.max(slash, hash);
		mark = Math.max(mark, colon);
		
		return stringValue.substring(0, mark+1);
	}
	

}
