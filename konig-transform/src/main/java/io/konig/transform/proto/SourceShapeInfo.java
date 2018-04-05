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
		for (PropertyModel sourceProperty : sourceShape.getProperties()) {
			PropertyGroup group = sourceProperty.getGroup();
			
			if (group.getTargetProperty() != null && group.getSourceProperty()==null) {
				ShapeModel valueModel = sourceProperty.getValueModel();
				if (valueModel != null) {
					matchCount += valueModel.getSourceShapeInfo().computeMatchCount();
				} else {
					matchCount++;
				}
				
			} else if (logger.isDebugEnabled()) {
				if (group.getTargetProperty() == null) {
					logger.debug("computeMatchCount: {}#{} in group[{}] does not match any target property",

							RdfUtil.localName(sourceProperty.getDeclaringShape().getShape().getId()), 
							sourceProperty.getPredicate().getLocalName(),
							group.hashCode());
				} 
			}
		}
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
	

}
