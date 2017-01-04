package io.konig.maven.sql;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import io.konig.core.vocab.Konig;
import io.konig.core.vocab.SH;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;
import io.konig.shacl.io.ShapeWriter;
import io.konig.sql.SQLColumnSchema;
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
	private List<SqlElement> elements = new ArrayList<>();
	
	@Parameter
	private List<Namespace> namespaces = new ArrayList<>();
	
	private ShapeWriter shapeWriter;
	private NamespaceManager nsManager;
	private SQLTableNamerImpl sqlNamer;
	private SQLTableShapeGenerator generator;
	private Map<String, SqlElement> elementMap;

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
			
			addRdfIdentifiers();
			mapElements();
			
			for (SQLSchema schema : schemaManager.listSchemas()) {
				processSchema(schema);
			}
			
		} catch (IOException | RDFHandlerException e) {
			throw new MojoExecutionException("Failed to load SQL files", e);
		}

	}

	private void mapElements() throws MojoExecutionException {
		elementMap = new HashMap<>();
		for (SqlElement e : elements) {
			elementMap.put(e.getSqlId(), e);
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
		for (SqlElement info : elements) {
			String sqlId = info.getSqlId();
			String rdfId = info.getIri();
			
			if (sqlId != null && rdfId!=null) {

				URI uri = new URIImpl(rdfId);
				String[] array = sqlId.split("[.]");
				if (array.length==3) {
					String columnName = columnName(sqlId);
					if (!columnName.equals(uri.getLocalName())) {
						throw new MojoExecutionException("Mismatched name. The local name of the iri property must match the local name of the column:" +
								sqlId + " <=> " + rdfId);
					}
				}
				
				sqlNamer.put(sqlId, uri);
			}
		}
		
	}

	private String columnName(String sqlId) {
		int start = sqlId.lastIndexOf('.')+1;
		return sqlId.substring(start);
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
		
		processElements(shape, table);

		shapeWriter.writeTurtle(nsManager, shape, file);
	}

	private void processElements(Shape shape, SQLTableSchema table) {
		
		for (SQLColumnSchema column : table.listColumns()) {
			String fullName = column.getFullName();
			SqlElement element = elementMap.get(fullName);
			if (element != null) {
				String equivalentPath = element.getEquivalentPath();
				if (equivalentPath != null) {
					URI predicate = sqlNamer.rdfPredicate(column);
					PropertyConstraint c = shape.getPropertyConstraint(predicate);
					if (c != null) {
						c.setEquivalentPath(equivalentPath);
					}
				}
			}
		}
		
	}

	private File shapeFile(SQLTableSchema table) {
		StringBuilder builder = new StringBuilder();
		builder.append(table.getSchema().getSchemaName());
		builder.append('_');
		builder.append(table.getTableName());
		
		return new File(outDir, builder.toString());
	}

}
