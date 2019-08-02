package io.konig.core.showl;

/**
 * Encapsulates information about a property that serves as one element of a unique key.
 * @author Greg McFall
 *
 */
public class UniqueKeyElement implements Comparable<UniqueKeyElement> {
	
	private ShowlPropertyShape propertyShape;
	private ShowlUniqueKeyCollection valueKeys;
	
	public UniqueKeyElement(ShowlPropertyShape propertyShape) {
		this.propertyShape = propertyShape;
	}

	public ShowlPropertyShape getPropertyShape() {
		return propertyShape;
	}

	@Override
	public int compareTo(UniqueKeyElement other) {
		return propertyShape.getPredicate().getLocalName().compareTo(
				other.getPropertyShape().getPredicate().getLocalName());
	}

	/**
	 * Get the collection of keys for the value NodeShape contained within the PropertyShape
	 * referenced by this element.
	 */
	public ShowlUniqueKeyCollection getValueKeys() {
		return valueKeys;
	}
	/**
	 * Set the collection of keys for the value NodeShape contained within the PropertyShape
	 * referenced by this element.
	 */
	public void setValueKeys(ShowlUniqueKeyCollection valueKeys) {
		this.valueKeys = valueKeys;
	}
}
