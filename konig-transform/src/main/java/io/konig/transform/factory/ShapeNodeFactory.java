package io.konig.transform.factory;

import java.util.Collection;
import java.util.List;

import org.openrdf.model.URI;
import org.openrdf.model.Value;

import io.konig.core.KonigException;
import io.konig.core.Path;
import io.konig.core.path.HasStep;
import io.konig.core.path.HasStep.PredicateValuePair;
import io.konig.core.path.InStep;
import io.konig.core.path.OutStep;
import io.konig.core.path.Step;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;

abstract public class ShapeNodeFactory<S extends ShapeNode<P>, P extends PropertyNode<S>> {


	public S createShapeNode(Shape shape) {
		S shapeNode = shape(shape);
		addProperties(shapeNode, shape.getProperty(), false);
		return shapeNode;
	}
	
	protected void addProperties(S shapeNode, Collection<PropertyConstraint> propertyList, boolean isDerived) {

		for (PropertyConstraint p : propertyList) {
			SharedSourceProperty preferredMatch = sharedSourceProperty(p);
			P propertyNode = property(p, -1, preferredMatch);
			propertyNode.setDerived(isDerived);
			shapeNode.add(propertyNode);
			handlePath(propertyNode, preferredMatch);
			handleValueShape(propertyNode);
		}
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
			S parent = propertyNode.getParent();
			int lastStep = stepList.size()-1;
			for (int i=0; i<stepList.size(); i++) {
				Step step = stepList.get(i);
				if (step instanceof OutStep) {
					
					if (i == 0) {
						parent = propertyNode.getParent();
					} else {
						parent = stepNode.getNestedShape();
						if (parent ==null) {
							parent = shape(null);
							stepNode.setNestedShape(parent);
						}
					}
					stepNode = property(p, i, i==lastStep ? preferredMatch : null);
					parent.add(stepNode);
				}
				if (step instanceof HasStep) {
					S container = stepNode.getNestedShape();
					if (container == null) {
						container = shape(null);
						stepNode.setNestedShape(container);
					}
					HasStep hasStep = (HasStep) step;
					for (PredicateValuePair pair : hasStep.getPairList()) {
						URI predicate = pair.getPredicate();
						Value value = pair.getValue();
						P pNode = property(p, i, predicate, value);
						container.add(pNode);
					}
				}
				if (step instanceof InStep) {
					throw new KonigException("In steps not supported");
				}
			}
		}
		
	}
	
	protected SharedSourceProperty sharedSourceProperty(PropertyConstraint p) {
		return null;
	}
	
	abstract protected S shape(Shape shape);
	
	abstract protected P property(PropertyConstraint p, int pathIndex, SharedSourceProperty preferredMatch);
	
	abstract protected P property(PropertyConstraint p, int pathIndex, URI predicate, Value value);


}
