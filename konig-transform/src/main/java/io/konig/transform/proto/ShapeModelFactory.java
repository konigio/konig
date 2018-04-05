package io.konig.transform.proto;

import static io.konig.core.impl.RdfUtil.localName;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.konig.core.OwlReasoner;
import io.konig.core.impl.RdfUtil;
import io.konig.core.path.HasStep;
import io.konig.formula.DirectedStep;
import io.konig.formula.Direction;
import io.konig.formula.HasPathStep;
import io.konig.formula.PathExpression;
import io.konig.formula.PathStep;
import io.konig.formula.PrimaryExpression;
import io.konig.formula.QuantifiedExpression;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.PropertyManager;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeManager;
import io.konig.shacl.ShapePropertyPair;
import io.konig.transform.ShapeTransformException;
import io.konig.transform.proto.SimplePropertyMapper.FromItemEnds;

public class ShapeModelFactory {
	private static Logger logger = LoggerFactory.getLogger(ShapeModelFactory.class);
	

	private ShapeManager shapeManager;
	private PropertyManager propertyManager;
	private DataChannelFactory dataChannelFactory;
	private OwlReasoner reasoner;
	
	public ShapeModelFactory(
			ShapeManager shapeManager, 
			PropertyManager propertyManager, 
			DataChannelFactory dataChannelFactory,
			OwlReasoner reasoner
	) {
		this.shapeManager = shapeManager;
		this.propertyManager = propertyManager;
		this.dataChannelFactory = dataChannelFactory;
		this.reasoner = reasoner;
	}
	
	
	public ShapeModel createShapeModel(Shape shape) throws ShapeTransformException {
		Worker worker = new Worker();
		
		return worker.execute(shape);
	}
	
	private class Worker {
		
		private ShapeModel root;

		private FromItemEnds fromItemEnds;
		
		
		public ShapeModel execute(Shape shape) throws ShapeTransformException {
			fromItemEnds = new FromItemEnds();
			ShapeModel result = createShapeModel(shape);
			mapProperties(result);
			result.getClassModel().setFromItem(fromItemEnds.getFirst());
			return result;
		}

		public ShapeModel createShapeModel(Shape shape) throws ShapeTransformException {
			if (logger.isDebugEnabled()) {
				logger.debug("createShapeModel: {}", localName(shape.getId()));
			}
			ShapeModel targetShapeModel = targetShapeModel(shape, shape.getTargetClass());
			if (root == null) {
				root = targetShapeModel;
			}
			
			findCandidateSourceShapes(targetShapeModel);
			
			return targetShapeModel;
		}

		private void mapProperties(ShapeModel targetShapeModel) throws ShapeTransformException {
			
			ClassModel classModel = targetShapeModel.getClassModel();
			
			List<SourceShapeInfo> infoList = new ArrayList<>(classModel.getSourceShapeInfo());
			sortSourceShapeInfo(infoList);
			
			while (!infoList.isEmpty()) {
				Iterator<SourceShapeInfo> sequence = infoList.iterator();
				while (sequence.hasNext()) {
					SourceShapeInfo info = sequence.next();
					if (info.getMatchCount()>0) {
						doMapProperties(targetShapeModel, info.getSourceShape());
					}
					sequence.remove();
				}
				sortSourceShapeInfo(infoList);
			}
			
			classModel.setFromItem(fromItemEnds.getFirst());
			
		}

		private void doMapProperties(ShapeModel targetShapeModel, ShapeModel sourceShapeModel) throws ShapeTransformException {
			boolean requiresFromClause = true;
			if (logger.isDebugEnabled()) {
				logger.debug("doMapProperties: Mapping {} to {}",
						RdfUtil.localName(sourceShapeModel.getShape().getId()),
						RdfUtil.localName(targetShapeModel.getShape().getId())
						);
			}
			for (PropertyModel sourceProperty : sourceShapeModel.getProperties()) {
				
				if (sourceProperty.getGroup().getSourceProperty()!=null) {
					continue;
				}
				
				PropertyGroup group = sourceProperty.getGroup();
				
				if (sourceProperty.getValueModel()!=null) {
					throw new ShapeTransformException("nested property not supported yet: " + sourceProperty.getPredicate().getLocalName());
				} else if (group.getTargetProperty() != null){
					if (logger.isDebugEnabled()) {
						logger.debug("doMapProperties: Mapped {}{} from {} to {}{} in {}",
								directionSymbol(sourceProperty),
								sourceProperty.getPredicate().getLocalName(),
								RdfUtil.localName(sourceShapeModel.getShape().getId()),
								directionSymbol(group.getTargetProperty()),
								group.getTargetProperty().getPredicate().getLocalName(),
								RdfUtil.localName(sourceProperty.getGroup().getTargetProperty().getDeclaringShape().getShape().getId())
								);
					}
					group.setSourceProperty(sourceProperty);
					
					if (requiresFromClause) {
						// TODO: finish implementation
					}
				} 
			}
			
		}

