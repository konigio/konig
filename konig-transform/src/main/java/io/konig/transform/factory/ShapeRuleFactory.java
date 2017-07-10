package io.konig.transform.factory;

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


import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.RDF;

import io.konig.core.Context;
import io.konig.core.OwlReasoner;
import io.konig.core.Path;
import io.konig.core.Vertex;
import io.konig.core.util.IriTemplate;
import io.konig.core.util.TurtleElements;
import io.konig.core.util.ValueFormat;
import io.konig.core.util.ValueFormat.Element;
import io.konig.core.vocab.Konig;
import io.konig.shacl.NodeKind;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeManager;
import io.konig.transform.rule.AlphabeticVariableNamer;
import io.konig.transform.rule.BinaryBooleanExpression;
import io.konig.transform.rule.BooleanExpression;
import io.konig.transform.rule.ContainerPropertyRule;
import io.konig.transform.rule.CopyIdRule;
import io.konig.transform.rule.DataChannel;
import io.konig.transform.rule.ExactMatchPropertyRule;
import io.konig.transform.rule.FormulaPropertyRule;
import io.konig.transform.rule.InjectLiteralPropertyRule;
import io.konig.transform.rule.IriTemplateIdRule;
import io.konig.transform.rule.LiteralPropertyRule;
import io.konig.transform.rule.MapValueTransform;
import io.konig.transform.rule.NullPropertyRule;
import io.konig.transform.rule.PropertyRule;
import io.konig.transform.rule.RenamePropertyRule;
import io.konig.transform.rule.ShapeRule;
import io.konig.transform.rule.TransformBinaryOperator;

public class ShapeRuleFactory {

	private ShapeManager shapeManager;
	private TransformStrategy strategy;
	private OwlReasoner owlReasoner;

	public ShapeRuleFactory(ShapeManager shapeManager, OwlReasoner owlReasoner) {
		this(shapeManager, owlReasoner, new SameShapeTransformStrategy());
	}
	


	public ShapeRuleFactory(ShapeManager shapeManager, OwlReasoner owlReasoner, TransformStrategy strategy) {
		this.shapeManager = shapeManager;
		this.owlReasoner = owlReasoner;
		this.strategy = strategy;
		if (strategy != null) {
			strategy.init(this);
		}
	}

	public TransformStrategy getStrategy() {
		return strategy;
	}

	public void setStrategy(TransformStrategy strategy) {
		this.strategy = strategy;
		if (strategy != null) {
			strategy.init(this);
		}
	}

	public ShapeManager getShapeManager() {
		return shapeManager;
	}

	public ShapeRule createShapeRule(Shape targetShape) throws TransformBuildException {

		Worker worker = new Worker();
		return worker.build(targetShape);
	}

	private class Worker {

		private AlphabeticVariableNamer namer = new AlphabeticVariableNamer();
		public ShapeRule build(Shape targetShape) throws TransformBuildException {

//			URI targetClass = targetShape.getTargetClass();
//			if (targetClass == null) {
//				throw new TransformBuildException(
//						"Target class must be defined for shape " + TurtleElements.resource(targetShape.getId()));
//			}

			TargetShape target = TargetShape.create(targetShape);

			ShapeRule shapeRule = build(target);
			shapeRule.setVariableNamer(namer);
			return shapeRule;
		}

		public ShapeRule build(TargetShape target) throws TransformBuildException {

			List<SourceShape> sourceList = strategy.findCandidateSourceShapes(target);

			int expectedPropertyCount = target.totalPropertyCount();
			int currentPropertyCount = 0;
			int priorPropertyCount = -1;

			buildAggregations(target);

			// First pass.  Use the candidate source shapes provided by the transform strategy.
			// While the current number of properties mapped is less than the total expected property count,
			// try mapping another shape.
			//
			while (currentPropertyCount < expectedPropertyCount && currentPropertyCount != priorPropertyCount
					&& !sourceList.isEmpty()) {
				priorPropertyCount = currentPropertyCount;

				SourceShape bestSource = selectBest(sourceList);
				addJoinStatement(target, bestSource);
				target.commit(bestSource);

				currentPropertyCount = target.mappedPropertyCount();
			}

			TargetShape.State state = (currentPropertyCount == expectedPropertyCount) ? TargetShape.State.OK
					: TargetShape.State.FIRST_PASS;

			target.setState(state);
			

			if (state != TargetShape.State.OK) {
				
				buildNestedProperties(target);

				if (target.getState() != TargetShape.State.OK) {
					List<TargetProperty> unmapped = target.getUnmappedProperties();
					setNullProperties(target, unmapped);
					if (unmapped.isEmpty()) {
						unmapped = null;
					} else if (unmapped.size()==1) {
						TargetProperty tp = unmapped.get(0);
						if (tp.getPredicate().equals(Konig.modified)) {
							unmapped = null;
						}
					}
					if (unmapped != null) {
						throw new TransformBuildException(unmappedPropertyMessage(target, unmapped));
					}
				}
			}

			createDataChannels(target);
			createIdRule(target);
			return assemble(target);

		}

