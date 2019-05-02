package io.konig.core.showl;

import java.text.MessageFormat;

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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.openrdf.model.Namespace;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.OWL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.konig.core.NamespaceManager;
import io.konig.core.OwlReasoner;
import io.konig.core.impl.RdfUtil;
import io.konig.core.showl.expression.ShowlExpressionBuilder;
import io.konig.core.util.IriTemplate;
import io.konig.core.vocab.Konig;
import io.konig.datasource.DataSource;
import io.konig.formula.Direction;
import io.konig.formula.DirectionStep;
import io.konig.formula.Expression;
import io.konig.formula.Formula;
import io.konig.formula.FormulaVisitor;
import io.konig.formula.HasPathStep;
import io.konig.formula.IriValue;
import io.konig.formula.LiteralFormula;
import io.konig.formula.PathExpression;
import io.konig.formula.PathStep;
import io.konig.formula.PathTerm;
import io.konig.formula.PredicateObjectList;
import io.konig.formula.PrimaryExpression;
import io.konig.formula.QuantifiedExpression;
import io.konig.formula.VariableTerm;
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
	private ShowlTargetNodeSelector targetNodeSelector;
	private ShowlSourceNodeSelector sourceNodeSelector;
	private Map<Resource, EnumMappingAction> enumMappingActions;
	private ShowlFactory showlFactory;
	private ShowlMappingStrategy mappingStrategy;
	
	private StaticDataSource staticDataSource = null;
	
	private Set<ShowlNodeShape> consumable;
	
	
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
		this.showlFactory = new Factory();
	}
	
	public ShowlManager(
			ShapeManager shapeManager, 
			OwlReasoner reasoner, 
			ShowlTargetNodeSelector targetNodeSelector,
			ShowlSourceNodeSelector sourceNodeSelector,
			ShowlNodeShapeConsumer consumer
		) {
			this.shapeManager = shapeManager;
			this.reasoner = reasoner;
			this.targetNodeSelector = targetNodeSelector;
			this.sourceNodeSelector = sourceNodeSelector;
			this.mappingStrategy = new RankedSourceMappingStrategy();
			this.consumer = consumer;
			this.showlFactory = new Factory();
		}
	
	public ShapeManager getShapeManager() {
		return shapeManager;
	}
	
	public void load() throws ShowlProcessingException {

		clear();
		loadShapes();
		inferTargetClasses();
		inferInverses();
		buildJoinConditions();
	}
	
	public ShowlTargetNodeSelector getTargetNodeSelector() {
		return targetNodeSelector;
	}

	public void setTargetNodeSelector(ShowlTargetNodeSelector targetNodeSelector) {
		this.targetNodeSelector = targetNodeSelector;
	}

	public ShowlSourceNodeSelector getSourceNodeSelector() {
		return sourceNodeSelector;
	}

	public void setSourceNodeSelector(ShowlSourceNodeSelector sourceNodeSelector) {
		this.sourceNodeSelector = sourceNodeSelector;
	}

	public Set<Resource> listNodeShapeIds() {
		return nodeShapes.keySet();
	}
	
	public ShowlNodeShape getNodeShape(Resource shapeId) {
		return nodeShapes.get(shapeId).top();
	}
	
	public ShowlNodeShapeSet getNodeShapeSet(Resource shapeId) {
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

	protected void inferInverses() {
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
		enumMappingActions = new HashMap<>();
		consumable = null;
		
		buildTransformExpressions();
		
		// TODO: remove the body of this method and use buildTransformExpressions only
		
//		for (Shape shape : shapeManager.listShapes()) {
//			if (!shape.getShapeDataSource().isEmpty()) {
//				for (ShowlNodeShape node : getNodeShape(shape.getId())) {
//					buildJoinConditions(node, MappingRole.TARGET, null, consumer);
//				}
//			}
//			
//		}
//		
//		for (EnumMappingAction action : enumMappingActions.values()) {
//			action.execute();
//		}
//		
		if (consumable != null) {
			for (ShowlNodeShape node : consumable) {
				consumer.consume(node);
			}
		}
		consumable = null;
		nodeMappings = null;
		enumMappingActions = null;
		
	}
	
	private void buildTransformExpressions() {
		for (Shape shape : shapeManager.listShapes()) {
			
			List<ShowlNodeShape> targetList = targetNodeSelector.produceTargetNodes(showlFactory, shape);
			for (ShowlNodeShape targetNode : targetList) {
				buildTransform(targetNode);
				
				if (mappingStrategy != null) {
					Set<ShowlPropertyShape> unmapped = mappingStrategy.selectMappings(this, targetNode);
					if (unmapped.isEmpty()) {
						
						if (consumer != null) {
							consumer.consume(targetNode);
						}
					} else {
						// TODO: Seek other source NodeShapes that may be joined to the selected shapes
						// For now, just log the missing shapes
						
						logger.warn("Failed to map the following properties:...");
						for (ShowlPropertyShape p : unmapped) {
							logger.warn("    {}", p.getPath());
						}
					}
				}
				
			}
			
		}
		
	}

	private void buildTransform(ShowlNodeShape targetNode) {
		
		
		bindVariables(targetNode);
		
		Set<ShowlNodeShape> candidates = sourceNodeSelector.selectCandidateSources(showlFactory, targetNode);
		if (candidates.isEmpty()) {
			if (logger.isWarnEnabled()) {
				logger.warn("buildTransform:  Cannot build transform for {}.  No candidate sources were found.", 
						targetNode.getPath());
			}
		}
		
		for (ShowlNodeShape sourceNode : candidates) {

			if (logger.isTraceEnabled()) {
				logger.trace("buildTransform: From source {} to target {}", sourceNode.getPath(), targetNode.getPath());
			}
		    for (ShowlDirectPropertyShape targetProperty : targetNode.getProperties()) {
		    	
		    	// Check for a direct match.
		    	
		    	boolean matched = matchProperty(sourceNode, targetProperty);
		    	
		    	if (!matched) {
		    		
		    		// Check for a synonym match
		    	
			    	ShowlPropertyShape synonym = targetProperty.getSynonym();
			    	matched = matchProperty(sourceNode, synonym);
		    	}
		    	
		    	if (!matched) {
		    		System.out.println("now what?");
		    	}
		    }
		}
		
		// Add mappings to static enum values
		
		for (ShowlDirectPropertyShape direct : targetNode.getProperties()) {
			
			ShowlNodeShape valueShape = direct.getValueShape();
			if (valueShape != null) {
				ShowlClass owlClass = valueShape.getOwlClass();
				if (owlClass != null && reasoner.isEnumerationClass(owlClass.getId())) {
					addEnumMappings(valueShape);
				}
			}
			
		}
		
		
	}


	private void bindVariables(ShowlNodeShape targetNode) {
		for (PropertyConstraint constraint : targetNode.getShape().getVariable()) {
			URI predicate = constraint.getPredicate();
			
			ShowlPropertyShape var = targetNode.findOut(predicate);
			if (var == null) {
				fail("Variable ?{} not found in {}", predicate.getLocalName(), targetNode.getPath());
			}
			
			ShowlNodeShape varNode = var.getValueShape();
			if (varNode != null) {
				Set<ShowlNodeShape> candidates = sourceNodeSelector.selectCandidateSources(showlFactory, varNode);
			}
		}
		
	}

	private void addEnumMappings(ShowlNodeShape targetShape) {
		
		// We need two NodeShape instances for an enumeration.
		
		// The globalEnumShape keeps track of all properties of the Enumeration class that are referenced.
		// This is necessary because the same enumeration might be referenced at more than one point within 
		// the transform.  It's unlikely, but possible so we need to handle that case.
		
		// The localEnumShape keeps track of the properties of the Enumeration class that are referenced by
		// the given targetShape.
		
		ShowlNodeShape globalEnumShape = showlFactory.logicalNodeShape(targetShape.getOwlClass().getId());
		ShowlNodeShape localEnumShape = localEnumShape(globalEnumShape, targetShape);
		
		localEnumShape.setLogicalNodeShape(globalEnumShape);
		
		
		for (ShowlDirectPropertyShape direct : targetShape.getProperties()) {
			
			// For now, we assume that every property of the enumTargetShape is available statically within
			// the background graph.
			
			// We may need to back off that assumption later and consider synonyms.
			
			// Add the property from the target shape to both the global and the local Enumeration NodeShape.
			// In the case of the globalEnumShape, this call will do nothing if the property already exists.
			
			directProperty(globalEnumShape, direct.getProperty());
			
			ShowlDirectPropertyShape localEnumProperty = directProperty(localEnumShape, direct.getProperty());
			
			direct.addExpression(new ShowlEnumPropertyExpression(localEnumProperty));
			
			if (logger.isTraceEnabled()) {
				logger.trace("addEnumMapping: {}...{}", localEnumProperty.getPath(), direct.getPath());
			}
		}
		
	}

	

	private ShowlNodeShape localEnumShape(ShowlNodeShape globalEnumShape, ShowlNodeShape targetShape) {
		ShowlNodeShape local = new ShowlNodeShape(null, globalEnumShape.getShape(), globalEnumShape.getOwlClass());
		local.setTargetProperty(targetShape.getAccessor());
		local.setShapeDataSource(new ShowlDataSource(local, staticDataSource()));
		
		return local;
	}

	private DataSource staticDataSource() {
		if (staticDataSource == null) {
			staticDataSource = new StaticDataSource();
		}
		return staticDataSource;
	}

	private ShowlDirectPropertyShape directProperty(ShowlNodeShape enumShape, ShowlProperty property) {
		URI predicate = property.getPredicate();
		ShowlDirectPropertyShape direct = enumShape.getProperty(predicate);
		if (direct == null) {
			direct = createDirectPropertyShape(enumShape, property, null);
			enumShape.addProperty(direct);
		}
		return direct;
	}

	private boolean matchProperty(ShowlNodeShape sourceNode, ShowlPropertyShape targetProperty) {
		if (targetProperty == null) {
			return false;
		}
		URI targetPredicate = targetProperty.getPredicate();
		ShowlPropertyShape sourceProperty = sourceNode.getProperty(targetPredicate);
    	
    	if (match(sourceProperty, targetProperty)) {
    		return true;
    	}
    	
    	// Check for a derived property in the source node with the same predicate as the target property.
    	
    	ShowlDerivedPropertyList derivedList = sourceNode.getDerivedProperty(targetPredicate);
    	sourceProperty = derivedList==null ? null : derivedList.unfiltered();
    	
    	if (match(sourceProperty, targetProperty)) {
    		return true;
    	}
		return false;
	}

	private boolean match(ShowlPropertyShape sourceProperty, ShowlPropertyShape targetProperty) {

    	if (sourceProperty != null) {
    		
    		
    		if (!(sourceProperty instanceof ShowlDirectPropertyShape)) {
    			ShowlPropertyShape synonym = sourceProperty.getSynonym();
    			if (synonym instanceof ShowlDirectPropertyShape) {
    				sourceProperty = synonym;
    			}
    		}

    		if (!(targetProperty instanceof ShowlDirectPropertyShape)) {
    			ShowlPropertyShape synonym = targetProperty.getSynonym();
    			if (synonym instanceof ShowlDirectPropertyShape) {
    				targetProperty = synonym;
    			}
    		}
    		
    		addExpression(targetProperty, sourceProperty);
    		return true;
    	}
		return false;
	}

	private void buildJoinConditions(ShowlNodeShape nodeA, MappingRole role, ShowlJoinCondition joinCondition, ShowlNodeShapeConsumer consumer) throws ShowlProcessingException {
		
		if (nodeA.getShape().getNodeShapeCube() != null) {
			joinCube(nodeA, joinCondition);
			addConsumable(nodeA);
		} else if (nodeA.getAccessor()==null && nodeA.hasDataSource() && !isUndefinedClass(nodeA.getOwlClass())) {
			Set<ShowlNodeShape> candidates = sourceNodeSelector.selectCandidateSources(showlFactory, nodeA);
			if (candidates.isEmpty() && role==MappingRole.TARGET) {
				nodeA.setUnmapped(true);
			}
			for (ShowlNodeShape nodeB : candidates) {
				
				if (nodeB.getShape() == nodeA.getShape()) {
					// Identity mapping
					ShowlNodeShape targetNode = role==MappingRole.TARGET ? nodeA : nodeB;
					ShowlNodeShape sourceNode = role==MappingRole.TARGET ? nodeB : nodeA;
					createIdentityMapping(sourceNode, targetNode, null);
					continue;
				}
				
					
					
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
			
			joinObjectProperties(nodeA, joinCondition);
			if (consumer != null) {
				addConsumable(nodeA);
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

	private void joinCube(ShowlNodeShape targetNode, ShowlJoinCondition joinCondition) {
		
		List<PropertyConstraint> varList = targetNode.getShape().getVariable();
		
		if (varList.size() != 1) {
			fail("Expected a single variable for NodeShape {0} associated with a cadl:Cube", targetNode.getPath());
		}
		
		PropertyConstraint p = varList.get(0);
		
		URI valueType = RdfUtil.uri(p.getValueClass());
		
		if (valueType == null) {
			fail("sh:class of variable ?{0} on NodeShape {1} must be defined.", 
				p.getPredicate().getLocalName(), 
				RdfUtil.compactName(reasoner.getGraph().getNamespaceManager(), targetNode.getShape().getId()));
		}
		
		
		for (ShowlDerivedPropertyShape varProperty : targetNode.getDerivedProperty(p.getPredicate())) {
		
		
			ShowlNodeShape varNode = varProperty.getValueShape();
			if (varNode != null) {
			
				Set<ShowlNodeShape> candidates = sourceNodeSelector.selectCandidateSources(showlFactory, targetNode); 
				for (ShowlNodeShape sourceNode : candidates) {
					
					List<ShowlPropertyShape> idList = sourceNode.out(Konig.id);
					if (idList.isEmpty()) {
						continue;
					}
					
					ShowlPropertyShape sourceId = idList.get(0);
					ShowlTargetToSourceJoinCondition t2s = new ShowlTargetToSourceJoinCondition(varProperty, sourceId);
					ShowlFromCondition fromCondition = new ShowlFromCondition(t2s, sourceNode);
					
					for (ShowlPropertyShape targetOut : varNode.allOutwardProperties()) {
						
						for (ShowlPropertyShape sourceOut : sourceNode.out(targetOut.getPredicate())) {
							if (sourceOut instanceof ShowlDirectPropertyShape) {
								produceMapping(fromCondition, sourceOut, targetOut);
							} 
						}
						
						
					}
					
					
					
					
				}
			}
		
		}
		
	}

	

	private void fail(String pattern, Object... args) {
		String text = MessageFormat.format(pattern, args);
		throw new ShowlProcessingException(text);
		
	}

	private void createIdentityMapping(ShowlNodeShape sourceNode, ShowlNodeShape targetNode, ShowlJoinCondition join) {
		
		if (join == null) {
			join = new ShowlFromCondition(null, sourceNode);
		}
		
		for (ShowlDirectPropertyShape targetProperty : targetNode.getProperties()) {
			ShowlDirectPropertyShape sourceProperty = sourceNode.getProperty(targetProperty.getPredicate());
			
			new ShowlMapping(join, sourceProperty, targetProperty);
			
			if (targetProperty.getValueShape() != null) {
				createIdentityMapping(sourceProperty.getValueShape(), targetProperty.getValueShape(), join);
			}
			
		}
		
	}

	private void addConsumable(ShowlNodeShape nodeA) {
		if (consumable == null) {
			consumable = new HashSet<>();
		}
		consumable.add(nodeA);
		
		if (logger.isTraceEnabled()) {
			logger.trace("addConsumable({})", nodeA.getPath());
		}
		
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
		scanDerivedProperties(selector, sourceNode);
		selector.scan(sourceNode.getInwardProperties());
		
		return selector.getSelected();
	}

	private void scanDerivedProperties(NestedShapeSelector selector, ShowlNodeShape sourceNode) {
		for (List<ShowlDerivedPropertyShape> list : sourceNode.getDerivedProperties()) {
			selector.scan(list);
		}
		
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
		
		ShowlJoinCondition newJoin = new ShowlTargetToSourceJoinCondition(leftJoinProperty, rightJoinProperty);
		new ShowlMapping(newJoin, leftProperty, rightProperty);
		if (logger.isTraceEnabled()) {
			logger.trace("doJoinInwardObjectProperty: {} ... {}", leftProperty.getPath(), rightJoinProperty.getPath());
		}
		doBuildMappings(leftProperty.getValueShape(), rightJoinProperty.getDeclaringShape(), newJoin);
	}

	private void joinOutwardObjectPropeties(ShowlNodeShape leftNode, ShowlJoinCondition joinCondition) throws ShowlProcessingException {
		joinOutwardObjectProperties(leftNode.getProperties(), joinCondition);
		joinOutwardDerivedObjectProperties(leftNode.getDerivedProperties(), joinCondition);
		
	}


	private void joinOutwardDerivedObjectProperties(Collection<ShowlDerivedPropertyList> collection,
			ShowlJoinCondition joinCondition) {
		
		for (List<ShowlDerivedPropertyShape> list : collection) {
			joinOutwardObjectProperties(list, joinCondition);
		}
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
			ShowlJoinCondition join = new ShowlTargetToSourceJoinCondition(targetId, sourceId);
			
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
			buildDerivedMappings(leftNode.getDerivedProperties(), rightNode, join);
			buildInwardMappings(leftNode.getInwardProperties(), rightNode, join);
		}
	}

	private void buildDerivedMappings(Collection<ShowlDerivedPropertyList> collection,
			ShowlNodeShape rightNode, ShowlJoinCondition join) {
		for (List<ShowlDerivedPropertyShape> list : collection) {
			doBuildMappings(list, rightNode, join);
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
					
					if (reasoner.isEnumerationClass(rightNode.getOwlClass().getId())) {
						ShowlNodeShape leftNode = leftProperty.getDeclaringShape();
						EnumMappingAction action = enumMappingActions.get(rightNode.getId());
						if (action == null) {
							action = new EnumMappingAction(leftNode, showlFactory, reasoner);
							enumMappingActions.put(leftNode.getId(), action);
						}
						
					} else if (logger.isTraceEnabled()) {
						logger.trace("doBuildMappings - mapping not found for {} in {}",
							leftProperty.getPath(), rightNode.getPath());
					}
					continue;
				}
			}
			
			if (Konig.id.equals(leftProperty.getPredicate()) && rightProperty!=null && Konig.id.equals(rightProperty.getPredicate())) {

				produceMapping(join, leftProperty, rightProperty);
				continue;
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
			if (leftChild == null) {
				ShowlPropertyShape peer = leftProperty.getPeer();
				if (peer != null) {
					leftChild = peer.getValueShape();
				}
			}
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
			if (logger.isTraceEnabled()) {
				logger.trace("propertyToMap({}) => {}", p.getPath(), result.getPath());
			}
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
		
		// TODO: handle derived properties separately.
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
		
		ShowlPropertyShape accessor = node.getAccessor();
		if (accessor != null) {
			
			ShowlClass owlClass = accessor.getProperty().getRange();
			if (owlClass != null) {
				return setOf(owlClass.getId());
			}
			
			ShowlPropertyShape peer = accessor.getPeer();
			if (peer != null) {
				owlClass = peer.getProperty().getRange();
				if (owlClass != null) {
					return setOf(owlClass.getId());
				}
			}
		}
		
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

	@SuppressWarnings("unchecked")
	private <T> Set<T> setOf(T... elements) {
		Set<T> result = new HashSet<T>();
		for (T e : elements) {
			result.add(e);
		}
		return result;
	}

	private void replaceOwlClass(ShowlNodeShape node, URI owlClassId) {

		ShowlClass newClass = produceOwlClass(owlClassId);
		node.setOwlClass(newClass);
		
		if (logger.isDebugEnabled()) {
			logger.debug("Set OWL Class of " + node.getPath() + " as " + "<" + owlClassId.stringValue() + ">");
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

	

	protected void putReferences(List<PropertyConstraint> property, Map<Shape, Boolean> hasReference) {
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
			if (classlessShapes != null) {
				classlessShapes.add(result);
			}
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



	void addProperties(ShowlNodeShape declaringShape) {
		if (logger.isTraceEnabled()) {
			logger.trace("addProperties({})", declaringShape.getPath());
		}
		
		addIdProperty(declaringShape);

		for (PropertyConstraint p : declaringShape.getShape().getVariable()) {
			URI predicate = p.getPredicate();
			ShowlProperty property = produceShowlProperty(predicate);
			property.setDomain(declaringShape.getOwlClass());
			property.setRange(produceOwlClass(RdfUtil.uri(p.getValueClass())));
			ShowlOutwardPropertyShape out = new ShowlVariablePropertyShape(declaringShape, property);
			declaringShape.addDerivedProperty(out);
			if (logger.isTraceEnabled()) {
				logger.trace("addProperties: add variable ?{} to {}", predicate.getLocalName(), declaringShape.getPath());
			}
		}
		
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

	void addIdProperty(ShowlNodeShape declaringShape) {
		if (declaringShape.getShape().getIriTemplate() != null) {

			ShowlProperty property = produceShowlProperty(Konig.id);
			ShowlOutwardPropertyShape out = new ShowlOutwardPropertyShape(declaringShape, property);
			out.setFormula(ShowlFunctionExpression.fromIriTemplate(
					out, declaringShape.getShape().getIriTemplate()));
			
			declaringShape.addDerivedProperty(out);
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

	protected ShowlProperty produceShowlProperty(URI predicate) {
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
			setShowlExpression(ps, e);
			e.dispatch(new PathVisitor(ps));
		}
		
	}

	private void setShowlExpression(ShowlPropertyShape ps, QuantifiedExpression e) {
		
		ShowlExpressionBuilder builder = new ShowlExpressionBuilder(showlFactory);
		ShowlExpression ex = builder.expression(ps, e);
		ps.setFormula(ex);
		
		
	}

	class HasPathVisitor extends PathVisitor {

		public HasPathVisitor(ShowlPropertyShape propertyShape) {
			super(propertyShape);
		}

		@Override
		protected ShowlNodeShape targetShape() {
			return propertyShape.getValueShape();
		}
		
		@Override
		protected void setPeer() {
			
		}
	}
	
	
	
	class PathVisitor implements FormulaVisitor {
		protected ShowlPropertyShape propertyShape;
		private ShowlPropertyShape prior;
		private int depth=0;
		
		public PathVisitor(ShowlPropertyShape propertyShape) {
			this.propertyShape = propertyShape;
		}
		
		public ShowlPropertyShape getLast() {
			return prior;
		}

		@Override
		public void enter(Formula formula) {
			if (formula instanceof HasPathStep) {
				enterHasPathStep();
			}

			if (formula instanceof PathExpression) {
				PathExpression path = (PathExpression) formula;
				ShowlNodeShape declaringShape = targetShape();

				if (logger.isTraceEnabled()) {
					logger.debug("PathVisitor.enter(declaringShape: {}, formula: {}", declaringShape.getPath(),
							((PathExpression) formula).simpleText());
				}

				String shapeIdValue = declaringShape.getShape().getId().stringValue();
				prior = null;
				List<PathStep> stepList = path.getStepList();
				for (int i = 0; i < stepList.size(); i++) {

					PathStep step = stepList.get(i);
					if (step instanceof DirectionStep) {
						DirectionStep dirStep = (DirectionStep) step;
						URI predicate = dirStep.getTerm().getIri();
						ShowlProperty property = produceShowlProperty(predicate);

						ShowlNodeShape parentShape = null;
						ShowlPropertyShape thisStep = null;
						shapeIdValue += dirStep.getDirection().getSymbol() + predicate.getLocalName();
						switch (dirStep.getDirection()) {
						case OUT: {
							if (i == 0) {

								if (dirStep.getTerm() instanceof VariableTerm) {
									parentShape = varRoot(dirStep.getTerm());
								} else {
									parentShape = declaringShape;
								}
							} else {
								// Get OWL Class for parent shape, i.e. the
								// property domain.
								ShowlClass owlClass = property.inferDomain(ShowlManager.this);
								DirectionStep prevStep = path.directionStepBefore(i);
								ShowlClass prevClass = valueClassOf(prevStep);
								owlClass = theMostSpecificClass(owlClass, prevClass);
								parentShape = createNodeShape(prior, shapeIdValue, owlClass);
							}

							ShowlPropertyShape p = outwardProperty(parentShape, property, i);

							thisStep = p;
							break;
						}
						case IN: {
							if (i == 0) {
								parentShape = declaringShape;
							} else {
								// Get OWL Class for parent shape, i.e. the
								// property range.
								ShowlClass owlClass = property.inferRange(ShowlManager.this);
								DirectionStep prevStep = path.directionStepBefore(i);
								ShowlClass prevClass = valueClassOf(prevStep);
								owlClass = theMostSpecificClass(owlClass, prevClass);
								parentShape = createNodeShape(prior, shapeIdValue, owlClass);

							}

							ShowlInwardPropertyShape p = inwardProperty(parentShape, property);
							thisStep = p;
						}
						}

						prior = thisStep;

					} else {

						if (prior == null) {
							error("Top-level filter not supported");
						}

						ShowlNodeShape valueShape = prior.getValueShape();
						if (valueShape == null) {
							ShowlProperty property = produceShowlProperty(prior.getPredicate());
							ShowlClass owlClass = property.inferRange(ShowlManager.this);
							valueShape = createNodeShape(prior, shapeIdValue, owlClass);
							prior.setValueShape(valueShape);
						}
						buildHasStep(prior, (HasPathStep) step);

					}
				}

				setPeer();

			}

		}
		

		private ShowlNodeShape varRoot(PathTerm term) {
			URI predicate = term.getIri();
			
			for (ShowlPropertyShape p=propertyShape; p!=null;) {
				ShowlNodeShape node = p.getDeclaringShape();
				Shape shape = node.getShape();
				if (shape == null) {
					fail("Declaring Shape is null at {0}", p.getPath());
				}
				
				if (shape.getVariableById(predicate) != null) {
					return node;
				}
				
				p = node.getAccessor();
			}
			fail("Root node for ?{0} not found in formula of {1}", predicate.getLocalName(), propertyShape.getPath());
			return null;
		}

		private void enterHasPathStep() {
			depth++;
		}

		protected void setPeer() {
			if (depth==0 && prior != null) {
				
				QuantifiedExpression formula = propertyShape.getPropertyConstraint().getFormula();
				if (formula.asPrimaryExpression() instanceof PathExpression) {
					propertyShape.addPeer(prior);
					
					addExpression(propertyShape, prior);
					addExpression(prior, propertyShape);
				}
				
				
			}
			
		}

		protected ShowlNodeShape targetShape() {
			return propertyShape.getDeclaringShape();
		}

		private void buildHasStep(ShowlPropertyShape p, HasPathStep step) {
			if (logger.isDebugEnabled()) {
				logger.debug("buildHasStep(propertyShape: {}, step: {})", p.getPath(), step.toSimpleString());
			}
			for (PredicateObjectList pol : step.getConstraints()) {
				PathExpression path = pol.getPath();
				
				HasPathVisitor predicateVisitor = new HasPathVisitor(p);
				path.dispatch(predicateVisitor);
				
				ShowlPropertyShape last = predicateVisitor.getLast();
				
				for (Expression e : pol.getObjectList().getExpressions()) {
					
					PrimaryExpression primary = e.asPrimaryExpression();
					if (primary == null) {
						error("Expression not supported");
					}
					
					if (primary instanceof IriValue) {
						IriValue value = (IriValue) primary;
						URI iri = value.getIri();
						last.addHasValue(iri);
					} else if (primary instanceof LiteralFormula) {
						LiteralFormula formula = (LiteralFormula) primary;
						last.addHasValue(formula.getLiteral());
					}
					
					
					
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

		private ShowlPropertyShape outwardProperty(ShowlNodeShape parentShape, ShowlProperty property, int i) {
			
			ShowlPropertyShape prior = parentShape.findProperty(property.getPredicate());
			if (prior != null) {
				return prior;
			}

			PropertyConstraint c = null;
			if (i == 0) {
				c = new PropertyConstraint(property.getPredicate());
				c.setNodeKind(propertyShape.getNodeKind());
			}
			ShowlOutwardPropertyShape p = new ShowlOutwardPropertyShape(parentShape, property, c);
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
			
			NodeKind kind = accessor.getNodeKind();
			if (kind == null) {
				ShowlNodeShape nestedShape = accessor.getValueShape();
				if (nestedShape != null) {
					kind = nestedShape.getNodeKind();
				}
			}
			shape.setNodeKind(kind);

			ShowlNodeShape node = createShowlNodeShape(accessor, shape, owlClass);

			if (kind == NodeKind.IRI) {
				ShowlProperty konigId = produceShowlProperty(Konig.id);
				ShowlIdRefPropertyShape p = new ShowlIdRefPropertyShape(node, konigId, propertyShape);
				node.addDerivedProperty(p);
			}
			return node;
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
			if (formula instanceof HasPathStep) {
				depth--;
			}
		}
		
	}
	
	


	private ShowlClass theMostSpecificClass(ShowlClass a, ShowlClass b) {
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
	
	public class Factory implements ShowlFactory {

		@Override
		public ShowlNodeShape logicalNodeShape(URI owlClass) throws ShowlProcessingException {
			NamespaceManager nsManager = reasoner.getGraph().getNamespaceManager();
			Namespace ns = nsManager.findByName(owlClass.getNamespace());
			if (ns == null) {
				throw new ShowlProcessingException("Prefix not found for namespace <" + owlClass.getNamespace() + ">");
			}
			StringBuilder builder = new StringBuilder();
			builder.append("urn:konig:logicalShape:");
			builder.append(ns.getPrefix());
			builder.append(':');
			builder.append(owlClass.getLocalName());
			
			URI shapeId = new URIImpl(builder.toString());
			
			ShowlNodeShapeSet set = nodeShapes.get(shapeId);
			if (set == null) {
				set = new ShowlNodeShapeSet();
				nodeShapes.put(shapeId, set);
				Shape shape = new Shape(shapeId);
				ShowlClass showlClass = produceOwlClass(owlClass);
				shape.addShapeDataSource(staticDataSource());
				
				ShowlNodeShape node = new ShowlNodeShape(null, shape, showlClass);
				set.add(node);
				
				// For now, we assume that every logical shape describes a named individual.
				// We may need to back off that assumption in the future.
				
				shape.setNodeKind(NodeKind.IRI);
				addIdProperty(node);
				
				node.setShapeDataSource(new ShowlDataSource(node, staticDataSource()));
				
				return node;
			}
			
			return set.top();
		}

		@Override
		public ShowlNodeShape createNodeShape(Shape shape) throws ShowlProcessingException {
			URI owlClass = shape.getTargetClass();
			if (owlClass == null) {
				throw new ShowlProcessingException("sh:targetClass must be defined for shape " + 
						RdfUtil.compactName(getReasoner().getGraph().getNamespaceManager(), shape.getId()));
			}

			ShowlClass showlClass = targetOwlClass(null, shape);
			
			ShowlNodeShape nodeShape = new ShowlNodeShape(null, shape, showlClass);
			addProperties(nodeShape);
			if (logger.isTraceEnabled()) {
				logger.trace("Factory.createNodeShape({})", RdfUtil.localName(shape.getId()) );
			}
			return nodeShape;
		}

		@Override
		public ShowlNodeShape createNodeShape(Shape shape, DataSource ds) throws ShowlProcessingException {
			ShowlNodeShape node = createNodeShape(shape);
			if (ds != null) {
				node.setShapeDataSource(new ShowlDataSource(node, ds));
			}
			return node;
		}

		@Override
		public ShowlProperty produceProperty(URI predicate) throws ShowlProcessingException {
			return produceShowlProperty(predicate);
		}

		@Override
		public ShowlClass inferDomain(ShowlProperty p) {
			return p.inferDomain(ShowlManager.this);
		}

		@Override
		public ShowlClass inferRange(ShowlProperty p) {
			return p.inferRange(ShowlManager.this);
		}

		@Override
		public ShowlClass mostSpecificClass(ShowlClass a, ShowlClass b) {
			return theMostSpecificClass(a, b);
		}

		@Override
		public ShowlNodeShape createShowlNodeShape(ShowlPropertyShape accessor, Shape shape, ShowlClass owlClass) {
			ShowlNodeShape result = new ShowlNodeShape(accessor, shape, owlClass);
			if (Konig.Undefined.equals(owlClass.getId())) {
				if (classlessShapes != null) {
					classlessShapes.add(result);
				}
			}
			ShowlNodeShapeSet set = nodeShapes.get(shape.getId());
			if (set == null) {
				set = new ShowlNodeShapeSet();
				nodeShapes.put(shape.getId(), set);
			}
			set.add(result);
			return result;
			
		}

		

		
	}


	private void addExpression(ShowlPropertyShape a, ShowlPropertyShape b) {
		if (b instanceof ShowlDirectPropertyShape) {
			a.addExpression(new ShowlDirectPropertyExpression((ShowlDirectPropertyShape)b));
		} else {
			ShowlDerivedPropertyShape derived = (ShowlDerivedPropertyShape) b;
			if (derived.getFormula() != null) {
				a.addExpression(derived.getFormula());
			} else {
				a.addExpression(new ShowlDerivedPropertyExpression((ShowlDerivedPropertyShape)b));
			}
		}
		
	}
}
