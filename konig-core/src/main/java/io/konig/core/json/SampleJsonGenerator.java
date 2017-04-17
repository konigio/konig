package io.konig.core.json;

/*
 * #%L
 * Konig Core
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

import org.openrdf.model.URI;

import io.konig.core.impl.RdfUtil;
import io.konig.core.io.PrettyPrintWriter;
import io.konig.core.util.RandomGenerator;
import io.konig.shacl.NodeKind;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;

public class SampleJsonGenerator {
	
	private String domain = "http://example.com/";
	
	public void generate(Shape shape, PrettyPrintWriter out) {
		Worker worker = new Worker(out);
		worker.print(shape);
		
	}
	
	private class Worker {
		
		private PrettyPrintWriter out;
		private RandomGenerator random;

		public Worker(PrettyPrintWriter out) {
			this.out = out;
		}
	
		public void print(Shape shape) {
			random = new RandomGenerator(shape.getId().hashCode());
			printShape(shape);
		}

		public void printShape(Shape shape) {
			out.print('{');
			out.pushIndent();
			String comma = "";
			if (shape.getNodeKind().equals(NodeKind.IRI)) {
				out.println();
				out.indent();
				quote("id");
				out.print(" : ");
				quote(individualOf(shape.getTargetClass()));
				comma = ",";
			}
			
			List<PropertyConstraint> properties = shape.getProperty();
			if (!properties.isEmpty()) {
				properties = new ArrayList<>(properties);
				RdfUtil.sortByLocalName(properties);
				for (PropertyConstraint p : properties) {
					out.println(comma);
					out.indent();
					printProperty(p);
					comma = ",";
				}
			}
			
		
			
			out.popIndent();
			out.indent();
			out.println('}');
			
		}

		private void printProperty(PropertyConstraint p) {
			
			URI predicate = p.getPredicate();
			quote(predicate.getLocalName());
			out.print(" : ");
			
//			URI datatype 
			
		}

		private String individualOf(URI owlClass) {
			StringBuilder builder = new StringBuilder();
			builder.append(domain);
			builder.append(owlClass.getLocalName());
			builder.append('/');
			builder.append(random.alphanumeric(9));
			return builder.toString();
		}

		private void quote(String value) {
			out.print('"');
			out.print(value);
			out.print('"');
			
		}
		
		
	}

}
