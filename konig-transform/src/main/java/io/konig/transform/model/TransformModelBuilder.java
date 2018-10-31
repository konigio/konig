package io.konig.transform.model;

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


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.impl.BNodeImpl;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.konig.core.OwlReasoner;
import io.konig.core.UidGenerator;
import io.konig.core.Vertex;
import io.konig.core.impl.RdfUtil;
import io.konig.core.impl.UidGeneratorImpl;
import io.konig.core.util.IriTemplate;
import io.konig.core.util.ValueFormat;
import io.konig.core.vocab.Konig;
import io.konig.core.vocab.Schema;
import io.konig.datasource.DataSource;
import io.konig.formula.Direction;
import io.konig.formula.DirectionStep;
import io.konig.formula.Expression;
import io.konig.formula.FunctionExpression;
import io.konig.formula.LiteralFormula;
import io.konig.formula.PathExpression;
import io.konig.formula.PathStep;
import io.konig.formula.PrimaryExpression;
import io.konig.formula.QuantifiedExpression;
import io.konig.shacl.NodeKind;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeManager;

public class TransformModelBuilder {
	private static Logger logger = LoggerFactory.getLogger(TransformModelBuilder.class);

	private ShapeManager shapeManager;
	private OwlReasoner owlReasoner;
	
	public TransformModelBuilder(ShapeManager shapeManager, OwlReasoner owlReasoner) {
		this.shapeManager = shapeManager;
		this.owlReasoner = owlReasoner;
	}
	
	public TNodeShape build(Shape shape, DataSource datasource) throws ShapeTransformException {
		Worker worker = new Worker();
		TNodeShape result = worker.buildTargetShape(shape, datasource, null);
		worker.computeValueExpressions(result);
		return result;
	}
	

	private class Worker  implements TExpressionFactory {
		private DataSourceSelector datasourceSelector = new BasicDataSourceSelector();
		private UidGenerator uid = new UidGeneratorImpl();
		
		public Worker() {
		}

		private void computeValueExpressions(TNodeShape shape) throws ShapeTransformException {
			
			if (logger.isDebugEnabled()) {
				logger.debug("computeValueExpressions: " + shape.getTclass().toStructureString());
			}
			List<CandidateSource> stack = new ArrayList<>();
			addCandidateSources(stack, shape);
			
			int priorSize = -1;
			while (!stack.isEmpty() && (priorSize != stack.size())) {
				priorSize = stack.size();
				countValues(stack);
				Collections.sort(stack);
				
				discardUselessCandidates(stack);
				
				if (!stack.isEmpty()) {
					CandidateSource candidate = stack.remove(stack.size()-1);
					candidate.getSourceShape().assignValues();
				}
			}
			 
			confirmAllPropertiesMatched(shape);
			
		}

		private void confirmAllPropertiesMatched(TNodeShape shape) throws UnmatchedPropertiesException {
			List<TPropertyShape> unmatched = unmatchedProperties(shape);
			
			if (!unmatched.isEmpty()) {
				throw new UnmatchedPropertiesException(shape, unmatched);
			}
			
			
		}

		private List<TPropertyShape> unmatchedProperties(TNodeShape shape) {
			List<TPropertyShape> list = new ArrayList<>();
			addUnmatchedProperties(list, shape);
			return list;
		}

		private void addUnmatchedProperties(List<TPropertyShape> list, TNodeShape shape) {

			for (TPropertyShape p : shape.getProperties()) {
				TProperty group = p.getPropertyGroup();
				if (group.getValueExpression() == null) {
					list.add(p);
				}
				TNodeShape child = p.getValueShape();
				if (child != null) {
					addUnmatchedProperties(list, child);
				}
			}
			
		}

		private void discardUselessCandidates(List<CandidateSource> stack) {
			for (int i=stack.size()-1; i>=0; i--) {
				if (stack.get(i).getValueCount()==0) {
					stack.remove(i);
				} else {
					break;
				}
			}
			
		}

		private void countValues(List<CandidateSource> stack) {
			for (CandidateSource candidate : stack) {
				int valueCount = candidate.getSourceShape().countValues();
				candidate.setValueCount(valueCount);
			}
			
		}

