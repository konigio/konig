package io.konig.transform;

import io.konig.core.Path;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;

/**
 * A structure that maps a property of some source shape to a corresponding property on the target shape.
 * The corresponding property is defined by the {@link TransformAttribute} that contains this MappedProperty.
 * <p>
 * A MappedProperty has the following fields:
 * <ul>
 *   <li> sourceShape: The Shape of the source entity.
 *   <li> property:  A property on the sourceShape that is mapped to a corresponding property on the target shape.
 *   <li> stepIndex: The index of the step within property.equivalentPath that is
 *            associated with the target property identified by the parent TransformAttribute. This value is
 *            -1 if there is a direct mapping between the sourceShape property and the target property.
 *   <li> template: An {@link IriTemplateInfo} that prescribes a method for producing the target property from
 *        the sourceShape via an IriTemplate.
 *   
 *   
 * </ul>
 * </p>
 * @author Greg McFall
 *
 */
public class MappedProperty {
	
	private Shape sourceShape;
	private PropertyConstraint property;
	private IriTemplateInfo template;
	private int stepIndex = -1;
	
	public MappedProperty(Shape sourceShape, PropertyConstraint property, int stepIndex) {
		this.sourceShape = sourceShape;
		this.property = property;
		this.stepIndex = stepIndex;
	}

	public MappedProperty(Shape sourceShape, PropertyConstraint property) {
		this.sourceShape = sourceShape;
		this.property = property;
	}
	
	public boolean isLeaf() {
		Path path = property.getCompiledEquivalentPath();
		return template==null && (path==null || stepIndex == path.length()-1);
	}

	public void setTemplateInfo(IriTemplateInfo template) {
		this.template = template;
	}

	public IriTemplateInfo getTemplateInfo() {
		return template;
	}

	public Shape getSourceShape() {
		return sourceShape;
	}

	public PropertyConstraint getProperty() {
		return property;
	}

	public int getStepIndex() {
		return stepIndex;
	}
	
	

}
