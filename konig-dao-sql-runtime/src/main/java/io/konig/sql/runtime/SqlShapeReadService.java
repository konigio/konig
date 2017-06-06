package io.konig.sql.runtime;

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
	
	private TableNameService tableNameService;


	public SqlShapeReadService(TableNameService tableNameService) {
		this.tableNameService = tableNameService;
	}

	@Override
	public void execute(ShapeQuery query, Writer output, Format format) throws DaoException {
		
		String sql = toSql(query);
		executeSql(sql, output, format);
	}

	abstract protected void executeSql(String sql, Writer output, Format format) throws DaoException;

	private String toSql(ShapeQuery query) {
		
		String tableName = tableNameService.tableName(query.getShapeId());
		
		StringBuilder builder = new StringBuilder();
		builder.append("SELECT * FROM ");
		builder.append(tableName);
		
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
