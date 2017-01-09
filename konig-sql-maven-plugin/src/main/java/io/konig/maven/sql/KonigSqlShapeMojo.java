package io.konig.maven.sql;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.rio.RDFHandlerException;

import io.konig.core.NamespaceManager;
import io.konig.core.impl.MemoryNamespaceManager;
import io.konig.core.impl.RdfUtil;
import io.konig.core.vocab.Konig;
import io.konig.core.vocab.SH;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;
import io.konig.shacl.io.ShapeWriter;
import io.konig.sql.SQLFileLoader;
import io.konig.sql.SQLSchema;
import io.konig.sql.SQLSchemaManager;
import io.konig.sql.SQLTableNamerImpl;
import io.konig.sql.SQLTableSchema;
import io.konig.sql.SQLTableShapeGenerator;

@Mojo( name = "generate")
public class KonigSqlShapeMojo extends AbstractMojo {
	
	@Parameter
	private String baseIRI;

	@Parameter
	private File sourceDir;
	
	@Parameter
	private File outDir;
	
	@Parameter
	private String aliasNamespace;
	
	@Parameter
	private List<Schema> schemas = new ArrayList<>();
	
	@Parameter
	private List<Namespace> namespaces = new ArrayList<>();
	
	private ShapeWriter shapeWriter;
	private NamespaceManager nsManager;
	private SQLTableNamerImpl sqlNamer;
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
			nsManager.add("sh", SH.NAMESPACE);
			nsManager.add("xsd", XMLSchema.NAMESPACE);
			nsManager.add("konig", Konig.NAMESPACE);
			
			addSchemaNamespaces(schemaManager);
			addCustomNamespaces();
						
			for (SQLSchema schema : schemaManager.listSchemas()) {
				processSchema(schema);
			}
			
		} catch (IOException | RDFHandlerException e) {
			throw new MojoExecutionException("Failed to load SQL files", e);
		}

	}

	

	private void addCustomNamespaces() throws MojoExecutionException {
		for (Namespace ns : namespaces) {
			String prefix = ns.getPrefix();
			String iri = ns.getIri();
			if (prefix == null) {
				throw new MojoExecutionException("Namepace must have a 'prefix' property");
			}
			if (iri == null) {
				throw new MojoExecutionException("Namespace must have an 'iri' property");
			}
			nsManager.add(prefix, iri);
		}
		
	}

	private void addRdfIdentifiers() throws MojoExecutionException {
		
		for (Schema s : schemas) {
			String schemaName = s.getName();
			String schemaIRI = s.getIri();
			if (schemaName == null) {
				throw new MojoExecutionException("Schema element is missing a 'name' property");
			}
			if (schemaIRI != null) {
				sqlNamer.put(schemaName, uri(schemaIRI));
			}
			
			for (Table t : s.getTables()) {
				String tableName = t.getName();
				if (tableName == null) {
					throw new MojoExecutionException("Table element is missing a 'name' property");
				}
				String tableFullName = schemaName + "." + tableName;
				String tableIRI = t.getIri();
				if (tableIRI != null) {
					sqlNamer.put(tableFullName, uri(tableIRI));
				}
				
				for (Column c : t.getColumns()) {
					String columnName = c.getName();
					if (columnName == null) {
						throw new MojoExecutionException("Column element is missing a 'name' property");
					}
					String columnIRI = c.getIri();
					if (columnIRI != null) {
						String columnFullName = tableFullName + "." + columnName;
						sqlNamer.put(columnFullName, uri(columnIRI));
					}
				}
			}
		}
		
	}
	
	private URI uri(String value) {
		return new URIImpl(value);
	}

	private void addSchemaNamespaces(SQLSchemaManager schemaManager) {
		for (SQLSchema schema : schemaManager.listSchemas()) {
			String schemaName = schema.getSchemaName();
			URI schemaId = sqlNamer.schemaId(schema);
			nsManager.add(schemaName, schemaId.stringValue());
		}
	}

	private void processSchema(SQLSchema schema) throws RDFHandlerException, IOException, MojoExecutionException {
		
		for (SQLTableSchema table : schema.listTables()) {
			processTable(table);
		}
		
	}

	private void processTable(SQLTableSchema table) throws RDFHandlerException, IOException, MojoExecutionException {
		
		File file = shapeFile(table);
		Shape shape = generator.toShape(table);
		
		processElements(shape, table);

		shapeWriter.writeTurtle(nsManager, shape, file);
	}

	private void processElements(Shape shape, SQLTableSchema table) throws MojoExecutionException {
		Schema schema = getSchemaByName(table.getSchema().getSchemaName());
		if (schema != null) {
			String schemaName = schema.getName();
			String tableName = table.getTableName();
			Table t = schema.getTableByName(tableName);
			
			if (t != null) {
				
				String targetClass = t.getTargetClass();
				if (targetClass != null) {
					shape.setTargetClass(expand(targetClass));
				}
				
				for (Column c : t.getColumns()) {
					String columnName = c.getName();
					
					URI predicate = sqlNamer.rdfPredicate(schemaName, tableName, columnName);
					PropertyConstraint p = shape.getPropertyConstraint(predicate);
					if (p == null) {
						throw new MojoExecutionException("Column not found: " + schemaName + "." + tableName + "." + columnName);
					}
					p.setEquivalentPath(c.getEquivalentPath());
				}
			}
			
			
		}
	}
	



	private URI expand(String value) {
		return RdfUtil.expand(nsManager, value);
	}



	private Schema getSchemaByName(String name)  {
		for (Schema s : schemas) {
			if (name.equals(s.getName())) {
				return s;
			}
		}
		return null;
	}

	private File shapeFile(SQLTableSchema table) {
		StringBuilder builder = new StringBuilder();
		builder.append(table.getSchema().getSchemaName());
		builder.append('_');
		builder.append(table.getTableName());
		
		return new File(outDir, builder.toString());
	}

}
