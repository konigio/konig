package io.konig.transform;

import java.util.List;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.konig.core.KonigException;
import io.konig.core.Path;
import io.konig.core.impl.RdfUtil;
import io.konig.core.path.PathFactory;
import io.konig.core.util.IriTemplate;
import io.konig.shacl.NodeKind;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeManager;

public class TransformFrameBuilder {
	private static final Logger logger = LoggerFactory.getLogger(TransformFrameBuilder.class);
	private PathFactory pathFactory;
	private ShapeManager shapeManager;
	
	public TransformFrameBuilder(ShapeManager shapeManager, PathFactory pathFactory) {
		this.shapeManager = shapeManager;
		this.pathFactory = pathFactory;
	}

	public TransformFrame create(Shape targetShape) throws ShapeTransformException {
		TransformFrame frame = new TransformFrame(targetShape);
		
		URI targetClass = targetShape.getTargetClass();
		if (targetClass == null) {
			throw new ShapeTransformException("Target Class is not defined for shape: " + targetShape.getId());
		}
		
		
		List<Shape> list = shapeManager.getShapesByTargetClass(targetClass);
		for (Shape sourceShape : list) {
			addIdMapping(frame, sourceShape);
			if (sourceShape != targetShape) {
				addSourceShape(frame, sourceShape);
			}
		}
		
		return frame;
	}

	private void addIdMapping(TransformFrame frame, Shape sourceShape) {
		
		IriTemplate template = sourceShape.getIriTemplate();
		if (template != null) {
			IriTemplateInfo info = IriTemplateInfo.create(template, pathFactory.getNamespaceManager(), sourceShape);
			frame.addIdMapping(new MappedId(sourceShape, info));
		}
		
	}

	private void addSourceShape(TransformFrame frame, Shape sourceShape) throws ShapeTransformException {
		
		for (PropertyConstraint p : sourceShape.getProperty()) {
			URI predicate = p.getPredicate();
			if (predicate != null) {
				Path path = p.getCompiledEquivalentPath(pathFactory);
				if (path == null) {
					TransformAttribute attr = frame.getAttribute(predicate);
					if (attr != null) {
						MappedProperty m = new MappedProperty(sourceShape, p);
						attr.add(m);
						
						TransformFrame childFrame = attr.getEmbeddedFrame();
						if (childFrame != null) {
							Shape childShape = p.getShape();
							if (childShape != null) {
								addSourceShape(childFrame, childShape);
							}
						}
					}
				} else {
					handlePath(frame, sourceShape, p, path);
					
				}
			}
			
			
		}
		
	}

	private void handlePath(TransformFrame frame, Shape sourceShape, PropertyConstraint p, Path path) {
		
		int end = path.length()-1;
		for (int i=0; i<=end; i++) {
			URI first = RdfUtil.out(path, i);
			if (first == null) {
				break;
			} else {
				TransformAttribute attr = frame.getAttribute(first);
				if (attr != null) {
					MappedProperty m = new MappedProperty(sourceShape, p, i);
					attr.add(m);
					
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
												template, pathFactory.getNamespaceManager(), valueShape);
											
											if (info == null) {
												logger.warn("Cannot expand IRI template: " + template.toString());
											} else if (i==end-1) {
												Path subpath = path.subpath(i+1);
												
												if (inject(info, p, subpath)) {
													m.setTemplateInfo(info);
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
				Path qpath = q.getCompiledEquivalentPath(pathFactory);
				
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
