package io.konig.sql;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;

import io.konig.sql.antlr.SqlCreateTableBaseListener;
import io.konig.sql.antlr.SqlCreateTableLexer;
import io.konig.sql.antlr.SqlCreateTableParser;

public class SQLParser {

	private SQLSchemaManager schemaManager;
	private String defaultSchemaName = "global";
	
	public SQLParser() {
		
	}
	
	public SQLParser(SQLSchemaManager schemaManager) {
		this.schemaManager = schemaManager;
	}


	public SQLParser(SQLSchemaManager schemaManager, String defaultSchemaName) {
		this.schemaManager = schemaManager;
		this.defaultSchemaName = defaultSchemaName;
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
		private String constraintName = null;
		private SQLConstraint notNull;
		private SQLConstraint primaryKey;
		
		private List<SQLColumnSchema> columnList;
		
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
			constraintName = null;
			notNull = null;
			primaryKey = null;
		}

		@Override 
		public void exitColumnName(SqlCreateTableParser.ColumnNameContext ctx) {
			columnName = ctx.getText();
		}

		@Override 
		public void exitDatatype(SqlCreateTableParser.DatatypeContext ctx) { 
			String datatypeName = ctx.getText().toUpperCase();
			datatype = SQLDatatype.valueOf(datatypeName);
		}

		@Override 
		public void exitSizeValue(SqlCreateTableParser.SizeValueContext ctx) { 
			String text = ctx.getText();
			sizeValue = new Integer(text);
		}
		
		@Override 
		public void exitColumnPrimaryKey(SqlCreateTableParser.ColumnPrimaryKeyContext ctx) { 
			primaryKey = new SQLConstraint(constraintName);
		}

		@Override 
		public void exitColumnDef(SqlCreateTableParser.ColumnDefContext ctx) { 
			SQLColumnType columnType = new SQLColumnType(datatype, sizeValue);
			SQLColumnSchema column = new SQLColumnSchema(targetTable, columnName, columnType);
			column.setPrimaryKey(primaryKey);
			column.setNotNull(notNull);
		}
		
		@Override
		public void exitConstraintName(SqlCreateTableParser.ConstraintNameContext ctx) { 
			constraintName = ctx.getText();
		}

		@Override 
		public void exitNotNull(SqlCreateTableParser.NotNullContext ctx) { 
			notNull = new SQLConstraint(constraintName);
		}
	}
}
