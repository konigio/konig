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
