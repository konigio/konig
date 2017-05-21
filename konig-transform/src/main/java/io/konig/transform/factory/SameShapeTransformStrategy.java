package io.konig.transform.factory;

import java.util.ArrayList;
import java.util.List;

import org.openrdf.model.URI;

import io.konig.shacl.Shape;
import io.konig.shacl.ShapeManager;

/**
 * The simplest possible ShapeTransformStrategy which selects the source shape to be
 * identical to the target shape, except in the case of the root node.
 * @author Greg McFall
 *
 */
public class SameShapeTransformStrategy implements TransformStrategy {
	
	private ShapeRuleFactory factory;
	

	@Override
	public List<SourceShape> findCandidateSourceShapes(TargetShape target) throws TransformBuildException {
		Shape targetShape = target.getShape();
		List<SourceShape> result = new ArrayList<>();
		TargetProperty ta = target.getAccessor();
		if (ta==null) {
			ShapeManager shapeManager = factory.getShapeManager();
			URI targetClass = targetShape.getTargetClass();
			List<Shape> sourceList = shapeManager.getShapesByTargetClass(targetClass);
			
			for (Shape sourceShape : sourceList) {
				if (sourceShape == targetShape) {
					continue;
				}
				SourceShape source = SourceShape.create(sourceShape);
				target.match(source);
				
				result.add(source);
			}
		} else {
			SourceShape source = SourceShape.create(targetShape);
			SourceProperty sp = new SourceProperty(ta.getPropertyConstraint());
			sp.setParent(source);
			sp.setMatch(ta);
			sp.setNestedShape(source);
			target.match(source);
			ta.setPreferredMatch(sp);
			
			result.add(source);
		}
		return result;
	}


	@Override
	public void init(ShapeRuleFactory factory) {
		this.factory = factory;
	}

}