		private String directionSymbol(PropertyModel property) {
			if (property instanceof StepPropertyModel) {
				StepPropertyModel step = (StepPropertyModel) property;
				return Character.toString(step.getDirection().getSymbol());
			}
			return "";
		}

		private void sortSourceShapeInfo(List<SourceShapeInfo> infoList) {
			computeMatchCounts(infoList);
			Collections.sort(infoList);
			Iterator<SourceShapeInfo> sequence = infoList.iterator();
			while (sequence.hasNext()) {
				SourceShapeInfo info = sequence.next();
				if (info.getMatchCount() == 0) {
					if (logger.isDebugEnabled()) {
						logger.debug("sortSourceShapeInfo: remove {}", RdfUtil.localName(info.getSourceShape().getShape().getId()));
					}
					sequence.remove();
				}
			}
			
		}

		private void computeMatchCounts(List<SourceShapeInfo> infoList) {
			for (SourceShapeInfo info : infoList) {
				info.computeMatchCount();
			}
			
		}

		private void findCandidateSourceShapes(ShapeModel targetShapeModel) throws ShapeTransformException {
			
			// TODO: handle simple direct properties (without a formula)
			
			scanPathExpressions(targetShapeModel);
			
		}

		private void scanPathExpressions(ShapeModel targetShapeModel) throws ShapeTransformException {

			URI targetClass = targetShapeModel.getShape().getTargetClass();
			if (targetClass == null) {
				throw new ShapeTransformException("targetClass must be defined for " + 
						RdfUtil.localName(targetShapeModel.getShape().getId()));
			}
			
			for (PropertyModel p : targetShapeModel.getProperties()) {
				if (p instanceof DirectPropertyModel) {
					DirectPropertyModel direct = (DirectPropertyModel) p;
					if (direct.getPathHead() !=null) {
						scanPath(direct);
					}
				}
			}
			
		}


		private void scanPath(DirectPropertyModel direct) throws ShapeTransformException {
			StepPropertyModel step = direct.getPathHead();
			
			while (step != null) {
				switch (step.getDirection()) {
				case IN:
					scanInStep(step); 
					break;
				case OUT:
					scanOutStep(step);
					break;
				}
				step = step.getNextStep();
			}
			
		}

		private void scanOutStep(StepPropertyModel step) throws ShapeTransformException {
			
			List<SourceShapeInfo> set = shapeModelsForOutStep(step);
			for (SourceShapeInfo info : set) {
				ShapeModel sourceShapeModel = info.getSourceShape();
				producePropertyModel(step, sourceShapeModel);
			}
		}

		

		

		private PropertyModel producePropertyModel(PropertyModel targetProperty, ShapeModel sourceShapeModel) {
			
			URI predicate = targetProperty.getPredicate();
			PropertyModel sourceProperty = sourceShapeModel.getPropertyByPredicate(predicate);
			if (sourceProperty == null) {
				PropertyConstraint p = sourceShapeModel.getShape().getPropertyConstraint(predicate);
				if (p != null) {
					PropertyGroup group = targetProperty.getGroup();
					sourceProperty = new DirectPropertyModel(predicate, group, p);
					fullAttachProperty(sourceProperty, sourceShapeModel, group);
					if (logger.isDebugEnabled()) {
						logger.debug("Added '{}' from {} to group[{}] in ClassModel[{}]",
							predicate.getLocalName(), 
							RdfUtil.localName(sourceShapeModel.getShape().getId()),
							group.hashCode(),
							group.getParentClassModel().hashCode());
					}
				}
			}
			
			
			return sourceProperty;
		}

		private List<SourceShapeInfo> shapeModelsForOutStep(StepPropertyModel step) throws ShapeTransformException {
			if (step.getPreviousStep() != null) {
				return step.getPreviousStep().getValueShapeInfo();
			} else {
				throw new ShapeTransformException("TODO: handle initial step");
			}
		}

