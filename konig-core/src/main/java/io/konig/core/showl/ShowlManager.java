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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.konig.core.OwlReasoner;
import io.konig.core.impl.RdfUtil;
import io.konig.core.vocab.Konig;
import io.konig.formula.DirectionStep;
import io.konig.formula.Formula;
import io.konig.formula.FormulaVisitor;
import io.konig.formula.PathExpression;
import io.konig.formula.PathStep;
import io.konig.formula.QuantifiedExpression;
import io.konig.shacl.NodeKind;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeManager;

public class ShowlManager {
	private static final Logger logger = LoggerFactory.getLogger(ShowlManager.class);
	private Map<Resource,ShowlNodeShapeSet> nodeShapes = new HashMap<>();
	private Map<URI,ShowlClass> owlClasses = new HashMap<>();
	private Map<URI, ShowlProperty> properties = new HashMap<>();
	private ShowlNodeShapeSet emptySet = null;
	private OwlReasoner reasoner;
	
	public void load(ShapeManager shapeManager, OwlReasoner reasoner) {
		this.reasoner = reasoner;
		Worker worker = new Worker(shapeManager);
		
		worker.load();
	}
	
	public ShowlNodeShapeSet getNodeShape(Resource shapeId) {
		ShowlNodeShapeSet set =  nodeShapes.get(shapeId);
		return (set == null) ? emptySet() : set;
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



	private class Worker {

		private ShapeManager shapeManager;
		private List<ShowlNodeShape> classlessShapes = new ArrayList<>();
	
		private Set<NodeMapping> nodeMappings = null;

		public Worker(ShapeManager shapeManager) {
			this.shapeManager = shapeManager;
		}

		private void load() {
			loadShapes();
			inferTargetClasses();
			inferInverses();
			buildJoinConditions();
		}

		private void inferInverses() {
			for (ShowlProperty p : getProperties()) {
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

		private void buildJoinConditions() {
			nodeMappings = new HashSet<>();
			for (Shape shape : shapeManager.listShapes()) {
				if (!shape.getShapeDataSource().isEmpty()) {
					for (ShowlNodeShape node : getNodeShape(shape.getId())) {
						buildJoinConditions(node, null);
					}
				}
				
			}
			nodeMappings = null;
			
		}
		
		private void buildJoinConditions(ShowlNodeShape leftNode, ShowlJoinCondition joinCondition) {
			
			if (leftNode.isNamedRoot()) {
				ShowlClass owlClass = leftNode.getOwlClass();
				if (!isUndefinedClass(owlClass)) {
					for (ShowlNodeShape rightNode : owlClass.getTargetClassOf()) {
						if (rightNode == leftNode) {
							continue;
						}
						if (rightNode.isNamedRoot()) {
							// TODO: consider shapes that are not named roots
							
							doJoin(leftNode, rightNode, joinCondition);
							
						}
					}
					joinObjectProperties(leftNode, joinCondition);
				}
			}
		}

		private void joinObjectProperties(ShowlNodeShape leftNode, ShowlJoinCondition joinCondition) {
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
			
			ShowlJoinCondition newJoin = new ShowlJoinCondition(leftJoinProperty, rightJoinProperty, joinCondition);
			new ShowlMapping(newJoin, leftProperty, rightProperty);
			if (logger.isTraceEnabled()) {
				logger.trace("doJoinInwardObjectProperty: {} ... {}", leftProperty.getPath(), rightJoinProperty.getPath());
			}
			doBuildMappings(leftProperty.getValueShape(), rightJoinProperty.getDeclaringShape(), newJoin);
		}

		private void joinOutwardObjectPropeties(ShowlNodeShape leftNode, ShowlJoinCondition joinCondition) {
			joinOutwardObjectProperties(leftNode.getProperties(), joinCondition);
			joinOutwardObjectProperties(leftNode.getDerivedProperties(), joinCondition);
			
		}

		private void joinOutwardObjectProperties(Collection<? extends ShowlPropertyShape> properties,
				ShowlJoinCondition joinCondition) {
			
			for (ShowlPropertyShape leftProperty : properties) {
				ShowlNodeShape leftChild = leftProperty.getValueShape();
			
				if (leftChild != null) {	
					ShowlClass childClass = leftChild.getOwlClass();
					if (!isUndefinedClass(childClass)) {
						ShowlPropertyShape leftChildId = leftChild.findProperty(Konig.id);
						if (leftChildId != null) {
							buildJoinConditions(leftChild, joinCondition);
						}
						
						// Find other cases where the child object is referenced via the same predicate as
						// the leftProperty.
						for (ShowlProperty p : childClass.getRangeOf()) {
							for (ShowlPropertyShape rightProperty : p.getPropertyShapes()) {
								if (rightProperty == leftProperty) {
									continue;
								}
								if (leftChildId!=null && rightProperty.getDeclaringShape().isNamedRoot()) {
									continue;
								}
								if (rightProperty.getValueShape()==null) {
									continue;
								}
								
								doJoinOutwardObjectProperty(leftChild, rightProperty, joinCondition);
								
							}
						}
					}
					
				}
			}
			
		}

		private void doJoinOutwardObjectProperty(ShowlNodeShape leftChild, ShowlPropertyShape rightProperty,
				ShowlJoinCondition joinCondition) {
			
			if (logger.isTraceEnabled()) {
				logger.trace("doJoinOutwardObjectProperty: {} ... {}", leftChild.getPath(), rightProperty.getPath());
			}
			
		}

		private void doJoin(ShowlNodeShape leftNode, ShowlNodeShape rightNode, ShowlJoinCondition joinCondition) {
			ShowlPropertyShape leftId = leftNode.findProperty(Konig.id);
			ShowlPropertyShape rightId = rightNode.findProperty(Konig.id);
			
			if (leftId.findJoinCondition(rightId) == null) {
				ShowlJoinCondition join = new ShowlJoinCondition(leftId, rightId, joinCondition);
				
				buildMappings(leftNode, rightNode, join);
			}
			
		}

		private void buildMappings(ShowlNodeShape leftNode, ShowlNodeShape rightNode, ShowlJoinCondition join) {
			doBuildMappings(leftNode, rightNode, join);
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
			
			for (ShowlPropertyShape leftProperty : properties) {
				ShowlPropertyShape rightProperty = rightNode.findProperty(leftProperty.getPredicate());
				
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

		private void produceMapping(ShowlJoinCondition join, ShowlPropertyShape directLeft,
				ShowlPropertyShape directRight) {
			
			new ShowlMapping(join, directLeft, directRight);
			if (logger.isTraceEnabled()) {
				logger.trace("doBuildMappings: new Mapping {} ... {}", directLeft.getPath(), directRight.getPath());
			}
			
		}

		private ShowlPropertyShape directProperty(ShowlPropertyShape p) {
			return 
				p==null      ? null : 
				p.isDirect() ? p : p.getPeer();
		}

		private boolean isUndefinedClass(ShowlClass owlClass) {
			
			return owlClass == null || Konig.Undefined.equals(owlClass.getId());
		}

		private void buildMapping(
				ShowlJoinCondition join, 
				ShowlNodeShape leftNode, 
				ShowlPropertyShape leftProperty,
				ShowlNodeShape rightNode) {
			
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

		private void inferTargetClasses() {
			for (ShowlNodeShape gns : classlessShapes) {
				inferTargetClass(gns);
			}
			classlessShapes = null;
			
		}

		private void inferTargetClass(ShowlNodeShape node) {
			
			Set<URI> candidates = new HashSet<>();
			
			Set<ShowlPropertyShape> allProperties = node.allOutwardProperties();
			for (ShowlPropertyShape p : allProperties) {
				
				if (p.isNestedAccordingToFormula()) {
					continue;
					// TODO: add reasoning about nested fields
				}
				
				ShowlProperty property = p.getProperty();
				if (property != null) {
					Set<URI> domainIncludes = property.domainIncludes(reasoner);
					
					
					// Remove elements from the domain that are superclasses of an existing candidate.
					Iterator<URI> sequence = domainIncludes.iterator();
					while (sequence.hasNext()) {
						URI domain = sequence.next();
						if (Konig.Undefined.equals(domain)) {
							sequence.remove();
						} else {
							for (URI candidate : candidates) {
								if (reasoner.isSubClassOf(candidate, domain)) {
									sequence.remove();
								}
							}
						}
					}
					candidates.addAll(domainIncludes);
				}
			}
			
			if (candidates.size()==1) {
				
				replaceOwlClass(node, candidates.iterator().next());
			} else {
				
				if (logger.isWarnEnabled()) {
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

		private void replaceOwlClass(ShowlNodeShape node, URI owlClassId) {

			ShowlClass newClass = produceOwlClass(owlClassId);
			node.setOwlClass(newClass);
			
			if (logger.isInfoEnabled()) {
				logger.info("Set OWL Class of " + node.getPath() + " as " + "<" + owlClassId.stringValue() + ">");
			}
			
		}



		private void loadShapes() {
			
			Set<Shape> rootShapes = rootShapes(shapeManager);
			for (Shape shape : rootShapes) {
				createNodeShape(null, shape);
			}
		}
		
		private Set<Shape> rootShapes(ShapeManager shapeManager) {
			Set<Shape> result = new HashSet<>();
			Map<Shape,Boolean> hasReference = new HashMap<>();
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
				logger.trace("Created direct Property Shape: {}", p.getPath());
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
		
		private void processFormula(ShowlPropertyShape gps) {
			PropertyConstraint p = gps.getPropertyConstraint();
			QuantifiedExpression e = (p==null) ? null : p.getFormula();
			if (e != null) {
				e.dispatch(new PathVisitor(gps));
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
	
								ShowlOutwardPropertyShape p = new ShowlOutwardPropertyShape(parentShape, property);
								parentShape.addDerivedProperty(p);
								if (logger.isTraceEnabled()) {
									logger.trace("PathVisitor.enter: Created derived Property Shape: {}", p.getPath());
								}
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
								ShowlInwardPropertyShape p = new ShowlInwardPropertyShape(parentShape, property);
								parentShape.addInwardProperty(p);
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
			

			private ShowlNodeShape createNodeShape(ShowlPropertyShape prior, String shapeIdValue,
					ShowlClass owlClass) {
				URI shapeId = new URIImpl(shapeIdValue);
				Shape shape = new Shape(shapeId);
				return createShowlNodeShape(prior, shape, owlClass);
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

		
	}
	

	ShowlClass produceOwlClass(URI owlClass) {
		if (owlClass == null) {
			owlClass = Konig.Undefined;
		}
		ShowlClass result = owlClasses.get(owlClass);
		if (result == null) {
			result = new ShowlClass(owlClass);
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
}
