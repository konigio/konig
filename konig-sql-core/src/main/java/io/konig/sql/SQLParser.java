package io.konig.sql;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.openrdf.model.URI;

import io.konig.core.KonigException;
import io.konig.core.NamespaceManager;
import io.konig.core.impl.MemoryNamespaceManager;
import io.konig.core.impl.RdfUtil;
import io.konig.core.util.IriTemplate;
import io.konig.core.util.LinkedValueMap;
import io.konig.core.util.NamespaceValueMap;
import io.konig.core.util.ValueFormat;
import io.konig.core.util.ValueMap;
import io.konig.sql.antlr.SqlCreateTableBaseListener;
import io.konig.sql.antlr.SqlCreateTableLexer;
import io.konig.sql.antlr.SqlCreateTableParser;

public class SQLParser {

	private SQLSchemaManager schemaManager;
	private String defaultSchemaName = "global";
	private NamespaceManager namespaceManager;
	

	
	public SQLParser() {
		
	}
	
	public SQLParser(SQLSchemaManager schemaManager) {
		this.schemaManager = schemaManager;
	}


	public SQLParser(SQLSchemaManager schemaManager, String defaultSchemaName) {
		this.schemaManager = schemaManager;
		this.defaultSchemaName = defaultSchemaName;
	}


	public NamespaceManager getNamespaceManager() {
		return namespaceManager;
	}

	public void setNamespaceManager(NamespaceManager namespaceManager) {
		this.namespaceManager = namespaceManager;
	}

	public SQLSchemaManager getSchemaManager() {
		return schemaManager;
	}


	public void setSchemaManager(SQLSchemaManager schemaManager) {
		this.schemaManager = schemaManager;
	}


	public String getDefaultSchemaName() {
		return defaultSchemaName;
	}


	public void setDefaultSchemaName(String defaultSchemaName) {
		this.defaultSchemaName = defaultSchemaName;
	}
	
	public void parseAll(String text) {
		StringReader reader = new StringReader(text);
		try {
			parseAll(reader);
		} catch (IOException e) {
			throw new SQLSchemaException(e);
		}
	}
	
	public void parseAll(Reader input) throws IOException {
		init();
		
		CharStream stream = new ANTLRInputStream(input);

		SqlCreateTableLexer lexer = new SqlCreateTableLexer(stream);
		
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		SqlCreateTableParser parser = new SqlCreateTableParser(tokens);
		Listener listener = new Listener();
		parser.addParseListener(listener);
		
		parser.sql();
		
	}
	
	public SQLTableSchema parseTable(Reader input) throws IOException {

		init();
		
		CharStream stream = new ANTLRInputStream(input);

		SqlCreateTableLexer lexer = new SqlCreateTableLexer(stream);
		
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		SqlCreateTableParser parser = new SqlCreateTableParser(tokens);
		Listener listener = new Listener();
		parser.addParseListener(listener);
		
		parser.createTable();
		
		return listener.getTargetTable();
	}


	public SQLTableSchema parseTable(String text) {
		
		try {
			return parseTable(new StringReader(text));
		} catch (IOException e) {
			throw new SQLSchemaException(e);
		}
		
	}
	

	private void init() {
		if (schemaManager == null) {
			schemaManager = new SQLSchemaManager();
		}
		
	}


	private class Listener extends SqlCreateTableBaseListener {
		private SQLSchema schema;
		private SQLTableSchema table;
		private SQLTableSchema targetTable;
		
		private String columnName;
		private SQLDatatype datatype;
		private Integer sizeValue;
		private Integer precision;
		private String constraintName = null;
		private SQLConstraint notNull;
		private SQLConstraint primaryKey;
		
		private NamespaceManager nsManager;
		private String nsPrefix;
		private String iriValue;
		private IriTemplate tableShapeIriTemplate;
		private IriTemplate tableTargetClassIriTemplate;
		private IriTemplate columnPredicateIriTemplate;
		private ValueMap tableValueMap;
		private NamespaceValueMap nsValueMap;
		private URI columnPredicate;
		private String columnPath;
		private ValueFormat columnPathTemplate;
		
		private List<SQLColumnSchema> columnList;
		
		private Listener() {
			
			nsManager = namespaceManager;
			
			if (nsManager == null) {
				nsManager = new MemoryNamespaceManager();
			}
			nsValueMap = new NamespaceValueMap(nsManager);
		}
		
