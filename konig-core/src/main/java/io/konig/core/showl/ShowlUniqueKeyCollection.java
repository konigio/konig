package io.konig.core.showl;

import java.util.ArrayList;
import java.util.List;

/**
 * A list of unique keys available for a given Node Shape.
 * In general, a given Node may have more than one unique key.  Therefore, it 
 * is useful to have a collection of keys.
 * @author Greg McFall
 *
 */
@SuppressWarnings("serial")
public class ShowlUniqueKeyCollection extends ArrayList<ShowlUniqueKey> {
	
	private ShowlNodeShape nodeShape;

	
	
	public ShowlUniqueKeyCollection(ShowlNodeShape nodeShape) {
		this.nodeShape = nodeShape;
	}
	
	/*
	 * Get the NodeShape that has the keys within this collection.
	 */
	public ShowlNodeShape getNodeShape() {
		return nodeShape;
	}

	/**
	 * Flatten this list if possible.  A ShowlUniqueKeyCollection can be flattened only if
	 * there is a single way to identify the associated NodeShape.  In other words, the
	 * collection can be flattened if it contains a single key, and each ShowlUniqueKeyCollection
	 * nested within it also contains a single key.
	 * 
	 * @return A flat list of PropertyShape entities contained within this collection provided it can be
	 * flattened, and null otherwise.
	 */
	public List<ShowlPropertyShape> flatten() {
		if (canFlatten()) {
			List<ShowlPropertyShape> list = new ArrayList<>();
			addProperties(list, this);
			return list;
		}
		return null;	
	}

	private void addProperties(List<ShowlPropertyShape> list, ShowlUniqueKeyCollection c) {
		
		for (ShowlUniqueKey key : c) {
			for (UniqueKeyElement e : key) {
				ShowlUniqueKeyCollection k = e.getValueKeys();
				if (k != null) {
					addProperties(list, k);
				} else {
					list.add(e.getPropertyShape());
				}
			}
		}
		
	}

	private boolean canFlatten() {
		if (size()==1) {
			for (ShowlUniqueKey key : this) {
				for (UniqueKeyElement e : key) {
					ShowlUniqueKeyCollection c = e.getValueKeys();
					if (c!=null && !c.canFlatten()) {
						return false;
					}
				}
			}
			return true;
		}
		return false;
	}
	

}