		private void addCandidateSources(List<CandidateSource> stack, TNodeShape targetShape) {
			TClass tclass = targetShape.getTclass();
			for (TNodeShape sourceShape : tclass.getSourceShapes()) {
				if (logger.isDebugEnabled()) {
					logger.debug("addCandidateSource: {} ", sourceShape.getPath());
				}
				stack.add(new CandidateSource(sourceShape));
				for (TPropertyShape p : sourceShape.getProperties()) {
					if (logger.isDebugEnabled()) {
						logger.debug("addCandidateSource Property: {}", p.getPath());
					}
					TNodeShape childShape = p.getValueShape();
					if (childShape != null) {
						addCandidateSources(stack, childShape);
					}
				}
			}
			
		}

		private TNodeShape buildTargetShape(Shape shape, DataSource ds, TPropertyShape accessor) throws ShapeTransformException {
			
			TNodeShape targetShape = accessor==null ? 
					new RootTNodeShape(shape) :
					new TNodeShape(shape);
					
			if (ds != null) {
				TDataSource datasource = new TDataSource(ds, targetShape);
				targetShape.setTdatasource(datasource);
			}
			
			Resource targetClass = null;
			if (accessor != null) {
				Shape accessorParent = accessor.getPropertyGroup().getTargetProperty().getOwner().getShape();
				PropertyConstraint p = accessorParent.getPropertyConstraint(accessor.getPredicate());
				if (p != null) {
					targetClass = p.getValueClass();
				}
			}
			if (targetClass == null) {
				targetClass = shape.getTargetClass();
			}
			
			TClass tclass = new TClass(targetClass, targetShape);
			targetShape.setTclass(tclass);
			addTargetProperties(targetShape);
			addSourceShapes(targetShape.getTclass());
			targetShape.setAccessor(accessor);
			
			return targetShape;
		}

		private void addSourceShapes(TClass tclass) throws ShapeTransformException {
			
			Resource targetClass = tclass.getId();
			if (targetClass == null) {
				throw new ShapeTransformException("targetClass must be defined for shape: " + 
						tclass.getTargetShape().getShape().getId().stringValue());
			}
			
			if (!(targetClass instanceof URI)) {
				throw new ShapeTransformException("BNode class definitions not supported");
			}

			
			Shape skip = tclass.getTargetShape().getShape();
			List<Shape> list = 
				
					shapeManager.getShapesByTargetClass((URI)targetClass);
			for (Shape s : list) {
				if (s == skip) {
					continue;
				}
				
				DataSource ds = datasourceSelector.create(tclass.getTargetShape(), s);
				if (ds != null) {
					TNodeShape sourceShape = new TNodeShape(s);
					sourceShape.setTclass(tclass);
					tclass.addSourceShape(sourceShape);
					TDataSource tds = new TDataSource(ds, sourceShape);
					sourceShape.setTdatasource(tds);
					addSourceProperties(sourceShape);
				}
				
				
			}
			
		}

		private void addSourceProperties(TNodeShape tshape) throws ShapeTransformException {
			addSourceIdProperty(tshape);
			addSourceProperties(tshape, tshape.getShape().getProperty(), false);
			addSourceProperties(tshape, tshape.getShape().getDerivedProperty(), true);
		}


		

		private void addSourceIdProperty(TNodeShape tshape) {
			Shape shape = tshape.getShape();
			if (shape.getNodeKind() == NodeKind.IRI || tshape.getShape().getIriTemplate()!=null ) {
				new TIdPropertyShape(this, tshape, false);
			}
			
		}

		private void addSourceProperties(TNodeShape tshape, List<PropertyConstraint> properties, boolean derived) throws ShapeTransformException {
			for (PropertyConstraint p : properties) {
				URI predicate = p.getPredicate();
				if (predicate != null) {
					TPropertyShape tps = null;
					QuantifiedExpression formula = p.getFormula();
					if (formula !=null) {

						PrimaryExpression primary = formula.asPrimaryExpression();
						if (primary instanceof FunctionExpression) {
							tps = new FunctionTPropertyShape(this, tshape, p, false);
						} else if (primary instanceof PathExpression) {
							
						}
					}
					if (tps == null) {
						SimpleTPropertyShape sps = new SimpleTPropertyShape(tshape, p, derived, false);
						addSourcePath(sps);
					}
				}
			}
			
		}


