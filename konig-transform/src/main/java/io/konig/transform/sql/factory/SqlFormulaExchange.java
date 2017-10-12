package io.konig.transform.sql.factory;

/*
 * #%L
 * Konig Transform
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


import javax.annotation.Generated;

import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;
import io.konig.sql.query.GroupingElement;
import io.konig.sql.query.TableItemExpression;
import io.konig.transform.rule.ShapeRule;

/**
 * An exchange used to request a SqlFormula from SqlFormulaFactory and capture side-effects.
 * @author Greg McFall
 *
 */
public class SqlFormulaExchange {
	private VariableTableMap tableMap;
	private TableItemExpression sourceTable;
	private Shape shape;
	private ShapeRule shapeRule;
	private SqlFactory.Worker sqlFactoryWorker;
	private PropertyConstraint property;
	private GroupingElement groupingElement;

	@Generated("SparkTools")
	private SqlFormulaExchange(Builder builder) {
		this.tableMap = builder.tableMap;
		this.sourceTable = builder.sourceTable;
		this.shape = builder.shape;
		this.property = builder.property;
		this.shapeRule = builder.shapeRule;
		this.sqlFactoryWorker = builder.sqlFactoryWorker;
	}
	

	public GroupingElement getGroupingElement() {
		return groupingElement;
	}

	public SqlFactory.Worker getSqlFactoryWorker() {
		return sqlFactoryWorker;
	}


	public void setGroupingElement(GroupingElement groupingElement) {
		this.groupingElement = groupingElement;
	}


	public ShapeRule getShapeRule() {
		return shapeRule;
	}


	public VariableTableMap getTableMap() {
		return tableMap;
	}
	public TableItemExpression getSourceTable() {
		return sourceTable;
	}
	public Shape getShape() {
		return shape;
	}
	public PropertyConstraint getProperty() {
		return property;
	}
	/**
	 * Creates builder to build {@link SqlFormulaExchange}.
	 * @return created builder
	 */
	@Generated("SparkTools")
	public static Builder builder() {
		return new Builder();
	}
	/**
	 * Builder to build {@link SqlFormulaExchange}.
	 */
	@Generated("SparkTools")
	public static final class Builder {
		private VariableTableMap tableMap;
		private TableItemExpression sourceTable;
		private Shape shape;
		private PropertyConstraint property;
		private ShapeRule shapeRule;
		private SqlFactory.Worker sqlFactoryWorker;

		private Builder() {
		}

		public Builder withTableMap(VariableTableMap tableMap) {
			this.tableMap = tableMap;
			return this;
		}

		public Builder withSourceTable(TableItemExpression sourceTable) {
			this.sourceTable = sourceTable;
			return this;
		}

		public Builder withShape(Shape shape) {
			this.shape = shape;
			return this;
		}

		public Builder withProperty(PropertyConstraint p) {
			this.property = p;
			return this;
		}
		
		public Builder withShapeRule(ShapeRule shapeRule) {
			this.shapeRule = shapeRule;
			return this;
		}
		
		public Builder withSqlFactoryWorker(SqlFactory.Worker worker) {
			sqlFactoryWorker = worker;
			return this;
		}

		public SqlFormulaExchange build() {
			return new SqlFormulaExchange(this);
		}
	}
	
	
}