		private void setNullProperties(TargetShape target, List<TargetProperty> unmapped) {

			Iterator<TargetProperty> sequence = unmapped.iterator();
			while (sequence.hasNext()) {
				TargetProperty tp = sequence.next();
				if (tp.getPredicate().equals(Konig.modified)) {
					continue;
				}
				
				PropertyConstraint p = tp.getPropertyConstraint();
				Integer minCount = p.getMinCount();
				if (minCount==null || minCount.equals(0)) {
					tp.setNull(true);
					sequence.remove();
				}
			}
			
		}

		private void buildAggregations(TargetShape target) throws TransformBuildException {
			List<VariableTargetProperty> varList = target.getVariableList();
			if (varList != null) {
				selectPreferredSourceForVariables(target);
				
			}
		}

		private void selectPreferredSourceForVariables(TargetShape target) throws TransformBuildException {

			List<VariableTargetProperty> varList = target.getVariableList();
			for (VariableTargetProperty vtp : varList) {
				// For now, just select the first candidate as the preferred SourceShape.
				// TODO: Select a shape that contains all of the properties that are referenced.
				//       Or even construct a shape if necessary.
				
				if (vtp.getPreferredMatch() == null) {
					Set<SourceShape> set = vtp.getCandidateSourceShape();
					if (set.isEmpty()) {
						StringBuilder msg = new StringBuilder();
						msg.append("Failed to build transform for ");
						msg.append(TurtleElements.resource(target.getShape().getId()));
						msg.append(".  No source found for variable ");
						msg.append(vtp.getPropertyConstraint().getPredicate().getLocalName());
						throw new TransformBuildException(msg.toString());
					}
					SourceShape source = set.iterator().next();
					vtp.setPreferredSourceShape(source);
				}
			}
			
		}

		private void createIdRule(TargetShape target) throws TransformBuildException {
			
			if (target.getShape().getNodeKind() == NodeKind.IRI) {
				SourceShape sourceWithIriTemplate = null;
				for (SourceShape source : target.getSourceList()) {
					Shape sourceShape = source.getShape();
					if (sourceShape.getNodeKind() == NodeKind.IRI) {
						CopyIdRule idRule = new CopyIdRule(source.getDataChannel());
						target.setIdRule(idRule);
						return;
					}
					
					if (sourceWithIriTemplate==null && sourceShape.getIriTemplate() != null) {
						sourceWithIriTemplate = source;
					}
				}
				
				if (sourceWithIriTemplate != null) {
					IriTemplateIdRule idRule = new IriTemplateIdRule(sourceWithIriTemplate.getDataChannel());
					target.setIdRule(idRule);
					return;
					
				}
				
				throw new TransformBuildException("Could not create IdRule for " + TurtleElements.resource(target.getShape().getId()));
			}
			
		}

		private void buildNestedProperties(TargetShape target) throws TransformBuildException {

			for (TargetProperty tp = target.getUnmappedProperty(); tp != null; tp = target.getUnmappedProperty()) {
				TargetShape parent = tp.getParent();
				TargetShape.State state = parent.getState();
				if (state.ordinal() > TargetShape.State.INITIALIZED.ordinal()) {
					// No more options for resolving this property.
					target.setState(TargetShape.State.FAILED);
					return;
				}

				build(parent);

			}

		}

