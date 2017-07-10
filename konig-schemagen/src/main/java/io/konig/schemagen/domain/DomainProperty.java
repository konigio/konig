package io.konig.schemagen.domain;

/*
 * #%L
 * Konig Schema Generator
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

import io.konig.core.Graph;
import io.konig.core.Vertex;
import io.konig.core.impl.RdfUtil;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;

public class DomainProperty {
	
	private Vertex vertex;
	private URI propertyId;
	private URI domain;
	private URI range;
	private List<ShapeProperty> shapePropertyList = new ArrayList<>();
	
	public DomainProperty(Vertex vertex) {
		this.vertex = vertex;
		propertyId = (URI) vertex.getId();
	}
	public Vertex getVertex() {
		return vertex;
	}
	
	public boolean isDatatypeProperty() {
		for (ShapeProperty p : shapePropertyList) {
			PropertyConstraint c = p.getConstraint();
			if (c.getDatatype() != null) {
				return true;
			}
		}
		
		return false;
	}
	
	public URI getPropertyId() {
		return propertyId;
	}
	public URI derivedDomain(Graph ontology) {
		if (domain!=null) {
			return domain;
		}
		URI result = null;
		for (ShapeProperty p : shapePropertyList) {
			URI d = p.getShape().getTargetClass();
			if (d != null) {
				if (result == null) {
					result = d;
				} else {
					Vertex v = ontology.vertex(result);
					if (RdfUtil.isSubClassOf(v, d)) {
						result = d;
					} else {
						v = ontology.vertex(d);
						if (!RdfUtil.isSubClassOf(v, result)) {
							return null;
						}
					}
				}
			}
		}
		
		return result;
	}
	
	public URI derivedRange(Graph ontology) {
		if (range != null) {
			return range;
		}
		URI result = null;
		for (ShapeProperty p : shapePropertyList) {
			URI r = DomainManager.rangeOf(p, ontology);
			if (r != null) {
				if (result == null) {
					result = r;
					
				} else {
					Vertex v = ontology.vertex(result);
					if (RdfUtil.isSubClassOf(v, r)) {
						result = r;
					} else {
						v = ontology.vertex(r);
						if (!RdfUtil.isSubClassOf(v, result)) {
							return null;
						}
					}
				}
			}
		}
		
		
		return result;
	}
	
	
	public URI getDomain() {
		return domain;
	}
	public void setDomain(URI domain) {
		this.domain = domain;
	}
	public URI getRange() {
		return range;
	}
	public void setRange(URI range) {
		this.range = range;
	}
	
	public List<ShapeProperty> getShapeProperyList() {
		return shapePropertyList;
	}
	
	public void add(ShapeProperty s) {
		if (!shapePropertyList.contains(s)) {
			shapePropertyList.add(s);
		}
	}
	
	public void addShapeProperty(Shape shape, PropertyConstraint p) {
		add(new ShapeProperty(shape, p));
	}
	
	

}