		private void addSourcePath(SimpleTPropertyShape sps) throws ShapeTransformException {
			PathExpression path = PathExpression.toPathExpression(sps.getConstraint().getFormula());
			if (path != null) {
				List<TNodeShape> enumShapeList = new ArrayList<>();
				List<PathStep> stepList = path.getStepList();
				TNodeShape parentShape = sps.getOwner();
				int end = stepList.size()-1;
				TPathPropertyShape prev = null;
				for (int i=0; i<stepList.size(); i++) {
					PathStep step = stepList.get(i);
					if (step instanceof DirectionStep) {
						DirectionStep dirStep = (DirectionStep) step;
						if(dirStep.getDirection() == Direction.OUT) {
							OutTPropertyShape outProperty = new OutTPropertyShape(parentShape, dirStep, sps, false);
							if (prev != null) {
								prev.setNext(outProperty);
							}
							prev = outProperty;
							parentShape = outParent(parentShape, outProperty);
							
							
							// Enqueue the parentShape if the target class is an enumeration
							// so that we can perform special reasoning about the enumeration later.
							enqueueEnumShape(enumShapeList, parentShape);
							
						} else {
							throw new ShapeTransformException("Path not supported: " + path.simpleText());
						}
					}
				}
				for (TNodeShape enumShape : enumShapeList) {
					handleEnumShape(enumShape);
				}
			}
			
		}

		private void handleEnumShape(TNodeShape enumShape) throws ShapeTransformException {
			
			TNodeShape targetShape = enumShape.getTclass().getTargetShape();
			if (targetShape != null) {
				List<Vertex> individuals = individuals(enumShape);
				TPropertyShape keyProperty = keyProperty(enumShape);
				if (targetShape.getProperty(Konig.id) != null) {
					addEnumProperty(enumShape, Konig.id, keyProperty, individuals);
				}
				if (targetShape.getProperty(Schema.name)!=null) {
					addEnumProperty(enumShape, Schema.name, keyProperty, individuals);
				}
			}
			
			
		}


		private TPropertyShape keyProperty(TNodeShape enumShape) throws ShapeTransformException {
			if (logger.isDebugEnabled()) {
				logger.debug("keyProperty: {}", enumShape.toString() );
				logger.debug("keyProperty: accessor={}", enumShape.getAccessor().toString());
			}
			TPropertyShape accessor = enumShape.getAccessor();
			if (accessor instanceof OutTPropertyShape) {
				OutTPropertyShape out = (OutTPropertyShape) accessor;
				if (out.getNext() instanceof OutTPropertyShape) {
					OutTPropertyShape next = (OutTPropertyShape) out.getNext();
					if (next.getNext()==null) {
						return next;
					}
				}
				
			}
			throw new ShapeTransformException("Failed to get keyProperty: " + enumShape.toString());
		}

		private List<Vertex> individuals(TNodeShape enumShape) throws ShapeTransformException {
			if (logger.isDebugEnabled()) {
				logger.debug("individuals: " + enumShape.getPath());
			}
			TPropertyShape accessor = enumShape.getAccessor();
			if (accessor != null) {
				TPropertyShape targetAccessor = accessor.getPropertyGroup().getTargetProperty();
				if (targetAccessor instanceof SimpleTPropertyShape) {
					SimpleTPropertyShape simple = (SimpleTPropertyShape) targetAccessor;
					PropertyConstraint p = simple.getConstraint();
					Resource valueClass = p.getValueClass();
					if (valueClass != null) {
						return owlReasoner.getGraph().v(valueClass).in(RDF.TYPE).toVertexList();
					}
				}
			}
			throw new ShapeTransformException("Enum type not found: " + enumShape.getPath());
		}