		private void addJoinStatement(TargetShape target, SourceShape right) throws TransformBuildException {

			Shape rightShape = right.getShape();
			NodeKind rightNodeKind = rightShape.getNodeKind();
			IriTemplate rightIriTemplate = rightShape.getIriTemplate();
			TargetProperty ta = target.getAccessor();
			
			if (rightNodeKind == NodeKind.IRI || rightIriTemplate!=null) {
				
				if (ta != null) {
					URI predicate = ta.getPredicate();
					TargetShape tap = ta.getParent();
					for (SourceShape left : tap.getSourceList()) {
						SourceProperty leftProperty = left.getProperty(predicate);
						if (leftProperty != null) {
							PropertyConstraint lpc = leftProperty.getPropertyConstraint();
							if (lpc != null && lpc.getShape()==null) {
								URI rightPredicate = iriTemplateParameter(rightIriTemplate);
								BooleanExpression condition = new BinaryBooleanExpression(TransformBinaryOperator.EQUAL, lpc.getPredicate(), rightPredicate);
								ProtoJoinStatement proto = new ProtoJoinStatement(left, right, condition);
								right.setProtoJoinStatement(proto);
								return;
							}
						}
					}
				}

				List<SourceShape> sourceList = target.getSourceList();
				if (!sourceList.isEmpty()) {

					for (SourceShape left : sourceList) {
						Shape leftShape = left.getShape();
						if (leftShape.getNodeKind() == NodeKind.IRI || leftShape.getIriTemplate()!=null) {
							BooleanExpression condition = new BinaryBooleanExpression(TransformBinaryOperator.EQUAL, Konig.id, Konig.id);
							ProtoJoinStatement proto = new ProtoJoinStatement(left, right, condition);
							right.setProtoJoinStatement(proto);
							return;
						}
					}
					
					StringBuilder msg = new StringBuilder();
					msg.append("Failed to find a join condition for ");
					msg.append(TurtleElements.resource(right.getShape().getId()));
					throw new TransformBuildException(msg.toString());
				}
				
						
				
			} else {
				List<SourceShape> sourceList = target.getSourceList();
				if (!sourceList.isEmpty() || ta!=null) {
					StringBuilder msg = new StringBuilder();
					msg.append("Cannot join ");
					msg.append(TurtleElements.resource(right.getShape().getId()));
					msg.append(".  Only shapes with sh:nodeKind equal to sh:IRI are supported.");
					throw new TransformBuildException(msg.toString());
				}
			}
			

		}

		private URI iriTemplateParameter(IriTemplate template) throws TransformBuildException {
			if (template == null) {
				return Konig.id;
			}
			List<? extends Element> list = template.toList();
			URI result = null;
			Context context = template.getContext();
			for (Element e : list) {
				if (e.getType() == ValueFormat.ElementType.VARIABLE) {
					if (result != null) {
						throw new TransformBuildException("IRI Templates with multiple variables not supported: " + template.toString());
					}
					String iriValue = context.expandIRI(e.getText());
					result = new URIImpl(iriValue);
				}
			}
			return result;
		}

		private ShapeRule assemble(TargetShape target) throws TransformBuildException {

			ShapeRule shapeRule = new ShapeRule(target.getShape());

			addChannels(target, shapeRule);

			for (TargetProperty tp : target.getProperties()) {
				if (tp.isDirectProperty()) {
					PropertyRule propertyRule = createPropertyRule(tp);
					shapeRule.addPropertyRule(propertyRule);
				}
			}
			shapeRule.setIdRule(target.getIdRule());

			return shapeRule;
		}

		private void addChannels(TargetShape target, ShapeRule shapeRule) throws TransformBuildException {

			List<SourceShape> sourceList = target.getSourceList();
			for (SourceShape source : sourceList) {
				shapeRule.addChannel(source.produceDataChannel(namer));
			}
			
			List<VariableTargetProperty> varList = target.getVariableList();
			if (varList != null) {
				for (VariableTargetProperty vtp : varList) {
					SourceShape source = vtp.getPreferredSourceShape();
					if (source == null) {
						throw new TransformBuildException(
							"Preferred SourceShape is not defined for property " +
							TurtleElements.resource(vtp.getPredicate())
						);
					}
					DataChannel channel = source.produceDataChannel(namer);
					channel.setVariableName(vtp.getPredicate().getLocalName());
					shapeRule.addChannel(channel);
				}
			}

		}

		private void createDataChannels(TargetShape target) {

			for (SourceShape ss : target.getSourceList()) {
				ss.produceDataChannel(namer);

			}

		}

