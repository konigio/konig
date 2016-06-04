package io.konig.datagen;

import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

import io.konig.shacl.Shape;

public class SimpleIriGenerator implements IriGenerator {

	private String baseURL;

	public SimpleIriGenerator(String baseURL) {
		this.baseURL = baseURL;
	}

	@Override
	public URI createIRI(URI owlClass, int index) {
		
		StringBuilder builder = new StringBuilder();
		builder.append(baseURL);
		builder.append(owlClass.getLocalName());
		builder.append('/');
		builder.append(index);
		return new URIImpl(builder.toString());
	}
	
	
}
