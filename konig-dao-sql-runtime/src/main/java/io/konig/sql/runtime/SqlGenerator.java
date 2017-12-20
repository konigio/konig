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

import java.util.List;

import io.konig.dao.core.CompositeDataFilter;
import io.konig.dao.core.DaoException;
import io.konig.dao.core.DataFilter;
import io.konig.dao.core.FieldPath;
import io.konig.dao.core.PredicateConstraint;
import io.konig.dao.core.ShapeQuery;

abstract public class SqlGenerator  {
	


	protected String toSql(EntityStructure struct, ShapeQuery query) throws DaoException {
		
		
		StringBuilder builder = new StringBuilder();
		builder.append("SELECT * FROM ");
		builder.append(struct.getName());
		
		DataFilter filter = query.getFilter();
		appendFilter(struct,builder, filter);
		
		return builder.toString();
	}

	protected void appendFilter(EntityStructure struct,StringBuilder builder, DataFilter filter) {
		if (filter != null) {
			builder.append("\nWHERE");
		
			doAppendFilter(struct,builder, filter);
		}
		
	}

	private void doAppendFilter(EntityStructure struct,StringBuilder builder, DataFilter filter) {
		if (filter instanceof CompositeDataFilter) {
			appendCompositeShapeFilter(struct, builder, (CompositeDataFilter)filter);
		} else if (filter instanceof PredicateConstraint) {
			builder.append(' ');
			appendPredicateConstraint(struct, builder, (PredicateConstraint)filter);
		}
		
	}
		
	private String getFieldType(String propertyName, List<FieldInfo> fieldsInfo) {
		String propertyType = null;
		for(FieldInfo fieldInfo : fieldsInfo) {	
			if(fieldInfo.getStruct() != null) {				
				propertyType = getFieldType(propertyName, fieldInfo.getStruct().getFields());				
				if(propertyType != null){
					return propertyType;
				}
			}
			if(propertyName.equals(fieldInfo.getName()) || 
					propertyName.endsWith("."+fieldInfo.getName())) {
				propertyType = fieldInfo.getFieldType().toString();				
				return propertyType;
			}
		}
		return propertyType;
	}
	
	private void appendPredicateConstraint(EntityStructure struct,StringBuilder builder, PredicateConstraint filter) {
		
		String propertyName = filter.getPropertyName();
		Object value = filter.getValue();
		builder.append(propertyName);
		String propertyType = getFieldType(propertyName,struct.getFields());
		
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
		
		appendValue(builder, value, propertyType);
		
	}

	private void appendValue(StringBuilder builder, Object value, String propertyType) {			
		switch (propertyType) {
		case "http://www.w3.org/2001/XMLSchema#string":						
			appendString(builder, (String)value);
			break;
		case "http://www.w3.org/2001/XMLSchema#dateTime":
			appendString(builder, (String)value);
			break;
		case "http://www.w3.org/2001/XMLSchema#date":
			appendString(builder, (String)value);
			break;
		case "http://www.w3.org/2001/XMLSchema#int":
			builder.append(Integer.valueOf(value==null?"":value.toString()));
			break;
		case "http://www.w3.org/2001/XMLSchema#long":
			builder.append(Long.valueOf(value==null?"":value.toString()));
			break;
		case "http://www.w3.org/2001/XMLSchema#float":
			builder.append(Float.valueOf(value==null?"":value.toString()));
			break;
		case "http://www.w3.org/2001/XMLSchema#double":
			builder.append(Double.valueOf(value==null?"":value.toString()));
			break;
		case "http://www.w3.org/2001/XMLSchema#boolean":
			builder.append(Boolean.valueOf(value==null?"":value.toString()));
			break;
		default:
			appendString(builder, (String)value);
			break;
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

	private void appendCompositeShapeFilter(EntityStructure struct,StringBuilder builder, CompositeDataFilter composite) {
		String operator = null;
		for (DataFilter filter : composite) {
			if (operator != null) {
				builder.append(" ");
				builder.append(operator);
			} else {
				operator = composite.getOperator().name();
			}
			builder.append("\n   ");
			doAppendFilter(struct,builder, filter);
		}
		
	}
	
	protected void appendColumns(EntityStructure struct, StringBuilder builder, FieldPath dimension) {
		String dimensionName = dimension.stringValue();
		String field = getField(struct,dimensionName);
		if(field != null) {
			builder.append("STRUCT(");
			builder.append(field);
			builder.append(')');
		} else {
			builder.append(dimensionName);
		}
	}
	
	protected void appendGroupBy(EntityStructure struct, StringBuilder builder, FieldPath dimension) {
		builder.append(" \n ");
		builder.append("GROUP BY ");
		String dimensionName = dimension.stringValue();
		String field = getField(struct,dimensionName);
		if(field != null) {
			builder.append(field);
		} else {
			builder.append(dimensionName);
		}
	}
	
	private String getField(EntityStructure struct, String dimensionName) {
		StringBuilder builder = new StringBuilder();
		for(FieldInfo fieldInfo : struct.getFields()){
			if (dimensionName.equals(fieldInfo.getName())) {
				appendStructField(fieldInfo, builder,dimensionName);
				return builder.toString();
			}
		}
		return null;
	}
	
	private void appendStructField(FieldInfo fieldInfo, StringBuilder builder, String dimensionName) {
		String comma = "";
		if (fieldInfo.getStruct() != null) {
			for(FieldInfo innerFieldInfo : fieldInfo.getStruct().getFields()){
				builder.append(comma);
				builder.append(dimensionName);
				builder.append(".");
				builder.append(innerFieldInfo.getName());
				comma = ",";
			}
		}else {
			builder.append(fieldInfo.getName());
		}
		
	}

}
