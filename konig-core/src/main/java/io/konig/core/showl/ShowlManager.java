package io.konig.core.showl;

/*
 * #%L
 * Konig Core
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
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.OWL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.konig.core.OwlReasoner;
import io.konig.core.impl.RdfUtil;
import io.konig.core.util.IriTemplate;
import io.konig.core.vocab.Konig;
import io.konig.formula.Direction;
import io.konig.formula.DirectionStep;
import io.konig.formula.Formula;
import io.konig.formula.FormulaVisitor;
import io.konig.formula.PathExpression;
import io.konig.formula.PathStep;
import io.konig.formula.PrimaryExpression;
import io.konig.formula.QuantifiedExpression;
import io.konig.shacl.NodeKind;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeManager;

public class ShowlManager implements ShowlClassManager {
	private static final Logger logger = LoggerFactory.getLogger(ShowlManager.class);
	private Map<Resource,ShowlNodeShapeSet> nodeShapes = new LinkedHashMap<>();
	private Map<URI,ShowlClass> owlClasses = new LinkedHashMap<>();
	private Map<URI, ShowlProperty> properties = new LinkedHashMap<>();
	private ShowlNodeShapeSet emptySet = null;
	private OwlReasoner reasoner;
	private ShowlNodeShapeConsumer consumer;
	

	private ShapeManager shapeManager;
	private List<ShowlNodeShape> classlessShapes;

	private Set<NodeMapping> nodeMappings = null;
	private ShowlSourceNodeSelector sourceNodeSelector;
	
	
	public ShowlManager(ShapeManager shapeManager, OwlReasoner reasoner) {
		this(shapeManager, reasoner, new ShowlRootTargetClassSelector(), null);
	}
	
	public ShowlManager(
		ShapeManager shapeManager, 
		OwlReasoner reasoner, 
		ShowlSourceNodeSelector sourceNodeSelector,
		ShowlNodeShapeConsumer consumer
	) {
		this.shapeManager = shapeManager;
		this.reasoner = reasoner;
		this.sourceNodeSelector = sourceNodeSelector;
		this.consumer = consumer;
	}
	
	public void load() throws ShowlProcessingException {

		clear();
		loadShapes();
		inferTargetClasses();
		inferInverses();
		buildJoinConditions();
	}
	
	public Set<Resource> listNodeShapeIds() {
		return nodeShapes.keySet();
	}
	
	public ShowlNodeShapeSet getNodeShape(Resource shapeId) {
		ShowlNodeShapeSet set =  nodeShapes.get(shapeId);
		return (set == null) ? emptySet() : set;
	}
	public ShowlProperty getProperty(URI propertyId) {
		return properties.get(propertyId);
	}
	public Collection<ShowlProperty> getProperties() {
		return properties.values();
	}
	
	private ShowlNodeShapeSet emptySet() {
		if (emptySet==null) {
			emptySet = new ShowlNodeShapeSet();
		}
		return emptySet;
	}
	
	public OwlReasoner getReasoner() {
		return reasoner;
	}
	
	






	
	public void clear() {
		nodeMappings = null;
		classlessShapes = null;
	}

	

	private void inferInverses() {
		List<ShowlProperty> list = new ArrayList<>(getProperties());
		for (ShowlProperty p : list) {
			if (p.getInverses().isEmpty()) {
				inferInverse(p);
			}
		}
		
	}

	private void inferInverse(ShowlProperty p) {
		URI predicate = p.getPredicate();
		Set<URI> set = reasoner.inverseOf(predicate);
		for (URI other : set) {
			p.addInverse(produceShowlProperty(other));
		}
		
		for (ShowlPropertyShape q : p.getPropertyShapes()) {
			ShowlPropertyShape peer = q.getPeer();
			if (peer != null) {
				if (peer.getDirection() != q.getDirection()) {
					p.addInverse(peer.getProperty());
				}
			}
		}
		
	}

	private void buildJoinConditions() throws ShowlProcessingException {
		nodeMappings = new LinkedHashSet<>();
		for (Shape shape : shapeManager.listShapes()) {
			if (!shape.getShapeDataSource().isEmpty()) {
				for (ShowlNodeShape node : getNodeShape(shape.getId())) {
					buildJoinConditions(node, MappingRole.TARGET, null, consumer);
				}
			}
			
		}
		nodeMappings = null;
		
	}
	
	private void buildJoinConditions(ShowlNodeShape nodeA, MappingRole role, ShowlJoinCondition joinCondition, ShowlNodeShapeConsumer consumer) throws ShowlProcessingException {
		if (nodeA.isNamedRoot() && !isUndefinedClass(nodeA.getOwlClass())) {
			Set<Shape> candidates = sourceNodeSelector.selectCandidateSources(nodeA);
			if (candidates.isEmpty() && role==MappingRole.TARGET) {
				nodeA.setUnmapped(true);
			}
			for (Shape shapeB : candidates) {
				for (ShowlNodeShape nodeB : getNodeShape(shapeB.getId())) {
					// For now we consider only root shapes.
					// TODO: Consider nested shapes.
					
					
					ShowlClass classA = nodeA.getOwlClass();
					ShowlClass classB = nodeB.getOwlClass();

					ShowlNodeShape targetNode = role==MappingRole.TARGET ? nodeA : nodeB;
					ShowlNodeShape sourceNode = role==MappingRole.TARGET ? nodeB : nodeA;
					
					if (classA.isSubClassOf(classB) || classB.isSubClassOf(classA)) {
						
						doJoin(targetNode, sourceNode, joinCondition);
					} else {
						joinNested(sourceNode, targetNode, joinCondition);
					}
				}
			}
			joinObjectProperties(nodeA, joinCondition);
			if (consumer != null) {
				consumer.consume(nodeA);
			}
		}
		
		
//		if (nodeA.isNamedRoot()) {
//			ShowlClass owlClass = nodeA.getOwlClass();
//			if (!isUndefinedClass(owlClass)) {
//				for (ShowlNodeShape nodeB : owlClass.getTargetClassOf()) {
//					if (nodeB == nodeA) {
//						continue;
//					}
//					if (nodeB.isNamedRoot()) {
//						// TODO: consider shapes that are not named roots
//						
//						ShowlNodeShape sourceNode = null;
//						ShowlNodeShape targetNode = null;
//						switch (role) {
//						case SOURCE:
//							sourceNode = nodeA;
//							targetNode = nodeB;
//							break;
//							
//						case TARGET:
//							sourceNode = nodeB;
//							targetNode = nodeA;
//						}
//						
//						doJoin(targetNode, sourceNode, joinCondition);
//						
//					}
//				}
//				joinObjectProperties(nodeA, joinCondition);
//			}
//		}
	}

	private void joinNested(ShowlNodeShape sourceNode, ShowlNodeShape targetNode, ShowlJoinCondition prior) {
		if (logger.isTraceEnabled()) {
			logger.trace("joinNested({}, {})", sourceNode.getPath(), targetNode.getPath());
		}
			
			
		ShowlClass targetClass = targetNode.getOwlClass();
		ShowlNodeShape nestedSource = findNestedSource(sourceNode, targetClass);
		if (nestedSource != null) {
			
			doJoin(targetNode, nestedSource, prior);
		}
		
	}

	private ShowlNodeShape findNestedSource(ShowlNodeShape sourceNode, ShowlClass targetClass) {
		
		NestedShapeSelector selector = new NestedShapeSelector(targetClass);
		selector.scan(sourceNode.getProperties());
		selector.scan(sourceNode.getDerivedProperties());
		selector.scan(sourceNode.getInwardProperties());
		
		return selector.getSelected();
	}

	private void joinObjectProperties(ShowlNodeShape leftNode, ShowlJoinCondition joinCondition) throws ShowlProcessingException {
		joinOutwardObjectPropeties(leftNode, joinCondition);
		joinInwardObjectProperties(leftNode, joinCondition);
		
	}

	private void joinInwardObjectProperties(ShowlNodeShape leftNode, ShowlJoinCondition joinCondition) {
		for (ShowlPropertyShape leftProperty : leftNode.getInwardProperties()) {
			
			ShowlClass childClass = leftProperty.getValueType(ShowlManager.this);
			if (!isUndefinedClass(childClass)) {
				
				for (ShowlNodeShape rightNode : childClass.getTargetClassOf()) {
					// For now consider only named roots. 
					// TODO: consider shapes that are not named roots.
					
					if (rightNode.isNamedRoot()) {
						ShowlPropertyShape rightJoinProperty = rightNode.findProperty(leftProperty.getPredicate());
						if (rightJoinProperty != null) {
							doJoinInwardObjectProperty(leftProperty, rightJoinProperty, joinCondition);
						}
					} else if (rightNode.getAccessor()!=leftProperty) {
						if (logger.isTraceEnabled()) {
							logger.trace("joinInwardObjectProperties: TODO: For {}, handle rightNode {}", leftProperty.getPath(), rightNode.getPath());
						}
					}
				}
			} else if (logger.isTraceEnabled()) {
				logger.trace("joinInwardObjectProperties: Class is not defined: {}", leftProperty.getPath());
			}
		}
		
	}

	private void doJoinInwardObjectProperty(ShowlPropertyShape leftProperty, ShowlPropertyShape rightJoinProperty,
			ShowlJoinCondition joinCondition) {
		
		ShowlPropertyShape leftJoinProperty = leftProperty.getDeclaringShape().findProperty(Konig.id);
		ShowlPropertyShape rightProperty = rightJoinProperty.getDeclaringShape().findProperty(Konig.id);
		
		if (leftJoinProperty == null) {
			if (logger.isTraceEnabled()) {
				logger.trace("doJoinInwardObjectProperty: aborting join because left node does not have an Id: {}", leftProperty.getPath());
			}
			return;
		}
		
		ShowlJoinCondition newJoin = new ShowlTargetToSourceJoinCondition(leftJoinProperty, rightJoinProperty, joinCondition);
		new ShowlMapping(newJoin, leftProperty, rightProperty);
		if (logger.isTraceEnabled()) {
			logger.trace("doJoinInwardObjectProperty: {} ... {}", leftProperty.getPath(), rightJoinProperty.getPath());
		}
		doBuildMappings(leftProperty.getValueShape(), rightJoinProperty.getDeclaringShape(), newJoin);
	}

	private void joinOutwardObjectPropeties(ShowlNodeShape leftNode, ShowlJoinCondition joinCondition) throws ShowlProcessingException {
		joinOutwardObjectProperties(leftNode.getProperties(), joinCondition);
		joinOutwardObjectProperties(leftNode.getDerivedProperties(), joinCondition);
		
	}

	private void joinOutwardObjectProperties(Collection<? extends ShowlPropertyShape> properties,
			ShowlJoinCondition joinCondition) throws ShowlProcessingException {
		
		for (ShowlPropertyShape sourceProperty : properties) {
			ShowlNodeShape sourceNode = sourceProperty.getValueShape();
		
			if (sourceNode != null) {	
				ShowlClass childClass = sourceNode.getOwlClass();
				if (!isUndefinedClass(childClass)) {
					ShowlPropertyShape sourceNodeId = sourceNode.findProperty(Konig.id);
					if (sourceNodeId != null) {
						buildJoinConditions(sourceNode, MappingRole.SOURCE, joinCondition, null);
					}
					
					
				}
				
			}
		}
		
	}


	private void doJoin(ShowlNodeShape targetNode, ShowlNodeShape sourceNode, ShowlJoinCondition joinCondition) {
		ShowlPropertyShape sourceId = sourceNode.findProperty(Konig.id);
		ShowlPropertyShape targetId = targetNode.findProperty(Konig.id);
		
		if (sourceId==null) {
			sourceId = useClassIriTemplate(sourceNode);
			if (sourceId == null) {
				if (logger.isTraceEnabled()) {
					logger.trace("Cannot join {}...{} because left Id is not defined", sourceNode.getPath(), targetNode.getPath());
				}
				return;
			}
		}
		if (targetId==null) {
			targetId = useClassIriTemplate(targetNode);
			if (targetId == null) {
				if (logger.isTraceEnabled()) {
					logger.trace("Cannot join {}...{} because right Id is not defined", sourceNode.getPath(), targetNode.getPath());
				}
				return;
			}
		}
		
		if (sourceId.findJoinCondition(targetId) == null) {
			ShowlJoinCondition join = new ShowlTargetToSourceJoinCondition(targetId, sourceId, joinCondition);
			
			buildMappings(sourceNode, targetNode, join);
		}
		
	}

	private ShowlPropertyShape useClassIriTemplate(ShowlNodeShape sourceNode) {
		ShowlClass owlClass = sourceNode.getOwlClass();
		if (owlClass != null) {
			IriTemplate template = owlClass.getIriTemplate();
			if (template != null) {
				ShowlTemplatePropertyShape p = new ShowlTemplatePropertyShape(sourceNode, null, template);
				sourceNode.addDerivedProperty(p);
				if (logger.isTraceEnabled()) {
					logger.trace("useClassIriTemplate({}) ... {}", sourceNode.getPath(), template.getText());
				}
				return p;
			}
		}
		return null;
	}

	private void buildMappings(ShowlNodeShape leftNode, ShowlNodeShape rightNode, ShowlJoinCondition join) {
		if (logger.isTraceEnabled()) {
			logger.trace("buildMappings({}, {})", leftNode.getPath(), rightNode.getPath());
		}
		doBuildMappings(leftNode, rightNode, join);
		
		// TODO: re-think the following line.  
		// Do we really want to do this?
		doBuildMappings(rightNode, leftNode, join);
		
	}

	private void doBuildMappings(ShowlNodeShape leftNode, ShowlNodeShape rightNode, ShowlJoinCondition join) {
		if (leftNode==null || rightNode==null) {
			return;
		}
		NodeMapping key = new NodeMapping(leftNode, rightNode, join);
		if (!nodeMappings.contains(key)) {
			nodeMappings.add(key);
			doBuildMappings(leftNode.getProperties(), rightNode, join);
			doBuildMappings(leftNode.getDerivedProperties(), rightNode, join);
			buildInwardMappings(leftNode.getInwardProperties(), rightNode, join);
		}
	}

	private void buildInwardMappings(Collection<? extends ShowlPropertyShape> properties, ShowlNodeShape rightNode,
			ShowlJoinCondition join) {
		
		
		
		for (ShowlPropertyShape leftProperty : properties) {
			ShowlPropertyShape rightProperty = rightNode.getInwardProperty(leftProperty.getPredicate());
		
			if (rightProperty==null) {
				for (ShowlProperty inverse : leftProperty.getProperty().getInverses()) {
					rightProperty = rightNode.findProperty(inverse.getPredicate());
					if (rightProperty != null) {
						break;
					}
				}
			}
			
			
			if (rightProperty == null) {
				 
				 if (logger.isTraceEnabled()) {
					 logger.trace("buildInwardMapping - Failed to find mapping for {} in {}", 
							 leftProperty.getPath(), rightNode.getPath());
				 }

				 continue;
			}
			
			ShowlPropertyShape directLeft = directProperty(leftProperty);
			ShowlPropertyShape directRight = directProperty(rightProperty);
			if (directLeft!=null && directRight!=null && directLeft.getMapping(join)==null) {
				produceMapping(join, directLeft, directRight);
			}

			ShowlNodeShape leftChild = leftProperty.getValueShape();
			if (leftChild != null) {
				ShowlNodeShape rightChild = rightProperty.getValueShape();
				if (rightChild != null) {
					buildMappings(leftChild, rightChild, join);
				}
			}
			
		}
		
	}
	private void doBuildMappings(Collection<? extends ShowlPropertyShape> properties, ShowlNodeShape rightNode,
			ShowlJoinCondition join) {

		ShowlPropertyShape rightAccessor = rightNode.getAccessor();
		for (ShowlPropertyShape leftProperty : properties) {
			ShowlPropertyShape rightProperty = rightNode.findProperty(leftProperty.getPredicate());
			if (rightProperty == null) {
				
				if (
					rightAccessor instanceof ShowlInwardPropertyShape && 
					rightAccessor.getPredicate().equals(leftProperty.getPredicate())
				) { 
					
					ShowlPropertyShape rightId = produceIdProperty(rightAccessor.getDeclaringShape());
					produceMapping(join, leftProperty, rightId);
					if (leftProperty.getValueShape() != null) {
						
						doBuildMappings(leftProperty.getValueShape(), rightAccessor.getDeclaringShape(), join);
					}
				} else {
					if (logger.isTraceEnabled()) {
						logger.trace("doBuildMappings - mapping not found for {} in {}",
							leftProperty.getPath(), rightNode.getPath());
					}
					continue;
				}
			}
//			if (Konig.id.equals(leftProperty.getPredicate()) || Konig.id.equals(rightProperty.getPredicate())) {
//				// TODO: We ought to find a better way to handle konig:id mappings
//				
//				// For now, we skip them
//				if (logger.isTraceEnabled()) {
//					logger.trace("doBuildMappings - Skipping mapping: {}...{}", 
//						leftProperty.getPath(), rightProperty.getPath());
//				}
//				continue;
//			}
			
			leftProperty = propertyToMap(leftProperty);
			rightProperty = propertyToMap(rightProperty);
			

			if (leftProperty == null || rightProperty==null) {
				continue;
			}
			produceMapping(join, leftProperty, rightProperty);
			
			

			ShowlNodeShape leftChild = leftProperty.getValueShape();
			if (leftChild != null) {
				ShowlNodeShape rightChild = rightProperty.getValueShape();
				if (rightChild != null) {
					buildMappings(leftChild, rightChild, join);
				}
			}
			
		}
		
	}

	private ShowlPropertyShape produceIdProperty(ShowlNodeShape rightNode) {
		ShowlPropertyShape id = rightNode.findProperty(Konig.id);
		if (id == null) {
			id = useClassIriTemplate(rightNode);
		}
		return id;
	}

	private ShowlPropertyShape propertyToMap(ShowlPropertyShape p) {
		ShowlPropertyShape result = directProperty(p);
		if (result != null) {
			return result;
		}
		result = derivedByFormula(p);
		if (result != null) {
			return result;
		}
		return p;
	}

	private ShowlDerivedPropertyShape derivedByFormula(ShowlPropertyShape p) {
		if (p instanceof ShowlDerivedPropertyShape) {
			ShowlDerivedPropertyShape derived = (ShowlDerivedPropertyShape) p;
			if (derived.getPropertyConstraint()!=null && derived.getPropertyConstraint().getFormula()!=null) {
				return derived;
			}
		}
			
		return null;
	}

	private void produceMapping(ShowlJoinCondition join, ShowlPropertyShape left,
			ShowlPropertyShape right) {
		
		if (join.isJoinProperty(left) && join.isJoinProperty(right)) {
			new ShowlJoinMapping(join);
		} else {
			new ShowlMapping(join, left, right);
		}
		
	}

	private ShowlPropertyShape directProperty(ShowlPropertyShape p) {
		return 
			p==null      ? null : 
			p.isDirect() ? p : p.getPeer();
	}

	private boolean isUndefinedClass(ShowlClass owlClass) {
		
		return ShowlUtil.isUndefinedClass(owlClass);
	}

	private void buildMapping(
			ShowlJoinCondition join, 
			ShowlNodeShape leftNode, 
			ShowlPropertyShape leftProperty,
			ShowlNodeShape rightNode) {
		if (logger.isTraceEnabled()) {
			logger.trace("buildMapping: {}...{}", leftProperty.getPath(), rightNode.getPath());
		}
		if (leftProperty.getMapping(join)==null) {
			ShowlPropertyShape rightProperty = rightNode.findProperty(leftProperty.getPredicate());
			
			if (rightProperty == null) {
				Set<ShowlProperty> set = leftProperty.getProperty().getConnectedProperties();
				for (ShowlProperty q : set) {
					if (q == leftProperty.getProperty()) {
						continue;
					}
					rightProperty = rightNode.findProperty(q.getPredicate());
					if (rightProperty != null) {
						break;
					}
					
				}
			}
			
			if (rightProperty != null) {
				if (rightProperty.isLeaf() && !rightProperty.isDirect()) {
					rightProperty = rightProperty.getPeer();
					if (rightProperty == null) {
						return;
					}
					
				}
				ShowlMapping mapping = new ShowlMapping(join, leftProperty, rightProperty);
				leftProperty.addMapping(mapping);
				rightProperty.addMapping(mapping);
				buildNestedMappings(join, leftProperty, rightProperty);
			}
			
			
		}
		
		
		
	}

	private void buildNestedMappings(ShowlJoinCondition join, ShowlPropertyShape leftProperty,
			ShowlPropertyShape rightProperty) {

		ShowlNodeShape leftNode = leftProperty.getValueShape();
		ShowlNodeShape rightNode = rightProperty.getValueShape();
		if (leftNode!=null && rightNode!=null) {
			for (ShowlPropertyShape p : leftNode.getProperties()) {
				buildMapping(join, leftNode, p, rightNode);
			}
		}
		
	}

	public void inferTargetClasses() {
		for (ShowlNodeShape gns : classlessShapes) {
			inferTargetClass(gns);
		}
		classlessShapes = null;
		
	}

	private void inferTargetClass(ShowlNodeShape node) {
		
		if (!ShowlUtil.isUndefinedClass(node.getOwlClass())) {
			return;
		}
		
		DomainReasoner domainReasoner = new DomainReasoner(reasoner);
		
		Set<ShowlPropertyShape> allProperties = node.allOutwardProperties();
		for (ShowlPropertyShape p : allProperties) {
			
			if (p.isNestedAccordingToFormula()) {
				continue;
			}
			
			ShowlProperty property = p.getProperty();
			if (property != null) {
				if (property.getDomain() != null) {
					domainReasoner.require(property.getDomain().getId());
				} else {
					domainReasoner.domainIncludes(property.domainIncludes(this));
				}
			}
		}
		
		Set<URI> candidates = domainReasoner.getRequiredClasses();
		
		if (candidates.isEmpty()) {
			candidates = inferTargetClassFromFormula(node, domainReasoner);
		}
		
		if (candidates.size()==1) {
			URI owlClass = candidates.iterator().next();
			replaceOwlClass(node, owlClass);
			if (logger.isTraceEnabled()) {
				logger.trace("inferTargetClass: Set {} as target class of {}", owlClass.getLocalName(), node.getPath());
			}
		} else {
			
		
			
			if (logger.isWarnEnabled()) {
				if (candidates.isEmpty()) {
					candidates = domainReasoner.getAllClasses();
				}
				if (candidates.isEmpty()) {
					logger.warn("No candidates found for target class of " + node.getPath());
				} else {
					StringBuilder builder = new StringBuilder();
					builder.append("Target class at " + node.getPath() + " is ambiguous.  Candidates include\n");
					for (URI c : candidates) {
						builder.append("  ");
						builder.append(c.getLocalName());
						builder.append('\n');
					}
					logger.warn(builder.toString());
				
				}
			}
		}
		
		
	}

	/**
	 * Add OWL classes from the domain of a given Property to the set of candidates.
	 * @param candidates The set of candidates to be augmented.
	 * @param property
	 * @return true if there is no conflict between the existing candidates and the given property
	 */
	private boolean updateCandidates(Set<URI> candidates, ShowlProperty property) {

		Set<URI> domainIncludes = property.domainIncludes(this);
		
		// Remove owl:Thing and konig:Undefined from consideration
		Iterator<URI> sequence = domainIncludes.iterator();
		while (sequence.hasNext()) {
			URI domain = sequence.next();
			if (OWL.THING.equals(domain) || Konig.Undefined.equals(domain)) {
				sequence.remove();
			}
		}
		
		if (domainIncludes.isEmpty()) {
			return true;
		}
		
		if (candidates.isEmpty()) {
			candidates.addAll(domainIncludes);
			return true;
		}
		
		
		boolean compatible = true;
		
		domainLoop : for (URI domain : domainIncludes) {
			sequence = candidates.iterator();
			while (sequence.hasNext()) {
				URI candidate = sequence.next();
				if (domain.equals(candidate)) {
					continue domainLoop;
				}
				if (reasoner.isSubClassOf(domain, candidate)) {
					// replace candidate with the more narrow domain
					sequence.remove();
					candidates.add(domain);
					continue domainLoop;
				}
				if (reasoner.isSubClassOf(candidate, domain)) {
					// The domain element is consistent with the existing
					// candidates since there is a candidate that is a 
					// subclass of the domain.
					
					// Keep the more narrow candidate.
					
					continue domainLoop;
				}
			}
		}
		
		
		
		
		
		return compatible;
	}
	



	private Set<URI> inferTargetClassFromFormula(ShowlNodeShape node, DomainReasoner domainReasoner) {
		
		boolean updated = false;
		for (ShowlPropertyShape p : node.getProperties()) {
			
			PropertyConstraint constraint = p.getPropertyConstraint();
			if (constraint != null) {
				QuantifiedExpression formula = constraint.getFormula();
				if (formula != null) {
					PrimaryExpression primary = formula.asPrimaryExpression();
					if (primary instanceof PathExpression) {
						PathExpression path = (PathExpression) primary;
						List<PathStep> stepList = path.getStepList();
						if (stepList.size() == 1) {
							PathStep step = stepList.get(0);
							if (step instanceof DirectionStep) {
								DirectionStep dirStep = (DirectionStep) step;
								if (dirStep.getDirection() == Direction.OUT) {
									URI predicate = dirStep.getTerm().getIri();
									ShowlProperty property = getProperty(predicate);
									if (property != null) {
										Set<URI> domainIncludes = property.domainIncludes(this);
										
										domainReasoner.domainIncludes(domainIncludes);
										updated = true;
										
									}
								}
							}
						}
					}
				}
			}
		}
		
		return updated ? domainReasoner.getRequiredClasses() : Collections.emptySet();
		
	}

	private void replaceOwlClass(ShowlNodeShape node, URI owlClassId) {

		ShowlClass newClass = produceOwlClass(owlClassId);
		node.setOwlClass(newClass);
		
		if (logger.isInfoEnabled()) {
			logger.info("Set OWL Class of " + node.getPath() + " as " + "<" + owlClassId.stringValue() + ">");
		}
		
	}



	protected void loadShapes() {
		classlessShapes = new ArrayList<>();
		Set<Shape> rootShapes = selectShapes();
		for (Shape shape : rootShapes) {
			createNodeShape(null, shape);
		}
	}
	
	protected Set<Shape> selectShapes() {
		Set<Shape> result = new LinkedHashSet<>();
		Map<Shape,Boolean> hasReference = new LinkedHashMap<>();
		List<Shape> shapeList = shapeManager.listShapes();
		for (Shape shape : shapeList) {
			putReferences(shape.getProperty(), hasReference);
		}
		for (Shape shape : shapeList) {
			if (!shape.getShapeDataSource().isEmpty() || hasReference.get(shape)==null) {
				result.add(shape);
			}
		}
		
		return result;
	}

	

	private void putReferences(List<PropertyConstraint> property, Map<Shape, Boolean> hasReference) {
		for (PropertyConstraint p : property) {
			Shape shape = p.getShape();
			if (shape != null) {
				
				if (hasReference.put(shape, Boolean.TRUE)==null) {
					putReferences(shape.getProperty(), hasReference);
				}
			}
			
		}
		
	}

	private ShowlNodeShape createShowlNodeShape(ShowlPropertyShape accessor, Shape shape, ShowlClass owlClass) {

		ShowlNodeShape result = new ShowlNodeShape(accessor, shape, owlClass);
		if (Konig.Undefined.equals(owlClass.getId())) {
			classlessShapes.add(result);
		}
		ShowlNodeShapeSet set = nodeShapes.get(shape.getId());
		if (set == null) {
			set = new ShowlNodeShapeSet();
			nodeShapes.put(shape.getId(), set);
		}
		set.add(result);
		return result;
	}
	
	private ShowlNodeShape createNodeShape(ShowlPropertyShape accessor, Shape shape) {
		ShowlClass owlClass = targetOwlClass(accessor, shape);
		ShowlNodeShape result = createShowlNodeShape(accessor, shape, owlClass);
		addProperties(result);
		return result;
	}

	private ShowlClass targetOwlClass(ShowlPropertyShape accessor, Shape shape) {
		if (accessor != null) {
			PropertyConstraint p = accessor.getPropertyConstraint();
			if (p != null) {
				if (p.getValueClass() instanceof URI) {
					return  produceOwlClass((URI)p.getValueClass());
				}
			}
		}
		URI classId = shape.getTargetClass();
		if (classId ==null) {
			classId = Konig.Undefined;
		}
		return produceOwlClass(classId);

	}



	private void addProperties(ShowlNodeShape declaringShape) {
		if (logger.isTraceEnabled()) {
			logger.trace("addProperties({})", declaringShape.getPath());
		}
		
		addIdProperty(declaringShape);
		
		for (PropertyConstraint p : declaringShape.getShape().getProperty()) {
			URI predicate = p.getPredicate();
			if (predicate != null) {
				ShowlProperty property = produceShowlProperty(predicate);
				ShowlDirectPropertyShape q = createDirectPropertyShape(declaringShape, property, p);
				declaringShape.addProperty(q);
				processFormula(q);
				
				Shape childShape = p.getShape();
				if (childShape != null) {
					if (declaringShape.hasAncestor(childShape.getId())) {
						error("Cyclic shape detected at: " + q.getPath());
					}
					createNodeShape(q, childShape);
				}
			}
		}
		
		for (PropertyConstraint p : declaringShape.getShape().getDerivedProperty()) {
			URI predicate = p.getPredicate();
			if (predicate != null) {
				ShowlProperty property = produceShowlProperty(predicate);
				ShowlFormulaPropertyShape q = createFormulaPropertyShape(declaringShape, property, p);
				declaringShape.addDerivedProperty(q);
				
				Shape childShape = p.getShape();
				if (childShape != null) {
					if (declaringShape.hasAncestor(childShape.getId())) {
						error("Cyclic shape detected at: " + q.getPath());
					}
					// TODO: create node shape for childShape.
					error("child shape of derived property not supported yet for " + q.getPath());
				}
			}
		}
		
	}
	
	private ShowlFormulaPropertyShape createFormulaPropertyShape(ShowlNodeShape declaringShape, ShowlProperty property,
			PropertyConstraint constraint) {

		ShowlFormulaPropertyShape p = new ShowlFormulaPropertyShape(declaringShape, property, constraint);
		if (logger.isTraceEnabled()) {
			logger.trace("createDerivedPropertyShape: {}", p.getPath());
		}
		return p;
	}

	private void addIdProperty(ShowlNodeShape declaringShape) {
		if (declaringShape.getShape().getIriTemplate() != null) {
			ShowlProperty property = produceShowlProperty(Konig.id);
			ShowlTemplatePropertyShape p = new ShowlTemplatePropertyShape(
					declaringShape, property, declaringShape.getShape().getIriTemplate());
			declaringShape.addDerivedProperty(p);
		} else if (declaringShape.getShape().getNodeKind() == NodeKind.IRI) {
			ShowlProperty property = produceShowlProperty(Konig.id);
			ShowlDirectPropertyShape p = createDirectPropertyShape(declaringShape, property, null);
			declaringShape.addProperty(p);
		}
		
	}

	private ShowlDirectPropertyShape createDirectPropertyShape(ShowlNodeShape declaringShape, ShowlProperty property,
			PropertyConstraint constraint) {
		ShowlDirectPropertyShape p = new ShowlDirectPropertyShape(declaringShape, property, constraint);
		if (logger.isTraceEnabled()) {
			logger.trace("createDirectPropertyShape: created {}", p.getPath());
		}
		return p;
	}

	private void error(String text) {
		logger.error(text);
	}

	private ShowlProperty produceShowlProperty(URI predicate) {
		ShowlProperty property = properties.get(predicate);
		if (property == null) {
			property = new ShowlProperty(predicate);
			properties.put(predicate, property);

			URI domain = RdfUtil.uri(reasoner.getDomain(predicate));
			URI range = RdfUtil.uri(reasoner.getRange(predicate));
			
			if (domain != null) {
				property.setDomain(produceOwlClass(domain));
			}
			if (range != null) {
				property.setRange(produceOwlClass(range));
			}
		}
		return property;
		
	}
	
	private void processFormula(ShowlPropertyShape ps) {
		PropertyConstraint p = ps.getPropertyConstraint();
		QuantifiedExpression e = (p==null) ? null : p.getFormula();
		if (e != null) {
			if (logger.isTraceEnabled()) {
				logger.trace("processFormula({})", ps.getPath());
			}
			e.dispatch(new PathVisitor(ps));
		}
		
	}

	
	
	class PathVisitor implements FormulaVisitor {
		private ShowlPropertyShape propertyShape;
		
		public PathVisitor(ShowlPropertyShape propertyShape) {
			this.propertyShape = propertyShape;
		}

		@Override
		public void enter(Formula formula) {
			if (formula instanceof PathExpression) {
				PathExpression path = (PathExpression) formula;
				ShowlNodeShape declaringShape = propertyShape.getDeclaringShape();
				
				String shapeIdValue = declaringShape.getShape().getId().stringValue();
				ShowlPropertyShape prior = null;
				List<PathStep> stepList = path.getStepList();
				for (int i=0; i<stepList.size(); i++) {
					
					PathStep step = stepList.get(i);
					if (step instanceof DirectionStep) {
						DirectionStep dirStep = (DirectionStep) step;
						URI predicate = dirStep.getTerm().getIri();
						ShowlProperty property = produceShowlProperty(predicate);

						ShowlNodeShape parentShape = null;
						ShowlPropertyShape thisStep = null;
						shapeIdValue += dirStep.getDirection().getSymbol() + predicate.getLocalName();
						switch (dirStep.getDirection()) {
						case OUT : {
							if (i==0) {
								parentShape = declaringShape;
							} else {
								// Get OWL Class for parent shape, i.e. the property domain.
								ShowlClass owlClass = property.inferDomain(ShowlManager.this);
								DirectionStep prevStep = path.directionStepBefore(i);
								ShowlClass prevClass = valueClassOf(prevStep);
								owlClass = mostSpecificClass(owlClass, prevClass);
								parentShape = createNodeShape(prior, shapeIdValue, owlClass);
							}

							ShowlPropertyShape p = outwardProperty(parentShape, property);
							
							thisStep = p;
							break;								
						}
						case IN : {
							if (i==0) {
								parentShape = declaringShape;
							} else {
								// Get OWL Class for parent shape, i.e. the property range.
								ShowlClass owlClass = property.inferRange(ShowlManager.this);
								DirectionStep prevStep = path.directionStepBefore(i);
								ShowlClass prevClass = valueClassOf(prevStep);
								owlClass = mostSpecificClass(owlClass, prevClass);
								parentShape = createNodeShape(prior, shapeIdValue, owlClass);
								
							}
							
							ShowlInwardPropertyShape p = inwardProperty(parentShape, property);
							thisStep = p;
						}
						}
						
						prior = thisStep;
						
					} else {
						error("HasStep not supported yet");
					}
				}
				if (prior != null) {
					propertyShape.setPeer(prior);
				}
				
				
			}
			
		}
		

		private ShowlInwardPropertyShape inwardProperty(ShowlNodeShape parentShape, ShowlProperty property) {

			ShowlInwardPropertyShape prior = parentShape.getInwardProperty(property.getPredicate());
			if (prior != null) {
				return prior;
			}
			ShowlInwardPropertyShape p = new ShowlInwardPropertyShape(parentShape, property);
			parentShape.addInwardProperty(p);
			return p;
		}

		private ShowlPropertyShape outwardProperty(ShowlNodeShape parentShape, ShowlProperty property) {
			
			ShowlPropertyShape prior = parentShape.findProperty(property.getPredicate());
			if (prior != null) {
				return prior;
			}

			ShowlOutwardPropertyShape p = new ShowlOutwardPropertyShape(parentShape, property);
			parentShape.addDerivedProperty(p);
			if (logger.isTraceEnabled()) {
				logger.trace("outwardProperty: created {}", p.getPath());
			}
			return p;
		}

		private ShowlNodeShape createNodeShape(ShowlPropertyShape accessor, String shapeIdValue,
				ShowlClass owlClass) {
			ShowlNodeShape value = accessor.getValueShape();
			if (value != null) {
				return value;
			}
			URI shapeId = new URIImpl(shapeIdValue);
			Shape shape = new Shape(shapeId);
			return createShowlNodeShape(accessor, shape, owlClass);
		}

		/**
		 * Compute the OWL Class for the value obtained by traversing a given step.
		 */
		private ShowlClass valueClassOf(DirectionStep step) {
			if (step != null) {
				ShowlProperty property = produceShowlProperty(step.getTerm().getIri());
				switch (step.getDirection()) {
				case OUT: return property.inferRange(ShowlManager.this);
				case IN: return property.inferDomain(ShowlManager.this);
				}
			}
			return produceOwlClass(Konig.Undefined);
		}

		@Override
		public void exit(Formula formula) {
			// Do Nothing
		}
		
	}
	
	


	private ShowlClass mostSpecificClass(ShowlClass a, ShowlClass b) {
		ShowlClass result = 
			a==null ? b :
			b==null ? a :
			reasoner.isSubClassOf(a.getId(), b.getId()) ? a :
			b;
		
		return result==null ? produceOwlClass(Konig.Undefined) : result;
	}

		
	
	

	ShowlClass produceOwlClass(URI owlClass) {
		if (owlClass == null) {
			owlClass = Konig.Undefined;
		}
		ShowlClass result = owlClasses.get(owlClass);
		if (result == null) {
			result = new ShowlClass(this, owlClass);
			owlClasses.put(owlClass, result);
		}
		return result;
	}
	
	private static class NodeMapping {
		private ShowlNodeShape leftNode;
		private ShowlNodeShape rightNode;
		private ShowlJoinCondition join;
		
		public NodeMapping(ShowlNodeShape leftNode, ShowlNodeShape rightNode, ShowlJoinCondition join) {
			this.leftNode = leftNode;
			this.rightNode = rightNode;
			this.join = join;
		}
		
		public boolean equals(Object other) {
			if (other instanceof NodeMapping) {
				final NodeMapping b = (NodeMapping) other;
				return b.leftNode==this.leftNode && b.rightNode==this.rightNode && b.join==this.join;
			}
			return false;
		}
		
		@Override
		public int hashCode() {
			return Objects.hash(leftNode, rightNode, join);
		}
		
	}
	
	private static enum MappingRole {
		SOURCE,
		TARGET
	}
	
	private class DomainReasoner  {
		private OwlReasoner reasoner;
		private Set<ModalClass> candidates = new HashSet<>();
		
		
		public DomainReasoner(OwlReasoner reasoner) {
			this.reasoner = reasoner;
		}
		
		public void require(URI owlClass) {
			candidates.add(new ModalClass(owlClass, true));
		}

		public void domainIncludes(Set<URI> domainIncludes) {
			
			boolean required = domainIncludes.size()==1;
			for (URI domain : domainIncludes) {
				URI matched = null;
				for (ModalClass modal : candidates) {
					URI candidate = modal.getOwlClass();
					if (reasoner.isSubClassOf(domain, candidate)) {
						// The domain is more narrow than the candidate
						// so replace the candidate with the domain.
						
						modal.update(domain, required);
						matched = domain;
					} else if (reasoner.isSubClassOf(candidate, domain)) {
						// The existing candidate is more narrow than the domain
						// so keep the existing candidate
						
						modal.update(candidate, required);
						matched = candidate;
					}
				}
				if (matched==null) {
					candidates.add(new ModalClass(domain, required));
				}
			}
		}
		
		public Set<URI> getRequiredClasses() {
			Set<URI> result = new HashSet<>();
			for (ModalClass modal : candidates) {
				if (modal.isRequired()) {
					result.add(modal.getOwlClass());
				}
			}
			return result;
		}
		
		public Set<URI> getAllClasses() {
			Set<URI> result = new HashSet<>();
			for (ModalClass modal : candidates) {
				result.add(modal.getOwlClass());
			}
			return result;
		}
	}
	
	/**
	 * A structure that decorates the URI for an OWL class with
	 * a flag that specifies whether or not the class is required.
	 *
	 */
	private class ModalClass {
		private boolean required;
		private URI owlClass;
		public ModalClass(URI owlClass, boolean required) {
			this.required = required;
			this.owlClass = owlClass;
		}
		public boolean isRequired() {
			return required;
		}
		public URI getOwlClass() {
			return owlClass;
		}
		
		public void update(URI owlClass, boolean required) {
			this.owlClass = owlClass;
			if (required) {
				this.required = true;
			}
		}
		
		
	}
	
	private static class NestedShapeSelector {
		private ShowlNodeShape selected;
		private ShowlClass owlClass;
		private boolean failed=false;
		
		
		
		public NestedShapeSelector(ShowlClass owlClass) {
			this.owlClass = owlClass;
		}

		void scan(Collection<? extends ShowlPropertyShape> list) {
			if (!failed) {
				for (ShowlPropertyShape p : list) {
					ShowlNodeShape nested = p.getValueShape();
					if (nested != null &&  nested.getOwlClass().isSubClassOf(owlClass)) {
						if (selected == null) {
							selected = nested;
						} else {
							if (logger.isTraceEnabled() && failed == false) {
								logger.trace(
									"Nested shape selection is ambiguous. Options include {} and {}",
									selected.getPath(), nested.getPath());
							}
							failed = true;
							return;
						}
					}
				}
			}
		}
		
		
		
		public ShowlNodeShape getSelected() {
			return failed ? null : selected;
		}
		
	}




	@Override
	public Collection<ShowlClass> listClasses() {
		return owlClasses.values();
	}

	@Override
	public ShowlClass findClassById(URI classId) {
		return owlClasses.get(classId);
	}
	
}
