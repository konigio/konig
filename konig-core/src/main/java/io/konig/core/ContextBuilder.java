package io.konig.core;

public class ContextBuilder {
	private Context context;

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
		Term term = new Term(prefix, namespaceIRI, null, null);
		context.add(term);
		return this;
	}
	
	public ContextBuilder type(String key, String iri) {
		Term term = new Term(key, iri, null, null);
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
