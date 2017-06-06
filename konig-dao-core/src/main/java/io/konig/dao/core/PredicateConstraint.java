package io.konig.dao.core;

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
