package io.konig.transform.proto;

/**
 * An enumeration used to classify the status of a shape as a source used to build some target shape.
 * @author Greg McFall
 *
 */
public enum SourceShapeStatus {
	
	/**
	 * The given shape is a potential source for building the target shape.
	 */
	CANDIDATE,
	
	/**
	 * The given shape has been excluded from consideration as a source.
	 */
	EXCLUDED,
	
	/**
	 * The given shape has been committed as a source shape.
	 */
	COMMITTED
}
