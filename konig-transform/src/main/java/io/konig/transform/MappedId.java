package io.konig.transform;

import io.konig.shacl.Shape;

/**
 * A structure which prescribes a method for generating an IRI for instances of some target shape.
 * MappedId objects are contained within a {@link TransformFrame} which specifies the target shape.
 * @author Greg McFall
 *
 */
public class MappedId {
	private Shape sourceShape;
	private IriTemplateInfo templateInfo;
	public MappedId(Shape sourceShape, IriTemplateInfo templateInfo) {
		this.sourceShape = sourceShape;
		this.templateInfo = templateInfo;
	}
	
	/**
	 * Get the source shape from which an IRI may be generated.
	 */
	public Shape getSourceShape() {
		return sourceShape;
	}
	
	/**
	 * Get information about the IRI template which can be used to generate the IRI for instances
	 * of the target shape.
	 */
	public IriTemplateInfo getTemplateInfo() {
		return templateInfo;
	}
	
	

}
