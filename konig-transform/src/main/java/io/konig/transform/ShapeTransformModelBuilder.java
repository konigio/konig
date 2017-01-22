package io.konig.transform;

import java.util.Iterator;
import java.util.List;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;

import io.konig.core.Path;
import io.konig.core.path.OutStep;
import io.konig.core.path.PathFactory;
import io.konig.core.path.PathImpl;
import io.konig.core.path.Step;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeManager;

public class ShapeTransformModelBuilder {
	
	private ShapeManager shapeManager;
	private PathFactory pathFactory;

	public ShapeTransformModelBuilder(ShapeManager shapeManager, PathFactory pathFactory) {
		this.shapeManager = shapeManager;
		this.pathFactory = pathFactory;
	}

	public ShapeTransformModel build(Shape target) throws ShapeTransformException {
		
		Worker worker = new Worker();
		return worker.build(target);
		
	}
	
	private class Worker {

		private ShapeTransformModel model;

		public ShapeTransformModel build(Shape target) throws ShapeTransformException {
			
			URI targetClass = target.getTargetClass();
			if (targetClass == null) {
				throw new ShapeTransformException("Target Class must be defined on Shape: " + target.getId());
			}
			
			model = new ShapeTransformModel(target);
			
			List<Shape> sourceShapeList = sourceShapeList(targetClass);
			for (Shape s : sourceShapeList) {
				TransformElement e = createTransformElement(s);
				if (e != null) {
					model.add(e);
				}
			}
			
			return model;
		}
		
	

		private TransformElement createTransformElement(Shape s) throws ShapeTransformException {
			
			TransformElement result = null;
			
			
			for (PropertyConstraint p : s.getProperty()) {
				PropertyTransform t = createPropertyTransform(p);
				if (t != null) {
					if (result == null) {
						result = new TransformElement(s);
					}
					result.add(t);
				}
			}
			
			
			return result;
		}



		private PropertyTransform createPropertyTransform(PropertyConstraint p) throws ShapeTransformException {
			
			Path path = p.getCompiledEquivalentPath(pathFactory);
			if (path == null) {
				return predicatePropertyTransform(p);
			} 
			return pathPropertyTransform(p);
		}



		private PropertyTransform pathPropertyTransform(PropertyConstraint p) throws ShapeTransformException {
			
			if (p.getPredicate() == null) {
				return null;
			}
			
				
			Path targetPath = p.getCompiledEquivalentPath();

			
			Shape targetShape = model.getTargetShape();
			
			List<Step> stepList = targetPath.asList();
			
			for (Step s : stepList) {
				
				if (targetShape == null) {
					return null;
				}
				
				if (s instanceof OutStep) {
					OutStep out = (OutStep) s;
					URI predicate = out.getPredicate();
					
					PropertyConstraint q = targetShape.getPropertyConstraint(predicate);
					if (q == null) {
						return null;
					}
					
					targetShape = q.getShape(shapeManager);
				}
			}

			Path sourcePath = new PathImpl().out(p.getPredicate());
			
			return new PropertyTransform(sourcePath, targetPath);
		}


		private PropertyTransform predicatePropertyTransform(PropertyConstraint p) {
			PropertyTransform result = null;
			Shape  target = model.getTargetShape();
			URI predicate = p.getPredicate();
			if (predicate != null) {
				PropertyConstraint q = target.getPropertyConstraint(predicate);
				if (q != null) {
					Path path = new PathImpl().out(predicate);
					
					result = new PropertyTransform(path, path);
				}
			}
			return result;
		}

		private List<Shape> sourceShapeList(URI targetClass) {
			Shape targetShape = model.getTargetShape();
			Resource targetShapeId = targetShape.getId();
			
			List<Shape> list = shapeManager.getShapesByTargetClass(targetClass);
			Iterator<Shape> sequence = list.iterator();
			while (sequence.hasNext()) {
				Shape s = sequence.next();
				// TODO: Implement other filters for the source shape
				if (s==targetShape || (targetShapeId!=null && targetShapeId.equals(s.getId()))) {
					sequence.remove();
					break;
				}
			}
			return list;
		}
	}
	
}
