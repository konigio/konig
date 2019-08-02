package io.konig.core.showl;

import java.util.ArrayList;

/**
 * Encapsulates information about a unique key.
 * 
 * In general, a unique key may consist of multiple properties, hence the ShowlUniqueKey object
 * is defined as a list of UniqueKeyElement entities.
 * 
 * @author Greg McFall
 *
 */
@SuppressWarnings("serial")
public class ShowlUniqueKey extends ArrayList<UniqueKeyElement> implements Comparable<ShowlUniqueKey> {

	public ShowlUniqueKey() {
	}
	
	public ShowlUniqueKey(UniqueKeyElement...e) {
		for (UniqueKeyElement element : e) {
			add(element);
		}
	}

	@Override
	public int compareTo(ShowlUniqueKey other) {
		int result = size() - other.size();
		if (result == 0) {
			for (int i=0; result==0 && i<size(); i++) {
				ShowlPropertyShape a = get(i).getPropertyShape();
				ShowlPropertyShape b = other.get(i).getPropertyShape();
				result = a.getPredicate().getLocalName().compareTo(b.getPredicate().getLocalName());
			}
		}
		return result;
	}

}
