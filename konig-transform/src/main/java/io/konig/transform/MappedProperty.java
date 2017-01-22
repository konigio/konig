package io.konig.transform;

import io.konig.core.Path;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;

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
