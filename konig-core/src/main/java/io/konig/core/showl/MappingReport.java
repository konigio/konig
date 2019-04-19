package io.konig.core.showl;

/*
 * #%L
 * Konig Core
 * %%
 * Copyright (C) 2015 - 2019 Gregory McFall
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


import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openrdf.model.Resource;

import io.konig.core.NamespaceManager;
import io.konig.core.io.PrettyPrintWriter;
import io.konig.core.util.IriTemplate;
import io.konig.core.util.ValueFormat;

public class MappingReport {

	public MappingReport() {
	}
	
	public void write(Writer out, ShowlManager manager, NamespaceManager nsManager) {
		
		PrettyPrintWriter pretty = PrettyPrintWriter.of(out);
		pretty.setNamespaceManager(nsManager);
		
		Worker worker = new Worker(manager, pretty);
		
		worker.printAll();
		pretty.flush();
		
	}
	
	
	private static class Worker {
		private ShowlManager manager;
		private PrettyPrintWriter out;
		
		public Worker(ShowlManager manager, PrettyPrintWriter out) {
			this.manager = manager;
			this.out = out;
		}
		public void printAll() {
			for (Resource shapeId : manager.listNodeShapeIds()) {
				ShowlNodeShapeSet set = manager.getNodeShapeSet(shapeId);
				for (ShowlNodeShape shape : set) {
					if (shape.getAccessor()==null) {
						printRootShape(shape);
						out.println();
					}
				}
				
			}
			
		}
		
		
		private void printRootShape(ShowlNodeShape shape) {
			NodeNamer namer = new NodeNamer();
			out.indent();
			out.print("CONSTRUCT ");
			out.resource(shape.getId());
			out.println();
			printJoin(shape, namer);
			printProperties(shape, namer);
			

			out.println();
			
		}
		
		private void printJoin(ShowlNodeShape shape, NodeNamer namer) {
			
			// Collect and sort Join Conditions
			
			Set<ShowlJoinCondition> set = new HashSet<>();
			for (ShowlDirectPropertyShape direct : shape.getProperties()) {
				for (ShowlMapping m : direct.getMappings()) {
					set.add(m.getJoinCondition());
				}
			}
			
			List<ShowlJoinCondition> list = new ArrayList<>(set);
			
			// Print FROM clause
			
			out.indent();
			out.print("FROM");
			boolean indented = list.size()>1;
			if (indented) {
				out.pushIndent();
			}
			for (ShowlJoinCondition join : list) {
				if (indented) {
					out.println();
					out.indent();
				} else {
					out.print(' ');
				}
				
				ShowlNodeShape other = join.otherNode(shape);
				out.uri(other.getShape().getIri());
				String varName = join.focusAlias(namer);
				out.print(" AS ");
				out.print(varName);
			}
			
			if (indented) {
				out.popIndent();
			}
			out.println();
			
			// Print WHERE clause
			
//			out.indent();
//			out.print("WHERE");
//
//			if (indented) {
//				out.pushIndent();
//			}
//			
//			for (ShowlJoinCondition join : list) {
//				if (indented) {
//					out.println();
//					out.indent();
//				} else {
//					out.print(' ');
//				}
//				ShowlPropertyShape left = join.getLeft();
//				ShowlPropertyShape right = join.getRight();
//				
//				if (right.getDeclaringShape() == shape) {
//					ShowlPropertyShape tmp = left;
//					left = right;
//					right = tmp;
//				}
//				
//				String leftText = joinValue(left, shape, join, namer);
//				String rightText = joinValue(right, shape, join, namer);
//				
//				out.print(leftText);
//				out.print(" = ");
//				out.print(rightText);
//				
//			}
//
//			if (indented) {
//				out.popIndent();
//			}
//			out.println();
		}
		
		
		private String joinValue(ShowlPropertyShape p, ShowlNodeShape shape, ShowlJoinCondition join,
				NodeNamer namer) {
			if (p.getDeclaringShape() == shape) {
				return p.getPredicate().getLocalName();
			}
			
			if (p instanceof ShowlTemplatePropertyShape) {
				ShowlTemplatePropertyShape t = (ShowlTemplatePropertyShape) p;
				IriTemplate template = t.getTemplate();
				StringBuilder builder = new StringBuilder();
				builder.append("CONCAT(");
				String comma = "";
				String shapeAlias = join.focusAlias(namer);
				for (ValueFormat.Element e : template.toList()) {
					builder.append(comma);
					comma = ", ";
					switch (e.getType()) {
					case TEXT:
						builder.append('"');
						builder.append(e.getText());
						builder.append('"');
						break;
						
					case VARIABLE:
						builder.append(shapeAlias);
						builder.append('.');
						builder.append(e.getText());
						break;
					}
				}
				builder.append(")");
				return builder.toString();
			}
			
			return path(p, join, namer);
		}
		private void printProperties(ShowlNodeShape shape, NodeNamer namer) {
			List<ShowlDirectPropertyShape> list = new ArrayList<>(shape.getProperties());
			if (list.isEmpty()) {
				return;
			}
			
			Collections.sort(list, new Comparator<ShowlDirectPropertyShape>(){

				@Override
				public int compare(ShowlDirectPropertyShape a, ShowlDirectPropertyShape b) {
					String x = a.getPredicate().getLocalName();
					String y = b.getPredicate().getLocalName();
					return x.compareTo(y);
				}});
			

			out.indent();
			out.print("SET");
			
			out.pushIndent();
			
			String comma = "";
			for (ShowlDirectPropertyShape p : list) {
				out.println(comma);
				comma = ", ";
				printProperty(p, namer);
			}
			
			out.popIndent();
			
		}
		private void printProperty(ShowlDirectPropertyShape p, NodeNamer namer) {
			
			out.indent();
			out.print(p.getPredicate().getLocalName());
			
			printPropertyMappings(p, namer);
			
			
			
		}
		
		private void printPropertyMappings(ShowlDirectPropertyShape p, NodeNamer namer) {
			
			Collection<ShowlMapping> mappings = p.getMappings();
			
			int size = mappings.size();
			
			if (size==0) {
				printMappingFromJoinCondition(p, namer);
				return;
			}

			if (size > 1) {
				out.pushIndent();
			}
			for (ShowlMapping m : mappings) {
				ShowlPropertyShape other = m.findOther(p);
				if (size == 1) {
					out.print(" = ");
				} else {
					out.println();
					out.indent();
					out.print("= ");
				}
				out.print(path(other, m.getJoinCondition(), namer));
				
			}
			
			if (size > 1) {
				out.popIndent();
			}
			
		}
		
		private void printMappingFromJoinCondition(ShowlDirectPropertyShape p, NodeNamer namer) {

			
			ShowlNodeShape node = p.getDeclaringShape();
			for (ShowlDirectPropertyShape q : node.getProperties()) {
				for (ShowlMapping m : q.getMappings()) {
					ShowlJoinCondition join = m.getJoinCondition();
					
					ShowlPropertyShape r = join.propertyOf(node);
					if (r == p) {
						ShowlPropertyShape other = join.otherProperty(r);
						out.print(" = ");
						out.print(joinValue(other, node, join, namer));
						return;
					}
				}
			}

			out.print(" = ?");
			
		}
		private String path(ShowlPropertyShape source, ShowlJoinCondition join, NodeNamer namer) {

			List<ShowlPropertyShape> elements = new ArrayList<>();
			ShowlNodeShape node = null;
			for (ShowlPropertyShape p=source; p!=null; p=node.getAccessor()) {
				elements.add(p);
				node = p.getDeclaringShape();
			}
			StringBuilder builder = new StringBuilder();
			String varname = join.focusAlias(namer);
			builder.append(varname);
			for (int i=elements.size()-1; i>=0; i--) {
				ShowlPropertyShape p = elements.get(i);
				builder.append(p.pathSeparator());
				builder.append(p.getPredicate().getLocalName());
			}
			
			return builder.toString();
		}

		
		
	}

}
