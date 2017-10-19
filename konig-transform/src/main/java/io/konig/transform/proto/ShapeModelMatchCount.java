package io.konig.transform.proto;

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


public class ShapeModelMatchCount implements Comparable<ShapeModelMatchCount>{
	
	private ShapeModel shapeModel;
	private int matchCount;
	
	public ShapeModelMatchCount(ShapeModel shapeModel) {
		this.shapeModel = shapeModel;
		this.matchCount = matchCount(shapeModel);
	}

	public int getMatchCount() {
		return matchCount;
	}

	public ShapeModel getShapeModel() {
		return shapeModel;
	}
	
	public void updateMatchCount() {
		matchCount = matchCount(shapeModel);
	}
	
	private int matchCount(ShapeModel model) {
		int propertyMatchCount = 0;
		for (PropertyModel p : model.getProperties()) {
			PropertyGroup group = p.getGroup();
			if (group.getTargetProperty() != null && group.getSourceProperty()==null) {
				
				ShapeModel valueModel = p.getValueModel();
				propertyMatchCount += valueModel==null ? 1 : matchCount(valueModel);
			}
		}
		return propertyMatchCount;
	}

	@Override
	public int compareTo(ShapeModelMatchCount other) {
		
		return this.matchCount - other.matchCount;
	}
}
