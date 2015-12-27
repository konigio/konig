package io.konig.core;

import org.openrdf.model.Namespace;

public interface NamespaceManager {

	Namespace findByPrefix(String prefix);
	Namespace findByName(String name);
	NamespaceManager add(Namespace ns);
	NamespaceManager add(String prefix, String namespace);

}
