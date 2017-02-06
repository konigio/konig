package io.konig.shacl.services;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Namespace;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.model.vocabulary.XMLSchema;

import io.konig.core.Edge;
import io.konig.core.Graph;
import io.konig.core.KonigException;
import io.konig.core.NamespaceManager;
import io.konig.core.OwlReasoner;
import io.konig.core.Vertex;
import io.konig.core.util.IriTemplate;
import io.konig.core.util.SimpleValueMap;
import io.konig.shacl.NodeKind;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeManager;
import io.konig.shacl.ShapeVisitor;

/**
 * A service which generates a shape based on example instances of a given class.
 * @author Greg McFall
 *
 */
public class ShapeProducer {
	private NamespaceManager nsManager;
	private ShapeMatcher matcher;
	private ShapeVisitor visitor;
	
	
	public ShapeProducer(NamespaceManager nsManager, ShapeManager shapeManager) {
		this.nsManager = nsManager;
		matcher = new ShapeMatcher(shapeManager);
	}
	
	

	public ShapeVisitor getVisitor() {
		return visitor;
	}



	public void setVisitor(ShapeVisitor visitor) {
		this.visitor = visitor;
	}



	public Shape produceShape(Vertex targetClass, IriTemplate shapeIdTemplate) {
		return produceShape(targetClass, null, shapeIdTemplate);
	}
	
	/**
	 * Get or create a shape for a specified OWL Class with a specified name.
	 * <p>
	 * This method will return an existing shape if one exists that is consistent with all instances.
	 * If there is more than one existing shape, then this method returns the the smallest consistent shape 
	 * (i.e. the one with the fewest properties).  If there are multiple, existing, consistent shapes of 
	 * minimal size, the method returns the one whose name comes first lexically.  
	 * </p>
	 * 
	 * <p>
	 * If no existing, consistent shape is found, this method will return a newly created shape.
	 * This method iterates over all properties on all instances of the OWL Class.
	 * It ensures that the returned shape contains an appropriate PropertyConstraint
	 * for each property according to the rules below.
	 * <ol>
	 * 	<li> If all values are literals of the same type, then the sh:datatype is set to that type.
	 *  <li> If the values are literals of different types, and error occurs.
	 * </ol>
	 * </p>
	 * @param targetClass The OWL Class for which a Shape is to be produced.
	 * @param shapeId The URI that names the shape if a new Shape is created.
	 * @return An existing Shape consistent with all instances of the OWL Class, or a newly minted Shape.
	 */
	public Shape produceShape(Vertex targetClass, URI shapeId) {
		return produceShape(targetClass, shapeId, null);
	}

	private Shape produceShape(Vertex targetClass, URI shapeId, IriTemplate shapeIdTemplate) {
		Worker worker = new Worker(targetClass);
		return worker.produceShape(targetClass, shapeId, shapeIdTemplate);
	}

	
	
	private class Worker {
		private SimpleValueMap valueMap;
		
		private Vertex targetClass;
		private URI shapeId;
		private OwlReasoner reasoner;
		
		public Worker(Vertex targetClass) {
			this.targetClass = targetClass;
			valueMap = new SimpleValueMap();
			reasoner = new OwlReasoner(graph());
		}
		

		Graph graph() {
			return targetClass.getGraph();
		}

		public Shape produceShape(Vertex targetClass, URI shapeId, IriTemplate shapeIdTemplate) {
			
			Shape shape = null;
			Resource id = targetClass.getId();
			if (id instanceof URI) {
				URI targetClassId = (URI) id;
				


				List<Vertex> individuals = targetClass.asTraversal().in(RDF.TYPE).toVertexList();
				shape = matcher.bestMatch(individuals, targetClassId);
				if (shape == null) {
					if (shapeId == null) {
						shapeId = shapeId(targetClassId, shapeIdTemplate);
					}
					if (this.shapeId == null) {
						this.shapeId = shapeId;
					}
					
					shape = new Shape(shapeId);
					shape.setNodeKind(NodeKind.IRI);
					shape.setTargetClass(targetClassId);
					
					Map<URI,List<Vertex>> bnodeMap = new HashMap<URI, List<Vertex>>();
					addProperties(shape, individuals, bnodeMap);
					handleBNodes(shape, bnodeMap);
					if (visitor != null) {
						visitor.visit(shape);
					}
				}
				
				
			}
			
			return shape;
			
		}


		private void handleBNodes(Shape shape, Map<URI, List<Vertex>> bnodeMap) {
			for (Entry<URI, List<Vertex>> e : bnodeMap.entrySet()) {
				URI predicate = e.getKey();
				List<Vertex> list = e.getValue();
				PropertyConstraint p = shape.getPropertyConstraint(predicate);
				URI targetClass = range(p.getPredicate());
				
				Shape valueShape = null;
				if (targetClass != null) {
					valueShape = matcher.bestMatch(list, targetClass);
				}
				if (valueShape == null) {
					URI shapeId = nestedShapeId(shape, p);
					valueShape = new Shape(shapeId);
					valueShape.setTargetClass(targetClass);
					Map<URI, List<Vertex>> map = new HashMap<>();
					addProperties(valueShape, list, map);
					if (visitor!=null) {
						visitor.visit(valueShape);
					}
				}
				p.setShape(valueShape);
			}
			
		}


