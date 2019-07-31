package io.konig.transform.beam;

import java.util.ArrayList;

/**
 * Encapsulates information about a unique key.
 * 
 * In general, a unique key may consist of multiple properties, hence the BeamUniqueKey object
 * is defined as a list of UniqueKeyElement entities.
 * 
 * @author Greg McFall
 *
 */
@SuppressWarnings("serial")
public class BeamUniqueKey extends ArrayList<UniqueKeyElement> {

	public BeamUniqueKey() {
	}
	
	public BeamUniqueKey(UniqueKeyElement...e) {
		for (UniqueKeyElement element : e) {
			add(element);
		}
	}

}
