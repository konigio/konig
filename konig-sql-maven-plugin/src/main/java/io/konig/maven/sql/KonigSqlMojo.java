package io.konig.maven.sql;

import java.io.File;
import java.io.IOException;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;
import org.openrdf.model.URI;
import org.openrdf.rio.RDFHandlerException;

import io.konig.core.NamespaceManager;
import io.konig.core.impl.MemoryNamespaceManager;
import io.konig.shacl.Shape;
import io.konig.shacl.io.ShapeWriter;
import io.konig.sql.SQLFileLoader;
import io.konig.sql.SQLNamer;
import io.konig.sql.SQLSchema;
import io.konig.sql.SQLSchemaManager;
import io.konig.sql.SQLTableNamerImpl;
import io.konig.sql.SQLTableSchema;
import io.konig.sql.SQLTableShapeGenerator;

public class KonigSqlMojo extends AbstractMojo {
	
	@Parameter
	private String baseIRI;

	@Parameter
	private File sourceDir;
	
	@Parameter
	private File outDir;
	
	@Parameter
	private String aliasNamespace;
	
	private ShapeWriter shapeWriter;
	private NamespaceManager nsManager;
	private SQLNamer sqlNamer;
	private SQLTableShapeGenerator generator;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		
		
		SQLFileLoader loader = new SQLFileLoader();
		try {
			loader.load(sourceDir);
			SQLSchemaManager schemaManager = loader.getSchemaManager();
			
			sqlNamer = new SQLTableNamerImpl(baseIRI, aliasNamespace);
			generator = new SQLTableShapeGenerator(sqlNamer);
			shapeWriter = new ShapeWriter();
			
			nsManager = new MemoryNamespaceManager();
			nsManager.add("alias", aliasNamespace);
			
			addSchemaNamespaces(schemaManager);
			
			for (SQLSchema schema : schemaManager.listSchemas()) {
				processSchema(schema);
			}
			
		} catch (IOException | RDFHandlerException e) {
			throw new MojoExecutionException("Failed to load SQL files", e);
		}

	}

	private void addSchemaNamespaces(SQLSchemaManager schemaManager) {
		for (SQLSchema schema : schemaManager.listSchemas()) {
			String schemaName = schema.getSchemaName();
			URI schemaId = sqlNamer.schemaId(schema);
			nsManager.add(schemaName, schemaId.stringValue());
		}
	}

	private void processSchema(SQLSchema schema) throws RDFHandlerException, IOException {
		
		for (SQLTableSchema table : schema.listTables()) {
			processTable(table);
		}
		
	}

	private void processTable(SQLTableSchema table) throws RDFHandlerException, IOException {
		
		File file = shapeFile(table);
		Shape shape = generator.toShape(table);

		shapeWriter.writeTurtle(nsManager, shape, file);
	}

	private File shapeFile(SQLTableSchema table) {
		StringBuilder builder = new StringBuilder();
		builder.append(table.getSchema().getSchemaName());
		builder.append('_');
		builder.append(table.getTableName());
		
		return new File(outDir, builder.toString());
	}

}