		private URI nestedShapeId(Shape shape, PropertyConstraint p) {

			StringBuilder builder = new StringBuilder();
			builder.append(shape.getId().stringValue());
			builder.append('/');
			builder.append(p.getPredicate().getLocalName());
			return new URIImpl(builder.toString());
		}


		private URI range(URI predicate) {
			if (predicate != null) {
				Vertex v = graph().getVertex(predicate);
				if (v != null) {
					Value range = v.getValue(RDFS.RANGE);
					if (range instanceof URI) {
						return (URI) range;
					}
				}
			}
			
			return null;
		}


		private void addProperties(Shape shape, List<Vertex> individuals, Map<URI,List<Vertex>> bnodeMap) {
			
			for (Vertex v : individuals) {
				handleIndividual(shape, v, bnodeMap);
			}
			
			
		}

		private void handleIndividual(Shape shape, Vertex v, Map<URI, List<Vertex>> bnodeMap) {
			
			Set<Entry<URI, Set<Edge>>> set = v.outEdges();
			for (Entry<URI, Set<Edge>> e : set) {
				URI predicate = e.getKey();
				
				if (predicate == RDF.TYPE) {
					continue;
				}
				
				Set<Edge> edges = e.getValue();
				
				PropertyConstraint p = shape.getPropertyConstraint(predicate);
				boolean unbounded = false;
				if (p == null) {
					p = new PropertyConstraint(predicate);
					shape.add(p);
				} else {
					unbounded = p.getMaxCount()==null;
				}
				p.setMinCount(0);
				p.setMaxCount(edges.size()==1 && !unbounded ? 1 : null);
				
				for (Edge edge : edges) {
					setValueType(shape, p, edge, bnodeMap);
				}
				
			}
			
		}


		private void setValueType(Shape shape, PropertyConstraint p, Edge edge, Map<URI, List<Vertex>> bnodeMap) {
			URI priorDatatype = p.getDatatype();			
			
			Value object = edge.getObject();
			
			if (object instanceof Literal) {
				Literal literal = (Literal)object;
				URI datatype = literal.getDatatype();
				if (datatype == null) {
					datatype = XMLSchema.STRING;
				}
				
				if (priorDatatype == null) {
					p.setDatatype(datatype);
				} else if (!priorDatatype.equals(datatype)) {
					
					StringBuilder err = new StringBuilder();
					err.append("Conflicting datatypes for ");
					err.append(localName(shape));
					err.append('.');
					err.append(p.getPredicate().getLocalName());
					err.append(": <");
					err.append(priorDatatype.stringValue());
					err.append("> and <");
					err.append(datatype.stringValue());
					err.append(">");
					throw new KonigException(err.toString());
				}
				
			} else if (object instanceof BNode) {
				
				NodeKind kind = p.getNodeKind();
				p.setNodeKind(NodeKind.or(kind, NodeKind.BlankNode));
				
				URI predicate = edge.getPredicate();
				Vertex v = graph().getVertex((Resource)object);
				List<Vertex> list = bnodeMap.get(predicate);
				if (list == null) {
					list = new ArrayList<>();
					bnodeMap.put(predicate, list);
				}
				list.add(v);
				
			} else {

				NodeKind kind = p.getNodeKind();
				p.setNodeKind(NodeKind.or(kind, NodeKind.IRI));
				
				URI objectId = (URI) object;
				
				
				Resource valueClass = p.getValueClass();
				if (valueClass == null) {
					valueClass = range(p.getPredicate());
				}
				
				Resource objectType = type(objectId);
				valueClass = reasoner.leastCommonSuperClass(valueClass, objectType);
				p.setValueClass(valueClass);
			}
			
		}



		private URI type(Resource objectId) {
			
			Vertex v = graph().getVertex(objectId);
			if (v != null) {
				Set<Edge> out = v.outProperty(RDF.TYPE);
				Resource best = null;
			
				for (Edge edge : out) {
					Resource type = (Resource) edge.getObject();
					if (best == null) {
						best = type;
					} else {
						best = reasoner.leastCommonSuperClass(best,  type);
					}
				}
				if (best instanceof URI) {
					return (URI) best;
				}
			}
			
			return null;
		}


		private String localName(Shape shape) {
			
			return ((URI) shape.getId()).getLocalName();
		}


		private URI shapeId(URI targetClassId, IriTemplate shapeIdTemplate) {
			
			String classLocalName = targetClassId.getLocalName();
			String classNamespaceIri = targetClassId.getNamespace();
			Namespace n = nsManager.findByName(classNamespaceIri);
			String classNamespacePrefix = n==null ? null : n.getPrefix();
			
			valueMap.put("classLocalName", classLocalName);
			valueMap.put("classNamespacePrefix", classNamespacePrefix);
			
			return shapeIdTemplate.expand(valueMap);
		}
	}


}
