package io.konig.maven.sql;

import java.io.File;
import java.io.IOException;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.rio.RDFHandlerException;

import io.konig.core.NamespaceManager;
import io.konig.core.impl.MemoryNamespaceManager;
import io.konig.core.vocab.Konig;
import io.konig.core.vocab.SH;
import io.konig.shacl.Shape;
import io.konig.shacl.io.ShapeWriter;
import io.konig.sql.SQLFileLoader;
import io.konig.sql.SQLSchema;
import io.konig.sql.SQLSchemaManager;
import io.konig.sql.SQLTableSchema;
import io.konig.sql.SQLTableShapeGenerator;

@Mojo( name = "generate")
public class KonigSqlShapeMojo extends AbstractMojo {
	
	@Parameter(defaultValue="${basedir}/src/sql")
	private File sqlSourceDir;
	
	
	private ShapeWriter shapeWriter;
	private NamespaceManager nsManager;
	private SQLTableShapeGenerator generator;
	
	@Parameter(defaultValue="${basedir}/target/generated/rdf/shapes/origin")
	private File originShapeDir;
	
	@Parameter(defaultValue="${basedir}/target/generated/rdf/shapes/target")
	private File targetShapeDir;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		

		originShapeDir.mkdirs();
		targetShapeDir.mkdirs();
		
		nsManager = new MemoryNamespaceManager();
		SQLSchemaManager schemaManager = new SQLSchemaManager();
		
		SQLFileLoader loader = new SQLFileLoader(nsManager, schemaManager);
		try {
			loader.load(sqlSourceDir);
			
			generator = new SQLTableShapeGenerator();
			shapeWriter = new ShapeWriter();
			
			nsManager.add("sh", SH.NAMESPACE);
			nsManager.add("xsd", XMLSchema.NAMESPACE);
			nsManager.add("konig", Konig.NAMESPACE);
			
						
			for (SQLSchema schema : schemaManager.listSchemas()) {
				processSchema(schema);
			}
			
		} catch (IOException | RDFHandlerException e) {
			throw new MojoExecutionException("Failed to load SQL files", e);
		}

	}

	


	private void processSchema(SQLSchema schema) throws RDFHandlerException, IOException, MojoExecutionException {
		
		for (SQLTableSchema table : schema.listTables()) {
			processTable(table);
		}
		
	}

	private void processTable(SQLTableSchema table) throws RDFHandlerException, IOException, MojoExecutionException {
		
		if (table.listColumns().isEmpty()) {
			return;
		}
		
		File file = shapeFile(table);
		Shape shape = generator.toShape(table);
		
		shapeWriter.writeTurtle(nsManager, shape, file);
		
		
		URI targetShapeId = table.getTableTargetShapeId();
		if (targetShapeId != null) {
			Shape targetShape = generator.toStructuredShape(table);
			targetShape.setId(targetShapeId);
			file = targetShapeFile(targetShapeId);
			shapeWriter.writeTurtle(nsManager, targetShape, file);
		}
	}

	

	private File shapeFile(SQLTableSchema table) {
		StringBuilder builder = new StringBuilder();
		builder.append(table.getSchema().getSchemaName());
		builder.append('_');
		builder.append(table.getTableName());
		builder.append(".ttl");
		
		return new File(originShapeDir, builder.toString());
	}


	private File targetShapeFile(URI shapeId) {
		
		StringBuilder builder = new StringBuilder();
		org.openrdf.model.Namespace ns = nsManager.findByName(shapeId.getNamespace());
		if (ns != null) {
			builder.append(ns.getPrefix());
			builder.append('_');
		}
		builder.append(shapeId.getLocalName());
		builder.append(".ttl");
		
		return new File(targetShapeDir, builder.toString());
	}
	
}
