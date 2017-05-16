package io.konig.transform.assembly;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openrdf.model.URI;

import io.konig.core.Path;
import io.konig.core.path.InStep;
import io.konig.core.path.OutStep;
import io.konig.core.path.Step;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeManager;

public class PropertyManager {
	
	private Map<URI,List<PropertyAccessPoint>> accessPointMap = new HashMap<>();
	
	public PropertyManager(ShapeManager shapeManager) {
		init(shapeManager);
	}
	
	private void init(ShapeManager shapeManager) {
		for (Shape shape : shapeManager.listShapes()) {
			processShape(shape);
		}
		
	}

	private void processShape(Shape shape) {
		for (PropertyConstraint p : shape.getProperty()) {
			PropertyAccessPoint point = new PropertyAccessPoint(shape, p);
			add(point);
			Path path = p.getEquivalentPath();
			if (path != null) {
				processPath(shape, p, path);
			}
		}
		
	}

	private void processPath(Shape shape, PropertyConstraint p, Path path) {
		List<Step> list = path.asList();
		for (int i=0; i<list.size(); i++) {
			URI predicate = null;
			Step step = list.get(i);
			if (step instanceof OutStep) {
				predicate = ((OutStep) step).getPredicate();
			} else if (step instanceof InStep) {
				predicate = ((InStep) step).getPredicate();
			}
			if (predicate != null) {
				PropertyAccessPoint point = new EquivalentPathPropertyAccessPoint(shape, p, i, predicate);
				add(point);
			}
		}
		
	}

	public void add(PropertyAccessPoint p) {
		URI predicate = p.getTargetPredicate();
		List<PropertyAccessPoint> list = accessPointMap.get(predicate);
		if (list == null) {
			list = new ArrayList<>();
			accessPointMap.put(predicate, list);
		}
		list.add(p);
	}
	public List<PropertyAccessPoint> getAccessPoint(URI predicate) {
		return accessPointMap.get(predicate);
	}

}
