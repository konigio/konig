package io.konig.transform.proto;

import java.util.ArrayList;

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

import java.util.List;

import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.LiteralImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.konig.core.vocab.Konig;
import io.konig.formula.BareExpression;
import io.konig.formula.BinaryRelationalExpression;
import io.konig.formula.ConditionalAndExpression;
import io.konig.formula.ConditionalOrExpression;
import io.konig.formula.Direction;
import io.konig.formula.DirectionStep;
import io.konig.formula.Formula;
import io.konig.formula.GeneralAdditiveExpression;
import io.konig.formula.MultiplicativeExpression;
import io.konig.formula.PathExpression;
import io.konig.formula.PathStep;
import io.konig.formula.PathTerm;
import io.konig.formula.PrimaryExpression;
import io.konig.formula.QuantifiedExpression;
import io.konig.formula.UnaryExpression;
import io.konig.formula.VariableTerm;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;
import io.konig.sql.query.ColumnExpression;
import io.konig.sql.query.GroupingElement;
import io.konig.transform.ShapeTransformException;
import io.konig.transform.TransformProcessor;
import io.konig.transform.rule.AlphabeticVariableNamer;
import io.konig.transform.rule.BooleanExpression;
import io.konig.transform.rule.ChannelProperty;
import io.konig.transform.rule.ContainerPropertyRule;
import io.konig.transform.rule.CopyIdRule;
import io.konig.transform.rule.DataChannel;
import io.konig.transform.rule.ExactMatchPropertyRule;
import io.konig.transform.rule.FixedValuePropertyRule;
import io.konig.transform.rule.FormulaIdRule;
import io.konig.transform.rule.FormulaPropertyRule;
import io.konig.transform.rule.FromItem;
import io.konig.transform.rule.IdPropertyRule;
import io.konig.transform.rule.IdRule;
import io.konig.transform.rule.InjectLiteralPropertyRule;
import io.konig.transform.rule.IriTemplateIdRule;
import io.konig.transform.rule.JoinRule;
import io.konig.transform.rule.NullPropertyRule;
import io.konig.transform.rule.PropertyComparison;
import io.konig.transform.rule.PropertyRule;
import io.konig.transform.rule.RenamePropertyRule;
import io.konig.transform.rule.ShapeRule;
import io.konig.transform.rule.TransformBinaryOperator;
import io.konig.transform.rule.TransformPostProcessor;
import io.konig.transform.rule.VariableNamer;

public class ShapeModelToShapeRule {
	
	private static final Logger logger = LoggerFactory.getLogger(ShapeModelToShapeRule.class);
	
	private boolean failIfPropertyNotMapped=true;
	private List<TransformPostProcessor> listTransformprocess;
	public List<TransformPostProcessor> getListTransformprocess() {
		if(listTransformprocess==null)
		{
			listTransformprocess=new ArrayList<>();	
		}
		return listTransformprocess;
	}


	public void addListTransformprocess(TransformPostProcessor transformprocess) {
		this.listTransformprocess.add(transformprocess);
	}


	public void setListTransformprocess(List<TransformPostProcessor> listTransformprocess) {
		this.listTransformprocess = listTransformprocess;
	}


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
		private boolean useAlias = false;
		
		
		private Worker() throws ShapeTransformException {
			variableNamer = new AlphabeticVariableNamer();
		}

		private ShapeRule buildShapeRule(ShapeModel shapeModel) throws ShapeTransformException {

			if (logger.isDebugEnabled()) {
				logger.debug("BEGIN buildShapeRule(shape.id=<{}>", shapeModel.getShape().getId());
			}
			buildDataChannels(shapeModel);
			
			ShapeRule shapeRule = toShapeRule(shapeModel);
			shapeRule.setFromItem(fromItem(shapeModel.getClassModel().getFromItem()));
			invokePostProcessors(shapeModel, shapeRule);
			
			return shapeRule;
		}
		
		private void invokePostProcessors(ShapeModel shapeModel, ShapeRule shapeRule) throws ShapeTransformException {

			for (TransformPostProcessor processor : shapeModel.getPostProcessorList()) {
				processor.process(shapeRule);
			}
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
					propertyRule.setSourcePropertyModel(group.getSourceProperty());
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
				
				return new FixedValuePropertyRule(null, fixed.getPredicate(), value);
			}
			
			if (sourceProperty instanceof DerivedPropertyModel) {

				if (targetProperty != sourceProperty) {
					if (targetPredicate.equals(sourceProperty.getPredicate())) {
						DataChannel channel = sourceProperty.getDeclaringShape().getDataChannel();
						DerivedPropertyModel derived = (DerivedPropertyModel) sourceProperty;
						
						if (channel == null  && outPathFormula(derived)) {
							return new ExactMatchPropertyRule(sourceProperty.getDeclaringShape().getDataChannel(), targetPredicate);
						}
					}
				}

				

				
				return derivedProperty((DerivedPropertyModel)sourceProperty);
			}
			