		private void scanInStep(StepPropertyModel step) throws ShapeTransformException {
			
			if (step.getPreviousStep() != null){
				// TODO: Handle steps after the first step.
				throw new ShapeTransformException("Inverse path elements are currently supported only at the beginning of a path");
			}
			ShapeModel targetShapeModel = step.getDeclaringShape();
			DirectPropertyModel direct = step.getDeclaringProperty();
			
			URI targetClass = targetShapeModel.getShape().getTargetClass();
			
			URI predicate = step.getPredicate();
			if (logger.isDebugEnabled()) {
				logger.debug("scanPathExpressions: Inspecting '{}' for {}#{}",
						predicate.getLocalName(), 
						RdfUtil.localName(targetShapeModel.getShape().getId()), 
						direct.getPredicate().getLocalName());
			}
			Set<ShapePropertyPair> set = propertyManager.propertyConstraintsByPathOrFormula(step.getPredicate());
			
			if (set == null) {
				if (logger.isDebugEnabled()) {
					logger.debug(
							"scanPathExpressions: No source shapes found for ^{}",
							predicate.getLocalName());
				}
			} else {
			
				for (ShapePropertyPair pair : set) {
					
					PropertyConstraint sourceProperty = pair.getProperty();
					URI valueType = valueClass(sourceProperty);
					if (valueType != null && reasoner.isSubClassOf(valueType, targetClass)) {
						addInverseSource(targetShapeModel, direct, pair);
						
					} else {
						if (logger.isDebugEnabled() && sourceProperty!=direct.getPropertyConstraint()) {
							logger.debug("scanPathExpressions: Value class not defined for '{}' referenced from '{}' in '{}'", 
									RdfUtil.localName(sourceProperty.getPredicate()), 
									direct.getPredicate().getLocalName(),
									RdfUtil.localName(targetShapeModel.getShape().getId()));
						}
					}
				}
			}
		}

		private void addInverseSource(ShapeModel targetShapeModel, DirectPropertyModel targetProperty, ShapePropertyPair pair) throws ShapeTransformException {
			
			
			StepPropertyModel head = targetProperty.getPathHead();
			
			if (head == null) {
				throw new ShapeTransformException("Path head not found");
			}
			ClassModel targetClassModel = targetShapeModel.getClassModel();
			
			ShapeModel sourceShapeModel = new ShapeModel(pair.getShape());
			ClassModel sourceClassModel = head.getValueClassModel();
			sourceShapeModel.setClassModel(sourceClassModel);
			
			
			if (logger.isDebugEnabled()) {
				logger.debug("addInverseSource: Add {} to ClassModel[{}]", 
						RdfUtil.localName(sourceShapeModel.getShape().getId()),
						sourceClassModel.hashCode());
			}
			
			InverseSourceShapeInfo info = new InverseSourceShapeInfo(sourceShapeModel, targetProperty);
			targetClassModel.addCandidateSourceShapeModel(info);

			head.addValueShapeInfo(info);
		}

		private URI valueClass(PropertyConstraint sourceProperty) {
			Resource type = sourceProperty.getValueClass();
			if (type instanceof URI) {
				return (URI) type;
			} else {
				Shape shape = sourceProperty.getShape();
				if (shape != null) {
					return shape.getTargetClass();
				}
			}
			return null;
		}

		private ShapeModel targetShapeModel(Shape shape, URI targetClass) throws ShapeTransformException {
			if (targetClass == null) {
				throw new ShapeTransformException(
					"targetClass must be defined for Shape " + localName(shape.getId()));
			}
			ClassModel classModel = new ClassModel(shape.getTargetClass());
			if (logger.isDebugEnabled()) {
				logger.debug("targetShapeModel created ShapeModel for {} in ClassModel[{}]]", 
						localName(shape.getId()), 
						classModel.hashCode());
			}
			ShapeModel targetShapeModel = new ShapeModel(shape);
			targetShapeModel.setClassModel(classModel);
			classModel.setTargetShapeModel(targetShapeModel);
			
			addTargetDirectProperties(targetShapeModel);
			
			return targetShapeModel;
		}

		private void addTargetDirectProperties(ShapeModel shapeModel) throws ShapeTransformException {
			if (logger.isDebugEnabled()) {
				logger.debug("addTargetDirectProperties for {}", localName(shapeModel.getShape().getId()));
			}
			
			
			ClassModel targetClassModel = shapeModel.getClassModel();
			
			for (PropertyConstraint p : shapeModel.getShape().getProperty()) {
				QuantifiedExpression formula = p.getFormula();
				PropertyGroup group=null;
				URI predicate=p.getPredicate();
				if (predicate != null) {
					
					group = targetClassModel.produceGroup(Direction.OUT, predicate);
					
					if (logger.isDebugEnabled()) {
						logger.debug("addTargetDirectProperties: Adding {} from {} to group[{}] in ClassModel[{}]",
								predicate.getLocalName(),
								localName(shapeModel.getShape().getId()),
								group.hashCode(),
								targetClassModel.hashCode());
					}
				}
				if (formula != null) {
					PrimaryExpression primary = formula.asPrimaryExpression();
					if (primary instanceof PathExpression) {
						DirectPropertyModel direct = directPropertyModel(shapeModel, predicate, group, p);
						attachTargetSteps(direct, (PathExpression) primary);
					} else {
						throw new ShapeTransformException("TODO: support generic formula in target property");
					}
				} else {
					throw new ShapeTransformException("TODO: support simple, direct property");
				}
			}
			
		}

