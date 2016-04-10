package io.konig.schemagen;

import java.util.Comparator;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;

import io.konig.core.Vertex;

public class LocalnameComparator implements Comparator<Vertex> {

	@Override
	public int compare(Vertex a, Vertex b) {
		Resource aId = a.getId();
		Resource bId = b.getId();
		
		String aString = (aId instanceof URI) ? ((URI)aId).getLocalName() : aId.stringValue();
		String bString = (bId instanceof URI) ? ((URI)bId).getLocalName() : bId.stringValue();
		
		return aString.compareTo(bString);
	}

}