		private PropertyRule createPropertyRule(TargetProperty tp) throws TransformBuildException {
			if (tp.isNull()) {
				return new NullPropertyRule(null, tp.getPredicate());
			}
			if (tp instanceof DerivedDirectTargetProperty) {
				
				return new FormulaPropertyRule(null, tp.getPropertyConstraint(), tp.getPropertyConstraint());
			}
			SourceProperty sp = tp.getPreferredMatch();
			URI predicate = tp.getPredicate();
			if (sp == null) {
				if (Konig.modified.equals(predicate)) {
					return new InjectLiteralPropertyRule(null, Konig.modified, new LiteralImpl("{modified}"));
				}
			}
			DataChannel channel = sp.getParent().produceDataChannel(namer);

			if (tp.getNestedShape() != null) {
				ContainerPropertyRule rule = new ContainerPropertyRule(tp.getPredicate(), channel);
				ShapeRule nestedRule = assemble(tp.getNestedShape());
				rule.setNestedRule(nestedRule);

				return rule;

			}

			
			if (sp.getValue() instanceof Literal) {
				return new LiteralPropertyRule(channel, predicate, (Literal) sp.getValue());
			}

			int pathIndex = sp.getPathIndex();
			if (pathIndex < 0) {
				
				if (sp.isDerived()) {
					return new FormulaPropertyRule(channel, tp.getPropertyConstraint(), sp.getPropertyConstraint());
				}
				
				
				return new ExactMatchPropertyRule(channel, predicate);
			}

			PropertyConstraint sourceProperty = sp.getPropertyConstraint();
			RenamePropertyRule rename = new RenamePropertyRule(predicate, channel, sourceProperty, pathIndex);
			Path path = sourceProperty.getEquivalentPath();
			if (pathIndex < path.asList().size()-1) {
				addValueTransform(rename, tp, pathIndex);
			}
			
			return rename;
		}

		private void addValueTransform(RenamePropertyRule rename, TargetProperty tp, int pathIndex) throws TransformBuildException {
			PropertyConstraint tpc = tp.getPropertyConstraint();
			Resource owlClass = tpc.getValueClass();
			if (owlClass!=null && owlReasoner.isEnumerationClass(owlClass)) {
				

				PropertyConstraint spc = rename.getSourceProperty();
				// Get the equivalent path, relative to the source Shape.
				Path path = spc.getEquivalentPath();
				
				// Get a path relative to the Enum member
				Path subpath = path.subpath(pathIndex+1);
				
				MapValueTransform transform = new MapValueTransform();
				rename.setValueTransform(transform);
				
				// Get all instances of the owlClass
				List<Vertex> enumMemberList = owlReasoner.getGraph().v(owlClass).in(RDF.TYPE).toVertexList();
				
				for (Vertex member : enumMemberList) {
					Resource id = member.getId();
					Set<Value> valueSet = subpath.traverse(member);
					if (valueSet.size()==1) {
						transform.put(valueSet.iterator().next(), id);
					}
					
				}
				
				return;
			}
			
			StringBuilder msg = new StringBuilder();
			
			msg.append("For shape ");
			msg.append(TurtleElements.resource(tp.getParent().getShape().getId()));
			msg.append(", failed to produce ValueTransform for property ");
			msg.append(TurtleElements.resource(tp.getPredicate()));
			throw new TransformBuildException(msg.toString());
			
		}

		private String unmappedPropertyMessage(TargetShape target, List<TargetProperty> unmapped) {
			StringBuilder builder = new StringBuilder();

			builder.append("Failed to produce transform for Shape ");
			builder.append(TurtleElements.resource(target.getShape().getId()));
			builder.append("\n   Could not find a mapping for the following properties: ");
			for (TargetProperty tp : unmapped) {
				builder.append("\n   ");
				TransformBuildUtil.appendSimplePath(builder, tp);
			}
			return builder.toString();
		}

		private SourceShape selectBest(List<SourceShape> sourceList) {

			if (sourceList.size() == 1) {
				return sourceList.remove(0);
			}

			int bestCount = 0;
			SourceShape bestShape = null;
			Iterator<SourceShape> sequence = sourceList.iterator();
			while (sequence.hasNext()) {
				SourceShape candidate = sequence.next();
				int count = candidate.potentialMatchCount();
				if (count > bestCount) {
					bestCount = count;
					bestShape = candidate;
					sequence.remove();
				}
			}

			return bestShape;
		}
	}

}
