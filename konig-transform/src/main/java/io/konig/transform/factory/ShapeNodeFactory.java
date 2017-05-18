package io.konig.transform.factory;

import java.util.List;

import io.konig.core.Path;
import io.konig.core.path.OutStep;
import io.konig.core.path.Step;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;

abstract public class ShapeNodeFactory<S extends ShapeNode<P>, P extends PropertyNode<S>> {


	public S createShapeNode(Shape shape) {
		S shapeNode = shape(shape);
		for (PropertyConstraint p : shape.getProperty()) {
			SharedSourceProperty preferredMatch = sharedSourceProperty(p);
			P propertyNode = property(p, -1, preferredMatch);
			shapeNode.add(propertyNode);
			handlePath(propertyNode, preferredMatch);
			handleValueShape(propertyNode);
		}
		return shapeNode;
	}

	private void handleValueShape(P propertyNode) {
		
		PropertyConstraint p = propertyNode.getPropertyConstraint();
		Shape valueShape = p.getShape();
		if (valueShape != null) {
			S nested = createShapeNode(valueShape);
			propertyNode.setNestedShape(nested);
		}
		
	}

	private void handlePath(P propertyNode, SharedSourceProperty preferredMatch) {
		PropertyConstraint p = propertyNode.getPropertyConstraint();
		Path path = p.getEquivalentPath();
		if (path != null) {
			
			List<Step> stepList = path.asList();
			P stepNode = null;
			int lastStep = stepList.size()-1;
			for (int i=0; i<stepList.size(); i++) {
				Step step = stepList.get(i);
				if (step instanceof OutStep) {
					
					S parent = null;
					if (i == 0) {
						parent = propertyNode.getParent();
					} else {
						parent = shape(null);
						stepNode.setNestedShape(parent);
					}
					stepNode = property(p, i, i==lastStep ? preferredMatch : null);
					parent.add(stepNode);
				}
			}
		}
		
	}
	
	protected SharedSourceProperty sharedSourceProperty(PropertyConstraint p) {
		return null;
	}
	
	abstract protected S shape(Shape shape);
	
	abstract protected P property(PropertyConstraint p, int pathIndex, SharedSourceProperty preferredMatch);


}
