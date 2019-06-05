package io.konig.core.showl;

/*
 * #%L
 * Konig Core
 * %%
 * Copyright (C) 2015 - 2019 Gregory McFall
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


public class ShowlEnumJoinInfo {
	
	private ShowlPropertyShape targetProperty;
	private ShowlPropertyShape enumProperty;
	private ShowlPropertyShape sourceProperty;
	private ShowlEnumIndivdiualReference hardCodedReference;
	
	private ShowlEnumJoinInfo(ShowlPropertyShape targetProperty, ShowlPropertyShape enumProperty, ShowlPropertyShape sourceProperty) {
		this.targetProperty = targetProperty;
		this.enumProperty = enumProperty;
		this.sourceProperty = sourceProperty;
	}
	
	private ShowlEnumJoinInfo(ShowlPropertyShape targetProperty, ShowlPropertyShape enumProperty, ShowlEnumIndivdiualReference hardCodedReference) {
		this.targetProperty = targetProperty;
		this.enumProperty = enumProperty;
		this.hardCodedReference = hardCodedReference;
	}

	public ShowlPropertyShape getTargetProperty() {
		return targetProperty;
	}

	public ShowlEnumIndivdiualReference getHardCodedReference() {
		return hardCodedReference;
	}

	public ShowlPropertyShape getEnumProperty() {
		return enumProperty;
	}
	
	public ShowlPropertyShape getSourceProperty() {
		return sourceProperty;
	}

	public static ShowlEnumJoinInfo forEnumProperty(ShowlPropertyShape targetProperty) throws ShowlProcessingException {
		if (targetProperty.getSelectedExpression() instanceof ShowlEnumNodeExpression) {
			ShowlNodeShape enumNode = ((ShowlEnumNodeExpression)targetProperty.getSelectedExpression()).getEnumNode();
			ShowlChannel channel = ShowlUtil.channelFor(enumNode, targetProperty.getRootNode().getChannels());
			if (channel != null) {
				if (channel.getJoinStatement() instanceof ShowlEqualStatement) {
					ShowlEqualStatement equal = (ShowlEqualStatement) channel.getJoinStatement();
					ShowlPropertyShape enumProperty = ShowlUtil.propertyOf(equal, enumNode);
					ShowlPropertyShape sourceProperty = ShowlUtil.otherProperty(equal, enumNode);
					
					if (enumProperty != null) {
						
						ShowlPropertyShape targetJoinProperty = ShowlUtil.propertyMappedTo(targetProperty.getValueShape(), sourceProperty);
					
						if (sourceProperty != null) {
							return new ShowlEnumJoinInfo(targetJoinProperty, enumProperty, sourceProperty);
						}
						
						
						if (equal.getLeft() instanceof ShowlEnumIndivdiualReference) {
							return new ShowlEnumJoinInfo(targetJoinProperty, enumProperty, (ShowlEnumIndivdiualReference) equal.getLeft());
						}
						
						if (equal.getRight() instanceof ShowlEnumIndivdiualReference) {
							return new ShowlEnumJoinInfo(targetJoinProperty, enumProperty, (ShowlEnumIndivdiualReference)equal.getRight());
						}
					}
				}
			}
			
			throw new ShowlProcessingException("Failed to create ShowlEnumJoinInfo for " + targetProperty.getPath());
		}
		
		return null;
	}

}
