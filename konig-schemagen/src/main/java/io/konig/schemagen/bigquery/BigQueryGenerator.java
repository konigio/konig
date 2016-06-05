package io.konig.schemagen.bigquery;

import java.io.Closeable;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

import io.konig.core.Graph;
import io.konig.core.Vertex;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.RdfUtil;
import io.konig.core.vocab.GCP;
import io.konig.pojo.io.PojoFactory;
import io.konig.pojo.io.SimplePojoFactory;
import io.konig.schemagen.SchemaGeneratorException;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeManager;

/**
 * A utility that generates BigQuery table definitions from SHACL Shapes.
 * @author Greg McFall
 *
 */
public class BigQueryGenerator {
	private static Logger logger = LoggerFactory.getLogger(BigQueryGenerator.class);
	private ShapeManager shapeManager;
	private BigQueryDatatypeMapper datatypeMap = new BigQueryDatatypeMapper();
	
	
	public BigQueryGenerator(ShapeManager shapeManager) {
		this.shapeManager = shapeManager;
	}
	
	/**
	 * Generate a BigQuery table definition for each resource of type gcp:BigQueryTable within a given graph.
	 * @param graph The graph containing BigQueryTable resources.
	 * @param outDir The directory to which the BigQuery table definitions will be emitted.
	 * @throws IOException
	 * @throws SchemaGeneratorException
	 */
	public void writeTableDefinitions(Graph graph, File outDir) throws IOException, SchemaGeneratorException {
		List<Vertex> list = graph.v(GCP.BigQueryTable).in(RDF.TYPE).toVertexList();

		JsonFactory jsonFactory = new JsonFactory();
		PojoFactory factory = new SimplePojoFactory();	
		for (Vertex v : list) {
			BigQueryTable table = factory.create(v, BigQueryTable.class);
			String tableFileName = tableFileName(table);
			File outFile = new File(outDir, tableFileName);
			FileWriter writer = new FileWriter(outFile);
			try {
				JsonGenerator json = jsonFactory.createGenerator(writer);
				json.useDefaultPrettyPrinter();
				writeTableDefinition(table, json);
				json.flush();
			} finally {
				close(writer);
			}
		}
	}


	public void writeTableDefinitions(File sourceDir, File outDir) throws IOException, SchemaGeneratorException {
		Graph graph = new MemoryGraph();
		outDir.mkdirs();
		try {
			RdfUtil.loadTurtle(sourceDir, graph, null);
			writeTableDefinitions(graph, outDir);
			
		} catch (RDFParseException | RDFHandlerException e) {
			throw new SchemaGeneratorException(e);
		}
	}

	private void close(Closeable stream) {
		try {
			stream.close();
		} catch (Throwable oops) {
			logger.warn("Failed to close stream", oops);
		}
		
	}


	private String tableFileName(BigQueryTable table) {
		BigQueryTableReference ref = table.getTableReference();
		if (ref ==null) {
			throw new SchemaGeneratorException("tableReference is not defined");
		}
		String tableId = ref.getTableId();
		String datasetId = ref.getDatasetId();
		String projectId = ref.getProjectId();
		if (tableId == null) {
			throw new SchemaGeneratorException("tableId is not defined");
		}
		if (datasetId == null) {
			throw new SchemaGeneratorException("datasetId is not defined for table " + tableId);
		}
		if (projectId == null) {
			throw new SchemaGeneratorException("projectId is not defined for table " + tableId);
		}
		
		StringBuilder builder = new StringBuilder();
		builder.append(projectId);
		builder.append('.');
		builder.append(datasetId);
		builder.append('.');
		builder.append(tableId);
		
		return builder.toString();
	}


	public void writeTableDefinition(BigQueryTable table, JsonGenerator json)
		throws IOException, SchemaGeneratorException {
	
		BigQueryTableReference ref = table.getTableReference();
		if (ref == null) {
			throw new SchemaGeneratorException("tableReference is not defined");
		}
		String tableId = ref.getTableId();
		String datasetId = ref.getDatasetId();
		String projectId = ref.getProjectId();
		String description = table.getDescription();
		URI tableShapeId = table.getTableShape();
		if (tableId == null) {
			throw new SchemaGeneratorException("tableId is not defined");
		}
		if (datasetId == null) {
			throw new SchemaGeneratorException("datasetId is not defined for table " + tableId);
		}
		if (projectId == null) {
			throw new SchemaGeneratorException("projectId is not defined for table " + tableId);
		}
		if (tableShapeId == null) {
			throw new SchemaGeneratorException("tableShape is not defined for table " + tableId);
		}
		
		Shape shape = shapeManager.getShapeById(tableShapeId);
		if (shape == null) {
			throw new SchemaGeneratorException("Shape not found: " + tableShapeId);
		}
		
		json.writeStartObject();
		
		json.writeFieldName("tableReference");
		json.writeStartObject();
		json.writeStringField("projectId", projectId);
		json.writeStringField("datasetId", datasetId);
		json.writeStringField("tableId", tableId);
		json.writeEndObject();
		if (description != null) {
			json.writeStringField("description", description);
		}
		
		json.writeFieldName("schema");
		json.writeStartObject();
		writeFields(shape, json);
		json.writeEndObject();
		
		json.writeEndObject();
		
	}



	private void writeFields(Shape shape, JsonGenerator json) throws IOException {
		List<PropertyConstraint> list = shape.getProperty();
		
		json.writeFieldName("fields");
		json.writeStartArray();
		
		for (PropertyConstraint p : list) {
			writeProperty(p, json);
		}
		
		json.writeEndArray();
		
	}



	private void writeProperty(PropertyConstraint p, JsonGenerator json) throws IOException {
		
		String fieldName = p.getPredicate().getLocalName();
		FieldMode fieldMode = fieldMode(p);
		BigQueryDatatype type = datatypeMap.type(p);

		json.writeStartObject();
		json.writeStringField("name", fieldName);
		json.writeStringField("type", type.toString());
		json.writeStringField("mode", fieldMode.toString());
		json.writeEndObject();
		
		if (type == BigQueryDatatype.RECORD) {

			Shape valueShape = p.getValueShape();
			if (valueShape == null) {
				Resource shapeId = p.getValueShapeId();
				if (shapeId instanceof URI) {
					valueShape = shapeManager.getShapeById((URI) shapeId);
					writeFields(valueShape, json);
				} else {
					throw new SchemaGeneratorException("Blank nodes not support for valueShape identifier");
				}
				
			}
		} 
	}
	
	private FieldMode fieldMode(PropertyConstraint p) {
		Integer minCount = p.getMinCount();
		Integer maxCount = p.getMaxCount();
		
		if (maxCount==null || maxCount>1) {
			return FieldMode.REPEATED;
		}
		if (minCount!=null && maxCount!=null && minCount==1 && maxCount==1) {
			return FieldMode.REQUIRED;
		}
		return FieldMode.NULLABLE;
	}
	
}
