package io.konig.transform.beam;

import io.konig.core.showl.ShowlPropertyShape;

/**
 * Encapsulates information about a property that serves as one element of a unique key.
 * @author Greg McFall
 *
 */
public class UniqueKeyElement {
	
	private ShowlPropertyShape propertyShape;
	private RdfJavaType type;
	
	public UniqueKeyElement(ShowlPropertyShape propertyShape, RdfJavaType type) {
		this.propertyShape = propertyShape;
		this.type = type;
	}

	public ShowlPropertyShape getPropertyShape() {
		return propertyShape;
	}

	public RdfJavaType getType() {
		return type;
	}

}
