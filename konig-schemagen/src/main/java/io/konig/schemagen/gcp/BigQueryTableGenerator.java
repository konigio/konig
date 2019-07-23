package io.konig.schemagen.gcp;

/*
 * #%L
 * Konig Schema Generator
 * %%
 * Copyright (C) 2015 - 2017 Gregory McFall
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


import java.io.Closeable;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;
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

import io.konig.core.Graph;
import io.konig.core.KonigException;
import io.konig.core.OwlReasoner;
import io.konig.core.Vertex;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.RdfUtil;
import io.konig.core.pojo.PojoFactory;
import io.konig.core.pojo.SimplePojoFactory;
import io.konig.core.showl.ShowlUtil;
import io.konig.core.vocab.Konig;
import io.konig.gcp.datasource.BigQueryTableReference;
import io.konig.schemagen.SchemaGeneratorException;
import io.konig.schemagen.merge.ShapeAggregator;
import io.konig.schemagen.sql.SqlKeyType;
import io.konig.schemagen.sql.SqlTableGeneratorUtil;
import io.konig.shacl.AndConstraint;
import io.konig.shacl.NodeKind;
import io.konig.shacl.OrConstraint;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeManager;
import io.konig.shacl.ShapeNamer;
import io.konig.shacl.XoneConstraint;

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
	
	public TableSchema toErrorSchema(Shape shape) {
		TableSchema schema = toTableSchema(shape);
		List<TableFieldSchema> fieldList = schema.getFields();

		makeNullable(fieldList);
		
		String sourceNodeName = ShowlUtil.shortShapeName(shape.getIri());
		TableFieldSchema sourceNodeField = new TableFieldSchema()
				.setName(sourceNodeName)
				.setType(BigQueryDatatype.RECORD.name())
				.setMode(FieldMode.REQUIRED.name())
				.setFields(fieldList);
		
		fieldList = new ArrayList<>();
		

		schema.setFields(fieldList);
		
		fieldList.add(new TableFieldSchema()
				.setName("errorId")
				.setType(BigQueryDatatype.STRING.name())
				.setDescription("A UUID that uniquely identifies the error record")
				.setMode(FieldMode.REQUIRED.name())
		);

		fieldList.add(new TableFieldSchema()
				.setName("errorCreated")
				.setType(BigQueryDatatype.TIMESTAMP.name())
				.setDescription("A timestamp that records when the error record was created")
				.setMode(FieldMode.REQUIRED.name())
		);
		
		fieldList.add(new TableFieldSchema()
				.setName("errorMessage")
				.setType(BigQueryDatatype.STRING.name())
				.setMode(FieldMode.REQUIRED.name())
		);
		
		
		fieldList.add(new TableFieldSchema()
				.setName("pipelineJobName")
				.setType(BigQueryDatatype.STRING.name())
				.setDescription("The Job Name for the ETL pipeline")
				.setMode(FieldMode.REQUIRED.name())
		);
		
		
		fieldList.add(sourceNodeField);
		
		return schema;
	}
	
	private void makeNullable(List<TableFieldSchema> fieldList) {
		for (TableFieldSchema field : fieldList) {
			if (field.getMode().equals(FieldMode.REQUIRED.name())) {
				field.setMode(FieldMode.NULLABLE.name());
			}
		}
		
	}

	public TableSchema toTableSchema(Shape shape) {
		Traversal traversal = new Traversal(shape);
		return toTableSchema(shape, traversal);
	}
	
	private TableSchema toTableSchema(Shape shape, Traversal traversal) {

		shape = flatten(shape);
		TableSchema schema = new TableSchema();
		schema.setFields(listFields(shape, traversal));
		return schema;
	}
	
	private List<TableFieldSchema> listFields(Shape shape, Traversal traversal) {
		List<TableFieldSchema> list = new ArrayList<>();

		List<PropertyConstraint> plist = shape.getProperty();
		
		if (shape.getNodeKind() == NodeKind.IRI) {
			TableFieldSchema idField = new TableFieldSchema();
			idField.setName("id");
			idField.setType(BigQueryDatatype.STRING.name());
			idField.setMode(FieldMode.REQUIRED.name());
			list.add(idField);
		} else if (shape.getNodeKind() == NodeKind.BlankNodeOrIRI) {

			TableFieldSchema idField = new TableFieldSchema();
			idField.setName("id");
			idField.setType(BigQueryDatatype.STRING.name());
			idField.setMode(FieldMode.NULLABLE.name());
			list.add(idField);
		}
		
		for (PropertyConstraint p : plist) {
			if(!SqlTableGeneratorUtil.isValidRdbmsShape(shape) && 
					SqlKeyType.SYNTHETIC_KEY.equals(SqlTableGeneratorUtil.getKeyType(p))){
				logger.error("konig:synthicKey is applicable only for shapes with datasource GoogleCloudSqlTable or AwsAurora");
				p.setStereotype(null);
			}
			if (p.getPredicate() != null) {
				TableFieldSchema field = toField(p, traversal);
				list.add(field);
			}
		}
		return list;
	}

	private TableFieldSchema toField(PropertyConstraint p, Traversal traversal) {
		traversal.push(p);
		TableFieldSchema result = new TableFieldSchema();
		String fieldName = p.getPredicate().getLocalName();
		FieldMode fieldMode = fieldMode(p);
		BigQueryDatatype fieldType = datatypeMap.type(p);
		
		result.setName(fieldName);
		result.setType(fieldType.name());
		result.setMode(fieldMode.name());
		
		if (RDF.LANGSTRING.equals(p.getDatatype())) {
			result.setFields(langStringFields(p));
		} else if (fieldType == BigQueryDatatype.RECORD) {
			Shape valueShape = p.getShape();
			if (valueShape == null) {
				Resource shapeId = p.getShapeId();
				if (valueShape instanceof URI) {
					valueShape = shapeManager.getShapeById((URI) shapeId);
					if (valueShape == null) {
						throw new SchemaGeneratorException("Shape not found: " + shapeId.stringValue());
					}
				} else {
					throw new SchemaGeneratorException("Blank nodes not supported for valueShape identifier");
				}
			}
			TableSchema fieldSchema = toTableSchema(valueShape, traversal);
			if (fieldSchema.getFields().isEmpty()) {
				URI shapeId = RdfUtil.uri(valueShape.getId());
				String shapeName = shapeId == null ? "ANONYMOUS" : shapeId.getLocalName();
				
				String msg = MessageFormat.format("{0} at {1} contains no properties", shapeName, fieldName);
				throw new SchemaGeneratorException(msg);
			}
			result.setFields(fieldSchema.getFields());
		}
		
		traversal.pop();
		return result;
	}

	private List<TableFieldSchema> langStringFields(PropertyConstraint p) {
		
		List<TableFieldSchema> list = new ArrayList<>();
		TableFieldSchema stringValue = new TableFieldSchema();
		list.add(stringValue);
		stringValue.setName("stringValue");
		stringValue.setMode(FieldMode.REQUIRED.name());
		stringValue.setType(BigQueryDatatype.STRING.name());
		
		TableFieldSchema languageCode = new TableFieldSchema();
		list.add(languageCode);
		languageCode.setName("languageCode");
		languageCode.setMode(FieldMode.REQUIRED.name());
		languageCode.setType(BigQueryDatatype.STRING.name());
		
		
		return list;
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
			RdfUtil.loadTurtle(sourceDir, graph);
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
			
		} else if (shape.getNodeKind() == NodeKind.BlankNodeOrIRI) {

			json.writeStartObject();
			json.writeStringField("name", "id");
			json.writeStringField("type", BigQueryDatatype.STRING.name());
			json.writeStringField("mode", FieldMode.NULLABLE.name());
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

			Shape valueShape = p.getShape();
			if (valueShape == null) {
				Resource shapeId = p.getShapeId();
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
	
	private static class Traversal {
		private Shape root;
		private List<PropertyConstraint> path = new ArrayList<>();
		private Traversal(Shape root) {
			this.root = root;
		}
		
		private void push(PropertyConstraint p) {
			boolean err = path.contains(p);
			path.add(p);
			if (err) {
				StringBuilder builder = new StringBuilder();
				builder.append("Cyclic path detected: ");
				builder.append(RdfUtil.localName(root.getId()));
				for (PropertyConstraint c : path) {
					builder.append('.');
					builder.append(RdfUtil.localName(c.getPredicate()));
				}
				
				throw new KonigException(builder.toString());
			}
		}
		
		private void pop() {
			if (!path.isEmpty()) {
				path.remove(path.size()-1);
			}
		}
		
	}
	
	private Shape flatten(Shape sourceShape) {
		AndConstraint and = sourceShape.getAnd();
		OrConstraint or = sourceShape.getOr();
		XoneConstraint xone = sourceShape.getXone();
		
		
		
		if (and==null && or==null && xone==null) {
			return sourceShape;
		}
		sourceShape.setXone(null);
		sourceShape.setOr(null);
		sourceShape.setAnd(null);
		
		Shape result = sourceShape.deepClone();
		
		sourceShape.setAnd(and);
		sourceShape.setOr(or);
		sourceShape.setXone(xone);
		
		if (or != null) {
			OrContext orContext = new OrContext();
			orContext.doMerge(result, or.getShapes());
		}
		
		if (and != null) {
			AndContext andContext = new AndContext();
			andContext.doMerge(result, and.getShapes());
		}
		
		if (xone != null) {
			OrContext orContext = new OrContext();
			orContext.doMerge(result, xone.getShapes());
		}
		
		
		return result;
	}


	
	
	private abstract class BaseConstraintContext  {
		
		

		
		void doMerge(Shape sourceShape, List<Shape> constraints) {

			Set<URI> predicateSet = collectPredicates(constraints);
			for (URI predicate : predicateSet) {
				merge(sourceShape,predicate, constraints);
			}
			
		}


		private void merge(Shape sinkShape, URI predicate, List<Shape> constraints) {
			PropertyConstraint q = sinkShape.getPropertyConstraint(predicate);
			
			if (q != null) {
				throw new KonigException("Base shape <" + sinkShape.getId().stringValue() + "> must not contain property " + predicate);
			}
			q = new PropertyConstraint(predicate);
			q.setMinCount(-1);
			q.setMaxCount(-1);
			sinkShape.add(q);

			List<Shape> valueShapes = new ArrayList<>();
			for (Shape s : constraints) {
				
				PropertyConstraint p = s.getPropertyConstraint(predicate);
				q.setMaxCount(maxCount(p, q));
				q.setMinCount(minCount(p, q));
				if (p==null) {
					 continue;
				}
				q.setNodeKind(nodeKind(p.getNodeKind(), q.getNodeKind()));
				setValueType(p, q);
				Shape pShape = p.getShape();
				
				if (pShape != null) {
					valueShapes.add(pShape);
				}
			}
			if (!valueShapes.isEmpty()) {
				Shape childShape = new Shape();
				q.setShape(childShape);
				doMerge(childShape, valueShapes);
			}
			q.setMinCount(countValue(q.getMinCount()));
			q.setMaxCount(countValue(q.getMaxCount()));
			
		}

		


		private Integer countValue(Integer count) {
			return 
				count==null || count.intValue()<0  ? null : count;
		}


		abstract protected Integer maxCount(PropertyConstraint p, PropertyConstraint q);

		abstract protected Integer minCount(PropertyConstraint p, PropertyConstraint q);



		/**
		 * Merge the properties into a single target
		 * @param p The property whose values are to be merged into the sink
		 * @param q The property into which values are to be merged.
		 */
		private void setValueType(PropertyConstraint p, PropertyConstraint q) {
			URI predicate = p.getPredicate();
			URI pDatatype = p.getDatatype();
			URI qDatatype = q.getDatatype();
			
			Resource pValueClass = valueClass(p);
			Resource qValueClass = valueClass(q);
			
			if (
				(pDatatype!=null && qValueClass!=null) ||
				(pValueClass!=null && qDatatype!=null) ) {
				throw new KonigException("Cannot merge " + predicate);
			}

			
			q.setDatatype(leastCommonDatatype(predicate, pDatatype, qDatatype));
			q.setValueClass(leastCommonValueClass(predicate, pValueClass, qValueClass));
			
			
			
			
			
		}

		private Resource leastCommonValueClass(URI predicate, Resource a, Resource b) {
			if (a==null && b==null) {
				return null;
			}
			Resource least = owl.leastCommonSuperClass(a, b);
			return least instanceof URI ? (URI) least : null;
		}

		private NodeKind nodeKind(NodeKind a, NodeKind b) {
			if (a==b) {
				return a;
			}
			if (a==null) {
				return b;
			}
			if (b==null) {
				return a;
			}
			if (a==NodeKind.IRI || b==NodeKind.IRI) {
				return NodeKind.IRI;
			}
			return null;
		}

		private URI leastCommonDatatype(URI predicate, URI a, URI b) {
			if (a==null && b==null) {
				return null;
			}
			Resource least = owl.leastCommonSuperClass(a, b);
			if (least instanceof URI && !OWL.THING.equals(least)) {
				return (URI)least;
			}
			throw new KonigException("Incompatible datatypes for predicate < " + 
					predicate + ">: " + a.getLocalName() + " AND " + b.getLocalName());
		}

		private Resource valueClass(PropertyConstraint q) {
			Resource valueClass = q.getValueClass();
			if (valueClass == null && q.getShape()!= null) {
				valueClass = q.getShape().getTargetClass();
			}
			return valueClass;
		}

		private Set<URI> collectPredicates(List<Shape> list) {
			Set<URI> result = new HashSet<>();
			for (Shape s : list) {
				for (PropertyConstraint p : s.getProperty()) {
					URI predicate = p.getPredicate();
					if (predicate != null) {
						result.add(predicate);
					}
				}
			}
			return result;
		}

		
	}
	
	private class OrContext extends BaseConstraintContext {
		
		

		@Override
		protected Integer minCount(PropertyConstraint p, PropertyConstraint q) {
			if (p==null) {
				return 0;
			}
			Integer pMin = p.getMinCount();
			Integer qMin = q.getMinCount();
			
			if (qMin.intValue()==-1) {
				return pMin;
			}
			
			if (pMin == null) {
				pMin = 0;
			}
			
			
			return pMin.intValue() < qMin.intValue() ? pMin : qMin;
		}

		@Override
		protected Integer maxCount(PropertyConstraint p, PropertyConstraint q) {
			if (p == null) {
				return q.getMaxCount();
			}
	
			
			Integer pMax = p.getMaxCount();
			Integer qMax = q.getMaxCount();
			
			if (qMax==null || pMax==null) {
				return null;
			}
			if (qMax.intValue()==-1) {
				return pMax;
			}
			
			return pMax.intValue() > qMax.intValue() ? pMax : qMax;
		}

		
	}
	
	private class AndContext extends BaseConstraintContext {

		@Override
		protected Integer maxCount(PropertyConstraint p, PropertyConstraint q) {

			if (p == null) {
				return q.getMaxCount();
			}
			Integer pMax = p.getMaxCount();
			Integer qMax = q.getMaxCount();
			if (qMax==null) {
				return pMax;
			}
			if (pMax == null) {
				return qMax;
			}
			return pMax.intValue() > qMax.intValue() ? qMax : pMax;
		}

		@Override
		protected Integer minCount(PropertyConstraint p, PropertyConstraint q) {

			if (p==null) {
				return q.getMinCount();
			}
			Integer pMin = p.getMinCount();
			Integer qMin = q.getMinCount();
			
			if (pMin == null) {
				pMin = 0;
			}
			if (qMin == null) {
				qMin = 0;
			}
			
			return pMin.intValue() > qMin.intValue() ? pMin : qMin;
		}

	
		
	}
	
}
