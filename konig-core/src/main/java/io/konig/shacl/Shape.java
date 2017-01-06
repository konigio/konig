package io.konig.shacl;

import java.io.IOException;
import java.io.StringWriter;

/*
 * #%L
 * konig-shacl
 * %%
 * Copyright (C) 2015 Gregory McFall
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.BNodeImpl;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

import io.konig.activity.Activity;
import io.konig.annotation.OwlClass;
import io.konig.annotation.RdfProperty;
import io.konig.core.Context;
import io.konig.core.Graph;
import io.konig.core.UidGenerator;
import io.konig.core.vocab.Konig;
import io.konig.core.vocab.SH;
import io.konig.shacl.impl.EmptyList;

public class Shape {
	private static final List<PropertyConstraint> EMPTY_PROPERTY_LIST = new EmptyList<PropertyConstraint>();
	
	private Resource id;
	private URI targetClass;
	private List<PropertyConstraint> property;
	private Constraint constraint;
	private Context jsonldContext;
	private URI equivalentRelationalShape;
	private NodeKind nodeKind;
	private URI aggregationOf;
	private URI rollUpBy;
	
	private AndConstraint and;
	private OrConstraint or;
	
	private Activity wasGeneratedBy;
	private String bigQueryTableId;
	
	
	public Shape() {
		String bnodeId = UidGenerator.INSTANCE.next();
		id = new BNodeImpl(bnodeId);
	}
	
	public Shape(Resource id) {
		this.id = id;
	}
	
	public URI getType() {
		return SH.Shape;
	}
	
	/**
	 * Save this shape in a given Graph
	 * @param graph The graph into which this shape will be saved.
	 */
	public void save(Graph graph) {
		if (id == null) {
			id = graph.vertex().getId();
		}
		edge(graph, SH.targetClass, targetClass);
		edge(graph, SH.nodeKind, nodeKind==null ? null : nodeKind.getURI());
		edge(graph, Konig.aggregationOf, aggregationOf);
		edge(graph, Konig.rollUpBy, rollUpBy);
		
		if (property!=null) {
			for (PropertyConstraint p : property) {
				
				Resource pId = p.getId();
				if (pId == null) {
					pId = graph.vertex().getId();
					p.setId(pId);
					
				}
				edge(graph, SH.property, p.getId());
				p.save(graph);
			}
		}
		
	}
	
	private void edge(Graph graph, URI predicate, Value value) {
		if (value != null) {
			graph.edge(id, predicate, value);
		}
	}

	public List<PropertyConstraint> getProperty() {
		return property==null ? EMPTY_PROPERTY_LIST : property;
	}
	
	
	public void setProperty(List<PropertyConstraint> list) {
		property = list;
	}
	
	
	public AndConstraint getAnd() {
		return and;
	}

	public void setAnd(AndConstraint and) {
		this.and = and;
	}
	
	

	public OrConstraint getOr() {
		return or;
	}

	public void setOr(OrConstraint or) {
		this.or = or;
	}

	public Constraint getConstraint() {
		return constraint;
	}
	
	public Shape setConstraint(Constraint constraint) {
		this.constraint = constraint;
		return this;
	}
	
	public PropertyConstraint getTimeParam() {
		if (property != null) {
			for (PropertyConstraint c : property) {
				if (c.isTimeParam()) {
					return c;
				}
			}
		}
		return null;
	}
	
	public PropertyConstraint getDimensionConstraint(URI dimension) {
		if (property != null) {
			for (PropertyConstraint c : property) {
				URI d = c.getDimensionTerm();
				if (dimension.equals(d)) {
					return c;
				}
			}
		}
		return null;
	}
	
	public PropertyConstraint getPropertyConstraint(URI predicate) {
		
		if (property!=null) {
			for (PropertyConstraint c : property) {
				if (predicate.equals(c.getPredicate())) {
					return c;
				}
			}
		}
		
		return null;
	}
	
	public boolean hasPropertyConstraint(URI predicate) {
		
		if (getPropertyConstraint(predicate) != null) {
			return true;
		}
		
		if (constraint != null) {
			return constraint.hasPropertyConstraint(predicate);
		}
		
		return false;
	}
	
	/**
	 * Get the PropertyConstraint with the specified predicate.
	 * @param predicate The predicate of the requested PropertyConstraint
	 * @return The PropertyConstraint with the specified predicate, or null if this Shape does not contain such a property constraint.
	 */
	public PropertyConstraint property(URI predicate) {
		if (property != null) {
			for (PropertyConstraint p : property) {
				if (p.getPredicate().equals(predicate)) {
					return p;
				}
			}
		}
		return null;
	}
	
	@RdfProperty(SH.PROPERTY)
	public Shape add(PropertyConstraint c) {
		if (property == null) {
			property  = new ArrayList<PropertyConstraint>();
		}
		URI id = c.getPredicate();
		for (PropertyConstraint p : property) {
			URI predicate = p.getPredicate();
			if (predicate!=null && predicate.equals(id)) {
				return this;
			}
		}
		property.add(c);
		return this;
	}

	public URI getTargetClass() {
		return targetClass;
	}

	public void setTargetClass(URI targetClass) {
		this.targetClass = targetClass;
	}

	public Resource getId() {
		return id;
	}
	
	public void setId(Resource id) {
		this.id = id;
	}

	public String toString() {
		
		StringWriter out = new StringWriter();
		JsonFactory factory = new JsonFactory();
		
		try {
			JsonGenerator json = factory.createGenerator(out);
			json.useDefaultPrettyPrinter();
			Set<Shape> memory = new HashSet<>();
			toJson(memory, json);
			json.flush();
			
		} catch (Throwable e) {
			return "ERROR: " + e.getMessage();
		}
		
		return out.toString();
	}
	
	public void toJson(Set<Shape> memory, JsonGenerator json) throws IOException {
		json.writeStartObject();
		json.writeStringField("id", id.toString());
		if (targetClass != null) {
			json.writeStringField("targetClass", targetClass.stringValue());
		}
		
		if (nodeKind!=null) {
			json.writeStringField("nodeKind", nodeKind.getURI().getLocalName());
		}
		
		if (!memory.contains(this)) {
			memory.add(this);
			if (property!=null && !property.isEmpty()) {
				json.writeFieldName("property");
				json.writeStartArray();
				for (PropertyConstraint p : property) {
					p.toJson(memory, json);
				}
				json.writeEndArray();
			}
			
		}
		json.writeEndObject();
		
		
	}
	
	

	/**
	 * Get the default JSON-LD context for this shape.
	 * @return The default JSON-LD context for this shape.
	 */
	public Context getJsonldContext() {
		return jsonldContext;
	}

	/**
	 * Set the default JSON-LD context for this shape
	 * @param jsonldContext The default JSON-LD context for this shape
	 */
	public void setJsonldContext(Context jsonldContext) {
		this.jsonldContext = jsonldContext;
	}

	public URI getEquivalentRelationalShape() {
		return equivalentRelationalShape;
	}

	public void setEquivalentRelationalShape(URI equivalentRelationalShape) {
		this.equivalentRelationalShape = equivalentRelationalShape;
	}

	public NodeKind getNodeKind() {
		return nodeKind;
	}

	public void setNodeKind(NodeKind nodeKind) {
		this.nodeKind = nodeKind;
	}

	public URI getAggregationOf() {
		return aggregationOf;
	}

	public void setAggregationOf(URI aggregationOf) {
		this.aggregationOf = aggregationOf;
	}

	public URI getRollUpBy() {
		return rollUpBy;
	}

	public void setRollUpBy(URI rollUpBy) {
		this.rollUpBy = rollUpBy;
	}

	public Activity getWasGeneratedBy() {
		return wasGeneratedBy;
	}

	public void setWasGeneratedBy(Activity wasGeneratedBy) {
		this.wasGeneratedBy = wasGeneratedBy;
	}

	public String getBigQueryTableId() {
		return bigQueryTableId;
	}

	public void setBigQueryTableId(String bigQueryTableId) {
		this.bigQueryTableId = bigQueryTableId;
	}
	
}