		private SQLSchema getSchema() {
			if (schema == null) {
				schema = schemaManager.getSchemaByName(defaultSchemaName);
				if (schema == null) {
					schema = new SQLSchema(defaultSchemaName);
					schemaManager.add(schema);
				}
			}
			return schema;
		}
		
		SQLTableSchema getTargetTable() {
			return targetTable;
		}

		@Override 
		public void exitTableColumnPathTemplate(SqlCreateTableParser.TableColumnPathTemplateContext ctx) { 
			table.setColumnPathTemplate(new ValueFormat(ctx.getText()));
		}

		@Override 
		public void exitColumnPathTemplate(SqlCreateTableParser.ColumnPathTemplateContext ctx) { 
			columnPathTemplate = new ValueFormat(columnPath);
		}

		@Override 
		public void exitIriRef(SqlCreateTableParser.IriRefContext ctx) { 
			iriValue = ctx.getText();
			iriValue = iriValue.substring(1,  iriValue.length()-1); 
		}
		
		
		@Override 
		public void exitCurie(SqlCreateTableParser.CurieContext ctx) { 
			iriValue = ctx.getText();
		}
		
		@Override 
		public void exitTableShapeIriTemplate(SqlCreateTableParser.TableShapeIriTemplateContext ctx) { 
			tableShapeIriTemplate = new IriTemplate(iriValue);
		}

		@Override 
		public void exitColumnPredicateIriTemplate(SqlCreateTableParser.ColumnPredicateIriTemplateContext ctx) { 
			columnPredicateIriTemplate = new IriTemplate(iriValue);
		}

		@Override 
		public void exitTableTargetClassIriTemplate(SqlCreateTableParser.TableTargetClassIriTemplateContext ctx) { 
			tableTargetClassIriTemplate = new IriTemplate(iriValue);
		}

		@Override 
		public void exitCreateTable(SqlCreateTableParser.CreateTableContext ctx) { 
			tableValueMap = null;
			
			if (table.getTableShapeId()==null && tableShapeIriTemplate!=null) {
				ValueMap map = tableValueMap();
				URI shapeId = tableShapeIriTemplate.expand(map);
				table.setTableShapeId(shapeId);
			}
			
			if (table.getTargetClass()==null && tableTargetClassIriTemplate!=null) {
				ValueMap map = tableValueMap();
				URI targetClass = tableTargetClassIriTemplate.expand(map);
				table.setTargetClass(targetClass);
			}
			
			ValueMap map = tableValueMap();
			table.applyTemplates(map);
		}
		
		private ValueMap tableValueMap() {
			if (tableValueMap == null) {
				TableValueMap tableMap = new TableValueMap(table);
				SchemaValueMap schemaMap = new SchemaValueMap(table.getSchema());
				ValueMap rest = nsValueMap==null ? schemaMap : new LinkedValueMap(schemaMap, nsValueMap);
				tableValueMap =  new LinkedValueMap(tableMap, rest);
			}
			return tableValueMap;
			
		}

		@Override 
		public void exitTableShapeId(SqlCreateTableParser.TableShapeIdContext ctx) { 
			table.setTableShapeId(iri());
		}
		

		private URI iri() {
			return RdfUtil.expand(nsManager, iriValue);
		}

		@Override 
		public void exitTableColumnPredicateIriTemplate(SqlCreateTableParser.TableColumnPredicateIriTemplateContext ctx) { 
			IriTemplate template = new IriTemplate(iriValue);
			table.setColumnPredicateIriTemplate(template);
		}

		@Override 
		public void exitTableTargetClass(SqlCreateTableParser.TableTargetClassContext ctx) {
			table.setTargetClass(iri());
		}

		
		

		@Override 
		public void exitPrefixDirective(SqlCreateTableParser.PrefixDirectiveContext ctx) { 
			
			nsManager.add(nsPrefix, iriValue);
		}
		
		@Override
		public void exitNsPrefix(SqlCreateTableParser.NsPrefixContext ctx) { 
			nsPrefix = ctx.getText();
		}


		@Override 
		public void enterColumnList(SqlCreateTableParser.ColumnListContext ctx) { 
			columnList = new ArrayList<>();
		}