		private void addEnumProperty(TNodeShape enumShape, URI predicate, TPropertyShape keyProperty, List<Vertex> individuals) {
			TPropertyShape p = new TNamedIndividualPropertyShape(enumShape, predicate, keyProperty, individuals);

			if (logger.isDebugEnabled()) {
				logger.debug("addEnumProperty: {}", p.getPath());
			}
		}

		private void enqueueEnumShape(List<TNodeShape> enumShapeList, TNodeShape nodeShape) {
			TPropertyShape propertyShape = nodeShape.getAccessor();
			TClass rangeClass = propertyShape.getPropertyGroup().getRangeClass();
			if (rangeClass != null) {
				Resource classId = rangeClass.getId();
				if (owlReasoner.isEnumerationClass(classId)) {
					enumShapeList.add(nodeShape);
				}
			}
			
		}

		private TNodeShape outParent(TNodeShape parentShape, OutTPropertyShape accessor) {
			URI predicate = accessor.getPredicate();
			
		
			TClass parentClass = parentShape.getTclass();
			TProperty outProperty = parentClass.getOut(predicate);
			
			if (outProperty == null) {
				outProperty = parentClass.produceOut(predicate);
			}
			
			TClass rangeClass = outProperty.getRangeClass();
			if (rangeClass == null) {
				Set<URI> rangeIncludes = owlReasoner.rangeIncludes(predicate);
				Resource leastCommon = owlReasoner.leastCommonSuperClass(rangeIncludes);	
				if (leastCommon instanceof URI) {
					URI rangeId = (URI) leastCommon;
					rangeClass = new TClass(rangeId, null);
					outProperty.setRangeClass(rangeClass);
				}
			}
			if (rangeClass == null) {
				rangeClass = new TClass(OWL.THING, null);
				outProperty.setRangeClass(rangeClass);
			}
			
			Shape outShape = new Shape(new BNodeImpl(uid.next()));
			
			TNodeShape outNodeShape = new TNodeShape(outShape, null);
			outNodeShape.setAccessor(accessor);
			outNodeShape.setTclass(rangeClass);
			rangeClass.addSourceShape(outNodeShape);
			accessor.setValueShape(outNodeShape);
			
			
			return outNodeShape;
		}

		private void addTargetProperties(TNodeShape tshape) throws ShapeTransformException {
			
			Shape shape = tshape.getShape();
			TClass tclass = tshape.getTclass();
			
			addTargetIdProperty(tshape);
			
			for (PropertyConstraint p : shape.getProperty()) {
				URI predicate = p.getPredicate();
				if (predicate != null) {
					SimpleTPropertyShape sps = new SimpleTPropertyShape(true, tshape, p);
					tshape.add(sps);
					
					TProperty group = tclass.produceOut(predicate);
					
					
					Shape childShape = p.getShape();
					if (childShape != null) {
						TNodeShape tchildShape = buildTargetShape(childShape, null, sps);
						sps.setValueShape(tchildShape);
						group.setRangeClass(tchildShape.getTclass());
					}
				}
			}
			
		}

		private void addTargetIdProperty(TNodeShape tshape) {
			Shape shape = tshape.getShape();
			
			if (shape.getNodeKind() == NodeKind.IRI || shape.getIriTemplate()!=null) {
				new TIdPropertyShape(this, tshape, true);
			}
			
		}

		@Override
		public TExpression createExpression(TPropertyShape sourceProperty, Expression e)
				throws ShapeTransformException {
			
			PrimaryExpression primary = e.asPrimaryExpression();
			if (primary instanceof PathExpression) {
				return pathExpression(sourceProperty, (PathExpression) primary);
			} else if (primary instanceof LiteralFormula) {
				return literalExpression(sourceProperty, (LiteralFormula) primary);
			}
			
			String msg = "Unable to create Expression of type " + primary.getClass().getSimpleName() + " at " + sourceProperty.getPath();
			throw new ShapeTransformException(msg);
		}

		private TExpression literalExpression(TPropertyShape sourceProperty, LiteralFormula formula) {
			
			return new TLiteralExpression(sourceProperty, formula.getLiteral());
		}

