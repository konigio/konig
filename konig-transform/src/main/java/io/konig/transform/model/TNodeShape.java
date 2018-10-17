package io.konig.transform.model;

/*
 * #%L
 * Konig Transform
 * %%
 * Copyright (C) 2015 - 2018 Gregory McFall
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


import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openrdf.model.URI;

import io.konig.core.impl.RdfUtil;
import io.konig.shacl.Shape;

/**
 * A SHACL NodeShape decorated with information useful for generating transforms.
 * @author Greg McFall
 *
 */
public class TNodeShape {
	private Shape shape;
	private TClass tclass;
	private TDataSource tdatasource;
	private TPropertyShape accessor;
	
	private Map<URI,TPropertyShape> properties = new HashMap<>();
	private Set<TPropertyShape> propertySet = new LinkedHashSet<>();
	
	
	public TNodeShape(Shape shape) {
		this.shape = shape;
	}

	public TNodeShape(Shape shape, TDataSource tdatasource) {
		this.shape = shape;
		this.tdatasource = tdatasource;
	}

	public Shape getShape() {
		return shape;
	}
	
	public TPropertyShape getProperty(URI predicate) {
		return properties.get(predicate);
	}
	
	public TDataSource getTdatasource() {
		return tdatasource;
	}
	
	public void setTdatasource(TDataSource tdatasource) {
		this.tdatasource = tdatasource;
	}
	public TClass getTclass() {
		return tclass;
	}
	public void setTclass(TClass tclass) {
		this.tclass = tclass;
	}

	public void add(TPropertyShape p) {
		properties.put(p.getPredicate(), p);
		propertySet.add(p);
	}
	

	public TPropertyShape getAccessor() {
		return accessor;
	}

	public void setAccessor(TPropertyShape accessor) {
		this.accessor = accessor;
	}
	
	public Set<TPropertyShape> getProperties() {
		return propertySet;
	}
	
	public TNodeShape getRoot() {
		if (accessor != null) {
			return accessor.getOwner().getRoot();
		}
		
		return tclass.getTargetShape().getRoot();
		
	}
	

	public List<TFromItem> getFromClause() {
		return getRoot().getFromClause();
	}

	public int countValues() {
		int count = 0;
		for (TPropertyShape p : propertySet) {
			count += p.countValues();
		}
		return count;
	}

	public void assignValues() throws ShapeTransformException {
		boolean createFromItem = true;
		for (TPropertyShape p : propertySet) {
			TProperty group = p.assignValue();
			if (group != null && createFromItem) {
				createFromItem=false;
				if (!createFromItem()) {
					// Failed to create FromItem so undo the value expression
					// and abort.
					
					group.setValueExpression(null);
					return;
				}
			}
			
			TNodeShape nested = p.getValueShape();
			if (nested != null) {
				nested.assignValues();
			}
		}
		
	}

	private boolean createFromItem() {
		// For now, we assume that there is a single FROM item.
		// TODO: Add logic for joins.
		
		TFromItem item = new TFromItem(this);
		getFromClause().add(item);
		
		return true;
	}
	
	public String getPath() {
		return accessor==null ? toString() : accessor.toString();
	}
	
	public String toString() {
		return "TNodeShape[" + RdfUtil.localName(shape.getId()) + "]";
	}

}
