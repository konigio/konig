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


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.konig.core.impl.RdfUtil;

public class SourceShapeInfo implements Comparable<SourceShapeInfo> {
	private static final Logger logger = LoggerFactory.getLogger(SourceShapeInfo.class);
	
	private ShapeModel sourceShape;
	private int matchCount;
	
	private MatchCounter counter = new MatchCounter();
	

	public SourceShapeInfo(ShapeModel sourceShape) {
		this.sourceShape = sourceShape;
		sourceShape.setSourceShapeInfo(this);
	}

	public PropertyModel getTargetProperty() {
		return null;
	}

	public ShapeModel getSourceShape() {
		return sourceShape;
	}
	
	
	public int getMatchCount() {
		return matchCount;
	}

	public int computeMatchCount() {
		matchCount = 0;
		dispatch(counter);
		return matchCount;
	}


	@Override
	public int compareTo(SourceShapeInfo other) {
		int result = 0;
		if (other != this) {
			result = other.matchCount - this.matchCount;
			if (result == 0) {
				result = other.getSourceShape().getShape().getId().stringValue().compareTo(
						sourceShape.getShape().getId().stringValue());
			}
		}
		return result;
	}
	
	public void dispatch(MatchVisitor visitor) {
		
		for (PropertyModel sourceProperty : sourceShape.getProperties()) {
			if (sourceProperty instanceof DirectPropertyModel) {
				DirectPropertyModel sourceDirect = (DirectPropertyModel) sourceProperty;
				
				if (sourceDirect.getValueModel() != null) {
					visitor.handleValueModel(sourceDirect.getValueModel());
				} else {
				
					PropertyGroup group = sourceProperty.getGroup();
					PropertyModel targetProperty = group.getTargetProperty();
					DirectPropertyModel targetDirect = null;
					if (targetProperty != null) {
						if (targetProperty instanceof DirectPropertyModel && targetProperty.getGroup().getSourceProperty()==null) {
							targetDirect = (DirectPropertyModel) targetProperty;
						} else if (targetProperty instanceof StepPropertyModel) {
							StepPropertyModel targetStep = (StepPropertyModel) targetProperty;
							if (targetStep.getDeclaringProperty().getGroup().getSourceProperty()==null) {
								targetDirect = targetStep.getDeclaringProperty();
							}
						}
					}
					
					StepPropertyModel sourceStep = sourceDirect.getPathTail();
					if (sourceStep != null) {
						group = sourceStep.getGroup();
						targetProperty = group.getTargetProperty();
						if (targetProperty != null) {
							if (targetProperty instanceof DirectPropertyModel && targetProperty.getGroup().getSourceProperty()==null) {
								targetDirect = (DirectPropertyModel) targetProperty;
							} else if (targetProperty instanceof StepPropertyModel) {
								StepPropertyModel targetStep = (StepPropertyModel) targetProperty;
								if (targetStep.getDeclaringProperty().getGroup().getSourceProperty()==null) {
									targetDirect = targetStep.getDeclaringProperty();
								}
							}
						}
					}
					if (targetDirect == null) {
						visitor.noMatch(sourceDirect);
					} else {
						visitor.match(sourceProperty, targetDirect);
					}
				}
			}
		}
	}
	
	private class MatchCounter implements MatchVisitor {

		@Override
		public void match(PropertyModel sourceProperty, DirectPropertyModel targetProperty) {
			matchCount++;
			
		}
		
		@Override
		public void handleValueModel(ShapeModel sourceShapeModel) {
			matchCount += sourceShapeModel.getSourceShapeInfo().computeMatchCount();
		}

		@Override
		public void noMatch(DirectPropertyModel sourceProperty) {

			if (logger.isDebugEnabled()) {
				logger.debug("computeMatchCount: {}#{} in group[{}] does not match any target property",
	
						RdfUtil.localName(sourceProperty.getDeclaringShape().getShape().getId()), 
						sourceProperty.getPredicate().getLocalName(),
						sourceProperty.getGroup().hashCode());
			}
			
		}

		
	}
	

}
