package io.konig.sql.runtime;

/*
 * #%L
 * Konig DAO SQL Runtime
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


import java.io.Writer;
import java.net.URI;

import io.konig.dao.core.CompositeShapeFilter;
import io.konig.dao.core.DaoException;
import io.konig.dao.core.Format;
import io.konig.dao.core.PredicateConstraint;
import io.konig.dao.core.ShapeFilter;
import io.konig.dao.core.ShapeQuery;
import io.konig.dao.core.ShapeReadService;

abstract public class SqlShapeReadService implements ShapeReadService {
	
	private EntityStructureService structureService;

	public SqlShapeReadService(EntityStructureService structureService) {
		this.structureService = structureService;
	}

	@Override
	public void execute(ShapeQuery query, Writer output, Format format) throws DaoException {

		EntityStructure struct = structureService.structureOfShape(query.getShapeId());
		String sql = toSql(struct, query);
		executeSql(struct, sql, output, format);
	}

	abstract protected void executeSql(EntityStructure struct, String sql, Writer output, Format format) throws DaoException;

	private String toSql(EntityStructure struct, ShapeQuery query) throws DaoException {
		
		
		StringBuilder builder = new StringBuilder();
		builder.append("SELECT * FROM ");
		builder.append(struct.getName());
		
		ShapeFilter filter = query.getFilter();
		appendFilter(builder, filter);
		
		return builder.toString();
	}

	private void appendFilter(StringBuilder builder, ShapeFilter filter) {
		if (filter != null) {
			builder.append("\nWHERE");
		
			doAppendFilter(builder, filter);
		}
		
	}

	private void doAppendFilter(StringBuilder builder, ShapeFilter filter) {
		if (filter instanceof CompositeShapeFilter) {
			appendCompositeShapeFilter(builder, (CompositeShapeFilter)filter);
		} else if (filter instanceof PredicateConstraint) {
			builder.append(' ');
			appendPredicateConstraint(builder, (PredicateConstraint)filter);
		}
		
	}

	private void appendPredicateConstraint(StringBuilder builder, PredicateConstraint filter) {
		
		String propertyName = filter.getPropertyName();
		Object value = filter.getValue();
		
		builder.append(propertyName);
		
		switch(filter.getOperator()) {
		case EQUAL:
			builder.append(" = ");
			break;
			
		case GREATER_THAN :
			builder.append(" > ");
			break;
			
		case GREATER_THAN_OR_EQUAL :
			builder.append(" >= ");
			break;
			
		case LESS_THAN :
			builder.append(" < ");
			break;
			
		case LESS_THAN_OR_EQUAL :
			builder.append(" <= ");
			break;
			
		case NOT_EQUAL :
			builder.append(" != ");
			break;
		}
		
		appendValue(builder, value);
		
	}

	private void appendValue(StringBuilder builder, Object value) {
		if (value instanceof String) {
			appendString(builder, (String)value);
		}
		
	}

	private void appendString(StringBuilder builder, String value) {
		
		if (value.indexOf('"') >=0) {
			// TODO: support strings containing quotes
			throw new RuntimeException("Strings containing quotes not supported yet.");
		}
		builder.append('"');
		builder.append(value);
		builder.append('"');
		
	}

	private void appendCompositeShapeFilter(StringBuilder builder, CompositeShapeFilter composite) {
		String operator = null;
		for (ShapeFilter filter : composite) {
			if (operator != null) {
				builder.append(operator);
			} else {
				operator = composite.getOperator().name();
			}
			builder.append("\n   ");
			doAppendFilter(builder, filter);
		}
		
	}

}
