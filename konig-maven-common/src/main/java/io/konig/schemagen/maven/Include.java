package io.konig.schemagen.maven;

public class Include extends FilterPart {

	@Override
	public boolean acceptNamespace(String namespace) {
		return getNamespaces().contains(namespace);
	}


}
