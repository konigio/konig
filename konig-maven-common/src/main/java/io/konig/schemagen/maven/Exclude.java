package io.konig.schemagen.maven;

public class Exclude extends FilterPart {

	@Override
	public boolean acceptNamespace(String namespace) {
		return !getNamespaces().contains(namespace);
	}

}
