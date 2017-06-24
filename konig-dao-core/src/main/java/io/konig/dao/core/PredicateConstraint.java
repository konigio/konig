package io.konig.dao.core;

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


public class PredicateConstraint implements ShapeFilter {

	private String propertyName;
	private ConstraintOperator operator;
	private Object value;
	
	
	
	public PredicateConstraint(String propertyName, ConstraintOperator operator, Object value) {
		this.propertyName = propertyName;
		this.operator = operator;
		this.value = value;
	}
	public String getPropertyName() {
		return propertyName;
	}
	public ConstraintOperator getOperator() {
		return operator;
	}
	public Object getValue() {
		return value;
	}
	
	public static class Builder {
		
		private ShapeQuery.Builder queryBuilder;
		
		private String propertyName;
		private ConstraintOperator operator;
		private Object value;
		
		public Builder(ShapeQuery.Builder queryBuilder) {
			this.queryBuilder = queryBuilder;
		}
		
		public Builder setPropertyName(String propertyName) {
			this.propertyName = propertyName;
			return this;
		}
		public Builder setOperator(ConstraintOperator operator) {
			this.operator = operator;
			return this;
		}
		public Builder setValue(Object value) {
			this.value = value;
			return this;
		}
		
		public ShapeQuery.Builder endPredicateConstraint() {
			queryBuilder.addFilter(new PredicateConstraint(propertyName, operator, value));
			return queryBuilder;
		}
		
		
	}

}
