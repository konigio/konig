package io.konig.schemagen.gcp;

import java.io.Closeable;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
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
import com.google.api.services.bigquery.model.Table;
import com.google.api.services.bigquery.model.TableFieldSchema;
import com.google.api.services.bigquery.model.TableSchema;

import io.konig.core.Graph;
import io.konig.core.OwlReasoner;
import io.konig.core.Vertex;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.RdfUtil;
import io.konig.core.vocab.GCP;
import io.konig.pojo.io.PojoFactory;
import io.konig.pojo.io.SimplePojoFactory;
import io.konig.schemagen.SchemaGeneratorException;
import io.konig.schemagen.merge.ShapeAggregator;
import io.konig.schemagen.merge.ShapeNamer;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeManager;

/**
 * A utility that generates BigQuery table definitions from SHACL Shapes.
 * @author Greg McFall
 *
 */
public class BigQueryTableGenerator {
	private static Logger logger = LoggerFactory.getLogger(BigQueryTableGenerator.class);
	private ShapeManager shapeManager;
	private BigQueryDatatypeMapper datatypeMap = new BigQueryDatatypeMapper();
	private OwlReasoner owl;
	private ShapeNamer shapeNamer;
	private BigQueryTableHandler handler;
	
	
	public BigQueryTableGenerator(ShapeManager shapeManager) {
		this.shapeManager = shapeManager;
	}

	public BigQueryTableGenerator(ShapeManager shapeManager, ShapeNamer shapeNamer, OwlReasoner reasoner) {
		this.shapeManager = shapeManager;
		this.shapeNamer = shapeNamer;
		owl = reasoner;
	}
	
	
	public BigQueryTableHandler getHandler() {
		return handler;
	}

	public void setHandler(BigQueryTableHandler handler) {
		this.handler = handler;
	}
	
	public Table toTable(BigQueryTable source) {
		
		Table sink = new Table();
		TableSchema schema = toTableSchema(source);
		sink.setSchema(schema);
		return sink;
	}
	
	public TableSchema toTableSchema(BigQueryTable source) {
		URI shapeId = source.getTableShape();
		if (shapeId == null) {
			throw new SchemaGeneratorException("Shape is not defined");
		}
		
		Shape shape = shapeManager.getShapeById(shapeId);
		if (shape == null) {
			throw new SchemaGeneratorException("Shape not found: " + shapeId);
		}
		
		
		return toTableSchema(shape);
	}
	
	public TableSchema toTableSchema(Shape shape) {
		TableSchema schema = new TableSchema();
		schema.setFields(listFields(shape));
		return schema;
	}
	
	private List<TableFieldSchema> listFields(Shape shape) {
		List<TableFieldSchema> list = new ArrayList<>();

		List<PropertyConstraint> plist = shape.getProperty();
		
		
		for (PropertyConstraint p : plist) {
			TableFieldSchema field = toField(p);
			list.add(field);
		}
		return list;
	}

	private TableFieldSchema toField(PropertyConstraint p) {
		
		TableFieldSchema result = new TableFieldSchema();
		String fieldName = p.getPredicate().getLocalName();
		FieldMode fieldMode = fieldMode(p);
		BigQueryDatatype fieldType = datatypeMap.type(p);
		
		result.setName(fieldName);
		result.setType(fieldType.name());
		result.setMode(fieldMode.name());
		
		if (fieldType == BigQueryDatatype.RECORD) {
			Shape valueShape = p.getValueShape();
			if (valueShape == null) {
				Resource shapeId = p.getValueShapeId();
				if (valueShape instanceof URI) {
					valueShape = shapeManager.getShapeById((URI) shapeId);
					if (valueShape == null) {
						throw new SchemaGeneratorException("Shape not found: " + shapeId.stringValue());
					}
				} else {
					throw new SchemaGeneratorException("Blank nodes not supported for valueShape identifier");
				}
			}
			TableSchema fieldSchema = toTableSchema(valueShape);
			result.setFields(fieldSchema.getFields());
		}
		
		
		return result;
	}

	/**
	 * Generate a BigQuery table definition for each resource of type gcp:BigQueryTable within a given graph.
	 * @param graph The graph containing BigQueryTable resources.
	 * @param outDir The directory to which the BigQuery table definitions will be emitted.
	 * @throws IOException
	 * @throws SchemaGeneratorException
	 */
	public void writeTableDefinitions(Graph graph, File outDir) throws IOException, SchemaGeneratorException {
		outDir.mkdirs();
		List<Vertex> list = graph.v(GCP.GoogleCloudProject).in(RDF.TYPE).toVertexList();

		JsonFactory jsonFactory = new JsonFactory();
		PojoFactory factory = new SimplePojoFactory();	
		for (Vertex v : list) {
			GoogleCloudProject project = factory.create(v, GoogleCloudProject.class);
			
			for (BigQueryDataset dataset : project.getDatasetList()) {
				for (BigQueryTable table : dataset.getTableList()) {
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
			
		}
	}


	public void writeTableDefinitions(File sourceDir, File outDir) throws IOException, SchemaGeneratorException {
		Graph graph = new MemoryGraph();
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
		URI tableClassId = table.getTableClass();
		
		if (tableId == null) {
			throw new SchemaGeneratorException("tableId is not defined");
		}
		if (datasetId == null) {
			throw new SchemaGeneratorException("datasetId is not defined for table " + tableId);
		}
		if (projectId == null) {
			throw new SchemaGeneratorException("projectId is not defined for table " + tableId);
		}
		if (tableShapeId == null && tableClassId == null) {
			throw new SchemaGeneratorException("tableShape or tableClass must be defined for table " + tableId);
		}
		
		Shape shape = null;
		
		if (tableShapeId != null) {
			shape = shapeManager.getShapeById(tableShapeId);
		} else {
			
			List<Shape> shapeList = shapeManager.getShapesByScopeClass(tableClassId);
			
			if (shapeList.isEmpty()) {
				throw new SchemaGeneratorException("No shapes found for class " + tableClassId);
			}
			
			if (shapeList.size()==1) {
				shape = shapeList.get(0);
			} else {
				URI shapeId = shapeNamer.shapeName(tableClassId);
				shape = shapeManager.getShapeById(shapeId);
				if (shape == null) {
					ShapeAggregator aggregator = new ShapeAggregator(owl, shapeManager);
					for (Shape s : shapeList) {
						if (shape == null) {
							shape = s;
						} else {
							shape = aggregator.merge(shapeId, shape, s);
						}
					}
				}
			}
			
			
		}
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
		
		if (handler != null) {
			handler.add(table);
		}
		
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
		
		if (type == BigQueryDatatype.RECORD) {

			Shape valueShape = p.getValueShape();
			if (valueShape == null) {
				Resource shapeId = p.getValueShapeId();
				if (shapeId instanceof URI) {
					valueShape = shapeManager.getShapeById((URI) shapeId);
					if (valueShape == null) {
						throw new SchemaGeneratorException("Shape not found: " + shapeId.stringValue());
					}
				} else {
					throw new SchemaGeneratorException("Blank nodes not support for valueShape identifier");
				}
			}
			writeFields(valueShape, json);
		} 
		json.writeEndObject();
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
