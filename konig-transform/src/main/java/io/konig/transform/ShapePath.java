package io.konig.transform;

import io.konig.core.io.AbstractPrettyPrintable;
import io.konig.core.io.PrettyPrintWriter;
import io.konig.shacl.Shape;

public class ShapePath extends AbstractPrettyPrintable {

	private String path;
	private Shape shape;
	private int count;
	
	public ShapePath(String path, Shape shape) {
		this.path = path;
		this.shape = shape;
	}
	
	@Override
	public int hashCode() {
		return path.hashCode()*31 + shape.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ShapePath) {
			ShapePath other = (ShapePath)obj;
			return path.equals(other.getPath()) && shape == other.getShape();
		}
		return false;
	}

	public String getPath() {
		return path;
	}

	public Shape getShape() {
		return shape;
	}

	public int getCount() {
		return count;
	}

	public void incrementCount() {
		count++;
	}
	
	public void decrementCount() {
		count--;
	}

	@Override
	public void print(PrettyPrintWriter out) {
		out.beginObject(this);
		out.field("path", path);
		out.field("shape", shape.getId());
		out.field("count", count);
		out.endObject();
		
	}
	
}
