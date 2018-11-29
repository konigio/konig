package io.konig.core.showl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.konig.core.KonigException;
import io.konig.core.OwlReasoner;
import io.konig.core.impl.RdfUtil;
import io.konig.core.vocab.Konig;
import io.konig.formula.Direction;
import io.konig.formula.DirectionStep;
import io.konig.formula.Formula;
import io.konig.formula.FormulaVisitor;
import io.konig.formula.PathExpression;
import io.konig.formula.PathStep;
import io.konig.formula.QuantifiedExpression;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeManager;

public class ShowlManager {
	private static final Logger logger = LoggerFactory.getLogger(ShowlManager.class);
	private Map<Resource,ShowlNodeShape> nodeShapes = new HashMap<>();
	private Map<URI,ShowlClass> owlClasses = new HashMap<>();
	private Map<URI, ShowlProperty> properties = new HashMap<>();
	
	public void load(ShapeManager shapeManager, OwlReasoner reasoner) {
		Worker worker = new Worker(reasoner);
		
		worker.load(shapeManager);
	}
	
	public ShowlNodeShape getNodeShape(Resource shapeId) {
		return nodeShapes.get(shapeId);
	}
	
	
	private class Worker {

		private OwlReasoner reasoner;
		private List<ShowlNodeShape> classlessShapes = new ArrayList<>();
	

		public Worker(OwlReasoner reasoner) {
			this.reasoner = reasoner;
		}

		private void load(ShapeManager shapeManager) {
			loadShapes(shapeManager);
			inferTargetClasses();
			mergeGroups();
		}
		
	


		/**
		 * Merge groups whose members have a compatible domain and the same predicate
		 */
		private void mergeGroups() {
			Map<ClassPropertyPair,ShowlPropertyGroup> map = new HashMap<>();
			for (ShowlProperty property : properties.values()) {
				for (ShowlPropertyShape p : property.getPropertyShapes()) {
					ShowlPropertyGroup g = produceGroup(p);
					URI domainId = p.getDeclaringShape().getOwlClass().getId();
					if (!Konig.Undefined.equals(domainId)) {
						
						ClassPropertyPair key = new ClassPropertyPair(domainId, property.getPredicate());
						
						ShowlPropertyGroup h = map.get(key);
						if (h == null) {
							map.put(key, g);
						} else {
							h.addAll(g);
						}
					}
				}
			}
			
			
		}

		private void inferTargetClasses() {
			for (ShowlNodeShape gns : classlessShapes) {
				inferTargetClass(gns);
			}
			classlessShapes = null;
			
		}

		private void inferTargetClass(ShowlNodeShape gns) {
			
			Set<URI> candidates = new HashSet<>();
			
			Set<ShowlPropertyShape> allProperties = gns.allProperties();
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

		private void replaceOwlClass(ShowlNodeShape node, URI owlClassId) {

			ShowlClass newClass = produceOwlClass(owlClassId);
			node.setOwlClass(newClass);
			
			if (logger.isInfoEnabled()) {
				logger.info("Set OWL Class of " + node.getPath() + " as " + "<" + owlClassId.stringValue() + ">");
			}
			
		}



		private void loadShapes(ShapeManager shapeManager) {
			for (Shape shape : shapeManager.listShapes()) {
				if (!shape.getShapeDataSource().isEmpty()) {
					createNodeShape(null, shape);
				}
			}
		}
		
		private ShowlNodeShape createShowlNodeShape(ShowlPropertyShape accessor, Shape shape, ShowlClass owlClass) {

			ShowlNodeShape result = new ShowlNodeShape(accessor, shape, owlClass);
			if (Konig.Undefined.equals(owlClass.getId())) {
				classlessShapes.add(result);
			}
			return result;
		}
		
		private ShowlNodeShape createNodeShape(ShowlPropertyShape accessor, Shape shape) {
			ShowlClass owlClass = targetOwlClass(accessor, shape);
			ShowlNodeShape result = createShowlNodeShape(accessor, shape, owlClass);
			nodeShapes.put(shape.getId(), result);
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


		private ShowlClass produceOwlClass(URI owlClass) {
			ShowlClass result = owlClasses.get(owlClass);
			if (result == null) {
				result = new ShowlClass(owlClass);
				owlClasses.put(owlClass, result);
			}
			return result;
		}

		private void addProperties(ShowlNodeShape declaringShape) {
			
			
			for (PropertyConstraint p : declaringShape.getShape().getProperty()) {
				URI predicate = p.getPredicate();
				if (predicate != null) {
					ShowlProperty property = produceShowlProperty(predicate);
					ShowlPropertyShape q = new ShowlPropertyShape(declaringShape, property, p);
					declaringShape.addProperty(q);
					processFormula(q);
					
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
					for (PathStep step : path.getStepList()) {
						
						
						if (step instanceof DirectionStep) {
							DirectionStep dirStep = (DirectionStep) step;
							URI predicate = dirStep.getTerm().getIri();
							
							ShowlNodeShape parentShape = null;
							if (prior == null) {
								parentShape = declaringShape;
							} else {
								shapeIdValue += "." + predicate.getLocalName();
								ShowlClass owlClass = produceOwlClass(prior);
								URI shapeId = new URIImpl(shapeIdValue);
								Shape shape = new Shape(shapeId);
								parentShape = createShowlNodeShape(prior, shape, owlClass);
								prior.setValueShape(parentShape);
							}
							
							if (dirStep.getDirection() == Direction.OUT) {
								ShowlProperty property = produceShowlProperty(predicate);
								ShowlPropertyShape p = parentShape.findProperty(predicate);
								if (p == null) {
									ShowlDerivedPropertyShape d = new ShowlDerivedPropertyShape(parentShape, property);
									parentShape.addDerivedProperty(d);
									p = d;
								}
								prior = p;
							} else {
								throw new KonigException("In step not supported yet");
							}
						} else {
							throw new KonigException("HasStep not supported yet");
						}
					}
					if (prior != null) {
						produceGroup(propertyShape).add(prior);
					}
					
					
				}
				
			}



			@Override
			public void exit(Formula formula) {
				// Do Nothing
			}
			
		}
		
		private ShowlPropertyGroup produceGroup(ShowlPropertyShape p) {
			ShowlPropertyGroup g = p.getGroup();
			if (g == null) {
				g = new ShowlPropertyGroup();
				g.add(p);
			}
			return g;
		}
		
		private ShowlClass produceOwlClass(ShowlPropertyShape p) {
			ShowlClass range = p.getProperty().getRange();
			return range==null ? produceOwlClass(Konig.Undefined) : range;
		}
	}
	
	private static class ClassPropertyPair {
		URI classId;
		URI propertyId;
		
		public ClassPropertyPair(URI classId, URI propertyId) {
			this.classId = classId;
			this.propertyId = propertyId;
		}
		
	
		public int hashCode() {
			int hash = 17;
			hash = hash * 31 + classId.hashCode();
			hash = hash * 31 + propertyId.hashCode();
			return hash;
		}
		
		public boolean equals(Object other) {
			if (other instanceof ClassPropertyPair) {
				ClassPropertyPair b = (ClassPropertyPair) other;
				return classId.equals(b.classId) && propertyId.equals(b.propertyId);
			}
			return false;
		}
		
		public String toString() {
			return "ClassPropertyPair(" + classId.getLocalName() + ", " + propertyId.getLocalName() + ")";
		}
	}
	
}
