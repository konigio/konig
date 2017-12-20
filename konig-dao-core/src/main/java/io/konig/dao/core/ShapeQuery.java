package io.konig.dao.core;

import java.util.Map;

/*
 * #%L
 * Konig DAO Core
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



public class ShapeQuery {

	private String shapeId;
	private DataFilter filter;
	private String view;
	private String aggregate;
	private Long limit;
	private String xSort;
	private String ySort;
	private Long offset;
	private String cursor;
	private Map<String,String> parameters;
	
	public ShapeQuery() {
		
	}

	public String getShapeId() {
		return shapeId;
	}

	public DataFilter getFilter() {
		return filter;
	}
	
	
	public String getView() {
		return view;
	}
	
	public String getAggregate() {
		return aggregate;
	}
	
	public Long getLimit() {
		return limit;
	}
	
	public Long getOffset() {
		return offset;
	}
	
	public String getXSort() {
		return xSort;
	}
	
	public String getYSort() {
		return ySort;
	}
	
	public String getCursor() {
		return cursor;
	}
	
	public Map<String, String> getParameters() {
		return parameters;
	}

	private void setParameters(Map<String, String> parameters) {
		this.parameters = parameters;
	}

	private void setShapeId(String shapeId) {
		this.shapeId = shapeId;
	}

	private void setFilter(DataFilter filter) {
		this.filter = filter;
	}

	private void setView(String view) {
		this.view = view;
	}
	
	private void setAggregate(String aggregate) {
		this.aggregate = aggregate;
	}

	public void setLimit(Long limit) {
		this.limit = limit;
	}
	
	public void setOffset(Long offset) {
		this.offset = offset;
	}

	private void setXSort(String xSort) {
		this.xSort = xSort;
	}
	
	private void setYSort(String ySort) {
		this.ySort = ySort;
	}
	
	private void setCursor(String cursor) {
		this.cursor = cursor;
	}
	
	public static Builder newBuilder() {
		return new Builder();
	}
	
	public static class Builder {
		ShapeQuery query = new ShapeQuery();
		
		public Builder setShapeId(String shapeId) {
			query.setShapeId(shapeId);
			return this;
		}
		
		public Builder setView(String view) {
			query.setView(view);
			return this;
		}
		
		public Builder setAggregate(String aggregate) {
			query.setAggregate(aggregate);
			return this;
		}
		
		public Builder setXSort(String xSort) {
			query.setXSort(xSort);
			return this;
		}
		
		public Builder setYSort(String ySort) {
			query.setYSort(ySort);
			return this;
		}
		
		public Builder setLimit(Long limit) {
			query.setLimit(limit);
			return this;
		}
		
		public Builder setOffset(Long offset) {
			query.setOffset(offset);
			return this;
		}
		
		public Builder setCursor(String cursor) {
			query.setCursor(cursor);
			return this;
		}
		
		public Builder setParameters(Map<String,String> parameters) {
			query.setParameters(parameters);
			return this;
		}
		public ShapeQuery build() {
			return query;
		}
		
		public PredicateConstraint.Builder beginPredicateConstraint() {
			return new PredicateConstraint.Builder(this);
		}
		
		public void addFilter(DataFilter filter) {
			DataFilter shapeFilter = query.getFilter();
			if (shapeFilter == null) {
				shapeFilter = filter;
			} else if (shapeFilter instanceof CompositeDataFilter) {
				CompositeDataFilter composite = (CompositeDataFilter) shapeFilter;
				composite.add(filter);
			} else {
				CompositeDataFilter composite = new CompositeDataFilter(CompositeOperator.AND);
				composite.add(shapeFilter);
				composite.add(filter);
				shapeFilter = composite;
			}
			query.setFilter(shapeFilter);
		}
	}
}
