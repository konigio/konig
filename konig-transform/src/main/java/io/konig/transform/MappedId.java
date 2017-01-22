package io.konig.transform;

import io.konig.shacl.Shape;

public class MappedId {
	private Shape sourceShape;
	private IriTemplateInfo templateInfo;
	public MappedId(Shape sourceShape, IriTemplateInfo templateInfo) {
		this.sourceShape = sourceShape;
		this.templateInfo = templateInfo;
	}
	public Shape getSourceShape() {
		return sourceShape;
	}
	public IriTemplateInfo getTemplateInfo() {
		return templateInfo;
	}
	
	

}
