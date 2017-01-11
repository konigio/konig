package io.konig.sql;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openrdf.model.URI;

import io.konig.core.NamespaceManager;
import io.konig.core.util.IriTemplate;
import io.konig.core.util.LinkedValueMap;
import io.konig.core.util.PathPattern;
import io.konig.core.util.SimpleValueFormat;
import io.konig.core.util.ValueMap;

public class SQLTableSchema {
	
	private String tableName;
	private SQLSchema schema;
	
	private List<SQLConstraint> constraints = new ArrayList<>();
	private Map<String,SQLColumnSchema> columnMap = new HashMap<>();
	
	// Semantic Properties
	private NamespaceManager nsManager;
	private URI targetClass;
	private URI tableShapeId;
	private URI tableTargetShapeId;
	private IriTemplate columnPredicateIriTemplate;
	private SimpleValueFormat columnPathTemplate;
	private List<PathPattern> pathPatternList = new ArrayList<>();
	
	

	public SQLTableSchema() {
	}


	public SQLTableSchema(String tableName) {
		this.tableName = tableName;
	}
	public SQLTableSchema(SQLSchema schema, String tableName) {
		this.schema = schema;
		this.tableName = tableName;
		schema.add(this);
	}


	public String getTableName() {
		return tableName;
	}


	public void setTableName(String tableName) {
		this.tableName = tableName;
	}


	public SQLSchema getSchema() {
		return schema;
	}


	public void setSchema(SQLSchema schema) {
		this.schema = schema;
	}


	public void addColumn(SQLColumnSchema columnSchema) {
		if (columnSchema.getColumnName() == null) {
			throw new SQLSchemaException("Column name must be defined");
		}
		columnMap.put(columnSchema.getColumnName(), columnSchema);
	}
	
	public SQLColumnSchema getColumnByName(String columnName) {
		return columnMap.get(columnName);
	}
	
	public Collection<SQLColumnSchema> listColumns() {
		return columnMap.values();
	}
	
	public String getFullName() {
		StringBuilder builder = new StringBuilder();
		String schemaName = schema==null ? "global" : schema.getSchemaName();
		String localName = tableName;
		builder.append(schemaName);
		builder.append('.');
		builder.append(localName);
		
		return builder.toString();
	}


	public List<SQLConstraint> getConstraints() {
		return constraints;
	}


	public void setConstraints(List<SQLConstraint> constraints) {
		this.constraints = constraints;
	}
	
	public void addConstraint(SQLConstraint constraint) {
		constraints.add(constraint);
	}

	public SQLPrimaryKeyConstraint getPrimaryKeyConstraint() {
		for (SQLConstraint c : constraints) {
			if (c instanceof SQLPrimaryKeyConstraint) {
				return (SQLPrimaryKeyConstraint) c;
			}
		}
		for (SQLColumnSchema col : listColumns()) {
			if (col.getPrimaryKey() != null) {
				SQLPrimaryKeyConstraint constraint = new SQLPrimaryKeyConstraint();
				constraint.addColumn(col);
				return constraint;
			}
		}
		return null;
	}


	public NamespaceManager getNamespaceManager() {
		return nsManager;
	}


	public void setNamespaceManager(NamespaceManager nsManager) {
		this.nsManager = nsManager;
	}


	public URI getTableShapeId() {
		return tableShapeId;
	}


	public void setTableShapeId(URI tableShapeId) {
		this.tableShapeId = tableShapeId;
	}


	public URI getTargetClass() {
		return targetClass;
	}


	public void setTargetClass(URI targetClass) {
		this.targetClass = targetClass;
	}


	public IriTemplate getColumnPredicateIriTemplate() {
		return columnPredicateIriTemplate;
	}


	public void setColumnPredicateIriTemplate(IriTemplate columnPredicateIriTemplate) {
		this.columnPredicateIriTemplate = columnPredicateIriTemplate;
	}


	public SimpleValueFormat getColumnPathTemplate() {
		return columnPathTemplate;
	}
	
	public void setColumnPathTemplate(SimpleValueFormat columnPathTemplate) {
		this.columnPathTemplate = columnPathTemplate;
	}
	
	public void applyTemplates(ValueMap map) {
		IriTemplate predicateTemplate = getColumnPredicateIriTemplate();
		SimpleValueFormat pathTemplate = getColumnPathTemplate();
		
		if (predicateTemplate != null || pathTemplate!=null || !pathPatternList.isEmpty()) {
			for (SQLColumnSchema column : listColumns()) {
				
				if (column.getColumnPredicate()==null) {
					if (predicateTemplate != null) {
						LinkedValueMap tableMap = new LinkedValueMap(new ColumnValueMap(column), map);
						column.setColumnPredicate(predicateTemplate.expand(tableMap));
					}
				}
				
				if (column.getEquivalentPath()==null ) {

					PathPattern pattern = findPathPattern(column);
					if (pattern != null && nsManager!=null) {
						String path = pattern.transform(column.getColumnName(), nsManager);
						column.setEquivalentPath(path);
					} else	if (pathTemplate!=null) {

						LinkedValueMap tableMap = new LinkedValueMap(new ColumnValueMap(column), map);
						column.setEquivalentPath(pathTemplate.format(tableMap));
					}
				}
			}
		}
	}
	
	public PathPattern findPathPattern(SQLColumnSchema column) {
		for (PathPattern pattern : pathPatternList) {
			if (pattern.matches(column.getColumnName())) {
				return pattern;
			}
		}
		return null;
	}


	public void add(PathPattern p) {
		pathPatternList.add(p);
	}
	
	public List<PathPattern> getPathPatternList() {
		return pathPatternList;
	}

	public URI getTableTargetShapeId() {
		return tableTargetShapeId;
	}

	public void setTableTargetShapeId(URI tableTargetShapeId) {
		this.tableTargetShapeId = tableTargetShapeId;
	}
	
	
	
}
