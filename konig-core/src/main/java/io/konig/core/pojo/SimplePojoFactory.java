package io.konig.core.pojo;

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


import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;

import io.konig.core.Edge;
import io.konig.core.Graph;
import io.konig.core.KonigException;
import io.konig.core.Vertex;
import io.konig.core.vocab.Konig;

public class SimplePojoFactory implements PojoFactory {
	
	
	private PojoContext context;
	private PojoBuilderFactory builderFactory;
	

	public SimplePojoFactory() {
		this(new PojoContext(), new BasicPojoBuilderFactory());
	}

	public SimplePojoFactory(PojoContext context) {
		this(context, new BasicPojoBuilderFactory());
	}

	public SimplePojoFactory(PojoContext context, PojoBuilderFactory builderFactory) {
		this.context = context;
		this.builderFactory = builderFactory;
	}

	@Override
	public <T> T create(Vertex v, Class<T> type) throws KonigException {
		
		
		PojoExchange exchange = new PojoExchange();
		exchange.setContext(context);
		exchange.setVertex(v);
		exchange.setJavaClass(type);
		
		return createPojo(exchange);
	}

	@SuppressWarnings("unchecked")
	private <T> T createPojo(PojoExchange exchange) {
		PojoBuilderFactory factory = getBuilderFactory();
		PojoBuilder builder = factory.createBuilder(exchange);
		exchange.setBuilder(builder);
		exchange.setErrorMessage(null);
		builder.beginPojo(exchange);
		if (exchange.getErrorMessage() != null) {
			return null;
		}
		
		Object pojo = exchange.getPojo();

		setProperties(exchange, null);
		
		builder.endPojo(exchange);
		
		return (T) pojo;
	}

	private void setProperties(PojoExchange pe, ValueExchange ve) {
		PojoBuilder pb = pe.getPojoBuilder();
		Object pojo = pe.getPojo();
//		System.out.println("PojoFactoryImpl.setProperties: " + pojo.getClass().getSimpleName());
		if (ve == null) {
			ve = new ValueExchange();
			ve.setContext(pe.getContext());
			ve.setJavaSubject(pojo);
			ve.setSubject(pe.getVertex());
			ve.setPojoExchange(pe);
		} 
		
		Vertex v = pe.getVertex();
		Set<Entry<URI,Set<Edge>>> out = v.outEdges();
		ve.setPredicate(Konig.id);
		ValueBuilder vb = pb.getValueBuilder(ve);
		if (vb != null) {
			ve.setValueBuilder(vb);
			ve.setObject(v.getId());
			vb.beginValue(ve);
			vb.endValue(ve);
		}
//		System.out.println("PojoFactoryImpl: After set Id");
		
		for (Entry<URI,Set<Edge>> e : out) {
			URI predicate = e.getKey();
			Set<Edge> edgeSet = e.getValue();
			
			
				
			ve.setPredicate(predicate);
			vb = pb.getValueBuilder(ve);
			if (vb != null) {
				ve.setValueBuilder(vb);
				CollectionBuilder cb = vb instanceof CollectionBuilder ? (CollectionBuilder) vb : null;
				
				if (cb != null) {
					cb.beginCollection(ve);
					ValueBuilder vb2 = ve.getValueBuilder();
					if (vb2 instanceof CollectionBuilder) {
						cb = (CollectionBuilder) vb2;
					}
				}
				for (Edge edge : edgeSet) {
//					System.out.println("PojoFactoryImpl: edge=" + edge);
					Value object = edge.getObject();
					ve.setValueBuilder(vb);
					ve.setPredicate(predicate);
					invokeValueBuilder(ve, object);
				}
				if (cb != null) {
					cb.endCollection(ve);
				}
			}
		}
		
		ve.pop();
		
	}

	private void buildList(ValueExchange ve, List<Value> valueList) {
		
		for (Value value : valueList) {
			
			invokeValueBuilder(ve, value);
			
		}
		
		
		
		
	}

	private void invokeValueBuilder(ValueExchange ve, Value object) {
		ValueExchange child = ve.getChild();
		
//		System.out.println(ve.getPath() + 
//				(child==null ? "" : ": child[" + child.getPredicate().getLocalName() + "]"));
		
		if (child != null) {
			ve = child;
		}

		ve.setObject(object);
		ve.setStructured(false);
		ve.setJavaObjectType(null);
		
		List<Value> valueList = null;
		Vertex vertex = null;
		if (object instanceof Resource) {
			
			// Get the Vertex that represents the Resource
			Graph graph = ve.getPojoExchange().getVertex().getGraph();
			vertex = graph.getVertex((Resource)object);
			//
			// We'll use this vertex later if we need to map it to
			// a POJO.
			
			// If this Vertex is an RDF List, then we need to update
			// the ValueExhange with a Java List of values.
			
			valueList = vertex.asList();
			
			
		}
		
		ValueBuilder vb = ve.getValueBuilder();
		if (vb == null) {
			throw new KonigException("ValueBuilder is null");
		}
		if (valueList == null) {
			vb.beginValue(ve);
		}
		
	
		if (ve.isStructured()) {
			
			Object pojo = vertex==null ? null : context.getIndividual(vertex.getId());
			
			
			if (pojo != null) {
				ve.setJavaObject(pojo);
			} else {
			
				PojoExchange pe = new PojoExchange();
				if (object instanceof Resource) {
					pe.setVertex(vertex);
					pe.setValue(null);
				} else {
					pe.setVertex(null);
					pe.setValue((Literal) object);
				}
				pe.setBuilder(ve.getPojoBuilder());
				pe.setContext(context);
				pe.setJavaClass(ve.getJavaObjectType());
				
				
				
				PojoBuilder pojoBuilder = builderFactory.createBuilder(pe);
				pe.setBuilder(pojoBuilder);
				
				pojoBuilder.beginPojo(pe);
				pojo = pe.getPojo();
				if (pojo == null) {
					return;
				}
				ve.setJavaObject(pojo);
				
				if (vertex != null) {
					ValueExchange ve2 = ve.push();
					ve2.setPojoExchange(pe);
					ve2.setValueBuilder(pe.getValueBuilder());
					setProperties(pe, ve2);
				}
				
				pojoBuilder.endPojo(pe);
			}
		}
		if (valueList != null) {
			buildList(ve, valueList);
		} else {
			vb.endValue(ve);
		}
		
	}

	private PojoBuilderFactory getBuilderFactory() {
		return builderFactory;
	}

	@Override
	public void createAll(Graph graph) throws KonigException {
		
		for (Vertex v : graph.vertices()) {
			Resource id = v.getId();
			if (id instanceof URI && context.getIndividual(v.getId()) == null) {
				create(v, null);
			}
		}

	}

}
