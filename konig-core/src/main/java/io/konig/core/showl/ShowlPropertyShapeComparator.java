package io.konig.core.showl;

import java.util.Comparator;

public class ShowlPropertyShapeComparator implements Comparator<ShowlPropertyShape> {

	@Override
	public int compare(ShowlPropertyShape a, ShowlPropertyShape b) {
		
		return a.getPredicate().getLocalName().compareTo(b.getPredicate().getLocalName());
	}


}
