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


import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.LiteralImpl;

import io.konig.core.vocab.Konig;
import io.konig.formula.QuantifiedExpression;
import io.konig.shacl.Shape;
import io.konig.transform.ShapeTransformException;
import io.konig.transform.rule.AlphabeticVariableNamer;
import io.konig.transform.rule.BooleanExpression;
import io.konig.transform.rule.ChannelProperty;
import io.konig.transform.rule.ContainerPropertyRule;
import io.konig.transform.rule.CopyIdRule;
import io.konig.transform.rule.DataChannel;
import io.konig.transform.rule.ExactMatchPropertyRule;
import io.konig.transform.rule.FormulaIdRule;
import io.konig.transform.rule.FormulaPropertyRule;
import io.konig.transform.rule.FromItem;
import io.konig.transform.rule.IdPropertyRule;
import io.konig.transform.rule.IdRule;
import io.konig.transform.rule.InjectLiteralPropertyRule;
import io.konig.transform.rule.IriTemplateIdRule;
import io.konig.transform.rule.JoinRule;
import io.konig.transform.rule.LiteralPropertyRule;
import io.konig.transform.rule.NullPropertyRule;
import io.konig.transform.rule.PropertyComparison;
import io.konig.transform.rule.PropertyRule;
import io.konig.transform.rule.RenamePropertyRule;
import io.konig.transform.rule.ShapeRule;
import io.konig.transform.rule.TransformBinaryOperator;
import io.konig.transform.rule.VariableNamer;

public class ShapeModelToShapeRule {
	
	private boolean failIfPropertyNotMapped=true;
	
	public ShapeModelToShapeRule() {
		
	}
	

	public boolean isFailIfPropertyNotMapped() {
		return failIfPropertyNotMapped;
	}


	public void setFailIfPropertyNotMapped(boolean failIfPropertyNotMapped) {
		this.failIfPropertyNotMapped = failIfPropertyNotMapped;
	}


	public ShapeRule toShapeRule(ShapeModel shapeModel) throws ShapeTransformException {
		Worker worker = new Worker();
		return worker.buildShapeRule(shapeModel);
	}
	
	
	private class Worker {
		private VariableNamer variableNamer;
		
		
		private Worker() throws ShapeTransformException {
			variableNamer = new AlphabeticVariableNamer();
		}

		private ShapeRule buildShapeRule(ShapeModel shapeModel) throws ShapeTransformException {

			buildDataChannels(shapeModel);
			
			ShapeRule shapeRule = toShapeRule(shapeModel);
			shapeRule.setFromItem(fromItem(shapeModel.getClassModel().getFromItem()));
			
			
			return shapeRule;
		}
		
		private void addPropertyRules(ShapeModel shapeModel, ShapeRule shapeRule) throws ShapeTransformException {
			ClassModel classModel = shapeModel.getClassModel();
			for (PropertyGroup group : classModel.getPropertyGroups()) {
				
				
				PropertyModel targetProperty = group.getTargetProperty();
				if (targetProperty==null) {
					continue;
				}
				if (targetProperty.getDeclaringShape() == shapeModel) {
					if (Konig.id.equals(targetProperty.getPredicate())) {
						// TODO: add IdRule
						continue;
					}
					PropertyRule propertyRule = propertyRule(group);
					shapeRule.addPropertyRule(propertyRule);
				}
				
			}
		}

		private PropertyRule propertyRule(PropertyGroup group) throws ShapeTransformException {
			PropertyModel targetProperty = group.getTargetProperty();
			URI targetPredicate = targetProperty.getPredicate();
			ShapeModel valueModel = targetProperty.getValueModel();
			if (valueModel != null) {
				ShapeRule valueRule = toShapeRule(valueModel);
				
				ContainerPropertyRule result = new ContainerPropertyRule(targetPredicate);
				result.setNestedRule(valueRule);
				return result;
			}
			
			PropertyModel sourceProperty = group.getSourceProperty();
			
			if (sourceProperty instanceof FixedPropertyModel) {
				FixedPropertyModel fixed = (FixedPropertyModel) sourceProperty;
				Value value = fixed.getValue();
				
				if (value instanceof Literal) {
					Literal literal = (Literal) value;
					return new LiteralPropertyRule(null, fixed.getPredicate(), literal);
				}
				
				
			}
			
			if (sourceProperty instanceof DerivedPropertyModel) {
				return derivedProperty((DerivedPropertyModel)sourceProperty);
			}
			
			if (Konig.modified.equals(targetPredicate)) {
				return new InjectLiteralPropertyRule(null, Konig.modified, new LiteralImpl("{modified}"));
			}
			
			if (sourceProperty == null) {
				if (failIfPropertyNotMapped) {
					throw new ShapeTransformException("Property is not mapped: " + targetProperty.simplePath());
				}
				return new NullPropertyRule(null, group.getTargetProperty().getPredicate());
			}
			
			URI sourcePredicate = sourceProperty.getPredicate();
			
			DirectPropertyModel targetDirect = targetProperty instanceof DirectPropertyModel ? 
					(DirectPropertyModel) targetProperty : null;
			
			DirectPropertyModel sourceDirect = sourceProperty instanceof DirectPropertyModel ? 
					(DirectPropertyModel) sourceProperty : null;
					
			StepPropertyModel sourceStep = sourceProperty instanceof StepPropertyModel ?
					(StepPropertyModel) sourceProperty : null;
					
			DataChannel channel = channel(sourceProperty);
			
			if (targetPredicate.equals(sourcePredicate) && targetDirect!=null && sourceDirect!=null) {
				return new ExactMatchPropertyRule(channel, targetPredicate);
				
			} else if (sourceStep!=null) {
				return new RenamePropertyRule(
					targetPredicate, channel, sourceStep.getPropertyConstraint(), sourceStep.getStepIndex());
			} else if (Konig.id.equals(sourcePredicate)) {
				return new IdPropertyRule(targetPredicate, channel);
			}
			
			
			
			throw new ShapeTransformException("Unable to create rule for property: " + targetProperty.simplePath());
			
		}

		

