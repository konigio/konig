package io.konig.transform;

/*
 * #%L
 * Konig Transform
 * %%
 * Copyright (C) 2015 - 2017 Gregory McFall
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


import io.konig.shacl.Shape;

/**
 * A structure which prescribes a method for generating an IRI for instances of some target shape.
 * MappedId objects are contained within a {@link TransformFrame} which specifies the target shape.
 * @author Greg McFall
 *
 */
public class MappedId {
	private ShapePath sourceShape;
	private IriTemplateInfo templateInfo;
	public MappedId(ShapePath sourceShape, IriTemplateInfo templateInfo) {
		this.sourceShape = sourceShape;
		this.templateInfo = templateInfo;
	}
	
	/**
	 * Get the source shape from which an IRI may be generated.
	 */
	public Shape getSourceShape() {
		return sourceShape.getShape();
	}
	
	public ShapePath getShapePath() {
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
