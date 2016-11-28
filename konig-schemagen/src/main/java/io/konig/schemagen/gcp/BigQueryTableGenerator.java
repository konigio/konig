package io.konig.schemagen.gcp;

import java.io.Closeable;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Set;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.DC;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.google.api.services.bigquery.model.Table;
import com.google.api.services.bigquery.model.TableFieldSchema;
import com.google.api.services.bigquery.model.TableReference;
import com.google.api.services.bigquery.model.TableSchema;

import io.konig.activity.Activity;
import io.konig.core.Edge;
import io.konig.core.Graph;
import io.konig.core.OwlReasoner;
import io.konig.core.UidGenerator;
import io.konig.core.Vertex;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.RdfUtil;
import io.konig.core.impl.UidGeneratorImpl;
import io.konig.core.pojo.PojoFactory;
import io.konig.core.pojo.SimplePojoFactory;
import io.konig.core.vocab.Konig;
import io.konig.core.vocab.Schema;
import io.konig.schemagen.SchemaGeneratorException;
import io.konig.schemagen.ShapeNamer;
import io.konig.schemagen.merge.ShapeAggregator;
import io.konig.shacl.NodeKind;
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
	private BigQueryTableMapper tableMapper;
	
	
	public BigQueryTableGenerator(ShapeManager shapeManager) {
		this.shapeManager = shapeManager;
	}
	
	public BigQueryTableGenerator() {
	}
	

	public BigQueryTableGenerator(ShapeManager shapeManager, ShapeNamer shapeNamer, OwlReasoner reasoner) {
		this.shapeManager = shapeManager;
		this.shapeNamer = shapeNamer;
		owl = reasoner;
	}
	
	public ShapeManager getShapeManager() {
		return shapeManager;
	}

	public BigQueryTableGenerator setShapeManager(ShapeManager shapeManager) {
		this.shapeManager = shapeManager;
		return this;
	}

	public ShapeNamer getShapeNamer() {
		return shapeNamer;
	}

	public BigQueryTableGenerator setShapeNamer(ShapeNamer shapeNamer) {
		this.shapeNamer = shapeNamer;
		return this;
	}

	public BigQueryTableMapper getTableMapper() {
		return tableMapper;
	}


	public BigQueryTableGenerator setTableMapper(BigQueryTableMapper tableMapper) {
		this.tableMapper = tableMapper;
		return this;
	}


	public BigQueryTableHandler getHandler() {
		return handler;
	}

	public void setHandler(BigQueryTableHandler handler) {
		this.handler = handler;
	}
	
	/**
	 * Generate BigQuery tables for the sub-classes of schema:Enumeration.
	 * @param sourceGraph The graph containing OWL class definitions.
	 * @param manager The Google Cloud Project manager into which generated tables will be placed.
	 */
	public void generateEnumTables(Graph sourceGraph, GoogleCloudManager manager) {
			
		Activity activity = new Activity();
		activity.setType(Konig.GenerateEnumTables);
		activity.setId(Activity.nextActivityId());
		
		List<Vertex> list = sourceGraph.v(Schema.Enumeration).in(RDFS.SUBCLASSOF).isIRI().toVertexList();
		for (Vertex owlClass : list) {
			
			URI classId = (URI) owlClass.getId();
			Collection<BigQueryTable> prior = manager.tablesForClass(classId);
			if (prior.isEmpty()) {
				if (tableMapper == null) {
					throw new GoogleCloudException("Cannot generate enum tables: tableMapper is not defined");
				}
				String tableId = tableMapper.tableForClass(owlClass);
				if (tableId == null) {
					continue;
				}
				if (shapeNamer == null) {
					throw new GoogleCloudException("Cannot generate enum tables: shapeNamer is not defined");
				}
				if (shapeManager == null) {
					throw new GoogleCloudException("Cannot generate enum tables: shapeManager is not defined");
				}

				BigQueryDataset dataset = manager.datasetForClass(owlClass);
				if (dataset != null) {
					BigQueryTable table = dataset.findDatasetTable(tableId);
					if (table == null) {
						table = new BigQueryTable();
						table.setTableId(tableId);
						table.setWasGeneratedBy(activity);
						URI shapeId = shapeNamer.shapeName(classId);
						if (shapeId != null) {
							
							Shape shape = shapeManager.getShapeById(shapeId);
							if (shape == null) {
								shape = new Shape();
								shape.setId(shapeId);
								shape.setNodeKind(NodeKind.IRI);
								shape.setTargetClass(classId);
								shape.setWasGeneratedBy(activity);
								
								PropertyConstraint nameProperty = new PropertyConstraint(Schema.name);
								nameProperty.setDatatype(XMLSchema.STRING);
								nameProperty.setMinCount(1);
								nameProperty.setMaxCount(1);
								shape.add(nameProperty);
								
								
								List<Vertex> memberList = owlClass.asTraversal().in(RDF.TYPE).toVertexList();

								int minCount = memberList.isEmpty() ? 0 : 1;
								int maxCount = 0;
								for (Vertex m : memberList) {
									Set<Edge>  set = m.outProperty(DC.IDENTIFIER);
									if (set.isEmpty()) {
										minCount=0;
									} else {
										maxCount = 1;
									}
								}
								
								if (maxCount > 0) {
									PropertyConstraint p = new PropertyConstraint(DC.IDENTIFIER);
									p.setDatatype(XMLSchema.STRING);
									p.setMinCount(minCount);
									p.setMaxCount(maxCount);
									shape.add(p);
								}
								
								shapeManager.addShape(shape);
							}

							dataset.addDatasetTable(table);
							table.setTableShape(shapeId);
						}
						
					}
					
				}
				
				
			}
			
		}
		activity.setEndTime((GregorianCalendar)GregorianCalendar.getInstance());
	}
	
	

	public Table toTable(BigQueryTable source) {
		
		Table sink = new Table();
		TableReference tableRef = tableReference(source);
		TableSchema schema = toTableSchema(source);
		sink.setTableReference(tableRef);
		sink.setSchema(schema);
		return sink;
	}
	
	private TableReference tableReference(BigQueryTable source) {
		TableReference result = new TableReference();
		result.setTableId(source.getTableId());
		result.setDatasetId(source.getTableDataset().getDatasetId());
		result.setProjectId(source.getTableDataset().getDatasetProject().getProjectId());
		
		return result;
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
		
		if (shape.getNodeKind() == NodeKind.IRI) {
			TableFieldSchema idField = new TableFieldSchema();
			idField.setName("id");
			idField.setType(BigQueryDatatype.STRING.name());
			idField.setMode(FieldMode.REPEATED.name());
			list.add(idField);
		}
		
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
		List<Vertex> list = graph.v(Konig.GoogleCloudProject).in(RDF.TYPE).toVertexList();

		JsonFactory jsonFactory = new JsonFactory();
		PojoFactory factory = new SimplePojoFactory();	
		for (Vertex v : list) {
			GoogleCloudProject project = factory.create(v, GoogleCloudProject.class);
			
			for (BigQueryDataset dataset : project.getProjectDataset()) {
				for (BigQueryTable table : dataset.getDatasetTable()) {
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
			
			List<Shape> shapeList = shapeManager.getShapesByTargetClass(tableClassId);
			
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
		
		if (shape.getNodeKind() == NodeKind.IRI) {

			json.writeStartObject();
			json.writeStringField("name", "id");
			json.writeStringField("type", BigQueryDatatype.STRING.name());
			json.writeStringField("mode", FieldMode.REQUIRED.name());
			json.writeEndObject();
		}
		
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
