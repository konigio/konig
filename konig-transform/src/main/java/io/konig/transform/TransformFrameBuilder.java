package io.konig.transform;

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


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.konig.core.NamespaceManager;
import io.konig.core.Path;
import io.konig.core.impl.RdfUtil;
import io.konig.core.path.HasStep;
import io.konig.core.path.HasStep.PredicateValuePair;
import io.konig.core.path.OutStep;
import io.konig.core.path.Step;
import io.konig.core.util.IriTemplate;
import io.konig.shacl.NodeKind;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeManager;

/**
 * A utility that builds a TransformFrame which prescribe a solution for transforming 
 * as set of source shapes into a given target shape.
 * @author Greg McFall
 *
 */
public class TransformFrameBuilder {
	private static final Logger logger = LoggerFactory.getLogger(TransformFrameBuilder.class);
	private ShapeManager shapeManager;
	private NamespaceManager nsManager;
	
	public TransformFrameBuilder(ShapeManager shapeManager, NamespaceManager nsManager) {
		this.shapeManager = shapeManager;
		this.nsManager = nsManager;
	}

	public TransformFrame create(Shape targetShape) throws ShapeTransformException {
		Worker worker = new Worker();
		return worker.createFrame("", targetShape);
	}
	
	private class Worker {

		
		Map<Shape,TransformFrame> shapeMap = new HashMap<>();
		
		private TransformFrame produceFrame(String targetContext, Shape targetShape) throws ShapeTransformException {
			TransformFrame frame = shapeMap.get(targetShape);
			if (frame == null) {
				frame = createFrame(targetContext, targetShape);
			}
			return frame;
		}
		
		private TransformFrame createFrame(String targetContext, Shape targetShape) throws ShapeTransformException {

			URI targetClass = targetShape.getTargetClass();
			if (targetClass == null) {
				throw new ShapeTransformException("Target Class is not defined for shape: " + targetShape.getId());
			}
			
			TransformFrame frame = new TransformFrame(targetShape);
			shapeMap.put(targetShape, frame);
			
			for (PropertyConstraint p : targetShape.getProperty()) {
				
				URI predicate = p.getPredicate();
				if (predicate != null) {
					TransformAttribute attr = new TransformAttribute(p);
					
					frame.addAttribute(attr);
					
					Shape valueShape = p.getShape();
					if (valueShape != null) {
						TransformFrame embeddedFrame = produceFrame(targetContext, valueShape);
						attr.setEmbeddedFrame(embeddedFrame);
					}
				}
			}

			List<Shape> list = shapeManager.getShapesByTargetClass(targetClass);
			for (Shape sourceShape : list) {
				if (sourceShape != targetShape) {
					addIdMapping(targetContext, frame, sourceShape);
					addSourceShape(targetContext, frame, sourceShape);
				}
			}
			return frame;
		}
		
		
		private void addIdMapping(String targetContext, TransformFrame frame, Shape sourceShape) throws ShapeTransformException {
			
			IriTemplate template = sourceShape.getIriTemplate();
			if (template != null) {
				IriTemplateInfo info = IriTemplateInfo.create(template, nsManager, sourceShape);
				ShapePath shapePath = new ShapePath(targetContext, sourceShape);
				frame.addIdMapping(new MappedId(shapePath, info));
			}
			
		}
		
		
		private void addSourceShape(String targetContext, TransformFrame frame, Shape sourceShape) throws ShapeTransformException {
			addSourceShape(targetContext, frame, sourceShape, sourceShape.getProperty());
			addSourceShape(targetContext, frame, sourceShape, sourceShape.getDerivedProperty());
		}

		private void addSourceShape(String targetContext, TransformFrame frame, Shape sourceShape, List<PropertyConstraint> propertyList) throws ShapeTransformException {
			
			if (propertyList != null) {
				for (PropertyConstraint p : propertyList) {
					URI predicate = p.getPredicate();
					if (predicate != null) {
						Path path = p.getEquivalentPath();
						if (path == null) {
							TransformAttribute attr = frame.getAttribute(predicate);
							if (attr != null) {
								MappedProperty m = new MappedProperty(new ShapePath(targetContext, sourceShape), p);
								attr.add(m);
								
								TransformFrame childFrame = attr.getEmbeddedFrame();
								if (childFrame != null) {
									Shape childShape = p.getShape();
									if (childShape != null) {
										addSourceShape(targetContext, childFrame, childShape);
									}
								}
							}
						} else {
							handlePath(targetContext, frame, sourceShape, p, path);
						}
					}
				}
			}
		}

