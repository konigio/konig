package io.konig.dao.core;


public class ShapeQuery {

	private String shapeId;
	private ShapeFilter filter;
	
	public ShapeQuery(String shapeId, ShapeFilter filter) {
		this.shapeId = shapeId;
		this.filter = filter;
	}

	public String getShapeId() {
		return shapeId;
	}

	public ShapeFilter getFilter() {
		return filter;
	}
	
	public static Builder newBuilder() {
		return new Builder();
	}
	
	public static class Builder {
		private String shapeId;
		private ShapeFilter shapeFilter;
		
		public Builder setShapeId(String shapeId) {
			this.shapeId = shapeId;
			return this;
		}
		public ShapeFilter getShapeFilter() {
			return shapeFilter;
		}
		
		public ShapeQuery build() {
			return new ShapeQuery(shapeId, shapeFilter);
		}
		
		public PredicateConstraint.Builder beginPredicateConstraint() {
			return new PredicateConstraint.Builder(this);
		}
		
		public void addFilter(ShapeFilter filter) {
			if (shapeFilter == null) {
				shapeFilter = filter;
			} else if (shapeFilter instanceof CompositeShapeFilter) {
				CompositeShapeFilter composite = (CompositeShapeFilter) shapeFilter;
				composite.add(filter);
			} else {
				CompositeShapeFilter composite = new CompositeShapeFilter(CompositeOperator.AND);
				composite.add(shapeFilter);
				composite.add(filter);
				shapeFilter = composite;
			}
		}
	}
}
