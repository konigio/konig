package io.konig.transform;

import io.konig.core.Path;
import io.konig.core.io.PrettyPrintWriter;
import io.konig.core.io.AbstractPrettyPrintable;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;

/**
 * A structure that maps a property of some source shape to a corresponding property on the target shape.
 * The corresponding property is defined by the {@link TransformAttribute} that contains this MappedProperty.
 * <p>
 * A MappedProperty has the following fields:
 * <ul>
 *   <li> targetContext: A string that identifies the path relative to the targetRoot where the sourceShape is utilized.
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
public class MappedProperty extends AbstractPrettyPrintable {
	
	private ShapePath shapePath;
	private PropertyConstraint property;
	private IriTemplateInfo template;
	private ShapePath templateShape;
	private int stepIndex = -1;
	
	public MappedProperty(ShapePath shapePath, PropertyConstraint property, int stepIndex) {
		this.shapePath = shapePath;
		this.property = property;
		this.stepIndex = stepIndex;
	}

	public MappedProperty(ShapePath shapePath, PropertyConstraint property) {
		this.shapePath = shapePath;
		this.property = property;
	}
	
	public String getTargetContext() {
		return shapePath.getPath();
	}
	
	public ShapePath getShapePath() {
		return shapePath;
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
		return shapePath.getShape();
	}

	public PropertyConstraint getProperty() {
		return property;
	}

	public int getStepIndex() {
		return stepIndex;
	}
	
	

	public ShapePath getTemplateShape() {
		return templateShape;
	}

	public void setTemplateShape(ShapePath templateShape) {
		this.templateShape = templateShape;
	}

	@Override
	public void print(PrettyPrintWriter out) {
		out.beginObject(this);
		if (property != null) {
			out.fieldName("property");
			out.print(property.getPredicate().stringValue());
		}
		out.field("shapePath", shapePath);
		out.field("stepIndex", stepIndex);
		out.field("template", template);
		out.field("templateShape", templateShape);
		out.endObject();
	}
	
	public boolean isDerivedProperty() {
		return property!=null && property.getFormula()!=null;
	}
	
}