		private TExpression pathExpression(TPropertyShape sourceProperty, PathExpression primary) throws ShapeTransformException {
			List<PathStep> stepList = primary.getStepList();
			PathStep last = stepList.get(stepList.size()-1);
			// TODO: The logic here needs some rework
			if (last instanceof DirectionStep) {
				DirectionStep dirStep = (DirectionStep) last;
				URI predicate = dirStep.getTerm().getIri();

				TNodeShape nodeShape = sourceProperty.getOwner();
				TPropertyShape p = nodeShape.getProperty(predicate);
				if (p != null) {
					return new ValueOfExpression(p);
				}
			}
			throw new ShapeTransformException("Failed to build expression for path " + primary.toString() + " at " + sourceProperty.getPath());
		}

		@Override
		public TIriTemplateExpression createIriTemplate(TPropertyShape sourceProperty, IriTemplate template)
				throws ShapeTransformException {
			TIriTemplateExpression result = new TIriTemplateExpression(sourceProperty);
			for (ValueFormat.Element e : template.toList()) {
				switch (e.getType()) {
				case TEXT:
					result.add(new TLiteralExpression(sourceProperty, new LiteralImpl(e.getText())));
					break;
					
				case VARIABLE:
					
					result.add(iriTemplateVariable(sourceProperty, e.getText()));
					break;
					
				}
			}
			return result;
		}

		private ValueOfExpression iriTemplateVariable(TPropertyShape sourceProperty, String propertyName) throws ShapeTransformException {

			// For now, we assume that all variables are string values.
			// TODO: Handle cases where the variable is not a string.
			
			TNodeShape shapeNode = sourceProperty.getOwner();
			
			TPropertyShape property = findProperty(shapeNode, propertyName);
			
			return new ValueOfExpression(property);
		}

		private TPropertyShape findProperty(TNodeShape shapeNode, String propertyName) throws ShapeTransformException {
			// For now, we assume the propertyName is a single unqualified simple name
			// TODO: Handle cases where propertyName is a CURIE
			// TODO: Handle case where propertyName is a fully-qualified IRI
			// TODO: Handle case where propertyName is a dot-separated path.
			
			for (TPropertyShape p : shapeNode.getProperties()) {
				URI predicate = p.getPredicate();
				if (predicate.getLocalName().equals(propertyName)) {
					return p;
				}
			}
			throw new ShapeTransformException("At " + shapeNode.getPath() + ", property not found: " + propertyName);
		}

		
	}
	
	
	private interface DataSourceSelector {
		DataSource create(TNodeShape targetShape, Shape sourceShape);
	}
	
	private static class BasicDataSourceSelector implements DataSourceSelector {

		@Override
		public DataSource create(TNodeShape targetShape, Shape sourceShape) {
			List<DataSource> list = sourceShape.getShapeDataSource();
			if (list != null) {
				Class<?> dataSourceType = targetShape.getTdatasource().getDatasource().getClass();
				for (DataSource ds : list) {
					if (ds.getClass() == dataSourceType) {
						return ds;
					}
				}
			}
			return null;
		}
		
	}
	
	private static class CandidateSource implements Comparable<CandidateSource> {
		private TNodeShape sourceShape;
		private int valueCount;

		public CandidateSource(TNodeShape sourceShape) {
			this.sourceShape = sourceShape;
		}

		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("CandidateSource[shape: <");
			builder.append(RdfUtil.localName(sourceShape.getShape().getId()));
			builder.append(">, valueCount: ");
			builder.append(valueCount);
			builder.append(']');
			return builder.toString();
		}

		/**
		 * Get the number of properties from the encapsulated source Shape that supply value
		 * for the target Shape.  This number excludes values that have already been supplied 
		 * by other sources.
		 * @return
		 */
		public int getValueCount() {
			return valueCount;
		}

		public void setValueCount(int valueCount) {
			this.valueCount = valueCount;
		}

		public TNodeShape getSourceShape() {
			return sourceShape;
		}

		@Override
		public int compareTo(CandidateSource other) {
			int result =  other.getValueCount() - valueCount;
			if (result==0) {
				result = other.getSourceShape().getShape().getId().stringValue()
						.compareTo(sourceShape.getShape().getId().stringValue());
			}
			return result;
		}
		
		
		
	}
}
