package io.konig.transform.proto;

/*
 * #%L
 * Konig Transform
 * %%
 * Copyright (C) 2015 - 2018 Gregory McFall
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


/**
 * Information about a SourceShape that connects to an inverse step in the formula for a 
 * given target property.
 * 
 * @author Greg McFall
 *
 */
public class InverseSourceShapeInfo extends SourceShapeInfo {
	
	private PropertyModel targetProperty;

	public InverseSourceShapeInfo(ShapeModel sourceShape, PropertyModel targetProperty) {
		super(sourceShape);
		this.targetProperty = targetProperty;
	}


	
}