		@Override 
		public void exitSimpleColumnName(SqlCreateTableParser.SimpleColumnNameContext ctx) { 
			String columnName = ctx.getText();
			columnList.add(new SQLColumnSchema(columnName));
		}

		@Override 
		public void exitTablePrimaryKey(SqlCreateTableParser.TablePrimaryKeyContext ctx) { 
			SQLPrimaryKeyConstraint primaryKey = new SQLPrimaryKeyConstraint(constraintName);
			primaryKey.setColumnList(columnList);
			columnList = null;
			table.addConstraint(primaryKey);
		
		}
		
		
		@Override 
		public void exitSchemaName(SqlCreateTableParser.SchemaNameContext ctx) { 
			
			String schemaName = ctx.getText();
			
			schema = schemaManager.getSchemaByName(schemaName);
			if (schema == null) {
				schema = new SQLSchema(schemaName);
				schemaManager.add(schema);
			}
			
		}
		
		@Override 
		public void enterTableId(SqlCreateTableParser.TableIdContext ctx) { 
			schema = null;
		}
		
		@Override
		public void exitTableName(SqlCreateTableParser.TableNameContext ctx) { 
			SQLSchema schema = getSchema();
			String tableName = ctx.getText();
			table = getOrCreateTable(schema, tableName);
		}

		private SQLTableSchema getOrCreateTable(SQLSchema schema, String tableName) {
			SQLTableSchema table = schema.getTableByName(tableName);
			if (table == null) {
				table = new SQLTableSchema(schema, tableName);
				table.setNamespaceManager(nsManager);
				table.setColumnPredicateIriTemplate(columnPredicateIriTemplate);
				table.setColumnPathTemplate(columnPathTemplate);
			}
			return table;
		}

		@Override public void enterTableParts(SqlCreateTableParser.TablePartsContext ctx) {
			targetTable = table;
		}

		@Override 
		public void enterColumnDef(SqlCreateTableParser.ColumnDefContext ctx) { 
			columnName = null;
			datatype = null;
			sizeValue = null;
			precision = null;
			constraintName = null;
			notNull = null;
			primaryKey = null;
			columnPredicate = null;
			columnPath = null;
		}

		@Override 
		public void exitColumnName(SqlCreateTableParser.ColumnNameContext ctx) {
			columnName = ctx.getText();
		}

		@Override 
		public void exitDatatype(SqlCreateTableParser.DatatypeContext ctx) { 
			String datatypeName = ctx.getText().toUpperCase();
			try {
				datatype = SQLDatatype.valueOf(datatypeName);
			} catch (IllegalArgumentException e) {
				throw new KonigException("Invalid SQL datatype: " + datatypeName);
			}
		}

		@Override 
		public void exitPrecision(SqlCreateTableParser.PrecisionContext ctx) { 
			String text = ctx.getText();
			precision = new Integer(text);
		}

		@Override 
		public void exitSizeValue(SqlCreateTableParser.SizeValueContext ctx) { 
			String text = ctx.getText();
			if ("max".equals(text)) {
				sizeValue = new Integer(-1);
			} else {
				sizeValue = new Integer(text);
			}
		}
		
		@Override 
		public void exitColumnPrimaryKey(SqlCreateTableParser.ColumnPrimaryKeyContext ctx) { 
			primaryKey = new SQLConstraint(constraintName);
		}


		@Override 
		public void exitColumnPredicate(SqlCreateTableParser.ColumnPredicateContext ctx) { 
			columnPredicate = iri();
		}

		@Override 
		public void exitColumnDef(SqlCreateTableParser.ColumnDefContext ctx) { 
			SQLColumnType columnType = new SQLColumnType(datatype, sizeValue, precision);
			SQLColumnSchema column = new SQLColumnSchema(targetTable, columnName, columnType);
			column.setPrimaryKey(primaryKey);
			column.setNotNull(notNull);
			column.setColumnPredicate(columnPredicate);
			column.setEquivalentPath(columnPath);
			
		}
		
		@Override
		public void exitConstraintName(SqlCreateTableParser.ConstraintNameContext ctx) { 
			constraintName = ctx.getText();
		}

		@Override 
		public void exitNotNull(SqlCreateTableParser.NotNullContext ctx) { 
			notNull = new SQLConstraint(constraintName);
		}

		@Override public void exitPathValue(SqlCreateTableParser.PathValueContext ctx) { 
			columnPath = ctx.getText();
		}
	}
}