		private FormulaPropertyRule derivedProperty(DerivedPropertyModel sourceProperty) throws ShapeTransformException {
			ShapeModel shape = sourceProperty.getDeclaringShape();
		
			DataChannel channel = shape==null ? null : shape.getDataChannel();
			PropertyModel targetPropertyModel = sourceProperty.getGroup().getTargetProperty();
			if (targetPropertyModel instanceof BasicPropertyModel) {
				BasicPropertyModel basicTarget = (BasicPropertyModel) targetPropertyModel;
				return new FormulaPropertyRule(channel, 
						basicTarget.getPropertyConstraint(), 
						sourceProperty.getPropertyConstraint());
			}
			
			throw new ShapeTransformException("Unable to create FormulaPropertyRule: " + sourceProperty.simplePath());
		}

		private DataChannel channel(PropertyModel sourceProperty) {
			DataChannel result = null;
			if (sourceProperty != null) {
				ShapeModel shapeModel = sourceProperty.getDeclaringShape();
				if (shapeModel != null) {
					result = shapeModel.getDataChannel();
				}
			}
			
			return result;
		}

		private FromItem fromItem(ProtoFromItem fromItem) throws ShapeTransformException {
			FromItem result = null;
			if (fromItem instanceof ShapeModel) {
				result = ((ShapeModel)fromItem).getDataChannel();
			} else if (fromItem instanceof ProtoJoinExpression) {
				ProtoJoinExpression proto = (ProtoJoinExpression) fromItem;
				FromItem left = fromItem(proto.getLeft());
				FromItem right = fromItem(proto.getRight());
				BooleanExpression condition = booleanExpression(proto.getCondition());
				result = new JoinRule(left, right, condition);
			}
			if (result == null) {
				throw new ShapeTransformException("Failed to create fromItem");
			}
			return result;
		}

		private BooleanExpression booleanExpression(ProtoBooleanExpression condition) throws ShapeTransformException {
			BooleanExpression result = null;
			if (condition instanceof ProtoBinaryBooleanExpression) {
				ProtoBinaryBooleanExpression proto = (ProtoBinaryBooleanExpression) condition;
				
				TransformBinaryOperator operator = proto.getOperator();
				URI leftPredicate = proto.getLeft().getPredicate();
				URI rightPredicate = proto.getRight().getPredicate();
				
				ChannelProperty left = new ChannelProperty(proto.getLeft().getDeclaringShape().getDataChannel(), leftPredicate);
				ChannelProperty right = new ChannelProperty(proto.getRight().getDeclaringShape().getDataChannel(), rightPredicate);
				
				result = new PropertyComparison(operator, left, right);
			}
			if (result == null) {
				throw new ShapeTransformException("Failed to create boolean expression");
			}
			return result;
		}

		void buildDataChannels(ShapeModel root) throws ShapeTransformException {
			ProtoFromItem protoFromItem = root.getClassModel().getFromItem();
			if (protoFromItem == null) {
				throw new ShapeTransformException("FromItem not defined for shape: " + root.getShape().getId().stringValue());
			}
			setDataChannelName(protoFromItem);
			
		}

		

		private void setDataChannelName(ProtoFromItem item) throws ShapeTransformException {
			
			ShapeModel shapeModel = item.first();
			shapeModel.getDataChannel().setName(variableNamer.next());
			
			ProtoFromItem rest = item.rest();
			if (rest != null) {
				setDataChannelName(rest);
			}
			
		}

		public ShapeRule toShapeRule(ShapeModel shapeModel) throws ShapeTransformException {
			ShapeRule shapeRule = new ShapeRule(shapeModel.getShape());
			shapeRule.setVariableNamer(variableNamer);
			
			addIdRule(shapeModel, shapeRule);

			addPropertyRules(shapeModel, shapeRule);
			
			return shapeRule;
		}
		private void addIdRule(ShapeModel shapeModel, ShapeRule shapeRule) throws ShapeTransformException {
			
			PropertyModel p = shapeModel.getPropertyByPredicate(Konig.id);
			if (p != null) {

				IdRule idRule = null;
				PropertyModel sourceProperty = p.getGroup().getSourceProperty();
				if (sourceProperty != null) {
					ShapeModel sourceShapeModel = sourceProperty.getDeclaringShape();
					DataChannel channel = sourceShapeModel.getDataChannel();
					
					Shape sourceShape = sourceShapeModel.getShape();
					
					if (sourceShape.getIriFormula() != null) {
						idRule = new FormulaIdRule();
					} else if (sourceShape.getIriTemplate() != null) {
						idRule = new IriTemplateIdRule(channel);
					} else {
						idRule = new CopyIdRule(channel);
					}
				}
				
			
				if (idRule == null) {
					Shape targetShape = shapeModel.getShape();
					QuantifiedExpression formula = targetShape.getIriFormula();
					if (formula != null) {
						idRule = new FormulaIdRule();
					}
				}
				
				if (idRule!= null) {
					shapeRule.setIdRule(idRule);
				} else {
					throw new ShapeTransformException("idRule not found for ShapeRule: " + shapeModel.getShape().getId());
				}
				
				
				
				
				
			}
			
			
		}

	}

	

}