			if (sourceProperty instanceof FormulaPropertyModel) {
				FormulaPropertyModel formulaModel = (FormulaPropertyModel) sourceProperty;
				DirectPropertyModel direct = (DirectPropertyModel) group.getTargetProperty();
				PropertyConstraint targetConstraint = direct.getPropertyConstraint();
				PropertyConstraint sourceConstraint = new PropertyConstraint(direct.getPredicate());
				sourceConstraint.setMinCount(targetConstraint.getMinCount());
				sourceConstraint.setMaxCount(targetConstraint.getMaxCount());
				sourceConstraint.setFormula(quantifiedExpression(formulaModel.getFormula()));
				
				return new FormulaPropertyRule(null, targetConstraint, sourceConstraint);
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

		

		private QuantifiedExpression quantifiedExpression(Formula formula) throws ShapeTransformException {
			
			if (!(formula instanceof PrimaryExpression)) {
				// TODO: handle this case!
				throw new ShapeTransformException("Expected formula to be of type PrimaryExpression");
			}

			UnaryExpression unary = new UnaryExpression((PrimaryExpression) formula);
			MultiplicativeExpression mult = new MultiplicativeExpression(unary);
			GeneralAdditiveExpression add = new GeneralAdditiveExpression(mult);
			BinaryRelationalExpression binary = new BinaryRelationalExpression(null, add, null);
			ConditionalAndExpression and = new ConditionalAndExpression();
			and.add(binary);
			ConditionalOrExpression or = new ConditionalOrExpression();
			or.add(and);
			BareExpression bare = new BareExpression(or);
			QuantifiedExpression q = new QuantifiedExpression(bare, null);
			
			return q;
		}

		private boolean outPathFormula(DerivedPropertyModel sourceProperty) {
			QuantifiedExpression formula = sourceProperty.getPropertyConstraint().getFormula();
			PrimaryExpression primary = formula.asPrimaryExpression();
			if (primary instanceof PathExpression) {
				PathExpression path = (PathExpression) primary;
				PathStep step = path.getStepList().get(0);
				if (step instanceof DirectionStep) {
					DirectionStep dirStep = (DirectionStep) step;
					if (dirStep.getDirection() == Direction.OUT) {
						PathTerm term = dirStep.getTerm();
						if (!(term instanceof VariableTerm)) {
							return true;
						}
						
					}
				}
				
			}
			return false;
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
			return result;
		}

		void buildDataChannels(ShapeModel root) throws ShapeTransformException {
			ProtoFromItem protoFromItem = root.getClassModel().getFromItem();
			if (protoFromItem == null) {
				throw new ShapeTransformException("FromItem not defined for shape: " + root.getShape().getId().stringValue());
			}
			setDataChannelName(protoFromItem);
			
			useAlias = protoFromItem instanceof ProtoJoinExpression;
			
		}

		

		private void setDataChannelName(ProtoFromItem item) throws ShapeTransformException {
			if (item instanceof ShapeModel) {
				ShapeModel shapeModel = (ShapeModel) item;
				shapeModel.getDataChannel().setName(variableNamer.next());
			} else if (item instanceof ProtoJoinExpression) {
				ProtoJoinExpression join = (ProtoJoinExpression) item;
				setDataChannelName(join.getLeft());
				setDataChannelName(join.getRight());
			}
			
		}

		public ShapeRule toShapeRule(ShapeModel shapeModel) throws ShapeTransformException {
			ShapeRule shapeRule = new ShapeRule(shapeModel);
			shapeRule.setVariableNamer(variableNamer);
			
			addIdRule(shapeModel, shapeRule);

			addPropertyRules(shapeModel, shapeRule);
			addGroupBy(shapeModel, shapeRule);
			
			return shapeRule;
		}
		private void addGroupBy(ShapeModel shapeModel, ShapeRule shapeRule) throws ShapeTransformException {
			for (GroupByItem item : shapeModel.getGroupBy()) {
				GroupingElement element = groupingElement(shapeModel, shapeRule, item);
				shapeRule.addGroupingElement(element);
			}
			
		}

		private GroupingElement groupingElement(ShapeModel shapeModel, ShapeRule shapeRule, GroupByItem item) throws ShapeTransformException {
			if (item instanceof PropertyModel) {
				return column((PropertyModel)item);
			}
			throw new ShapeTransformException("GroupByItem type not supported: " + item.getClass().getSimpleName());
		}


		private GroupingElement column(PropertyModel p) throws ShapeTransformException {

			if (logger.isDebugEnabled()) {
				logger.debug("column({})", p.simplePath());
			}
			p = p.getGroup().getSourceProperty();
			
			List<PropertyModel> path = p.path();

			DataChannel channel = p.getDeclaringShape().getDataChannel();
			StringBuilder builder = new StringBuilder();
			if (useAlias && channel!=null) {
				
				builder.append(channel.getName());
				builder.append('.');
			}
			
			String dot = "";
			for (PropertyModel property : path) {
				builder.append(dot);
				dot = ".";
				URI predicate = property.getPredicate();
				builder.append(predicate.getLocalName());
			}
			return new ColumnExpression(builder.toString());
				
			
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
						idRule = formulaIdRule(p);
					} else if (sourceShape.getIriTemplate() != null) {
						idRule = new IriTemplateIdRule(sourceShape, channel);
					} else {
						idRule = new CopyIdRule(channel);
					}
				}
				
			
				if (idRule == null) {
					Shape targetShape = shapeModel.getShape();
					QuantifiedExpression formula = targetShape.getIriFormula();
					if (formula != null) {
						idRule = formulaIdRule(p);
					}
				}
				
				if (idRule!= null) {
					shapeRule.setIdRule(idRule);
				} else {
					throw new ShapeTransformException("idRule not found for ShapeRule: " + shapeModel.getShape().getId());
				}
				
				
				
				
				
			}
			
			
		}

		private FormulaIdRule formulaIdRule(PropertyModel p) {
			if (p.isTargetProperty()) {
				p = p.getGroup().getSourceProperty();
			}
			return new FormulaIdRule(p);
		}

	}

	

}