		private void attachTargetSteps(DirectPropertyModel direct, PathExpression path) throws ShapeTransformException {
			
			ShapeModel shapeModel = direct.getDeclaringShape();
			StepPropertyModel priorStep = null;
			int index = 0;
			ClassModel classModel = direct.getDeclaringShape().getClassModel();
			List<PathStep> stepList = path.getStepList();
			for (PathStep step : stepList) {
				index++;
				if (step instanceof HasPathStep) {
					
					if (priorStep == null) {
						throw new ShapeTransformException("Leading filter not supported");
					}
					
					HasStep hasStep = (HasStep) step;
					priorStep.setFilter(hasStep.getPairList());
					
					// TODO:
					// In shapeModelFactory1, we inject FixedPropertyModel elements within the group.
					// Should we do that here?
					
				} else if (step instanceof DirectedStep) {
					DirectedStep directStep = (DirectedStep) step;
					URI predicate = directStep.getTerm().getIri();
					PropertyGroup group = classModel.produceGroup(directStep.getDirection(), predicate);
					
					StepPropertyModel stepModel = new StepPropertyModel(predicate, directStep.getDirection(), group, direct, index);
					if (priorStep != null) {
						priorStep.setNextStep(stepModel);
					}
					if (logger.isDebugEnabled()) {
						logger.debug("attachTargetSteps: Add step {}{} to group[{}] in ClassModel[{}]",
							directStep.getDirection().getSymbol(),
								predicate.getLocalName(), group.hashCode(), classModel.hashCode());
					}
					attachProperty(stepModel, shapeModel, group);
					
					
					priorStep = stepModel;
				} else {
					throw new ShapeTransformException("Unsupported PathStep type: " + step.getClass().getName());
				}
				
				if (hasNextStep(stepList, index)) {
					// TODO: Find a way to define the OWL class.
					URI owlClass = null;
					classModel = new ClassModel(owlClass);
					priorStep.setValueClassModel(classModel);
				}
			}
			
			if (priorStep == null) {
				throw new ShapeTransformException("Path tail not found for property " + direct.getPredicate());
			}
				
			if (logger.isDebugEnabled()) {
				logger.debug("attachTargetSteps: Set '{}' as path tail of '{}'", 
						priorStep.getPredicate().getLocalName(), 
						direct.getPredicate().getLocalName());
			}
			direct.setPathTail(priorStep);
			priorStep.getGroup().setTargetProperty(priorStep);
				
			
		}



		private boolean hasNextStep(List<PathStep> stepList, int start) {
			for (int i=start; i<stepList.size(); i++) {
				PathStep step = stepList.get(i);
				if (step instanceof DirectedStep) {
					return true;
				}
			}
			return false;
		}

		private DirectPropertyModel directPropertyModel(ShapeModel shapeModel, URI predicate, PropertyGroup group,
				PropertyConstraint p) {
			DirectPropertyModel direct = new DirectPropertyModel(predicate, group, p);
			fullAttachProperty(direct, shapeModel, group);
			shapeModel.add(direct);
			return direct;
		}
		
		private void fullAttachProperty(PropertyModel p, ShapeModel shapeModel, PropertyGroup group) {
			attachProperty(p, shapeModel, group);
			shapeModel.add(p);
		}

		private void attachProperty(PropertyModel p, ShapeModel shapeModel, PropertyGroup group) {

			p.setDeclaringShape(shapeModel);
			group.add(p);
		}

		
	}
	
	static class FromItemEnds {

		private ProtoFromItem first;
		private ProtoFromItem last;
		
		public ProtoFromItem getFirst() {
			return first;
		}
		
		public boolean hasSingleItem() {
			return first == last;
		}
		
		public FromItemEnds setFirst(ProtoFromItem item) {
			first = item;
			return this;
		}
		
		public FromItemEnds setSingleItem(ProtoFromItem item) {
			first = last = item;
			return this;
		}
		
		public ProtoFromItem getLast() {
			return last;
		}
		public FromItemEnds setLast(ProtoFromItem last) {
			this.last = last;
			return this;
		}
		
	}

}
