package io.konig.shacl.graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.OWL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.konig.core.KonigException;
import io.konig.core.OwlReasoner;
import io.konig.core.vocab.Konig;
import io.konig.formula.Direction;
import io.konig.formula.DirectionStep;
import io.konig.formula.Formula;
import io.konig.formula.FormulaVisitor;
import io.konig.formula.HasPathStep;
import io.konig.formula.PathExpression;
import io.konig.formula.PathStep;
import io.konig.formula.QuantifiedExpression;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeManager;

public class GraphShapeManager {
	private static final Logger logger = LoggerFactory.getLogger(GraphShapeManager.class);
	private Map<Resource,GraphNodeShape> nodeShapes = new HashMap<>();
	private Map<URI,GraphOwlClass> owlClasses = new HashMap<>();
	
	public void load(ShapeManager shapeManager, OwlReasoner reasoner) {
		Worker worker = new Worker(reasoner);
		
		worker.load(shapeManager);
	}
	
	private class Worker {

		private OwlReasoner reasoner;
		private Map<URI, GraphPropertyInfo> propertyInfo = new HashMap<>();
		private List<GraphNodeShape> classlessShapes = new ArrayList<>();
	

		public Worker(OwlReasoner reasoner) {
			this.reasoner = reasoner;
		}

		private void load(ShapeManager shapeManager) {
			loadShapes(shapeManager);
			inferTargetClasses();
		}
		
		

		private void inferTargetClasses() {
			for (GraphNodeShape gns : classlessShapes) {
				inferTargetClass(gns);
			}
			classlessShapes = null;
			
		}

		private void inferTargetClass(GraphNodeShape gns) {
			
			Set<URI> candidates = new HashSet<>();
			for (GraphPropertyShape p : gns.getProperties()) {
				URI predicate = p.getPredicate();
				GraphPropertyInfo info = propertyInfo.get(predicate);
				if (info != null) {
					Set<URI> domainIncludes = info.domainIncludes(reasoner);
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
				replaceOwlClass(gns, candidates.iterator().next());
			} else if (logger.isWarnEnabled()) {
				if (candidates.isEmpty()) {
					logger.warn("No candidates found for target class of " + gns.getPath());
				} else {
					StringBuilder builder = new StringBuilder();
					builder.append("Target class at " + gns.getPath() + " is ambiguous.  Candidates include\n");
					for (URI c : candidates) {
						builder.append("  ");
						builder.append(c.getLocalName());
						builder.append('\n');
					}
					logger.warn(builder.toString());
				
				}
			}
			
			
		}

		private void replaceOwlClass(GraphNodeShape node, URI owlClassId) {

			GraphOwlClass oldClass = node.getOwlClass();
			GraphOwlClass newClass = produceOwlClass(owlClassId);
			node.setOwlClass(newClass);
			
			
			
		}


		private GraphOwlClass inferDomain(URI predicate, GraphNodeShape declaringShape) {
			
			GraphOwlClass owlClass = declaringShape.getOwlClass();
			if (!Konig.Undefined.equals(owlClass.getOwlClassId())) {
				return owlClass;
			}
			
			GraphPropertyInfo info = propertyInfo.get(predicate);
			Set<GraphPropertyShape> set = info.getDirectPropertyShapes();
			if (set.size()==1) {
				return set.iterator().next().getDeclaringShape().getOwlClass();
			}
			
			Set<URI> classes =	set.stream()
					.map(e -> e.getDeclaringShape().getOwlClass().getOwlClassId())
					.collect(Collectors.toSet());
			
			URI bestClass = reasoner.leastCommonSuperClass(classes);
			
			if (OWL.THING.equals(bestClass) && !classes.contains(OWL.THING)) {
				bestClass = Konig.Undefined;
			}
			
			return produceOwlClass(bestClass);
		}


		private void loadShapes(ShapeManager shapeManager) {
			for (Shape shape : shapeManager.listShapes()) {
				if (!shape.getShapeDataSource().isEmpty()) {
					createNodeShape(null, shape);
				}
			}
		}

		
		private GraphNodeShape createNodeShape(GraphPropertyShape accessor, Shape shape) {
			GraphNodeShape result = new GraphNodeShape(accessor, shape);
			URI owlClass = targetOwlClass(result);
			GraphOwlClass owl = produceOwlClass(owlClass);
			if (Konig.Undefined.equals(owlClass)) {
				classlessShapes.add(result);
			}
			result.setOwlClass(owl);
			
			nodeShapes.put(shape.getId(), result);
			addProperties(result);
			return result;
		}

		private URI targetOwlClass(GraphNodeShape node) {
			GraphPropertyShape accessor = node.getAccessor();
			if (accessor != null) {
				PropertyConstraint p = accessor.getPropertyConstraint();
				if (p != null) {
					if (p.getValueClass() instanceof URI) {
						return (URI) p.getValueClass();
					}
				}
			}
			URI result = node.getShape().getTargetClass();
			return result == null ? Konig.Undefined : result;
		}

		private GraphOwlClass produceOwlClass(URI owlClass) {
			GraphOwlClass result = owlClasses.get(owlClass);
			if (result == null) {
				result = new GraphOwlClass(owlClass);
				owlClasses.put(owlClass, result);
			}
			return null;
		}

		private void addProperties(GraphNodeShape declaringShape) {
			
			for (PropertyConstraint p : declaringShape.getShape().getProperty()) {
				URI predicate = p.getPredicate();
				if (predicate != null) {
					GraphPropertyShape q = new GraphPropertyShape(declaringShape, predicate, p);
					declaringShape.addProperty(q);
					producePropertyInfo(q);
					
					Shape childShape = p.getShape();
					if (childShape != null) {
						if (declaringShape.hasAncestor(childShape.getId())) {
							throw new KonigException("Cyclic shape detected at: " + q.getPath());
						}
						createNodeShape(q, childShape);
					}
				}
			}
			
		}
		
		private GraphPropertyInfo producePropertyInfo(final GraphPropertyShape gps) {
			PropertyConstraint p = gps.getPropertyConstraint();
			URI predicate = p.getPredicate();
			GraphPropertyInfo info = propertyInfo.get(predicate);
			if (info == null) {
				info = new GraphPropertyInfo(predicate);
				propertyInfo.put(predicate, info);
			}
			
			info.addDirectPropertyShape(gps);
			QuantifiedExpression e = p.getFormula();
			if (e != null) {
				final GraphPropertyInfo gpi = info;
				e.dispatch(new FormulaVisitor() {
					
					@Override
					public void exit(Formula formula) {
					}
					
					@Override
					public void enter(Formula formula) {
						if (formula instanceof PathExpression) {
							PathExpression path = (PathExpression) formula;
							
							List<PathStep> stepList = path.getStepList();
							PathStep first = stepList.isEmpty() ? null : stepList.get(0);
							
							DirectionStep dirStep = null;
							if (first instanceof DirectionStep) {
								dirStep = (DirectionStep) first;
							} else if (first instanceof HasPathStep) {
							
								PathStep second = stepList.size()>1 ? null : stepList.get(1);
								if (second instanceof DirectionStep) {
									dirStep = (DirectionStep) second;
								}
							}
							if (dirStep!=null && dirStep.getDirection() == Direction.OUT) {
								gpi.addIndirectPropertyShape(gps);
							}
						}
						
					}
				});
			}
			
			return info;
		}
	}
	
	
	
}
