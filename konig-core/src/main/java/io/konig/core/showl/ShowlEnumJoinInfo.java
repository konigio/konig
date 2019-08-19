package io.konig.core.showl;

import io.konig.core.vocab.Konig;

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


/**
 * Encapsulates some expression derived from source data that references a given property
 * on the enumeration class
 * @author Greg McFall
 *
 */
public class ShowlEnumJoinInfo {
	
	// The property on the target node that stores a member of the enumeration.
	private ShowlPropertyShape targetProperty;
	
	// The property on the enumeration class that is referenced by the source node.
	private ShowlPropertyShape enumProperty;
	
	// A property from the source node that references the enumProperty
	private ShowlPropertyShape sourceProperty;
	
	// A hard-coded reference to the enum member
	private ShowlEnumIndividualReference hardCodedReference;
	
	// An arbitrary expression for the enumProperty
	private ShowlExpression expression;
	
	private ShowlEnumJoinInfo(ShowlPropertyShape targetProperty, ShowlPropertyShape enumProperty, ShowlPropertyShape sourceProperty) {
		this.targetProperty = targetProperty;
		this.enumProperty = enumProperty;
		this.sourceProperty = sourceProperty;
	}
	
	private ShowlEnumJoinInfo(ShowlPropertyShape targetProperty, ShowlPropertyShape enumProperty, ShowlEnumIndividualReference hardCodedReference) {
		this.targetProperty = targetProperty;
		this.enumProperty = enumProperty;
		this.hardCodedReference = hardCodedReference;
	}

	private ShowlEnumJoinInfo(ShowlPropertyShape targetProperty, ShowlPropertyShape enumProperty,
			ShowlExpression expression) {
		this.targetProperty = targetProperty;
		this.enumProperty = enumProperty;
		this.expression = expression;
	}

	public ShowlExpression getExpression() {
		return expression;
	}

	public ShowlPropertyShape getTargetProperty() {
		return targetProperty;
	}

	public ShowlEnumIndividualReference getHardCodedReference() {
		return hardCodedReference;
	}

	public ShowlPropertyShape getEnumProperty() {
		return enumProperty;
	}
	
	public ShowlPropertyShape getSourceProperty() {
		return sourceProperty;
	}

	public static ShowlEnumJoinInfo forEnumProperty(ShowlPropertyShape targetProperty) throws ShowlProcessingException {
		ShowlExpression e = targetProperty.getSelectedExpression();
		if (e instanceof ShowlEnumNodeExpression) {
			ShowlEnumNodeExpression enumNodeExpr = (ShowlEnumNodeExpression)targetProperty.getSelectedExpression();
			ShowlNodeShape enumNode = enumNodeExpr.getEnumNode();

			ShowlChannel channel1 = enumNodeExpr.getChannel();
			ShowlChannel channel = ShowlUtil.channelFor(enumNode, targetProperty.getRootNode().getChannels());
			if (channel1==null || channel1!=channel) {
				System.out.print("yikes!");
			}
			if (channel != null) {
				if (channel.getJoinStatement() instanceof ShowlEqualStatement) {
					ShowlEqualStatement equal = (ShowlEqualStatement) channel.getJoinStatement();
					ShowlPropertyShape enumProperty = ShowlUtil.propertyOf(equal, enumNode);
					ShowlPropertyShape sourceProperty = ShowlUtil.otherProperty(equal, enumNode);
					
					if (enumProperty != null) {
						
						ShowlPropertyShape targetJoinProperty = ShowlUtil.propertyMappedTo(targetProperty.getValueShape(), sourceProperty);
					
						if (sourceProperty != null) {
							if (targetJoinProperty == null && enumProperty.getPredicate().equals(Konig.id)) {
								targetJoinProperty = targetProperty;
							}
							return new ShowlEnumJoinInfo(targetJoinProperty, enumProperty, sourceProperty);
						}
						
						
						if (equal.getLeft() instanceof ShowlEnumIndividualReference) {
							return new ShowlEnumJoinInfo(targetJoinProperty, enumProperty, (ShowlEnumIndividualReference) equal.getLeft());
						}
						
						if (equal.getRight() instanceof ShowlEnumIndividualReference) {
							return new ShowlEnumJoinInfo(targetJoinProperty, enumProperty, (ShowlEnumIndividualReference)equal.getRight());
						}
						
						// This code is getting complex.  We probably ought to take a step back and think about refactoring to 
						// simplify.  But for now, let's just add to the complexity (sigh!)
						
						ShowlExpression enumExpression = ShowlUtil.enumExpression(equal);
						if (enumExpression != null) {
							ShowlExpression other = equal.otherExpression(enumExpression);
							if (other != null) {
								return new ShowlEnumJoinInfo(targetJoinProperty, enumProperty, other);
							}
						}
					}
				}
			}
			
			throw new ShowlProcessingException("Failed to create ShowlEnumJoinInfo for " + targetProperty.getPath());
		}
		
		return null;
	}

}
