package io.konig.transform.proto;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.konig.core.impl.RdfUtil;

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
	private static Logger logger = LoggerFactory.getLogger(ShapeModelMatchCount.class);
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
		for (PropertyModel p : model.allProperties()) {
			PropertyGroup group = p.getGroup();
			
			if (group.getTargetProperty() != null && group.getSourceProperty()==null) {
				if (logger.isDebugEnabled()) {
					logger.debug("matchCount: For {}, matched property {}", 
							RdfUtil.localName(model.getShape().getId()),
							RdfUtil.localName(p.getPredicate()));
				}
				ShapeModel valueModel = valueModel(p);
				if (valueModel == null) {
					propertyMatchCount++;
				} else {
					propertyMatchCount = valueModel(group.getTargetProperty()) == null ? 1 : matchCount(valueModel);
				}
				
			} else if (p instanceof DirectPropertyModel){

					
				logGroupMembers(model, group, p);
				
				
			}
		}
		return propertyMatchCount;
	}

	private void logGroupMembers(ShapeModel model, PropertyGroup group, PropertyModel p) {

		if (logger.isDebugEnabled()) {
			logger.debug("matchCount: For {}, unmatched property {}",
					RdfUtil.localName(model.getShape().getId()),
					RdfUtil.localName(p.getPredicate()));
			if (group.size()==1) {
				logger.debug("matchCount: No other group members found in ClassModel[{}]", group.getParentClassModel().hashCode());
			} else {
			
				for (PropertyModel other : group) {
					if (other == p) {
						continue;
					}
					
					logger.debug("matchCount:  other {} in {}", 
							other.getClass().getSimpleName(),
							RdfUtil.localName(other.getDeclaringShape().getShape().getId()));
				}
			}
		}
		
	}

	private ShapeModel valueModel(PropertyModel p) {
		ShapeModel valueModel = null;
		if (p instanceof BasicPropertyModel) {
			BasicPropertyModel b = (BasicPropertyModel) p;
			valueModel = b.getValueModel();
		}
		
		return valueModel;
	}

	@Override
	public int compareTo(ShapeModelMatchCount other) {
		
		return this.matchCount - other.matchCount;
	}
}
