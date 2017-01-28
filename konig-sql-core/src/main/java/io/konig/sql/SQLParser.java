package io.konig.sql;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;

import io.konig.core.NamespaceManager;
import io.konig.shacl.ShapeManager;

public class SQLParser {

	private SQLSchemaManager schemaManager;
	private String defaultSchemaName = "global";
	private NamespaceManager namespaceManager;
	private ShapeManager shapeManager;
	

	
	public SQLParser() {
		
	}
	
	
	public SQLParser(NamespaceManager namespaceManager, SQLSchemaManager schemaManager) {
		this.schemaManager = schemaManager;
		this.namespaceManager = namespaceManager;
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
		} catch (IOException | RDFParseException | RDFHandlerException e) {
			throw new SQLSchemaException(e);
		}
	}
	
	public void parseAll(Reader input) throws IOException, RDFParseException, RDFHandlerException {
		init();
		
		SemanticSqlParser parser = new SemanticSqlParser(schemaManager, shapeManager);
		parser.parse(input);
		
	}
	

	private void init() {
		if (schemaManager == null) {
			schemaManager = new SQLSchemaManager();
		}
		
	}


	
}
