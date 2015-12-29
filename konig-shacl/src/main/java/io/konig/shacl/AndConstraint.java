package io.konig.shacl;

import java.util.ArrayList;
import java.util.List;

import io.konig.core.Vertex;

public class AndConstraint implements Constraint, ShapeConsumer {

	private List<Shape> shapes = new ArrayList<>();

	public List<Shape> getShapes() {
		return shapes;
	}
	
	public AndConstraint add(Shape shape) {
		shapes.add(shape);
		return this;
	}

	@Override
	public boolean accept(Vertex v) {
		
		GraphFilter filter = GraphFilter.INSTANCE;
		for (Shape s : shapes) {
			if (!filter.matches(v, s)) {
				return false;
			}
		}
		
		return true;
	}
}
