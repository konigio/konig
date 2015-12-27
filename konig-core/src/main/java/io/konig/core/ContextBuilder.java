package io.konig.core;

import java.util.HashMap;
import java.util.Map;

import org.openrdf.model.URI;

public class ContextBuilder {
	private Context context;
	private Map<String,String> namespaceMap = new HashMap<String, String>();
	public ContextBuilder(Context context) {
		this.context = context;
	}
	
	public ContextBuilder(String contextIRI) {
		context = new Context(contextIRI);
	}
	
	public Context getContext() {
		return context;
	}
	
	public ContextBuilder namespace(String prefix, String namespaceIRI) {
		namespaceMap.put(namespaceIRI, prefix);
		Term term = new Term(prefix, namespaceIRI, null, null);
		context.add(term);
		return this;
	}
	
	public ContextBuilder objectProperty(URI predicate) {
		return type(predicate);
	}
	
	public ContextBuilder type(URI type) {
		String prefix = namespaceMap.get(type.getNamespace());
		String id = (prefix==null) ? type.stringValue() : prefix + ":" + type.getLocalName();
		return property(type.getLocalName(), id, "@id");
	}
	
	public ContextBuilder type(String key, String iri) {
		Term term = new Term(key, iri, null, "@id");
		context.add(term);
		return this;
	}
	
	public ContextBuilder type(String key, URI iri) {
		Term term = new Term(key, iri.stringValue(), null, "@id");
		context.add(term);
		return this;
	}
	
	
	public ContextBuilder property(URI predicate, URI type) {

		String prefix = namespaceMap.get(predicate.getNamespace());
		String id = (prefix==null) ? type.stringValue() : prefix + ":" + predicate.getLocalName();
		
		String typePrefix = namespaceMap.get(type.getNamespace());
		String typeId = (typePrefix==null) ? type.stringValue() : typePrefix + ":" + type.getLocalName();
		
		return property(predicate.getLocalName(), id, typeId);
	}
	
	public ContextBuilder term(String key, String id) {
		Term term = new Term(key, id, null, null);
		context.add(term);
		return this;
	}
	
	
	public ContextBuilder property(String key, String id, String type) {
		Term term = new Term(key, id, null, type);
		context.add(term);
		return this;
	}
	
	
	public ContextBuilder objectProperty(String key, String id) {
		Term term = new Term(key, id, null, "@id");
		context.add(term);
		return this;
	}

}
