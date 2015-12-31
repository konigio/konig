package io.konig.core;

import java.util.List;
import java.util.Set;

import org.openrdf.model.URI;
import org.openrdf.model.Value;

import io.konig.core.path.Step;

public interface Path {
	
	Path out(URI predicate);
	Path in(URI predicate);
	
	Set<Value> traverse(Vertex source);
	
	List<Step> asList();
}
