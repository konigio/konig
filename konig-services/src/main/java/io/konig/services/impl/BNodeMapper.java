package io.konig.services.impl;

/*
 * #%L
 * Konig Services
 * %%
 * Copyright (C) 2015 - 2016 Gregory McFall
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.openrdf.model.BNode;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.vocabulary.RDFS;

import io.konig.core.Edge;
import io.konig.core.Graph;
import io.konig.core.Vertex;
import io.konig.core.impl.EdgeImpl;

public class BNodeMapper {

	Map<BNode, BNode> match = new HashMap<>();
	
	
	static class Scope {
		Map<BNode, BNode> match = new HashMap<>();
		Map<BNode, BNode> inverse = new HashMap<>();
		List<BNode> list = new ArrayList<>();
		
		void put(BNode x, BNode y) {
			match.put(x,  y);
			inverse.put(y,  x);
			list.add(x);
		}
		
		void rollback(int point) {
			for (int i=list.size()-1; i>=point; i--) {
				BNode x = list.remove(i);
				BNode y = match.remove(x);
				inverse.remove(y);
			}
		}
		
	}
	
	
	public Map<Vertex, Vertex> scan(Graph a, Graph b) {
		Scope scope = new Scope();
		for (Vertex x : a.vertices()) {
			if (x.getId() instanceof URI) {
				
				Vertex y = b.getVertex(x.getId());
				if (y != null) {
					
					
					for (Edge ex : x.outEdgeSet()) {
						Value ox = ex.getObject();
						if (ox instanceof BNode) {
							
							Vertex vx = a.getVertex((BNode)ox);
							URI predicate = ex.getPredicate();
							
							Set<Edge> yEdges = y.outProperty(predicate);
							for (Edge ey : yEdges) {
								Value oy = ey.getObject();
								if (oy instanceof BNode) {
									Vertex vy = b.getVertex((BNode)oy);
									
//									print("scan", vx, vy);
									match(scope, vx, vy);
								}
							}
							
						}
					}
				}
			}
		}
		return output(scope, a, b);
	}

	private Map<Vertex, Vertex> output(Scope scope, Graph a, Graph b) {
		Map<Vertex, Vertex> result = new HashMap<>();
		for (Entry<BNode, BNode> e : scope.match.entrySet()) {
			BNode x = e.getKey();
			BNode y = e.getValue();
			
			result.put(a.vertex(x), b.vertex(y));
		}
		
		return result;
	}
	
//	private String label(Vertex v) {
//		Value value = v.asTraversal().firstValue(RDFS.LABEL);
//		return value==null ? "null" : value.stringValue();
//	}
	
//	private void print(String method, Vertex x, Vertex y) {
//		System.out.println(method + " " + label(x) + " " + label(y));
//	}

	private boolean match(Scope scope, Vertex x, Vertex y) {

//		print("begin-match", x, y);
		
		Graph xGraph = x.getGraph();
		Graph yGraph = y.getGraph();
		
		BNode xNode = (BNode)x.getId();
		BNode yNode = (BNode)y.getId();
		
		BNode peer = scope.match.get(xNode);
		if (peer == yNode) {
			return true;
		}
		if (peer != null) {
//			print("duplicate-peer", x, y);
			return false;
		}
		peer = scope.inverse.get(yNode);
		
		if (peer != null) {
//			print("bad-inverse", x, y);
			return false;
		}
		
//		print("put", x, y);
		int mark = scope.list.size();
		scope.put(xNode, yNode);
		
		List<Edge> bnodeOut = new ArrayList<>();
		
		for (Edge ex : x.outEdgeSet()) {
			Value object = ex.getObject();
			
			if (object instanceof BNode) {
				bnodeOut.add(ex);
			} else {
				URI predicate = ex.getPredicate();
				Edge edge = new EdgeImpl(y.getId(), predicate, object);
				if (!y.hasEdge(edge)) {
//					print("remove", x, y);
					scope.rollback(mark);
					return false;
				}
			}
		}
		
		for (Edge ex : bnodeOut) {
			URI predicate = ex.getPredicate();
			BNode u = (BNode) ex.getObject();
			
			Vertex uVertex = xGraph.getVertex(u);
			boolean predicateOK = false;

//			print("analyze-out", x, uVertex);
			for (Edge ey : y.outProperty(predicate)) {
				Value objectY = ey.getObject();
				if (objectY instanceof BNode) {
					Vertex v = yGraph.getVertex((BNode)objectY);
					if (match(scope, uVertex, v)) {
						predicateOK = true;
						break;
					}
				}
			}
			if (!predicateOK) {
//				print("remove", x, y);
				scope.rollback(mark);
				return false;
			}
				
		}
		
		for (Edge ex : x.inEdgeSet()) {
			
			Resource subject = ex.getSubject();
			URI predicate = ex.getPredicate();
			
			if (subject instanceof BNode) {
				
				Vertex uVertex = xGraph.getVertex(subject);
				boolean predicateOK = false;
//				print("analyze-in", x, uVertex);
				for (Edge ey : y.inProperty(predicate)) {
					Value subjectY = ey.getSubject();
					if (subjectY instanceof BNode) {
						Vertex v = yGraph.getVertex((BNode)subjectY);
						if (match(scope, uVertex, v)) {
							predicateOK = true;
							break;
						}
					}
				}
				if (!predicateOK) {
//					print("remove", x, y);
					scope.rollback(mark);
					return false;
				}
				
			}
		}
		
//		print("end-match", x, y);
		
		return true;
	}
	
	
}