		private void handlePath(String targetContext, TransformFrame frame, Shape sourceShape, PropertyConstraint p, Path path) throws ShapeTransformException {
			
			int end = path.length()-1;
			MappedProperty lastMappedProperty = null;
			for (int i=0; i<=end; i++) {
				Step step = path.asList().get(i);
				if (step instanceof HasStep) {
					if (lastMappedProperty != null) {
						HasStep hasStep = (HasStep) step;
						lastMappedProperty.setHasValue(hasStep.getPairList());
						hasStep(targetContext, frame, lastMappedProperty);
					}
				} else if (step instanceof OutStep) {
					OutStep outStep = (OutStep) step;
					URI first = outStep.getPredicate();
					if (first == null) {
						break;
					} else {
						TransformAttribute attr = frame.getAttribute(first);
						if (attr != null) {
							MappedProperty m = new MappedProperty(new ShapePath(targetContext, sourceShape), p, i);
							attr.add(m);
							lastMappedProperty = m;
							
							if (i != end) {

								frame = attr.getEmbeddedFrame();
								if (frame == null) {
									NodeKind targetNodeKind = attr.getTargetProperty().getNodeKind();
									if (targetNodeKind == NodeKind.IRI) {
										// The target is expecting an IRI reference, but the source path has not ended.
										// Can we construct an IRI reference from the remaining
										// information in the source path?
										
										Resource valueClass = attr.getTargetProperty().getValueClass();
										if (valueClass instanceof URI) {
											// The value of the IRI reference must be of type valueClass.
											
											List<Shape> shapeList = shapeManager.getShapesByTargetClass((URI)valueClass);
											for (Shape valueShape : shapeList) {
												IriTemplate template = valueShape.getIriTemplate();
												if (template != null) {
													IriTemplateInfo info = IriTemplateInfo.create(
														template, nsManager, valueShape);
													
													if (info == null) {
														logger.warn("Cannot expand IRI template: " + template.toString());
													} else if (i==end-1) {
														Path subpath = path.subpath(i+1);
														
														if (inject(info, p, subpath)) {
															m.setTemplateInfo(info);
														} else {
															// Cannot satisfy the IRI template with currently available
															// properties.  Must join with the valueShape where the
															// template is defined.
															
															setTemplateShape(attr, m, targetContext, valueShape, info);
															
														}
														
														
													}
												}
											}
										}
									}
									break;
								}
							}
							
						}
					}
				}
				
			}	
		}
		


		private void hasStep(String targetContext, TransformFrame frame, MappedProperty mappedProperty) throws ShapeTransformException {
			for (PredicateValuePair pair : mappedProperty.getHasValue()) {
				URI predicate = pair.getPredicate();
				TransformAttribute attr = frame.getAttribute(predicate);
				if (attr != null) {
					Value value = pair.getValue();
					if (value instanceof Literal) {
						ShapePath shapePath = mappedProperty.getShapePath();
						PropertyConstraint property = new PropertyConstraint(predicate);
						property.addHasValue(value);
						
						MappedProperty m = new MappedProperty(shapePath, property);
						attr.add(m);
						
					} else {
						throw new ShapeTransformException("Resource values not supported");
					}
				}
			}
			
		}

		private void setTemplateShape(TransformAttribute attr, MappedProperty m, String targetContext, Shape valueShape, IriTemplateInfo info) {
			
			StringBuilder nextContext = new StringBuilder();
			nextContext.append(targetContext);
			nextContext.append('.');
			nextContext.append(attr.getPredicate().getLocalName());
			
			ShapePath nextShape = new ShapePath(nextContext.toString(), valueShape);
			m.setTemplateShape(nextShape);
			m.setTemplateInfo(info);
		}

		/**
		 * Inject a PropertyConstraint into a given IriTemplateInfo if an existing element matches
		 * a given subpath.
		 * @param info
		 * @param p
		 * @param subpath
		 * @return
		 */
		private boolean inject(IriTemplateInfo info, PropertyConstraint p, Path subpath) {
			URI last = RdfUtil.out(subpath, subpath.length()-1);
			
			if (last == null) {
				return false;
			}

			int count = 0;
			for (IriTemplateElement e : info) {
				PropertyConstraint q = e.getProperty();
				if (q != null) {
					count++;
					if (count > 1) {
						return false;
					}
					Path qpath = q.getEquivalentPath();
					
					if (qpath == null) {
						if (last.equals(q.getPredicate())) {
							e.setProperty(p);
						} else {
							return false;
						}
					} else {
						if (subpath.equals(qpath)) {
							e.setProperty(p);
						} else {
							return false;
						}
					}
				}
			}
			return count==1;
		}
	}
	

}
