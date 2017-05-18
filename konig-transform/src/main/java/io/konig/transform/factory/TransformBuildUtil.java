package io.konig.transform.factory;

import java.util.ArrayList;
import java.util.List;

import io.konig.core.util.TurtleElements;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;

public class TransformBuildUtil {

	public static int countShapeProperties(Shape shape) throws TransformBuildException {
		List<PropertyConstraint> path = new ArrayList<>();
		return countShapeProperties(path, shape);
	}

	private static int countShapeProperties(List<PropertyConstraint> path, Shape shape) throws TransformBuildException {
		
		int count = 0;
		for (PropertyConstraint p : shape.getProperty()) {
			if (path.contains(p)) {
				StringBuilder builder = new StringBuilder();
				builder.append("Rescursive shapes not supported.  Recursion detected on Shape ");
				builder.append(TurtleElements.resource(shape.getId()));
				builder.append(" at property ");
				builder.append(TurtleElements.resource(p.getPredicate()));
				throw new TransformBuildException(builder.toString());
			}
			count++;
			Shape valueShape = p.getShape();
			if (valueShape != null) {
				path.add(p);
				count += countShapeProperties(path, valueShape);
				path.remove(path.size()-1);
			}
		}
		return count;
	}

	public static void appendSimplePath(StringBuilder builder, TargetProperty tp) {
		List<TargetProperty> list = new ArrayList<>();
		while (tp != null) {
			list.add(tp);
			TargetShape parent = tp.getParent();
			tp = parent == null ? null : parent.getAccessor();
		}
		String slash = "";
		for (int i=list.size()-1; i>=0; i--) {
			tp = list.get(i);
			builder.append(slash);
			slash = "/";
			builder.append(tp.getPredicate().getLocalName());
		}
	}
	
	
}
