package io.konig.transform.factory;

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
