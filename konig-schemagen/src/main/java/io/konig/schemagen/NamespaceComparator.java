package io.konig.schemagen;

import java.util.Comparator;

import org.openrdf.model.Namespace;

public class NamespaceComparator implements Comparator<Namespace> {

	@Override
	public int compare(Namespace a, Namespace b) {
		return a.getPrefix().compareTo(b.getPrefix());
	}

}
