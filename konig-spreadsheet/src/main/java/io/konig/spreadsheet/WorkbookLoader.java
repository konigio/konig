package io.konig.spreadsheet;

/*
 * #%L
 * Konig Spreadsheet
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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.annotation.XmlSchema;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Hyperlink;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.openrdf.model.Literal;
import org.openrdf.model.Namespace;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.DCTERMS;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.konig.activity.Activity;
import io.konig.core.Edge;
import io.konig.core.Graph;
import io.konig.core.KonigException;
import io.konig.core.LocalNameService;
import io.konig.core.NameMap;
import io.konig.core.NamespaceManager;
import io.konig.core.OwlReasoner;
import io.konig.core.Path;
import io.konig.core.PathFactory;
import io.konig.core.SPARQLBuilder;
import io.konig.core.Term;
import io.konig.core.Term.Kind;
import io.konig.core.Vertex;
import io.konig.core.impl.BasicContext;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.RdfUtil;
import io.konig.core.impl.SimpleLocalNameService;
import io.konig.core.path.DataInjector;
import io.konig.core.path.NamespaceMapAdapter;
import io.konig.core.path.OutStep;
import io.konig.core.path.Step;
import io.konig.core.pojo.BeanUtil;
import io.konig.core.util.IriTemplate;
import io.konig.core.util.SimpleValueFormat;
import io.konig.core.util.StringUtil;
import io.konig.core.util.ValueFormat.Element;
import io.konig.core.vocab.AS;
import io.konig.core.vocab.AWS;
import io.konig.core.vocab.DC;
import io.konig.core.vocab.GCP;
import io.konig.core.vocab.Konig;
import io.konig.core.vocab.PROV;
import io.konig.core.vocab.SH;
import io.konig.core.vocab.Schema;
import io.konig.core.vocab.VANN;
import io.konig.core.vocab.XOWL;
import io.konig.core.vocab.XSD;
import io.konig.formula.Direction;
import io.konig.formula.DirectionStep;
import io.konig.formula.FormulaParser;
import io.konig.formula.PathExpression;
import io.konig.formula.PathStep;
import io.konig.formula.PrimaryExpression;
import io.konig.formula.QuantifiedExpression;
import io.konig.formula.ShapePropertyOracle;
import io.konig.rio.turtle.NamespaceMap;
import io.konig.shacl.CompositeShapeVisitor;
import io.konig.shacl.FormulaContextBuilder;
import io.konig.shacl.PredicatePath;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.PropertyPath;
import io.konig.shacl.PropertyPathUtil;
import io.konig.shacl.SequencePath;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeManager;
import io.konig.shacl.ShapeVisitor;
import io.konig.shacl.impl.MemoryShapeManager;
import io.konig.shacl.io.ShapeLoader;
import io.konig.shacl.io.ShapeWriter;
import io.konig.shacl.services.ShapeProducer;

/**
 * A utility that loads the contents of a workbook into a Graph.
 * 
 * @author Greg McFall
 *
 */
public class WorkbookLoader {
	private static final Logger logger = LoggerFactory.getLogger(WorkbookLoader.class);
	private static final int UNDEFINED = -1;
	private static final String ONTOLOGY_NAME = "Ontology Name";
	private static final String COMMENT = "Comment";
	private static final String NAMESPACE_URI = "Namespace URI";
	private static final String PREFIX = "Prefix";
	private static final String IMPORTS = "Imports";

	private static final String CLASS_NAME = "Class Name";
	private static final String CLASS_ID = "Class Id";
	private static final String CLASS_SUBCLASS_OF = "Subclass Of";

	private static final String PROPERTY_NAME = "Property Name";
	private static final String PROPERTY_ID = "Property Id";
	private static final String DOMAIN = "Domain";
	private static final String RANGE = "Range";
	private static final String INVERSE_OF = "Inverse Of";
	private static final String PROPERTY_TYPE = "Property Type";
	private static final String SUBPROPERTY_OF = "Subproperty Of";
	

	private static final String INDIVIDUAL_NAME = "Individual Name";
	private static final String INDIVIDUAL_ID = "Individual Id";
	private static final String INDIVIDUAL_TYPE = "Individual Type";
	private static final String INDIVIDUAL_CODE_VALUE = "Code Value";

	private static final String SHAPE_ID = "Shape Id";
	private static final String TARGET_CLASS = "Target Class";
	private static final String SCOPE_CLASS = "Scope Class";
	private static final String MEDIA_TYPE = "Media Type";
    private static final String SHAPE_TYPE = "Shape Type";
	private static final String AGGREGATION_OF = "Aggregation Of";
	private static final String ROLL_UP_BY = "Roll-up By";
	private static final String BIGQUERY_TABLE = "BigQuery Table";
	private static final String DATASOURCE = "Datasource";
	private static final String IRI_TEMPLATE = "IRI Template";
	private static final String DEFAULT_FOR = "Default For";
	private static final String TERM_STATUS = "Term Status";
	private static final String TABULAR_ORIGIN_SHAPE = "Tabular Origin Shape";

	private static final String SETTING_NAME = "Setting Name";
	private static final String SETTING_VALUE = "Setting Value";
	private static final String PATTERN = "Pattern";
	private static final String REPLACEMENT = "Replacement";
	
	private static final String PROPERTY_PATH = "Property Path";
	private static final String VALUE_TYPE = "Value Type";
	private static final String MIN_COUNT = "Min Count";
	private static final String MAX_COUNT = "Max Count";
	private static final String UNIQUE_LANG = "Unique Lang";
	private static final String VALUE_CLASS = "Value Class";
	private static final String STEREOTYPE = "Stereotype";
	private static final String VALUE_IN = "Value In";
	private static final String EQUALS = "Equals";
	private static final String EQUIVALENT_PATH = "Equivalent Path";
	private static final String SOURCE_PATH = "Source Path";
	private static final String PARTITION_OF = "Partition Of";
	private static final String FORMULA = "Formula";
	private static final String MIN_INCLUSIVE = "Min Inclusive";
	private static final String MAX_INCLUSIVE = "Max Inclusive";
	private static final String MIN_EXCLUSIVE = "Min Exclusive";
	private static final String MAX_EXCLUSIVE = "Max Exclusive";
	private static final String MIN_LENGTH = "Min Length";
	private static final String MAX_LENGTH = "Max Length";
	private static final String DECIMAL_PRECISION = "Decimal Precision";
	private static final String DECIMAL_SCALE = "Decimal Scale";
	private static final String SECURITY_CLASSIFICATION ="Security Classification";

	// Cloud SQL Instance
	private static final String INSTANCE_NAME = "Instance Name";
	private static final String INSTANCE_TYPE = "Instance Type";
	private static final String BACKEND_TYPE = "Backend Type";
	private static final String REGION = "Region";
	private static final String VERSION = "Database Version";
	private static final String TIER = "Tier";
    private static final String SHAPE_OF = "Input Of";
    private static final String ONE_OF = "One Of";

	//Cloud Formation Templates
	private static final String STACK_NAME="Stack name";
	private static final String AWS_REGION="AWS Region";
	private static final String CLOUD_FORMATION_TEMPLATES="Cloud Formation Templates";
	
	//Data Dictionary Template
	private static final String DATA_DICTIONARY_TEMPLATE="Template";
	private static final String SOURCE_SYSTEM = "Source System";
	private static final String SOURCE_OBJECT_NAME = "Source Object Name";
	private static final String FIELD = "Field";
	private static final String DATA_TYPE = "Data Type";
	private static final String CONSTRAINTS = "Constraints";
	private static final String BUSINESS_NAME = "Business Name";
	private static final String BUSINESS_DEFINITION = "Business Definition (Optional)";
	private static final String DATA_STEWARD = "Data Steward";
	private static final String TARGET_OBJECT_NAME = "Target Object Name\n(Flatfile or Aurora Tables)";
	private static final String TARGET_FIELD_NAME = "Target field Name";

	
	/** Security Tags **/
	private static final String SECURITY_TAGS = "Security Tags";
	private static final String AMAZON_RESOURCE_NAME = "Amazon Resource Name";
	private static final String TAG_KEY = "Tag Key";
	private static final String TAG_VALUE = "Tag Value";
	private static final String ENVIRONMENT = "Environment";
	
	private static final String ENUMERATION_DATASOURCE_TEMPLATE = "enumerationDatasourceTemplate";
	private static final String ENUMERATION_SHAPE_ID = "enumerationShapeId";

	private static final String SUBJECT = "Subject";
	private static final String LABEL = "Label";
	private static final String LANGUAGE = "Language";

	private static final String UNBOUNDED = "unbounded";
	
	private static final String AWS_DB_CLUSTER_NAME = "DB Cluster Name";
	private static final String AWS_DB_CLUSTER_ENGINE = "Engine";
	private static final String AWS_DB_CLUSTER_ENGINE_VERSION = "Engine Version";
	private static final String AWS_DB_CLUSTER_INSTANCE_CLASS = "Instance Class";
	private static final String AWS_DB_CLUSTER_AVAILABILITY_ZONE = "Availability Zone";
	private static final String AWS_DB_CLUSTER_BACKUP_PERIOD = "Backup Retention Period (days)";
	private static final String AWS_DB_CLUSTER_DATABASE_NAME = "Database Name";
	private static final String AWS_DB_CLUSTER_DB_SUBNET = "DB Subnet Group Name";
	private static final String AWS_DB_CLUSTER_BACKUP_WINDOW = "Preferred Backup Window";
	private static final String AWS_DB_CLUSTER_MAINTENANCE_WINDOW = "Preferred Maintenance Window";
	private static final String AWS_DB_CLUSTER_REPLICATION_SOURCE = "Replication Source Identifier";
	private static final String AWS_DB_CLUSTER_STORAGE_ENCRYPTED = "Storage Encrypted";
	
	private static final int COL_NAMESPACE_URI = 0x1;
	private static final int COL_CLASS_ID = 0x2;
	private static final int COL_PROPERTY_PATH = 0x4;
	private static final int COL_PROPERTY_ID = 0x4;
	private static final int COL_INDIVIDUAL_ID = 0x8;
	private static final int COL_SHAPE_ID = 0x10;
	private static final int COL_SETTING_NAME = 0x20;
	private static final int COL_INSTANCE_NAME = 0x40;
	private static final int COL_LABEL = 0x80;
	private static final int COL_AMAZON_DB_CLUSTER = 0x100;
	private static final int COL_CLOUD_FORMATION_TEMPLATE = 0x200;
	private static final int COL_SECURITY_TAGS = 0x400;
	private static final int COL_SOURCE_SYSTEM = 0x800;
	
	private static final int SHEET_ONTOLOGY = COL_NAMESPACE_URI;
	private static final int SHEET_CLASS = COL_CLASS_ID;
	private static final int SHEET_PROPERTY = COL_PROPERTY_ID;
	private static final int SHEET_INDIVIDUAL = COL_INDIVIDUAL_ID;
	private static final int SHEET_SHAPE = COL_SHAPE_ID;
	private static final int SHEET_PROPERTY_CONSTRAINT = COL_SHAPE_ID | COL_PROPERTY_PATH;
	private static final int SHEET_SETTING = COL_SETTING_NAME;
	private static final int SHEET_DB_INSTANCE = COL_INSTANCE_NAME;
	private static final int SHEET_LABEL = COL_LABEL;
	private static final int SHEET_AMAZON_RDS_CLUSTER = COL_AMAZON_DB_CLUSTER;
	private static final int SHEET_CLOUD_FORMATION_TEMPLATE = COL_CLOUD_FORMATION_TEMPLATE;
	private static final int SHEET_SECURITY_TAGS = COL_SECURITY_TAGS;
	private static final int SHEET_DATA_DICTIONARY_TEMPLATE=COL_SOURCE_SYSTEM;

	private static final String USE_DEFAULT_NAME = "useDefaultName";

	private static final String[] CELL_TYPE = new String[6];
	static {
		CELL_TYPE[Cell.CELL_TYPE_BLANK] = "Blank";
		CELL_TYPE[Cell.CELL_TYPE_BOOLEAN] = "Boolean";
		CELL_TYPE[Cell.CELL_TYPE_ERROR] = "Error";
		CELL_TYPE[Cell.CELL_TYPE_FORMULA] = "Formula";
		CELL_TYPE[Cell.CELL_TYPE_NUMERIC] = "Number";
		CELL_TYPE[Cell.CELL_TYPE_STRING] = "String";
	}

	private NamespaceManager nsManager;
	private ShapeManager shapeManager;
	private Graph graph;
	private ValueFactory vf = new ValueFactoryImpl();
	private IdMapper datasetMapper;
	private Set<String> defaultNamespace = new HashSet<>();

	private DataSourceGenerator dataSourceGenerator;
	private ShapePropertyOracle propertyOracle = new ShapePropertyOracle();
	private SimpleLocalNameService localNameService;

	private boolean inferRdfPropertyDefinitions;
	private boolean failOnWarnings = true;
	private boolean failOnErrors = true;
	private int errorCount = 0;


	public WorkbookLoader(NamespaceManager nsManager) {
		this(nsManager, new File("target/WorkbookLoader"));
	}
	
	public WorkbookLoader(NamespaceManager nsManager, File templateDir) {

		defaultNamespace.add(Schema.NAMESPACE);
		defaultNamespace.add(Konig.NAMESPACE);
		nsManager.add("vann", "http://purl.org/vocab/vann/");
		nsManager.add("owl", OWL.NAMESPACE);
		nsManager.add("sh", SH.NAMESPACE);
		nsManager.add("rdf", RDF.NAMESPACE);
		nsManager.add("rdfs", RDFS.NAMESPACE);
		nsManager.add("konig", Konig.NAMESPACE);
		nsManager.add("xsd", XMLSchema.NAMESPACE);
		nsManager.add("schema", Schema.NAMESPACE);
		nsManager.add("dc", DC.NAMESPACE);
		nsManager.add("prov", PROV.NAMESPACE);
		nsManager.add("as", AS.NAMESPACE);
		nsManager.add("gcp", GCP.NAMESPACE);
		nsManager.add("aws",AWS.NAMESPACE);
		this.nsManager = nsManager;

		try {

			Properties properties = new Properties();
			properties.load(getClass().getClassLoader().getResourceAsStream("WorkbookLoader/settings.properties"));

			dataSourceGenerator = new DataSourceGenerator(nsManager, templateDir, properties);

		} catch (IOException e) {
			throw new KonigException("Failed to create DataSourceGenerator", e);
		}
	}
	

	public boolean isFailOnErrors() {
		return failOnErrors;
	}

	public void setFailOnErrors(boolean failOnErrors) {
		this.failOnErrors = failOnErrors;
	}

	public int getErrorCount() {
		return errorCount;
	}

	public boolean isFailOnWarnings() {
		return failOnWarnings;
	}

	public void setFailOnWarnings(boolean failOnWarnings) {
		this.failOnWarnings = failOnWarnings;
	}

	public NamespaceManager getNamespaceManager() {
		return nsManager;
	}

	public Graph getGraph() {
		return graph;
	}

	public IdMapper getDatasetMapper() {
		return datasetMapper;
	}

	public DataSourceGenerator getDataSourceGenerator() throws IOException {

		return dataSourceGenerator;

	}

	public boolean isInferRdfPropertyDefinitions() {
		return inferRdfPropertyDefinitions;
	}

	public void setInferRdfPropertyDefinitions(boolean inferRdfPropertyDefinitions) {
		this.inferRdfPropertyDefinitions = inferRdfPropertyDefinitions;
	}

	public void setDatasetMapper(IdMapper datasetMapper) {
		this.datasetMapper = datasetMapper;
	}

	public void load(Workbook book, Graph graph) throws SpreadsheetException {
		this.graph = graph;
		Worker worker = new Worker(book);
		worker.run();
	}

	public ShapeManager getShapeManager() {
		return shapeManager;
	}

	private class Worker {
		private Workbook book;
		private PathFactory pathFactory;
		private OwlReasoner owlReasoner;
		private DataInjector dataInjector;
		private DataFormatter dataFormatter;
		private Graph defaultOntologies;

		private Properties settings = new Properties();
		private List<ShapeTemplate> shapeTemplateList = new ArrayList<>();
		private List<ImportInfo> importList = new ArrayList<>();
		private Map<URI, List<Function>> dataSourceMap = new HashMap<>();
		private List<String> warningList = new ArrayList<>();
		private List<PathHandler> pathHandlers = new ArrayList<>();
		private List<FormulaHandler> formulaHandlers = new ArrayList<>();

		private Set<String> errorMessages = new HashSet<>();
		private int ontologyNameCol = UNDEFINED;
		private int ontologyCommentCol = UNDEFINED;
		private int namespaceUriCol = UNDEFINED;
		private int prefixCol = UNDEFINED;
		private int importsCol = UNDEFINED;

		private int classNameCol = UNDEFINED;
		private int classCommentCol = UNDEFINED;
		private int classIdCol = UNDEFINED;
		private int classSubclassOfCol = UNDEFINED;

		private int propertyNameCol = UNDEFINED;
		private int propertyIdCol = UNDEFINED;
		private int domainCol = UNDEFINED;
		private int rangeCol = UNDEFINED;
		private int inverseOfCol = UNDEFINED;
		private int propertyTypeCol = UNDEFINED;
		private int subpropertyOfCol = UNDEFINED;
		private int propertyCommentCol = UNDEFINED;
		private int securityClassificationCol = UNDEFINED;

		private int individualNameCol = UNDEFINED;
		private int individualCommentCol = UNDEFINED;
		private int individualIdCol = UNDEFINED;
		private int individualTypeCol = UNDEFINED;
		private int individualCodeValueCol = UNDEFINED;

		private int shapeIdCol = UNDEFINED;
		private int shapeCommentCol = UNDEFINED;
		private int shapeTargetClassCol = UNDEFINED;
		private int shapeAggregationOfCol = UNDEFINED;
		private int shapeRollUpByCol = UNDEFINED;
		private int shapeMediaTypeCol = UNDEFINED;
        private int shapeTypeCol = UNDEFINED;
		private int shapeBigQueryTableCol = UNDEFINED;
		private int shapeDatasourceCol = UNDEFINED;
		private int shapeIriTemplateCol = UNDEFINED;
		private int defaultShapeForCol = UNDEFINED;
		private int tabularOriginShapeCol = UNDEFINED;

		private int pcShapeIdCol = UNDEFINED;
		private int pcPropertyIdCol = UNDEFINED;
		private int pcValueTypeCol = UNDEFINED;
		private int pcMinCountCol = UNDEFINED;
		private int pcMaxCountCol = UNDEFINED;
		private int pcUniqueLangCol = UNDEFINED;
		private int pcValueClassCol = UNDEFINED;
		private int pcValueInCol = UNDEFINED;
		private int pcCommentCol = UNDEFINED;
		private int pcStereotypeCol = UNDEFINED;
		private int pcEquivalentPathCol = UNDEFINED;
		private int pcEqualsCol = UNDEFINED;
		private int pcSourcePathCol = UNDEFINED;
		private int pcPartitionOfCol = UNDEFINED;
		private int pcFormulaCol = UNDEFINED;
		private int pcMinInclusive = UNDEFINED;
		private int pcMaxInclusive = UNDEFINED;
		private int pcMinExclusive = UNDEFINED;
		private int pcMaxExclusive = UNDEFINED;
		private int pcMinLength = UNDEFINED;
		private int pcMaxLength = UNDEFINED;
		private int pcDecimalPrecision = UNDEFINED;
		private int pcDecimalScale = UNDEFINED;
		private int pcSecurityClassification = UNDEFINED;

		private int settingNameCol = UNDEFINED;
		private int settingValueCol = UNDEFINED;
		private int settingPatternCol = UNDEFINED;
		private int settingReplacementCol = UNDEFINED;

		private int subjectCol = UNDEFINED;
		private int labelCol = UNDEFINED;
		private int languageCol = UNDEFINED;

		private URI activityId;
		private int pcTermStatusCol = UNDEFINED;
		private int classTermStatusCol = UNDEFINED;
		private int propertyTermStatusCol = UNDEFINED;

		private int gcpInstanceNameCol = UNDEFINED;
		private int gcpInstanceTypeCol = UNDEFINED;
		private int gcpBackendTypeCol = UNDEFINED;
		private int gcpRegionCol = UNDEFINED;
		private int gcpVersionCol = UNDEFINED;
		private int gcpTierCol = UNDEFINED;
		
		private int stackNameCol = UNDEFINED;
		private int awsRegionCol = UNDEFINED;
		private int cloudFormationTemplateCol = UNDEFINED;
		
		private int sourceSystemCol = UNDEFINED;
		private int sourceObjectNameCol = UNDEFINED;
		private int fieldCol = UNDEFINED;
		private int dataTypeCol = UNDEFINED;
		private int maxLengthCol = UNDEFINED;
		private int decimalPrecisionCol = UNDEFINED;
		private int decimalScaleCol = UNDEFINED;
		private int constraintsCol = UNDEFINED;
		private int businessNameCol = UNDEFINED;
		private int businessDefinitionCol = UNDEFINED;
		private int dataStewardCol = UNDEFINED;
		private int securityClassifCol = UNDEFINED;
		private int targetObjectNameCol = UNDEFINED;
		private int targetFieldNameCol = UNDEFINED;
		
		private int amazonResourceName = UNDEFINED;
		private int tagKey = UNDEFINED;
		private int tagValue = UNDEFINED;
		private int environment = UNDEFINED;
		
		private int awsDbClusterName = UNDEFINED;
		private int awsEngine = UNDEFINED;
		private int awsEngineVersion = UNDEFINED;
		private int awsInstanceClass = UNDEFINED;
		private int awsAvailabilityZone = UNDEFINED;
		private int awsBackupRetentionPeriod = UNDEFINED;
		private int awsDatabaseName = UNDEFINED;
		private int awsDbSubnetGroupName = UNDEFINED;
		private int awsPreferredBackupWindow = UNDEFINED;
		private int awsPreferredMaintenanceWindow = UNDEFINED;
		private int awsReplicationSourceIdentifier = UNDEFINED;
		private int awsStorageEncrypted	 = UNDEFINED;
		private int inputShapeOfCol = UNDEFINED;
		private int oneOfCol = UNDEFINED;
		
		public Worker(Workbook book) {
			this.book = book;
			if (shapeManager == null) {
				shapeManager = new MemoryShapeManager();
			}
			activityId = Activity.nextActivityId();

			owlReasoner = new OwlReasoner(graph);
			dataInjector = new DataInjector();
			dataFormatter = new DataFormatter(true);

		}

		private boolean useDefaultName() {
			return "true".equalsIgnoreCase(settings.getProperty(USE_DEFAULT_NAME, "true"));
		}

		private List<SheetInfo> collectSheetInfo() {
			List<SheetInfo> list = new ArrayList<>();
			for (int i = 0; i < book.getNumberOfSheets(); i++) {
				Sheet sheet = book.getSheetAt(i);

				int sheetType = sheetType(sheet);
				list.add(new SheetInfo(sheetType, i));
			}
			return list;
		}

		private void run() throws SpreadsheetException {

			try {
				List<SheetInfo> list = collectSheetInfo();
				Collections.sort(list);

				for (SheetInfo info : list) {
					loadSheet(info);
				}
				handlePaths();
				// buildRollUpShapes();
				loadIndividualProperties();
				emitProvenance();
				inferPropertyDefinitions();
				loadShapes();
				handleFormulas();
				produceEnumShapes();
				processShapeTemplates();
				processDataSources();
				visitShapes();
				processImportStatements();
				declareDefaultOntologies();
				handleWarnings();
			} catch (Throwable e) {
				logError("Failed to process workbook", e);
				
			}
		}

		private void handleFormulas() throws SpreadsheetException {

			localNameService = new SimpleLocalNameService();
			localNameService.addAll(getGraph());
			localNameService.add(Konig.Day);
			localNameService.add(Konig.Week);
			localNameService.add(Konig.Month);
			localNameService.add(Konig.Year);

			try {
				for (FormulaHandler handler : formulaHandlers) {
					handler.execute();
				}
			} catch (Throwable oops) {
				throw new SpreadsheetException(oops);
			}

		}

		private void handlePaths() throws SpreadsheetException {
			NameMap nameMap = new NameMap(graph);
			nameMap.addStaticFields(Konig.class);
			PathFactory pathFactory = new PathFactory(nsManager, nameMap);
			for (PathHandler handler : pathHandlers) {
				try {
					handler.execute(graph, pathFactory);
				} catch (Throwable e) {
					error(e);
				}
			}
		}

		private void handleWarnings() throws WarningSpreadsheetException {
			if (!warningList.isEmpty()) {
				if (failOnWarnings) {
					throw new WarningSpreadsheetException(warningList);
				} else {
					for (String text : warningList) {
						logger.warn(text);
					}
				}
			}

		}

		private void processDataSources() throws SpreadsheetException {

			for (Entry<URI, List<Function>> entry : dataSourceMap.entrySet()) {
				URI shapeId = entry.getKey();
				List<Function> dataSourceList = entry.getValue();

				try {
					DataSourceGenerator generator = getDataSourceGenerator();

					Shape shape = shapeManager.getShapeById(shapeId);
					if (shape == null) {
						throw new SpreadsheetException("Shape not found: " + shapeId);
					}
					for (Function func : dataSourceList) {
						generator.generate(shape, func, graph);
					}
					generator.close();

				} catch (IOException e) {
					logError("Failed to create DataSourceGenerator...", e);
				}
			}

		}

		private void declareDefaultOntologies() throws SpreadsheetException {

			Set<String> namespaceSet = new HashSet<>();
			List<Edge> list = new ArrayList<>(graph);

			for (Edge e : list) {
				declareDefaultOntology(namespaceSet, e.getSubject());
				declareDefaultOntology(namespaceSet, e.getPredicate());
				declareDefaultOntology(namespaceSet, e.getObject());
			}

		}

		private void declareDefaultOntology(Set<String> namespaceSet, Value value) throws SpreadsheetException {
			if (value instanceof URI) {
				URI uri = (URI) value;
				String name = uri.getNamespace();
				if (!namespaceSet.contains(name)) {
					namespaceSet.add(name);
					URI ontologyId = new URIImpl(name);
					Vertex v = graph.getVertex(ontologyId);
					if (v == null) {
						Graph defaultOntologyGraph = getDefaultOntologyGraph();
						v = defaultOntologyGraph.getVertex(ontologyId);
						if (v != null) {
							graph.addAll(v.outEdgeSet());
						}
					}
				}
			}

		}

		private Graph getDefaultOntologyGraph() throws SpreadsheetException {
			if (defaultOntologies == null) {
				defaultOntologies = new MemoryGraph();
				InputStream input = getClass().getClassLoader()
						.getResourceAsStream("WorkbookLoader/defaultOntologies.ttl");
				try {
					RdfUtil.loadTurtle(defaultOntologies, input, "");
				} catch (RDFParseException | RDFHandlerException | IOException e) {
					if (failOnErrors) {
						throw new SpreadsheetException("Failed to load defaultOntologies.ttl");
					} else {
						logError("Failed to load defaultOntologies.ttl");
					}
				}
			}
			return defaultOntologies;
		}

		private void logError(String message, Throwable e) {
			logger.error(message, e);
			String msg = (e.getMessage());
			if (!errorMessages.contains(msg)) {
				errorMessages.add(msg);
				errorCount++;
			}
			if (failOnErrors) {
				if (e instanceof RuntimeException) {
					throw (RuntimeException) e;
				}
				
				throw new RuntimeException(e);
			}

		}

		private void logError(String msg) {
			if (!errorMessages.contains(msg)) {
				errorMessages.add(msg);
				logger.error(msg);
				errorCount++;
			}
		}

		private void processImportStatements() throws SpreadsheetException {
			try {
				for (ImportInfo info : importList) {
					URI subject = info.getTargetOntology();

					for (String prefix : info.getImportList()) {

						if (prefix.equals("*")) {
							if (info.getImportList().size() > 1) {
								throw new SpreadsheetException(
										"Cannot combine wildcard with namespace prefix when declaring imports for ontology: "
												+ subject.stringValue());
							}
							List<Vertex> objectList = graph.v(OWL.ONTOLOGY).in(RDF.TYPE).toVertexList();
							for (Vertex v : objectList) {
								if (!subject.equals(v.getId()) && v.getId() instanceof URI) {
									graph.edge(subject, OWL.IMPORTS, v.getId());
								}
							}
						} else {
							Namespace ns = nsManager.findByPrefix(prefix);
							if (ns == null) {
								throw new SpreadsheetException("Cannot import into <" + subject.stringValue()
										+ ">.  Namespace not found for prefix: " + prefix);
							}
							URI object = vf.createURI(ns.getName());
							graph.edge(subject, OWL.IMPORTS, object);
						}
					}
				}
			} catch (Throwable e) {
				error(e);
			}

		}

		private void processShapeTemplates() throws SpreadsheetException {

			for (ShapeTemplate s : shapeTemplateList) {
				try {
					processShapeTemplate(s);
				} catch (Throwable e) {
					error(e);
				}
			}

		}

		private void processShapeTemplate(ShapeTemplate s) throws SpreadsheetException {

			Shape shape = shapeManager.getShapeById(s.shapeId);
			if (shape == null) {
				throw new SpreadsheetException("Shape not found: " + s.shapeId);
			}

			IriTemplate template = s.createTemplate(shape, nsManager);
			shape.setIriTemplate(template);
			graph.edge(s.shapeId, Konig.iriTemplate, literal(template.toString()));

			URI targetClass = shape.getTargetClass();
			if (targetClass != null && owlReasoner.isSubClassOf(targetClass, Schema.Enumeration)) {

				graph.edge(s.shapeId, Konig.idFormat, Konig.LocalName);
			} else {

				List<? extends Element> list = template.toList();
				if (list.size() == 2) {
					Element namespace = list.get(0);
					Namespace ns = nsManager.findByPrefix(namespace.getText());
					if (ns != null) {
						graph.edge(s.shapeId, Konig.idFormat, Konig.Curie);
					}
				}
			}

		}

		private void loadShapes() {
			ShapeLoader loader = new ShapeLoader(getShapeManager());
			loader.load(graph);
		}

		private void visitShapes() {

			NameMap nameMap = new NameMap();
			nameMap.addAll(graph);
			nameMap.addStaticFields(Konig.class);

			CompositeShapeVisitor visitor = new CompositeShapeVisitor(
					new FormulaContextBuilder(nsManager, nameMap, graph), new TargetClassReasoner(graph));

			for (Shape shape : getShapeManager().listShapes()) {
				visitor.visit(shape);
			}

		}

		private void produceEnumShapes() throws SpreadsheetException {

			try {

				IriTemplate shapeIdTemplate = enumShapeIdTemplate();

				if (shapeIdTemplate != null) {

					List<String> dataSourceTemplates = enumDatasourceTemplates();

					ShapeProducer producer = new ShapeProducer(nsManager, getShapeManager());

					producer.setVisitor(new EnumShapeVistor(new ShapeWriter(), getShapeManager(), graph));

					EnumShapeGenerator generator = new EnumShapeGenerator(producer, getDataSourceGenerator());
					generator.generateShapes(graph, shapeIdTemplate, dataSourceTemplates);
				}

			} catch (IOException e) {
				if (failOnErrors) {
					throw new SpreadsheetException("Failed to produce Enumeration shapes", e);
				} else {
					logError("Failed to produce Enumeration shapes...");
					logError(e.getMessage());
				}
			}

		}

		private List<String> enumDatasourceTemplates() {
			List<String> result = null;
			String text = settings.getProperty(ENUMERATION_DATASOURCE_TEMPLATE);
			if (text != null) {
				result = new ArrayList<>();
				StringTokenizer tokenizer = new StringTokenizer(text, " \t\r\n");
				while (tokenizer.hasMoreTokens()) {
					String token = tokenizer.nextToken();
					result.add(token);
				}
			}
			return result;
		}

		private IriTemplate enumShapeIdTemplate() {

			String templateText = settings.getProperty(ENUMERATION_SHAPE_ID);
			return templateText == null ? null : new IriTemplate(templateText);
		}

		private void inferPropertyDefinitions() {
			if (inferRdfPropertyDefinitions) {
				OwlReasoner reasoner = new OwlReasoner(graph);
				reasoner.inferRdfPropertiesFromPropertyConstraints(graph);
			}
		}

		private void loadIndividualProperties() throws SpreadsheetException {
			NameMap nameMap = new NameMap(graph);
			nameMap.addStaticFields(Konig.class);
			pathFactory = new PathFactory(nsManager, nameMap);
			for (int i = 0; i < book.getNumberOfSheets(); i++) {
				Sheet sheet = book.getSheetAt(i);
				int sheetType = sheetType(sheet);
				if (sheetType == COL_INDIVIDUAL_ID) {
					try {
						loadIndividualProperties(sheet);
					} catch (Throwable e) {
						error(e);
					}
				}
			}

		}

		private void loadIndividualProperties(Sheet sheet) throws SpreadsheetException {

			Row header = readIndividualHeader(sheet);
			int rowSize = sheet.getLastRowNum() + 1;

			List<PathInfo> pathInfo = loadPathInfo(header);
			Collections.sort(pathInfo, new Comparator<PathInfo>() {

				@Override
				public int compare(PathInfo a, PathInfo b) {
					return a.pathString.compareTo(b.pathString);
				}
			});

			for (int i = sheet.getFirstRowNum() + 1; i < rowSize; i++) {
				Row row = sheet.getRow(i);
				loadIndividualPropertiesRow(pathInfo, row);
			}

		}

		private List<PathInfo> loadPathInfo(Row header) throws SpreadsheetException {

			List<PathInfo> list = new ArrayList<>();

			int colSize = header.getLastCellNum() + 1;

			for (int i = header.getFirstCellNum(); i < colSize; i++) {
				Cell cell = header.getCell(i);
				if (cell == null) {
					continue;
				}

				String headerValue = cell.getStringCellValue();
				if (headerValue != null && headerValue.startsWith("/")) {

					Path path = pathFactory.createPath(headerValue);

					list.add(new PathInfo(i, path));
				}

			}

			return list;
		}

		private void loadIndividualPropertiesRow(List<PathInfo> pathList, Row row) throws SpreadsheetException {

			URI individualId = uriValue(row, individualIdCol);
			if (individualId != null) {

				for (PathInfo pathInfo : pathList) {

					Value value = getValue(row, pathInfo);
					if (value != null) {

						assignValue(individualId, pathInfo, value);
					}

				}
			}

		}

		private void assignValue(URI individualId, PathInfo pathInfo, Value value) {
			Vertex subject = graph.vertex(individualId);
			dataInjector.inject(subject, pathInfo.path, value);

		}

		private Value getValue(Row row, PathInfo pathInfo) throws SpreadsheetException {

			if (pathInfo.datatype != null) {
				return literal(row, pathInfo.column, pathInfo.datatype);
			}

			return uriValue(row, pathInfo.column);
		}

		private Value literal(Row row, int col, URI datatype) throws SpreadsheetException {
			String text = stringValue(row, col);
			return text == null ? null : vf.createLiteral(text, datatype);
		}

		private void emitProvenance() {
			Value endTime = BeanUtil.toValue(GregorianCalendar.getInstance());
			graph.edge(activityId, RDF.TYPE, Konig.LoadModelFromSpreadsheet);
			graph.edge(activityId, AS.endTime, endTime);

		}

		private void loadSheet(SheetInfo info) throws SpreadsheetException {

			Sheet sheet = book.getSheetAt(info.sheetIndex);
			int bits = info.sheetType;
			
			switch (bits) {
			case SHEET_ONTOLOGY:
				loadOntologies(sheet);
				break;
			case SHEET_CLASS:
				loadClasses(sheet);
				break;
			case SHEET_PROPERTY:
				loadProperties(sheet);
				break;
			case SHEET_INDIVIDUAL:
				loadIndividuals(sheet);
				break;
			case SHEET_SHAPE:
				loadShapes(sheet);
				break;
			case SHEET_PROPERTY_CONSTRAINT:
				loadPropertyConstraints(sheet);
			case SHEET_SETTING:
				loadSettings(sheet);
				break;

			case SHEET_DB_INSTANCE:
				loadGoogleCloudSqlInstance(sheet);
				break;

			case SHEET_LABEL:
				loadLabels(sheet);
				break;
			case SHEET_AMAZON_RDS_CLUSTER:
				loadAmazonRDSCluster(sheet);
				break;
			case SHEET_CLOUD_FORMATION_TEMPLATE:
				loadCloudFormationTemplate(sheet);
				break;
			case SHEET_SECURITY_TAGS:
				loadSecurityTags(sheet);
				break;
			case SHEET_DATA_DICTIONARY_TEMPLATE:
				loadDataDictionary(sheet);
				break;
				
			}

		}

		private void loadDataDictionary(Sheet sheet) throws SpreadsheetException {
			readDataDictionaryTemplateHeader(sheet);

			int rowSize = sheet.getLastRowNum() + 1;
			for (int i = sheet.getFirstRowNum() + 1; i < rowSize; i++) {
				Row row = sheet.getRow(i);
				loadDataDictionaryTemplateRow(row);
			}
			
		}

		private void loadDataDictionaryTemplateRow(Row row) throws SpreadsheetException {
			String sourceSystemName = stringValue(row, sourceSystemCol);
			String shapeIdLocalName = stringValue(row, sourceObjectNameCol);
			if(shapeIdLocalName==null )
				return;
			URI shapeId = getShapeURL(sourceSystemName,shapeIdLocalName);
			if (shapeId == null)
				return;
			String propertyIdValue = stringValue(row, fieldCol);
			if (propertyIdValue == null) {
				logger.warn("Shape Id is defined but Property Id is not defined: {}", shapeId.getLocalName());
				return;
			}
			URI propertyId = expandPropertyId(getPropertyBaseURL()+propertyIdValue);

			logger.debug("loadPropertyConstraintRow({},{})", RdfUtil.localName(shapeId), RdfUtil.localName(propertyId));

			String dataDictionarydataType=stringValue(row,dataTypeCol);

			Literal maxLength = numericLiteral(row, maxLengthCol);
			Literal decimalPrecision = intLiteral(row, decimalPrecisionCol);
			if(decimalPrecision!=null && (decimalPrecision.intValue()<1 || decimalPrecision.intValue()>65)){
				throw new KonigException("Decimal Precison should be between 1 to 65");
			}
			Literal decimalScale = intLiteral(row, decimalScaleCol);
			if(decimalScale!=null && 
					(decimalScale.intValue()<0 || decimalScale.intValue()>30 || 
							decimalScale.intValue()>decimalPrecision.intValue())){
				throw new KonigException("Decimal Scale should be less than Decimal Precision and between 0-30");
			}
			Literal businessName = stringLiteral(row, businessNameCol);
			Literal businessDefinition = stringLiteral(row, businessDefinitionCol);
			Literal dataSteward = stringLiteral(row, dataStewardCol);
			URI securityClassification=uriValue(row, securityClassifCol);
			String constraints = stringValue(row,constraintsCol);
			
			Vertex prior = getPropertyConstraint(shapeId, propertyId);
			if (prior != null) {
				logger.warn("Duplicate definition of property '{}' on '{}'", propertyId.getLocalName(),
						shapeId.getLocalName());
			}
			Vertex constraintVertex = graph.vertex();
			Resource constraint = constraintVertex.getId();			

			edge(shapeId, RDF.TYPE, SH.Shape);
			edge(shapeId, RDF.TYPE, Konig.TabularNodeShape);			
			edge(shapeId, SH.property, constraint);

			edge(constraint, SH.path, propertyId);
			edge(constraint, SH.name, businessName);
			edge(constraint, RDFS.COMMENT, businessDefinition);
			if(constraints!=null && constraints.contains("Primary Key")){
				edge(constraint, Konig.stereotype, Konig.primaryKey);
				edge(constraint, SH.minCount, vf.createLiteral(1));
			}
			else if(constraints!=null && constraints.contains("NOT NULL")){
				edge(constraint, SH.minCount, vf.createLiteral(1));
			}
			else{
				edge(constraint, SH.minCount, vf.createLiteral(0));
			}

			URI rdfDatatype=getRdfDatatype(dataDictionarydataType,constraint);
			edge(constraint,SH.datatype,rdfDatatype);
			edge(constraint, SH.maxCount, vf.createLiteral(1));
			if(rdfDatatype!=null && (rdfDatatype.equals(XMLSchema.STRING)|| (rdfDatatype.equals(XMLSchema.BASE64BINARY)))){
				edge(constraint, SH.maxLength, maxLength);
			}
			if(rdfDatatype!=null && rdfDatatype.equals(XMLSchema.DECIMAL)){
				edge(constraint, Konig.decimalPrecision, decimalPrecision);
			}
			if(dataDictionarydataType!=null && "DECIMAL".equals(dataDictionarydataType)){
				edge(constraint, Konig.decimalScale, decimalScale);
			}
			edge(constraint, Konig.dataSteward, dataSteward);
			edge(constraint, Konig.qualifiedSecurityClassification, securityClassification);
			
		}

		private String getPropertyBaseURL() throws SpreadsheetException {
			String propertyBaseURL=settings.getProperty("propertyBaseURL");
			if(propertyBaseURL==null){
				propertyBaseURL=System.getProperty("propertyBaseURL");
			}
			if(propertyBaseURL == null){
				throw new SpreadsheetException("propertyBaseURL is not found in both settings tab and system property.");			
			}
			else{
				nsManager.add("alias",propertyBaseURL);
			}
			return propertyBaseURL;
		}

		private URI getShapeURL(String sourceSystemName, String shapeIdLocalName) throws SpreadsheetException {
			URI shapeURI=null;
			String shapeURLTemplate=settings.getProperty("shapeURLTemplate");
			if(shapeURLTemplate==null){
				shapeURLTemplate=System.getProperty("shapeURLTemplate");
			}
			if(shapeURLTemplate == null){
				throw new SpreadsheetException("shapeURLTemplate is not found in both settings tab and system property.");			
			}
			else{
				String shapeURL=sourceSystemName==null?shapeURLTemplate:shapeURLTemplate.replace("{SOURCE_SYSTEM}", StringUtil.SNAKE_CASE(sourceSystemName));
				shapeURL=shapeURL.replace("{SOURCE_OBJECT_NAME}", StringUtil.SNAKE_CASE(shapeIdLocalName));
				shapeURI=new URIImpl(shapeURL);
				nsManager.add("shape", shapeURI.getNamespace());
			}
			return shapeURI;
		}
		
		private URI getRdfDatatype(String dataDictionarydataType, Resource constraint) {
			switch(dataDictionarydataType){
				case "CHAR":
				case "VARCHAR":
				case "TEXT":
				case "VARCHAR2":
					return XMLSchema.STRING;
				case "DATE":
					return XMLSchema.DATE;
				case "DATETIME":
					return XMLSchema.DATETIME;
				case "NUMBER":
					return XMLSchema.DECIMAL;
				case "SMALLINT":
					edge(constraint, SH.minInclusive, vf.createLiteral("-32768",XMLSchema.INTEGER));
					edge(constraint, SH.maxExclusive, vf.createLiteral("32767",XMLSchema.INTEGER));
					return XMLSchema.INTEGER;
				case "UNSIGNED SMALLINT":
					edge(constraint, SH.minInclusive, vf.createLiteral("0",XMLSchema.INTEGER));
					edge(constraint, SH.maxExclusive, vf.createLiteral("65535",XMLSchema.INTEGER));
					return XMLSchema.INTEGER;
				case "INT":
					edge(constraint, SH.minInclusive, vf.createLiteral("-2147483648",XMLSchema.INTEGER));
					edge(constraint, SH.maxExclusive, vf.createLiteral("2147483647",XMLSchema.INTEGER));
					return XMLSchema.INTEGER;
				case "UNSIGNED INT":
					edge(constraint, SH.minInclusive, vf.createLiteral("0",XMLSchema.INTEGER));
					edge(constraint, SH.maxExclusive, vf.createLiteral("4294967295",XMLSchema.INTEGER));
					return XMLSchema.INTEGER;
				case "BIGINT":
					edge(constraint, SH.minInclusive, vf.createLiteral("-9223372036854775808",XMLSchema.INTEGER));
					edge(constraint, SH.maxExclusive, vf.createLiteral("9223372036854775807",XMLSchema.INTEGER));
					return XMLSchema.INTEGER;
				case "UNSIGNED BIGINT":
					edge(constraint, SH.minInclusive, vf.createLiteral("0",XMLSchema.INTEGER));
					edge(constraint, SH.maxExclusive, vf.createLiteral("18446744073709551615F",XMLSchema.INTEGER));
					return XMLSchema.INTEGER;
				case "FLOAT":
					return XMLSchema.FLOAT;
				case "DOUBLE":
					return XMLSchema.DOUBLE;
				case "DECIMAL":
					return XMLSchema.DECIMAL;
				case "BINARY":
					return XMLSchema.BASE64BINARY;
				case "BOOLEAN":
					return XMLSchema.BOOLEAN;
			}
			return null;
		}

		private void readDataDictionaryTemplateHeader(Sheet sheet) {
			sourceSystemCol = sourceObjectNameCol = fieldCol = dataTypeCol = maxLengthCol = decimalPrecisionCol = decimalScaleCol = constraintsCol = businessNameCol = businessDefinitionCol = dataStewardCol = securityClassifCol = targetObjectNameCol = targetFieldNameCol = UNDEFINED;

			int firstRow = sheet.getFirstRowNum();
			Row row = sheet.getRow(firstRow);

			int colSize = row.getLastCellNum() + 1;
			for (int i = row.getFirstCellNum(); i < colSize; i++) {
				Cell cell = row.getCell(i);
				if (cell == null) {
					continue;
				}
				String text = cell.getStringCellValue();
				if (text != null) {
					text = text.trim();

					switch (text) {

					case SOURCE_SYSTEM:
						sourceSystemCol = i;
						break;
					
					case SOURCE_OBJECT_NAME:
						sourceObjectNameCol = i;
						break;
						
					case FIELD:
						fieldCol = i;
						break;
					
					case DATA_TYPE:
						dataTypeCol = i;
						break;
					
					case MAX_LENGTH:
						maxLengthCol = i;
						break;
						
					case DECIMAL_PRECISION:
						decimalPrecisionCol = i;
						break;
					
					case DECIMAL_SCALE:
						decimalScaleCol = i;
						break;
						
					case CONSTRAINTS:
						constraintsCol = i;
						break;
					
					case BUSINESS_NAME:
						businessNameCol = i;
						break;
					
					case BUSINESS_DEFINITION:
						businessDefinitionCol = i;
						break;
						
					case DATA_STEWARD:
						dataStewardCol = i;
						break;
						
					case SECURITY_CLASSIFICATION:
						securityClassifCol =i;
						break;
						
					case TARGET_OBJECT_NAME:
						targetObjectNameCol =i;
						break;
						
					case TARGET_FIELD_NAME:
						targetFieldNameCol = i;
						break;
					
					}
				}
			}
			
		}

		private int sheetType(Sheet sheet) {
			int rowNum = sheet.getFirstRowNum();

			Row header = sheet.getRow(rowNum);
			if (header == null) {
				return 0;
			}

			int colSize = header.getLastCellNum() + 1;

			int bits = 0;

			for (int i = header.getFirstCellNum(); i < colSize; i++) {
				Cell cell = header.getCell(i);
				if (cell == null)
					continue;
				String name = cell.getStringCellValue();

				switch (name) {
				case NAMESPACE_URI:
					bits = bits | COL_NAMESPACE_URI;
					break;
				case CLASS_ID:
					bits = bits | COL_CLASS_ID;
					break;

				case PROPERTY_PATH:
				case PROPERTY_ID:
					bits = bits | COL_PROPERTY_PATH;
					break;
				case INDIVIDUAL_ID:
					bits = bits | COL_INDIVIDUAL_ID;
					break;
				case SHAPE_ID:
					bits = bits | COL_SHAPE_ID;
					break;
				case SETTING_NAME:
					bits = bits | COL_SETTING_NAME;
					break;
				case INSTANCE_NAME:
					bits = bits | COL_INSTANCE_NAME;
					break;

				case LABEL:
					bits = bits | COL_LABEL;
					break;
				case AWS_DB_CLUSTER_NAME:
					bits = bits | COL_AMAZON_DB_CLUSTER;
					break;
				case STACK_NAME:
					bits = bits | COL_CLOUD_FORMATION_TEMPLATE;
					break;
				case AMAZON_RESOURCE_NAME:
					bits = bits | COL_SECURITY_TAGS;
					break;
				case SOURCE_SYSTEM:
					bits = bits | COL_SOURCE_SYSTEM;
				}
			}

			return bits;
		}

		private void loadGoogleCloudSqlInstance(Sheet sheet) {
			readGoogleCloudSqlInstanceHeader(sheet);

			int rowSize = sheet.getLastRowNum() + 1;
			for (int i = sheet.getFirstRowNum() + 1; i < rowSize; i++) {
				Row row = sheet.getRow(i);
				loadGoogleCloudSqlInstanceRow(row);
			}
		}
		
		
		private void loadAmazonRDSCluster(Sheet sheet) {
			readAmazonRDSClusterHeader(sheet);
			
			int rowSize = sheet.getLastRowNum() + 1;
			for(int i = sheet.getFirstRowNum() + 1; i < rowSize; i++) {
				Row row = sheet.getRow(i);
				loadAmazonRDSClusterRow(row);
			}
		}
		private void loadCloudFormationTemplate(Sheet sheet) throws SpreadsheetException{
			readCloudFormationTemplateHeader(sheet);

			int rowSize = sheet.getLastRowNum() + 1;
			for (int i = sheet.getFirstRowNum() + 1; i < rowSize; i++) {
				Row row = sheet.getRow(i);
				loadCloudFormationTemplateRow(row);
			}
		}
		
		private void loadSecurityTags(Sheet sheet) throws SpreadsheetException {
			readSecurityTagsHeader(sheet);
			int rowSize = sheet.getLastRowNum() + 1;
			for (int i = sheet.getFirstRowNum() + 1; i < rowSize; i++) {
				Row row = sheet.getRow(i);
				loadSecurityTagsRow(row);
			}
		}
		
		private void loadSettings(Sheet sheet) throws SpreadsheetException {
			readSettingHeader(sheet);
			int rowSize = sheet.getLastRowNum() + 1;

			for (int i = sheet.getFirstRowNum() + 1; i < rowSize; i++) {
				Row row = sheet.getRow(i);
				loadSettingsRow(row);
			}

			dataSourceGenerator.put(settings);
		}

		private void loadSettingsRow(Row row) throws SpreadsheetException {

			String name = stringValue(row, settingNameCol);
			String value = stringValue(row, settingValueCol);

			if (name != null && value != null) {
				
				String pattern = stringValue(row, settingPatternCol);
				String replacement = stringValue(row, settingReplacementCol);
				
				if (pattern!=null && replacement==null) {
					String msg = MessageFormat.format(
						"For the setting ''{0}'' a regular expression pattern is defined, but no replacement string is defined", name);
					fail(msg); 
				}
				
				if (replacement != null && pattern==null) {

					String msg = MessageFormat.format(
						"For the setting ''{0}'' a replacement string is defined, but no regular expression pattern is defined", name);
					fail(msg); 
				}
				
				if (replacement!=null && pattern!=null) {
					RegexRule rule = new RegexRule(name, value, pattern, replacement);
					dataSourceGenerator.addRegexRule(rule);
					
				} else {
					settings.setProperty(name, value);
				}
			}

		}

		private void loadLabels(Sheet sheet) throws SpreadsheetException {
			if (readLabelHeader(sheet)) {
				int rowSize = sheet.getLastRowNum() + 1;
				for (int i = sheet.getFirstRowNum() + 1; i < rowSize; i++) {
					Row row = sheet.getRow(i);
					try {
						loadLabelRow(row);
					} catch (Throwable e) {
						error(e);
					}
				}
			}
		}

		private boolean readLabelHeader(Sheet sheet) {
			subjectCol = labelCol = languageCol = UNDEFINED;

			int firstRow = sheet.getFirstRowNum();
			Row row = sheet.getRow(firstRow);

			int colSize = row.getLastCellNum() + 1;
			for (int i = row.getFirstCellNum(); i < colSize; i++) {
				Cell cell = row.getCell(i);
				if (cell == null) {
					continue;
				}
				String text = cell.getStringCellValue();
				if (text != null) {
					text = text.trim();
					switch (text) {
					case SUBJECT:
						subjectCol = i;
						break;

					case LABEL:
						labelCol = i;
						break;

					case LANGUAGE:
						languageCol = i;
						break;

					}
				}
			}

			return (subjectCol != UNDEFINED) && (labelCol != UNDEFINED) && (languageCol != UNDEFINED);

		}

		private void loadLabelRow(Row row) throws SpreadsheetException {

			URI subjectId = uriValue(row, subjectCol);
			String label = stringValue(row, labelCol);
			String language = stringValue(row, languageCol);

			if (subjectId == null || label == null || language == null) {
				return;
			}

			Literal labelValue = new LiteralImpl(label, language);

			edge(subjectId, RDFS.LABEL, labelValue);

		}

		private void loadPropertyConstraints(Sheet sheet) throws SpreadsheetException {
			readPropertyConstraintHeader(sheet);
			int rowSize = sheet.getLastRowNum() + 1;

			for (int i = sheet.getFirstRowNum() + 1; i < rowSize; i++) {
				Row row = sheet.getRow(i);
				try {
					loadPropertyConstraintRow(row);
				} catch (Throwable e) {
					error(e);
				}
			}

		}

		private Resource valueType(Row row, int col) throws SpreadsheetException {

			String text = stringValue(row, col);

			if (text != null) {
				if (text.indexOf('|') >= 0) {
					return orContraint(text);
				}

				return expandCurie(text);
			}
			return null;
		}

		private Resource orContraint(String text) throws SpreadsheetException {
			Resource shapeId = graph.vertex().getId();

			StringTokenizer tokenizer = new StringTokenizer(text, "|");

			Resource listId = graph.vertex().getId();

			edge(shapeId, SH.or, listId);

			while (tokenizer.hasMoreTokens()) {
				String token = tokenizer.nextToken().trim();
				URI arg = expandCurie(token);

				edge(listId, RDF.FIRST, arg);

				if (tokenizer.hasMoreTokens()) {
					Resource nextId = graph.vertex().getId();
					edge(listId, RDF.REST, nextId);
					listId = nextId;
				}

			}
			return shapeId;
		}

		private void loadPropertyConstraintRow(Row row) throws SpreadsheetException {
			URI shapeId = uriValue(row, pcShapeIdCol);
			if (shapeId == null)
				return;

			URI stereotype = uriValue(row, pcStereotypeCol);
			String propertyIdValue = stringValue(row, pcPropertyIdCol);
			if (propertyIdValue == null) {
				logger.warn("Shape Id is defined but Property Id is not defined: {}", shapeId.getLocalName());
				return;
			}
			URI propertyId = null;
			if (propertyIdValue.charAt(0) == '?') {
				stereotype = Konig.variable;
			} else {
				propertyId = expandPropertyId(propertyIdValue);
			}

			logger.debug("loadPropertyConstraintRow({},{})", RdfUtil.localName(shapeId), RdfUtil.localName(propertyId));

			URI termStatus = uriValue(row, pcTermStatusCol);
			Literal comment = stringLiteral(row, pcCommentCol);
			Resource valueType = valueType(row, pcValueTypeCol);
			Literal minCount = intLiteral(row, pcMinCountCol);
			Literal maxCount = intLiteral(row, pcMaxCountCol);
			Literal minInclusive = numericLiteral(row, pcMinInclusive);
			Literal maxInclusive = numericLiteral(row, pcMaxInclusive);
			Literal minExclusive = numericLiteral(row, pcMinExclusive);
			Literal maxExclusive = numericLiteral(row, pcMaxExclusive);
			Literal minLength = numericLiteral(row, pcMinLength);
			Literal maxLength = numericLiteral(row, pcMaxLength);
			Literal decimalPrecision = intLiteral(row, pcDecimalPrecision);
			if(decimalPrecision!=null && (decimalPrecision.intValue()<1 || decimalPrecision.intValue()>65)){
				throw new KonigException("Decimal Precison should be between 1 to 65");
			}			
			Literal decimalScale = intLiteral(row, pcDecimalScale);
			if(decimalScale!=null && 
					(decimalScale.intValue()<0 || decimalScale.intValue()>30 || 
							decimalScale.intValue()>decimalPrecision.intValue())){
				throw new KonigException("Decimal Scale should be less than Decimal Precision and between 0-30");
			}
			URI valueClass = uriValue(row, pcValueClassCol);
			List<Value> valueIn = valueList(row, pcValueInCol);
			Literal uniqueLang = booleanLiteral(row, pcUniqueLangCol);
			String formula = stringValue(row, pcEqualsCol);
			String sourcePath = stringValue(row, pcSourcePathCol);
			String partitionOf = stringValue(row, pcPartitionOfCol);
			List<URI> securityClassification = uriList(row, pcSecurityClassification);
			if (formula == null) {
				// Support legacy column name "Equivalent Path"
				formula = stringValue(row, pcEquivalentPathCol);
			}
			if (formula == null) {
				// Support legacy column name "Formula"
				formula = stringValue(row, pcFormulaCol);
			}

			if (formula != null) {
				// Check that the user terminates the where clause with a dot.

				Pattern pattern = Pattern.compile("\\sWHERE\\s");
				Matcher matcher = pattern.matcher(formula);
				if (matcher.find() && !formula.endsWith(".")) {
					formula = formula + " .";
				}

			}

			Vertex prior = getPropertyConstraint(shapeId, propertyId);
			if (prior != null) {

				logger.warn("Duplicate definition of property '{}' on '{}'", propertyId.getLocalName(),
						shapeId.getLocalName());
			}

			if (valueClass != null && (valueType instanceof URI)
					&& !XMLSchema.NAMESPACE.equals(((URI) valueType).getNamespace())
					&& !SH.NAMESPACE.equals(((URI) valueType).getNamespace())) {
				prior = getTargetClass(valueType);
				if (prior == null) {
					edge(valueType, RDF.TYPE, SH.Shape);
					edge(valueType, SH.targetClass, valueClass);
					edge(valueClass, RDF.TYPE, OWL.CLASS);
				}
			}
			ShapeManager shapeManager = new MemoryShapeManager();
			ShapeLoader shapeLoader = new ShapeLoader(shapeManager);
			shapeLoader.load(graph);
			Shape shape = shapeManager.getShapeById(shapeId);
			io.konig.core.RdbmsShapeValidator validator = new io.konig.core.RdbmsShapeValidator();

			if (shape != null && validator.isValidRDBMSShape(shape)){
				edge(shape.getId(), RDF.TYPE, Konig.TabularNodeShape);
			}
			if (Konig.id.equals(propertyId)) {
				int min = minCount == null ? 0 : minCount.intValue();
				int max = maxCount == null ? -1 : maxCount.intValue();

				if (max > 1) {
					String msg = MessageFormat.format(
							"Invalid maxCount for property konig:id on Shape <{0}>: must be less than or equal to 1.",
							shapeId);
					throw new SpreadsheetException(msg);
				}

				URI nodeKind = min == 0 ? SH.BlankNodeOrIRI : SH.IRI;
				edge(shapeId, SH.nodeKind, nodeKind);

				if (valueClass != null) {
					edge(shapeId, SH.targetClass, valueClass);
					edge(valueClass, RDF.TYPE, OWL.CLASS);
				}

				if (formula != null) {
					formulaHandlers.add(new ShapeFormulaHandler(shapeId, formula));
				}

				return;
			}

			Vertex constraintVertex = graph.vertex();
			Resource constraint = constraintVertex.getId();

			if (RDF.TYPE.equals(propertyId)) {

				if (valueClass == null) {
					valueClass = OWL.CLASS;
				}

				if (valueType.equals(XMLSchema.ANYURI) || valueType.equals(SH.IRI)) {
					edge(constraint, SH.nodeKind, SH.IRI);
					valueType = null;
				} else {
					logger.warn(
							"As a best practice, rdf:type fields should use a URI reference, but this shape uses a embedded shape: "
									+ shapeId.stringValue());
				}
			}

			URI property = Konig.derivedProperty.equals(stereotype) ? Konig.derivedProperty
					: Konig.variable.equals(stereotype) ? Konig.variable : SH.property;

			edge(shapeId, RDF.TYPE, SH.Shape);
			edge(shapeId, property, constraint);

			edge(constraint, SH.path, propertyId);

			edge(constraint, RDFS.COMMENT, comment);

			if (valueClass != null
					&& (valueType == null || XMLSchema.ANYURI.equals(valueType) || SH.IRI.equals(valueType))) {
				edge(constraint, SH.valueClass, valueClass);
				edge(constraint, SH.nodeKind, SH.IRI);
				if (!RDF.TYPE.equals(propertyId)) {
					edge(valueClass, RDF.TYPE, OWL.CLASS);
				}
			} else if (isDatatype(valueType)) {
				edge(constraint, SH.datatype, valueType);
			} else if (SH.IRI.equals(valueType)) {
				edge(constraint, SH.nodeKind, SH.IRI);
				if (valueClass != null) {
					edge(constraint, SH.valueClass, valueClass);
				}
			} else if (valueType != null) {
				edge(constraint, SH.shape, valueType);
			}

			if (propertyId == null) {
				pathHandlers.add(new PropertyPathHandler(constraint, SH.path, propertyIdValue));
			}

			if (sourcePath != null) {
				pathHandlers.add(new PathHandler(constraint, Konig.sourcePath, sourcePath));
			}

			if (partitionOf != null) {
				pathHandlers.add(new PathHandler(constraint, Konig.partitionOf, partitionOf));
			}

			edge(constraint, XOWL.termStatus, termStatus);
			edge(constraint, SH.minCount, minCount);
			edge(constraint, SH.maxCount, maxCount);
			edge(constraint, SH.minInclusive, minInclusive);
			edge(constraint, SH.maxInclusive, maxInclusive);
			edge(constraint, SH.minExclusive, minExclusive);
			edge(constraint, SH.maxExclusive, maxExclusive);
			edge(constraint, SH.minLength, minLength);
			edge(constraint, SH.maxLength, maxLength);
			edge(constraint, SH.uniqueLang, uniqueLang);
			edge(constraint, Konig.decimalPrecision, decimalPrecision);
			edge(constraint, Konig.decimalScale, decimalScale);
			edge(constraint, SH.in, valueIn);
			edge(constraint, Konig.stereotype, stereotype);
			if(securityClassification!= null && !securityClassification.isEmpty())
			{
				for (URI uri : securityClassification) {
					edge(constraint, Konig.qualifiedSecurityClassification, uri);					
				}
			}	
			if (formula != null) {
				formulaHandlers.add(new PropertyFormulaHandler(shapeId, constraintVertex, formula));
			}
			
		}

		private Vertex getTargetClass(Resource valueType) {

			return graph.v(valueType).out(SH.targetClass).firstVertex();
		}

		private Vertex getPropertyConstraint(URI shapeId, URI predicate) {
			if (predicate == null) {
				return null;
			}
			return graph.v(shapeId).out(SH.property).hasValue(SH.path, predicate).firstVertex();
		}

		private void edge(Resource subject, URI predicate, List<Value> object) {
			if (subject != null && object != null) {
				Vertex first = null;
				Vertex prev = null;

				for (Value value : object) {
					Vertex list = graph.vertex();
					if (first == null) {
						first = list;
						graph.edge(subject, predicate, list.getId());
					}
					if (prev != null) {
						graph.edge(prev.getId(), RDF.REST, list.getId());
					}
					graph.edge(list.getId(), RDF.FIRST, value);
					prev = list;
				}
				if (prev != null) {
					graph.edge(prev.getId(), RDF.REST, RDF.NIL);
				}

			}

		}

		private List<Value> valueList(Row row, int column) throws SpreadsheetException {
			if (column > 0) {
				String text = stringValue(row, column);
				if (text != null) {
					StringTokenizer tokens = new StringTokenizer(text, " \r\n\t");
					List<Value> list = new ArrayList<>();
					while (tokens.hasMoreTokens()) {
						URI curie = expandCurie(tokens.nextToken());
						list.add(curie);
					}
					return list;
				}

			}
			return null;
		}

		private void edge(Resource subject, URI predicate, Value object) {
			if (subject != null && object != null) {
				graph.edge(subject, predicate, object);
			}

		}

		private Literal booleanLiteral(Row row, int col) {
			String text = stringValue(row, col);
			if (text != null) {
				text.trim();
				if (text.length() > 0) {
					return vf.createLiteral(text, XMLSchema.BOOLEAN);
				}
			}
			return null;
		}

		private Literal numericLiteral(Row row, int column) {
			if (column >= 0) {
				Cell cell = row.getCell(column);
				if (cell != null) {
					int cellType = cell.getCellType();
					if (cellType == Cell.CELL_TYPE_NUMERIC) {
						double doubleValue = cell.getNumericCellValue();
						if (doubleValue % 1 == 0) {
							long longValue = (long) doubleValue;
							if (longValue > Integer.MIN_VALUE && longValue < Integer.MAX_VALUE) {
								return vf.createLiteral((int) longValue);
							}
							return vf.createLiteral(longValue);
						}
						if (doubleValue > Float.MIN_VALUE && doubleValue < Float.MAX_VALUE) {
							return vf.createLiteral((float) doubleValue);
						}
						return vf.createLiteral(doubleValue);
					}
				}
			}
			return null;
		}

		private Literal intLiteral(Row row, int column) throws SpreadsheetException {

			Literal literal = null;
			if (column >= 0) {
				Cell cell = row.getCell(column);
				if (cell != null) {

					int cellType = cell.getCellType();
					if (cellType == Cell.CELL_TYPE_STRING) {
						String stringValue = cell.getStringCellValue();
						if (UNBOUNDED.equalsIgnoreCase(stringValue) || stringValue.trim().length()==0) {
							return null;
						}
						
					}

					if (cellType == Cell.CELL_TYPE_NUMERIC) {
						int value = (int) cell.getNumericCellValue();
						literal = vf.createLiteral(value);
					} else if (cellType == Cell.CELL_TYPE_BLANK) {
						return null;
					} else {
						
						String pattern = "Expected integer value in cell ({0}, {1}) on sheet {2} but found {3}";
						String typeName = CELL_TYPE[cellType];
						String msg = MessageFormat.format(pattern, row.getRowNum(), column,
								row.getSheet().getSheetName(), typeName);

						throw new SpreadsheetException(msg);
					}
				}
			}
			return literal;
		}

		private void readGoogleCloudSqlInstanceHeader(Sheet sheet) {
			gcpInstanceNameCol = gcpBackendTypeCol = gcpInstanceTypeCol = gcpRegionCol = gcpVersionCol = gcpTierCol = UNDEFINED;

			int firstRow = sheet.getFirstRowNum();
			Row row = sheet.getRow(firstRow);

			int colSize = row.getLastCellNum() + 1;
			for (int i = row.getFirstCellNum(); i < colSize; i++) {
				Cell cell = row.getCell(i);
				if (cell == null) {
					continue;
				}
				String text = cell.getStringCellValue();
				if (text != null) {
					text = text.trim();

					switch (text) {

					case INSTANCE_NAME:
						gcpInstanceNameCol = i;
						break;

					case BACKEND_TYPE:
						gcpBackendTypeCol = i;
						break;

					case INSTANCE_TYPE:
						gcpInstanceTypeCol = i;
						break;

					case REGION:
						gcpRegionCol = i;
						break;

					case VERSION:
						gcpVersionCol = i;
						break;
						
					case TIER:
						gcpTierCol = i;
						break;
					
					}
				}
			}

		}
		
		private void readAmazonRDSClusterHeader(Sheet sheet) {
			awsDbClusterName = awsEngine = awsEngineVersion = awsInstanceClass = awsAvailabilityZone = awsBackupRetentionPeriod = awsDatabaseName = awsDbSubnetGroupName = awsPreferredBackupWindow = awsPreferredMaintenanceWindow = awsReplicationSourceIdentifier = awsStorageEncrypted	= UNDEFINED;
			int firstRow = sheet.getFirstRowNum();
			Row row = sheet.getRow(firstRow);

			int colSize = row.getLastCellNum() + 1;
			for (int i = row.getFirstCellNum(); i < colSize; i++) {
				Cell cell = row.getCell(i);
				if (cell == null) {
					continue;
				}
				String text = cell.getStringCellValue();
				if (text != null) {
					text = text.trim();

					switch (text) {

					case AWS_DB_CLUSTER_NAME:
						awsDbClusterName = i;
						break;

					case AWS_DB_CLUSTER_ENGINE:
						awsEngine = i;
						break;

					case AWS_DB_CLUSTER_ENGINE_VERSION:
						awsEngineVersion = i;
						break;

					case AWS_DB_CLUSTER_INSTANCE_CLASS:
						awsInstanceClass = i;
						break;

					case AWS_DB_CLUSTER_AVAILABILITY_ZONE:
						awsAvailabilityZone = i;
						break;
						
					case AWS_DB_CLUSTER_BACKUP_PERIOD:
						awsBackupRetentionPeriod = i;
						break;
					
					case AWS_DB_CLUSTER_DATABASE_NAME:
						awsDatabaseName = i;
						break;
					
					case AWS_DB_CLUSTER_DB_SUBNET:
						awsDbSubnetGroupName = i;
						break;
					
					case AWS_DB_CLUSTER_BACKUP_WINDOW:
						awsPreferredBackupWindow = i;
						break;
					
					case AWS_DB_CLUSTER_MAINTENANCE_WINDOW:
						awsPreferredMaintenanceWindow = i;
						break;
					
					case AWS_DB_CLUSTER_REPLICATION_SOURCE:
						awsReplicationSourceIdentifier = i;
						break;
					
					case AWS_DB_CLUSTER_STORAGE_ENCRYPTED:
						awsStorageEncrypted = i;
						break;
					
					}
				}
			}
		
		}
		private void readCloudFormationTemplateHeader(Sheet sheet){

			stackNameCol = awsRegionCol = cloudFormationTemplateCol = UNDEFINED;

			int firstRow = sheet.getFirstRowNum();
			Row row = sheet.getRow(firstRow);

			int colSize = row.getLastCellNum() + 1;
			for (int i = row.getFirstCellNum(); i < colSize; i++) {
				Cell cell = row.getCell(i);
				if (cell == null) {
					continue;
				}
				String text = cell.getStringCellValue();
				if (text != null) {
					text = text.trim();

					switch (text) {

					case STACK_NAME:
						stackNameCol = i;
						break;
					
					case AWS_REGION:
						awsRegionCol = i;
						break;
						
					case CLOUD_FORMATION_TEMPLATES:
						cloudFormationTemplateCol = i;
						break;
					
					}
				}
			}

		
		}
		
		private void readSecurityTagsHeader(Sheet sheet) {
			amazonResourceName = tagKey = tagValue = environment = UNDEFINED;
			int firstRow = sheet.getFirstRowNum();
			Row row = sheet.getRow(firstRow);
			int colSize = row.getLastCellNum() + 1;
			for (int i = row.getFirstCellNum(); i < colSize; i++) {
				Cell cell = row.getCell(i);
				if (cell == null) {
					continue;
				}
				String text = cell.getStringCellValue();
				if (text != null) {
					text = text.trim();
					switch (text) {
					case AMAZON_RESOURCE_NAME :
						amazonResourceName = i;
						break;
					case TAG_KEY:
						tagKey = i;
						break;
					case TAG_VALUE:
						tagValue = i;
						break;
					case ENVIRONMENT:
						environment = i;
						break;
					}
				}
			}
		}
		
		private void readSettingHeader(Sheet sheet) {
			settingNameCol = settingValueCol = settingPatternCol = settingReplacementCol = UNDEFINED;
			int firstRow = sheet.getFirstRowNum();
			Row row = sheet.getRow(firstRow);

			int colSize = row.getLastCellNum() + 1;
			for (int i = row.getFirstCellNum(); i < colSize; i++) {
				Cell cell = row.getCell(i);
				if (cell == null) {
					continue;
				}
				String text = cell.getStringCellValue();
				if (text != null) {
					text = text.trim();

					switch (text) {
					case SETTING_NAME:
						settingNameCol = i;
						break;
					case SETTING_VALUE:
						settingValueCol = i;
						break;
						
					case PATTERN:
						settingPatternCol = i;
						break;
						
					case REPLACEMENT:
						settingReplacementCol = i;
						break;

					}
				}
			}

		}

		private void readPropertyConstraintHeader(Sheet sheet) {
			pcShapeIdCol = pcCommentCol = pcPropertyIdCol = pcValueTypeCol = pcMinCountCol = pcMaxCountCol = pcUniqueLangCol = pcValueClassCol = pcValueInCol = pcStereotypeCol = pcFormulaCol = pcPartitionOfCol = pcSourcePathCol = pcEquivalentPathCol = pcEqualsCol = pcMinInclusive = pcMaxInclusive = pcMinExclusive = pcMaxExclusive = pcMinLength = pcMaxLength = pcDecimalPrecision = pcDecimalScale = pcSecurityClassification= UNDEFINED;

			int firstRow = sheet.getFirstRowNum();
			Row row = sheet.getRow(firstRow);

			int colSize = row.getLastCellNum() + 1;
			for (int i = row.getFirstCellNum(); i < colSize; i++) {
				Cell cell = row.getCell(i);
				if (cell == null) {
					continue;
				}
				String text = cell.getStringCellValue();
				if (text != null) {
					text = text.trim();

					switch (text) {
					case SHAPE_ID:
						pcShapeIdCol = i;
						break;
					case COMMENT:
						pcCommentCol = i;
						break;

					case PROPERTY_PATH:
					case PROPERTY_ID:
						pcPropertyIdCol = i;
						break;
					case VALUE_TYPE:
						pcValueTypeCol = i;
						break;
					case MIN_COUNT:
						pcMinCountCol = i;
						break;
					case MAX_COUNT:
						pcMaxCountCol = i;
						break;
					case MIN_INCLUSIVE:
						pcMinInclusive = i;
						break;
					case MAX_INCLUSIVE:
						pcMaxInclusive = i;
						break;
					case MIN_EXCLUSIVE:
						pcMinExclusive = i;
						break;
					case MAX_EXCLUSIVE:
						pcMaxExclusive = i;
						break;
					case MIN_LENGTH:
						pcMinLength = i;
						break;
					case MAX_LENGTH:
						pcMaxLength = i;
						break;
					case DECIMAL_PRECISION:
						pcDecimalPrecision = i;
						break;
					case DECIMAL_SCALE:
						pcDecimalScale = i;
						break;
					case UNIQUE_LANG:
						pcUniqueLangCol = i;
						break;
					case VALUE_CLASS:
						pcValueClassCol = i;
						break;
					case VALUE_IN:
						pcValueInCol = i;
						break;
					case STEREOTYPE:
						pcStereotypeCol = i;
						break;
					case EQUIVALENT_PATH:
						pcEquivalentPathCol = i;
						break;

					case EQUALS:
						pcEqualsCol = i;
						break;

					case SOURCE_PATH:
						pcSourcePathCol = i;
						break;
					case PARTITION_OF:
						pcPartitionOfCol = i;
						break;
					case FORMULA:
						pcFormulaCol = i;
						break;
					case TERM_STATUS:
						pcTermStatusCol = i;
						break;
					case SECURITY_CLASSIFICATION:
						pcSecurityClassification = i;
						break;
					}
				}
			}

		}

		private void loadShapes(Sheet sheet) throws SpreadsheetException {

			readShapeHeader(sheet);

			int rowSize = sheet.getLastRowNum() + 1;

			for (int i = sheet.getFirstRowNum() + 1; i < rowSize; i++) {
				Row row = sheet.getRow(i);
				try {
					loadShapeRow(row);
				} catch (Throwable e) {
					error(e);
				}
			}

		}

		private void loadShapeRow(Row row) throws SpreadsheetException {

			URI shapeId = uriValue(row, shapeIdCol);
			Literal shapeComment = stringLiteral(row, shapeCommentCol);
			URI targetClass = uriValue(row, shapeTargetClassCol);
			URI aggregationOf = uriValue(row, shapeAggregationOfCol);
			URI rollUpBy = uriValue(row, shapeRollUpByCol);
            URI shapeType=uriValue(row, shapeTypeCol);
            URI tabularOriginShape = uriValue(row, tabularOriginShapeCol);

			String iriTemplate = stringValue(row, shapeIriTemplateCol);
			Literal mediaType = stringLiteral(row, shapeMediaTypeCol);
			Literal bigqueryTable = bigQueryTableId(row, targetClass);
			List<URI> applicationList = uriList(row, defaultShapeForCol);
			List<URI> shapeOfList = uriList(row, inputShapeOfCol);
			List<Value> orList = valueList(row, oneOfCol);
			List<Function> dataSourceList = dataSourceList(row);

			if (shapeId == null) {
				return;
			}

			edge(shapeId, RDF.TYPE, SH.Shape);
            edge(shapeId, RDF.TYPE, shapeType);
			edge(shapeId, PROV.wasGeneratedBy, activityId);
			edge(shapeId, RDFS.COMMENT, shapeComment);
			edge(shapeId, SH.targetClass, targetClass);
			edge(targetClass, RDF.TYPE, OWL.CLASS);
			edge(shapeId, Konig.aggregationOf, aggregationOf);
			edge(shapeId, Konig.rollUpBy, rollUpBy);
			edge(shapeId, Konig.mediaTypeBaseName, mediaType);
			edge(shapeId, Konig.bigQueryTableId, bigqueryTable);
			edge(shapeId, Konig.tabularOriginShape, tabularOriginShape);
			edge(shapeId, SH.or, orList);
			


			if (iriTemplate != null) {
				shapeTemplateList.add(new ShapeTemplate(shapeId, iriTemplate));
			}

			if (applicationList != null && !applicationList.isEmpty()) {
				for (URI uri : applicationList) {
					edge(shapeId, Konig.defaultShapeFor, uri);
					edge(uri, RDF.TYPE, Schema.SoftwareApplication);
				}
			}

			if (dataSourceList != null) {
				dataSourceMap.put(shapeId, dataSourceList);
			}
			if(shapeOfList!= null && !shapeOfList.isEmpty())
			{
				for (URI uri : shapeOfList) {
					edge(shapeId, Konig.inputShapeOf, uri);					
				}
			}		

		}
		
		private void loadAmazonRDSClusterRow(Row row) {
			
			Literal dbClusterName = stringLiteral(row, awsDbClusterName);			
			Literal engine = stringLiteral(row, awsEngine);
			Literal engineVersion = stringLiteral(row, awsEngineVersion);
			Literal instanceClass = stringLiteral(row, awsInstanceClass);
			String availabilityZone = stringValue(row, awsAvailabilityZone);
			Literal backupRetentionPeriod = stringLiteral(row,awsBackupRetentionPeriod);			
			Literal databaseName = stringLiteral(row, awsDatabaseName);
			Literal dbSubnetGroupName = stringLiteral(row, awsDbSubnetGroupName);
			Literal preferredBackupWindow = stringLiteral(row, awsPreferredBackupWindow);
			Literal preferredMaintenanceWindow = stringLiteral(row, awsPreferredMaintenanceWindow);
			Literal replicationSourceIdentifier = stringLiteral(row,awsReplicationSourceIdentifier);
			Literal storageEncrypted = stringLiteral(row,awsStorageEncrypted);
			String dbClusterId = "${environmentName}-"+ stringValue(row, awsDbClusterName);
			
			URI id = new URIImpl("https://amazonaws.konig.io/rds/cluster/"+ dbClusterId);
			edge(id, RDF.TYPE, AWS.DbCluster);
			edge(id, AWS.dbClusterId, literal(dbClusterId));
			edge(id, AWS.dbClusterName, dbClusterName);
			edge(id, AWS.engine, engine);
			edge(id, AWS.engineVersion, engineVersion);
			edge(id, AWS.instanceClass, instanceClass);
			edge(id, AWS.backupRetentionPeriod, backupRetentionPeriod);
			edge(id, AWS.databaseName, databaseName);
			edge(id, AWS.dbSubnetGroupName, dbSubnetGroupName);
			edge(id, AWS.preferredBackupWindow, preferredBackupWindow);
			edge(id, AWS.preferredMaintenanceWindow, preferredMaintenanceWindow);
			edge(id, AWS.replicationSourceIdentifier, replicationSourceIdentifier);
			edge(id, AWS.storageEncrypted, storageEncrypted);
			
			if (availabilityZone != null && !availabilityZone.equals("")) {
				StringTokenizer st = new StringTokenizer(availabilityZone, " ");
				while (st.hasMoreTokens()) {
					edge(id, AWS.availabilityZone, literal(st.nextElement().toString().trim()));					
				}
			}
		}
		
		private void loadSecurityTagsRow(Row row) throws SpreadsheetException {
			String awsResourceName = stringValue(row, amazonResourceName);
			Literal awsTagKey = stringLiteral(row, tagKey);
			Literal awsTagValue = stringLiteral(row, tagValue);
			Literal awsEnvironment = stringLiteral(row, environment);
			URI id = new URIImpl("https://amazonaws.konig.io/resource/"+ awsResourceName);
			if (awsResourceName == null) {
				id = new URIImpl("https://amazonaws.konig.io/resource/arn.all");
			}
			edge(id, RDF.TYPE, AWS.SecurityTag);
			Vertex tagVertex = graph.vertex();
			Resource tagResource = tagVertex.getId();
			if(awsTagKey != null && !awsTagKey.equals("")) {
				edge(id, AWS.tags, tagResource);
				edge(tagResource, AWS.tagKey, awsTagKey);
				edge(tagResource, AWS.tagValue, awsTagValue);
				edge(tagResource, Konig.environment, awsEnvironment);
			}
		}
		
		private void loadCloudFormationTemplateRow(Row row) throws SpreadsheetException{
			
			Literal stackName = stringLiteral(row, stackNameCol);	
			Literal awsRegion = stringLiteral(row,awsRegionCol);
			Literal cloudFormationTemplate = stringLiteral(row, cloudFormationTemplateCol);
			
			if (stackName==null && awsRegion==null && cloudFormationTemplate==null) {
				return;
			}
			
			
			if (cloudFormationTemplate==null) {
				String msg = MessageFormat.format(
						"cloudFormationTemplate not defined for stackName: {0}, awsRegion: {1}", 
						stackName, awsRegion);
				throw new SpreadsheetException(msg);
			}
			if (stackName==null) {
				throw new SpreadsheetException("Stack Name must be defined for CloudFormation Template");
			}
			
			URI id = new URIImpl("https://amazonaws.konig.io/cloudformation/template/"+ stringValue(row,stackNameCol)+"_template");
			edge(id, RDF.TYPE, AWS.CloudFormationTemplate);
			edge(id, AWS.stackName, stackName);
			edge(id, AWS.awsRegion, awsRegion);
			edge(id, AWS.template, cloudFormationTemplate);
			
		}
		private void loadGoogleCloudSqlInstanceRow(Row row) {
			String instanceName = stringValue(row, gcpInstanceNameCol);
			URI backendType = gcpValue(row, gcpBackendTypeCol);
			URI instanceType = gcpValue(row, gcpInstanceTypeCol);
			URI region = gcpValue(row, gcpRegionCol);
			URI databaseVersion = gcpValue(row, gcpVersionCol);
			URI tier = gcpValue(row,gcpTierCol);

			if (instanceName != null) {
				String idValue = "https://www.googleapis.com/sql/v1beta4/projects/${gcpProjectId}/instances/"
						+ instanceName;
				URI id = new URIImpl(idValue);
				edge(id, RDF.TYPE, GCP.GoogleCloudSqlInstance);
				edge(id, GCP.name, literal(instanceName));
				edge(id, GCP.backendType, backendType);
				edge(id, GCP.instanceType, instanceType);
				edge(id, GCP.databaseVersion, databaseVersion);
				edge(id, GCP.region, region);
				Vertex tierVertex = graph.vertex();
				Resource tierResource = tierVertex.getId();
				edge(id, GCP.settings, tierResource);
				edge(tierResource,GCP.tier,tier);
				
				
				
			}
			

		}

		private URI gcpValue(Row row, int col) {
			String value = stringValue(row, col);

			return value == null ? null : new URIImpl(GCP.NAMESPACE + value);
		}

		private List<Function> dataSourceList(Row row) throws SpreadsheetException {

			String text = stringValue(row, shapeDatasourceCol);
			if (text == null) {
				return null;
			}

			ListFunctionVisitor visitor = new ListFunctionVisitor();
			FunctionParser parser = new FunctionParser(visitor);
			try {
				parser.parse(text);
			} catch (FunctionParseException e) {
				throw new SpreadsheetException("Failed to parse Datasource definition: " + text, e);
			}
			List<Function> list = visitor.getList();

			return list.isEmpty() ? null : list;
		}

		private Literal bigQueryTableId(Row row, URI targetClass) throws SpreadsheetException {

			if (targetClass != null) {

				String text = stringValue(row, shapeBigQueryTableCol);
				if (text != null) {
					if ("x".equalsIgnoreCase(text)) {
						if (datasetMapper == null) {
							throw new SpreadsheetException("datasetMapper is not defined");
						}
						Vertex vertex = graph.vertex(targetClass);
						String datasetId = datasetMapper.getId(vertex);

						if (datasetId == null) {
							throw new SpreadsheetException("Dataset id not defined for class: " + targetClass);
						}

						String localName = targetClass.getLocalName();
						StringBuilder builder = new StringBuilder();
						builder.append(datasetId);
						builder.append('.');
						builder.append(localName);
						text = builder.toString();
					}
					return literal(text);
				}
			}
			return null;
		}

		private void readShapeHeader(Sheet sheet) {
			shapeIdCol = shapeCommentCol = shapeTargetClassCol = shapeAggregationOfCol = shapeRollUpByCol = shapeTypeCol = shapeMediaTypeCol = shapeBigQueryTableCol = shapeDatasourceCol = defaultShapeForCol = shapeIriTemplateCol = tabularOriginShapeCol = UNDEFINED;
			int firstRow = sheet.getFirstRowNum();
			Row row = sheet.getRow(firstRow);

			int colSize = row.getLastCellNum() + 1;
			for (int i = row.getFirstCellNum(); i < colSize; i++) {
				Cell cell = row.getCell(i);
				if (cell == null) {
					continue;
				}
				String text = cell.getStringCellValue();
				if (text != null) {
					text = text.trim();

					switch (text) {
					case SHAPE_ID:
						shapeIdCol = i;
						break;
					case COMMENT:
						shapeCommentCol = i;
						break;
					case SCOPE_CLASS:
						shapeTargetClassCol = i;
						break;
					case TARGET_CLASS:
						shapeTargetClassCol = i;
						break;
					case AGGREGATION_OF:
						shapeAggregationOfCol = i;
						break;
					case ROLL_UP_BY:
						shapeRollUpByCol = i;
						break;
                    case SHAPE_TYPE:
                        shapeTypeCol = i;
                        break;
					case MEDIA_TYPE:
						shapeMediaTypeCol = i;
						break;
					case BIGQUERY_TABLE:
						shapeBigQueryTableCol = i;
						break;
					case DATASOURCE:
						shapeDatasourceCol = i;
						break;
					case IRI_TEMPLATE:
						shapeIriTemplateCol = i;
						break;
					case SHAPE_OF:
						inputShapeOfCol = i;
						break;
					case ONE_OF:
						oneOfCol = i;
						break;
					case DEFAULT_FOR:
						defaultShapeForCol = i;
						break;
					case TABULAR_ORIGIN_SHAPE:
						tabularOriginShapeCol = i;
						break;

					}
				}
			}

		}

		private void loadIndividuals(Sheet sheet) throws SpreadsheetException {

			readIndividualHeader(sheet);
			int rowSize = sheet.getLastRowNum() + 1;

			for (int i = sheet.getFirstRowNum() + 1; i < rowSize; i++) {
				Row row = sheet.getRow(i);
				try {
					loadIndividualRow(row);
				} catch (Throwable e) {
					error(e);
				}
			}

		}

		private void loadIndividualRow(Row row) throws SpreadsheetException {

			Literal name = stringLiteral(row, individualNameCol);
			Literal comment = stringLiteral(row, individualCommentCol);
			URI individualId = uriValue(row, individualIdCol);
			List<URI> typeList = uriList(row, individualTypeCol);
			Literal codeValue = stringLiteral(row, individualCodeValueCol);
			if (individualId == null) {
				return;
			}
			Vertex prior = graph.getVertex(individualId);
			if (prior != null) {
				logger.warn("Duplicate definition of named individual: {}", individualId.stringValue());
			}
			if (typeList != null) {
				for (URI value : typeList) {
					if (!value.equals(Schema.Enumeration)) {
						graph.edge(individualId, RDF.TYPE, value);
						graph.edge(value, RDF.TYPE, OWL.CLASS);
						graph.edge(value, RDFS.SUBCLASSOF, Schema.Enumeration);
					}
				}
			} else {
				graph.edge(individualId, RDF.TYPE, Schema.Enumeration);
			}
			if (name == null && useDefaultName()) {
				name = literal(individualId.getLocalName());
			}
			edge(individualId, Schema.name, name);
			edge(individualId, RDFS.COMMENT, comment);
			edge(individualId, DCTERMS.IDENTIFIER, codeValue);
		}

		private Row readIndividualHeader(Sheet sheet) {

			int firstRow = sheet.getFirstRowNum();
			Row row = sheet.getRow(firstRow);

			int colSize = row.getLastCellNum() + 1;
			for (int i = row.getFirstCellNum(); i < colSize; i++) {
				Cell cell = row.getCell(i);
				if (cell == null) {
					continue;
				}
				String text = cell.getStringCellValue();
				if (text != null) {
					text = text.trim();

					switch (text) {
					case INDIVIDUAL_NAME:
						individualNameCol = i;
						break;
					case COMMENT:
						individualCommentCol = i;
						break;
					case INDIVIDUAL_ID:
						individualIdCol = i;
						break;
					case INDIVIDUAL_TYPE:
						individualTypeCol = i;
						break;
					case INDIVIDUAL_CODE_VALUE:
						individualCodeValueCol = i;
						break;

					}
				}
			}
			return row;

		}

		private void loadProperties(Sheet sheet) throws SpreadsheetException {

			try {
				readPropertyHeader(sheet);
			} catch (Throwable e) {
				error(e);
				return;
			}

			int rowSize = sheet.getLastRowNum() + 1;

			for (int i = sheet.getFirstRowNum() + 1; i < rowSize; i++) {
				Row row = sheet.getRow(i);
				try {
					loadPropertyRow(row);
				} catch (Throwable e) {
					error(e);
				}
			}

		}

		private void loadPropertyRow(Row row) throws SpreadsheetException {

			Literal propertyName = stringLiteral(row, propertyNameCol);
			Literal comment = stringLiteral(row, propertyCommentCol);
			URI propertyId = uriValue(row, propertyIdCol);
			List<URI> domain = uriList(row, domainCol);
			List<URI> range = uriList(row, rangeCol);
			URI inverseOf = uriValue(row, inverseOfCol);
			List<URI> propertyType = uriList(row, propertyTypeCol);
			URI subpropertyOf = uriValue(row, subpropertyOfCol);
			URI termStatus = uriValue(row, propertyTermStatusCol);
			List<URI> securityClassification = uriList(row, securityClassificationCol);
			if (propertyId == null) {
				return;
			}

			Vertex subject = graph.vertex(propertyId);
			propertyType = analyzePropertyType(subject, propertyType, range);
			graph.edge(propertyId, RDF.TYPE, RDF.PROPERTY);
			for (URI value : propertyType) {
				if (!RDF.PROPERTY.equals(value)) {
					graph.edge(propertyId, RDF.TYPE, value);
				}
			}

			if (subpropertyOf != null) {
				graph.edge(propertyId, RDFS.SUBPROPERTYOF, subpropertyOf);
			}

			if (propertyName != null) {
				graph.edge(propertyId, RDFS.LABEL, propertyName);
			}

			if (comment != null) {
				graph.edge(propertyId, RDFS.COMMENT, comment);
			}

			if (domain != null) {
				if (domain.size() == 1) {
					graph.edge(propertyId, RDFS.DOMAIN, domain.get(0));
				} else {
					for (URI value : domain) {
						graph.edge(propertyId, Schema.domainIncludes, value);
					}
				}
			}

			if (range != null) {
				if (range.size() == 1) {
					graph.edge(propertyId, RDFS.RANGE, range.get(0));
				} else {
					for (URI value : range) {
						graph.edge(propertyId, Schema.rangeIncludes, value);
					}
				}
			}

			if (inverseOf != null) {
				graph.edge(propertyId, OWL.INVERSEOF, inverseOf);
			}
			if (termStatus != null) {
				graph.edge(propertyId, XOWL.termStatus, termStatus);
			}
			
			if(securityClassification!= null && !securityClassification.isEmpty())
			{
			  for (URI uri : securityClassification) {
			    graph.edge(propertyId, Konig.securityClassification, uri);					
			  }
			}

		}

		private List<URI> analyzePropertyType(Vertex subject, List<URI> propertyType, List<URI> range) {
			if (propertyType == null) {
				propertyType = new ArrayList<>();

				boolean datatypeProperty = false;
				boolean objectProperty = false;

				if (range != null) {
					for (URI type : range) {

						if (isDatatype(type)) {
							datatypeProperty = true;
						} else {
							objectProperty = true;
						}
					}
				}

				if (datatypeProperty && !objectProperty) {
					propertyType.add(OWL.DATATYPEPROPERTY);
				}
				if (!datatypeProperty && objectProperty) {
					propertyType.add(OWL.OBJECTPROPERTY);
				}

			}
			return propertyType;
		}

		/**
		 * Returns true if the given subject is in the xsd namespace, or one of
		 * the following individuals: rdfs:Literal, rdfs:Datatype,
		 * rdf:XMLLiteral, schema:Boolean, schema:Date, schema:DateTime,
		 * schema:Number, schema:Text, or schema:Time.
		 */
		private boolean isDatatype(Resource subject) {

			if (subject instanceof URI) {

				if (XMLSchema.NAMESPACE.equals(((URI) subject).getNamespace())) {
					return true;
				}

				if (subject.equals(Schema.Boolean) || subject.equals(Schema.Date) || subject.equals(Schema.DateTime)
						|| subject.equals(Schema.Number) || subject.equals(Schema.Float)
						|| subject.equals(Schema.Integer) || subject.equals(Schema.Text) || subject.equals(Schema.Time)
						|| subject.equals(RDFS.LITERAL) || subject.equals(RDFS.DATATYPE)
						|| subject.equals(RDF.XMLLITERAL)

				) {
					return true;
				}
			}

			return false;
		}

		private List<URI> uriList(Row row, int col) throws SpreadsheetException {
			List<URI> result = null;
			String text = stringValue(row, col);
			if (text != null) {
				StringTokenizer tokens = new StringTokenizer(text, " \n\t\r");
				if (tokens.hasMoreTokens()) {
					result = new ArrayList<>();
					while (tokens.hasMoreTokens()) {
						String value = tokens.nextToken();
						result.add(expandCurie(value));
					}
				}
			}
			return result;
		}

		private void readPropertyHeader(Sheet sheet) throws SpreadsheetException {
			propertyNameCol = UNDEFINED;
			propertyIdCol = UNDEFINED;
			domainCol = UNDEFINED;
			rangeCol = UNDEFINED;
			inverseOfCol = UNDEFINED;
			propertyTypeCol = UNDEFINED;
			subpropertyOfCol = UNDEFINED;
			propertyCommentCol = UNDEFINED;
			securityClassificationCol = UNDEFINED;
			int firstRow = sheet.getFirstRowNum();
			Row row = sheet.getRow(firstRow);

			int colSize = row.getLastCellNum() + 1;
			for (int i = row.getFirstCellNum(); i < colSize; i++) {
				Cell cell = row.getCell(i);
				if (cell == null) {
					continue;
				}
				String text = cell.getStringCellValue();
				if (text != null) {
					text = text.trim();

					switch (text) {
					case PROPERTY_NAME:
						propertyNameCol = i;
						break;

					case PROPERTY_ID:
						propertyIdCol = i;
						break;
					case DOMAIN:
						domainCol = i;
						break;
					case RANGE:
						rangeCol = i;
						break;
					case INVERSE_OF:
						inverseOfCol = i;
						break;
					case PROPERTY_TYPE:
						propertyTypeCol = i;
						break;
					case COMMENT:
						propertyCommentCol = i;
						break;

					case SUBPROPERTY_OF:
						subpropertyOfCol = i;
						break;
					case TERM_STATUS:
						propertyTermStatusCol = i;
						break;
					case SECURITY_CLASSIFICATION:
						securityClassificationCol = i;
						break;
					}
				}
			}

			String sheetName = sheet.getSheetName();

			if (propertyIdCol == UNDEFINED) {
				throw new MissingColumnException(PROPERTY_ID, sheetName);
			}

			if (domainCol == UNDEFINED) {
				throw new MissingColumnException(DOMAIN, sheetName);
			}

			if (rangeCol == UNDEFINED) {
				throw new MissingColumnException(RANGE, sheetName);
			}

		}

		private void loadClasses(Sheet sheet) throws SpreadsheetException {

			readClassHeader(sheet);

			int rowSize = sheet.getLastRowNum() + 1;

			for (int i = sheet.getFirstRowNum() + 1; i < rowSize; i++) {
				Row row = sheet.getRow(i);
				try {
					loadClassRow(row);
				} catch (Throwable e) {
					error(e);
				}
			}

		}

		private void loadClassRow(Row row) throws SpreadsheetException {

			Literal className = stringLiteral(row, classNameCol);
			Literal comment = stringLiteral(row, classCommentCol);
			URI classId = uriValue(row, classIdCol);
			List<URI> subclassOf = uriList(row, classSubclassOfCol);
			URI termStatus = uriValue(row, classTermStatusCol);
			if (classId != null) {
				graph.edge(classId, RDF.TYPE, OWL.CLASS);
				if (className != null) {
					graph.edge(classId, RDFS.LABEL, className);
				}
				if (comment != null) {
					graph.edge(classId, RDFS.COMMENT, comment);
				}
				if (subclassOf != null && !subclassOf.isEmpty()) {
					for (URI subclassId : subclassOf) {
						graph.edge(classId, RDFS.SUBCLASSOF, subclassId);
						graph.edge(subclassId, RDF.TYPE, OWL.CLASS);
					}

				}

				if (termStatus != null) {
					graph.edge(classId, XOWL.termStatus, termStatus);
				}
			}

		}

		private Literal stringLiteral(Row row, int col) {
			Literal result = null;
			String text = stringValue(row, col);
			if (text != null) {
				result = vf.createLiteral(text);
			}
			return result;
		}

		private URI uriValue(Row row, int col) throws SpreadsheetException {

			try {

				String text = stringValue(row, col);
				return expandCurie(text);
			} catch (SpreadsheetException e) {

				String template = "Error in row {0}, column {1} on sheet {2}: {3}";

				String msg = MessageFormat.format(template, row.getRowNum(), col, row.getSheet().getSheetName(),
						e.getMessage());
				throw new SpreadsheetException(msg);

			}
		}

		private String curie(URI uri) {

			String name = uri.getNamespace();
			Namespace ns = nsManager.findByName(name);
			if (ns == null) {
				return uri.stringValue();
			}

			StringBuilder builder = new StringBuilder();
			builder.append(ns.getPrefix());
			builder.append(':');
			builder.append(uri.getLocalName());

			return builder.toString();
		}

		private URI expandPropertyId(String text) throws SpreadsheetException {

			if (text == null) {
				throw new SpreadsheetException("SHACL path must be defined");
			}
			if (text.startsWith("http://") || text.startsWith("https://") || text.startsWith("urn:")) {
				return vf.createURI(text);
			}
			int c = text.charAt(0);
			if (c == '/' || c == '^' || text.indexOf('.') >= 0) {
				return null;
			}

			return expandCurie(text);
		}

		private URI expandCurie(String text) throws SpreadsheetException {
			if (text == null) {
				return null;
			}
			if (text.startsWith("http://") || text.startsWith("https://") || text.startsWith("urn:")) {
				return vf.createURI(text);
			}
			int colon = text.indexOf(':');
			if (colon < 1) {
				throw new SpreadsheetException("Invalid URI: " + text);
			}
			String prefix = text.substring(0, colon);
			Namespace ns = nsManager.findByPrefix(prefix);
			if (ns == null) {
				throw new SpreadsheetException(format("Namespace not found for prefix ''{0}''", prefix));
			}
			StringBuilder builder = new StringBuilder();
			builder.append(ns.getName());

			String localName = text.substring(colon + 1);

			builder.append(localName);

			if (localName.indexOf('/') >= 0) {
				StringBuilder err = new StringBuilder();
				err.append("The localname of a CURIE should not contain a slash, but found '");
				err.append(text);
				err.append("'");
				warningList.add(err.toString());
			}

			return vf.createURI(builder.toString());
		}
		private void readClassHeader(Sheet sheet) {

			classNameCol = UNDEFINED;
			classCommentCol = UNDEFINED;
			classIdCol = UNDEFINED;
			classSubclassOfCol = UNDEFINED;
			classTermStatusCol = UNDEFINED;

			int firstRow = sheet.getFirstRowNum();
			Row row = sheet.getRow(firstRow);

			int colSize = row.getLastCellNum() + 1;
			for (int i = row.getFirstCellNum(); i < colSize; i++) {
				Cell cell = row.getCell(i);
				if (cell == null) {
					continue;
				}
				String text = cell.getStringCellValue();
				if (text != null) {
					text = text.trim();

					switch (text) {
					case CLASS_NAME:
						classNameCol = i;
						break;
					case COMMENT:
						classCommentCol = i;
						break;
					case CLASS_ID:
						classIdCol = i;
						break;
					case CLASS_SUBCLASS_OF:
						classSubclassOfCol = i;
						break;
					case TERM_STATUS:
						classTermStatusCol = i;
						break;
					}
				}
			}
		}

		private void loadOntologies(Sheet sheet) throws SpreadsheetException {

			try {
				readOntologyHeader(sheet);
			} catch (Throwable e) {
				error(e);
				return;
			}

			int rowSize = sheet.getLastRowNum() + 1;

			for (int i = sheet.getFirstRowNum() + 1; i < rowSize; i++) {
				Row row = sheet.getRow(i);
				try {
					loadOntologyRow(row);
				} catch (Throwable e) {
					error(e);
				}
			}

		}

		private void fail(String message) throws SpreadsheetException {
			if (failOnErrors) {
				throw new SpreadsheetException(message);
			}
			logError(message);
		}

		private void error(Throwable e) throws SpreadsheetException {
			if (failOnErrors) {
				throw e instanceof SpreadsheetException ? (SpreadsheetException) e : new SpreadsheetException(e);
			}
			logError(e.getMessage());
		}

		private String format(String pattern, Object... args) {
			return MessageFormat.format(pattern, args);
		}

		private void loadOntologyRow(Row row) throws SpreadsheetException {

			Literal ontologyName = stringLiteral(row, ontologyNameCol);
			Literal comment = stringLiteral(row, ontologyCommentCol);
			String namespaceURI = stringValue(row, namespaceUriCol);
			Literal prefix = stringLiteral(row, prefixCol);
			List<String> importList = imports(row, importsCol);

			if (ontologyName == null && comment == null && namespaceURI == null && prefix == null) {
				return;
			}

			String sheetName = row.getSheet().getSheetName();

			if (namespaceURI == null) {
				throw new SpreadsheetException(format("''{0}'' is missing on row {1} of the ''{2}'' sheet.",
						NAMESPACE_URI, row.getRowNum(), sheetName));
			}

			if (!namespaceURI.endsWith("/") && !namespaceURI.endsWith("#")) {
				String msg = format("Namespace must end with ''/'' or ''#'' but found: {0}", namespaceURI);
				fail(msg);
			}

			if (prefix == null) {
				throw new SpreadsheetException(format("''{0}'' is missing on row {1} of the ''{2}'' sheet.", PREFIX,
						row.getRowNum(), sheetName));
			}

			nsManager.add(prefix.stringValue(), namespaceURI);

			URI subject = uri(namespaceURI);

			edge(subject, RDF.TYPE, OWL.ONTOLOGY);
			edge(subject, VANN.preferredNamespacePrefix, prefix);
			edge(subject, RDFS.LABEL, ontologyName);
			edge(subject, RDFS.COMMENT, comment);
			
			if (importList != null) {
				this.importList.add(new ImportInfo(subject, importList));
			}

		}

		private List<String> imports(Row row, int column) {
			List<String> list = null;
			String text = stringValue(row, column);
			if (text != null) {
				text = text.trim();
				if (text.length() > 0) {
					list = new ArrayList<>();
					StringTokenizer tokens = new StringTokenizer(text, " \t\r\n");
					while (tokens.hasMoreTokens()) {
						list.add(tokens.nextToken());
					}
				}
			}
			return list;
		}

		private URI uri(String text) {
			return vf.createURI(text);
		}

		private Literal literal(String text) {
			return vf.createLiteral(text);
		}

		private String stringValue(Row row, int column) {
			if (row == null) {
				return null;
			}
			String text = null;
			if (column >= 0) {
				Cell cell = row.getCell(column);
				if (cell != null) {

					text = dataFormatter.formatCellValue(cell);
					if (text != null && !text.startsWith("HYPERLINK(")) {
						text = text.trim();
						if (text.length() == 0) {
							text = null;
						}
					} else {

						Hyperlink link = cell.getHyperlink();
						if (link != null) {
							text = link.getLabel();
							if (text == null) {
								text = link.getAddress();
							}
							if (text != null) {
								text = text.trim();

							}
						}
					}
				}
			}

			return text == null || text.isEmpty() ? null : text;
		}

		private void readOntologyHeader(Sheet sheet) throws SpreadsheetException {

			ontologyNameCol = UNDEFINED;
			ontologyCommentCol = UNDEFINED;
			namespaceUriCol = UNDEFINED;
			prefixCol = UNDEFINED;
			importsCol = UNDEFINED;

			int firstRow = sheet.getFirstRowNum();
			Row row = sheet.getRow(firstRow);

			int colSize = row.getLastCellNum() + 1;
			for (int i = row.getFirstCellNum(); i < colSize; i++) {
				Cell cell = row.getCell(i);
				if (cell == null) {
					continue;
				}
				String text = cell.getStringCellValue();
				if (text != null) {
					text = text.trim();

					switch (text) {
					case ONTOLOGY_NAME:
						ontologyNameCol = i;
						break;
					case COMMENT:
						ontologyCommentCol = i;
						break;
					case NAMESPACE_URI:
						namespaceUriCol = i;
						break;
					case PREFIX:
						prefixCol = i;
						break;
					case IMPORTS:
						importsCol = i;
						break;

					}
				}
			}

			String sheetName = sheet.getSheetName();
			if (ontologyNameCol == UNDEFINED) {
				throw new MissingColumnException(ONTOLOGY_NAME, sheetName);
			}

			if (ontologyCommentCol == UNDEFINED) {
				throw new MissingColumnException(COMMENT, sheetName);
			}

			if (namespaceUriCol == UNDEFINED) {
				throw new MissingColumnException(NAMESPACE_URI, sheetName);
			}

			if (prefixCol == UNDEFINED) {
				throw new MissingColumnException(PREFIX, sheetName);
			}

		}

		private class PathInfo {
			private int column;
			private String pathString;
			private Path path;
			private URI datatype;

			public PathInfo(int column, Path path) throws SpreadsheetException {
				this.column = column;
				this.path = path;

				SPARQLBuilder builder = new SPARQLBuilder(nsManager, owlReasoner);
				path.visit(builder);
				pathString = builder.toString();

				List<Step> stepList = path.asList();
				Step lastStep = stepList.get(stepList.size() - 1);
				if (lastStep instanceof OutStep) {
					URI predicate = ((OutStep) lastStep).getPredicate();
					Set<URI> valueClassSet = owlReasoner.valueType(graph.vertex(predicate));
					for (URI valueClass : valueClassSet) {
						if (owlReasoner.isDatatype(valueClass)) {
							if (datatype == null) {
								datatype = valueClass;
							} else if (!datatype.equals(valueClass)) {
								StringBuilder msg = new StringBuilder();
								msg.append("Conflicting value types for predicate ");
								msg.append(predicate.getLocalName());
								msg.append(": ");
								msg.append(datatype);
								msg.append(" AND ");
								msg.append(valueClass);
								throw new SpreadsheetException(msg.toString());
							}
						}
					}
				}

			}

		}
	}

	static class SheetInfo implements Comparable<SheetInfo> {
		int sheetType;
		int sheetIndex;

		public SheetInfo(int sheetType, int sheetIndex) {
			this.sheetType = sheetType;
			this.sheetIndex = sheetIndex;
		}

		@Override
		public int compareTo(SheetInfo other) {
			int result = sheetType - other.sheetType;
			if (result == 0) {
				result = sheetIndex - other.sheetIndex;
			}
			return result;
		}
	}

	static class EnumShapeVistor implements ShapeVisitor {

		private ShapeWriter shapeWriter;
		private ShapeManager shapeManager;
		private Graph graph;

		public EnumShapeVistor(ShapeWriter shapeWriter, ShapeManager shapeManager, Graph graph) {
			this.shapeWriter = shapeWriter;
			this.shapeManager = shapeManager;
			this.graph = graph;
		}

		@Override
		public void visit(Shape shape) {
			shapeManager.addShape(shape);
			shapeWriter.emitShape(shape, graph);
		}

	}

	static class ShapeTemplate {
		private URI shapeId;
		private String templateText;

		public ShapeTemplate(URI shapeId, String iriTemplate) {
			this.shapeId = shapeId;
			this.templateText = iriTemplate;
		}

		public IriTemplate createTemplate(Shape shape, NamespaceManager nsManager) throws SpreadsheetException {
			SimpleValueFormat format = new SimpleValueFormat(templateText);
			BasicContext context = new BasicContext(null);
			IriTemplate iriTemplate = new IriTemplate();
			iriTemplate.setContext(context);

			for (Element e : format.toList()) {

				switch (e.getType()) {

				case TEXT:
					iriTemplate.addText(e.getText());
					break;

				case VARIABLE:
					String name = e.getText();
					iriTemplate.addVariable(name);
					int colon = name.indexOf(':');
					if (colon > 0) {
						String prefix = name.substring(0, colon);

						Term nsTerm = context.getTerm(prefix);
						if (nsTerm == null) {
							Namespace ns = nsManager.findByPrefix(prefix);
							if (ns != null) {
								nsTerm = new Term(prefix, ns.getName(), Kind.NAMESPACE);
								context.add(nsTerm);
							} else {
								throw new SpreadsheetException("Namespace prefix not defined: " + prefix);
							}
						}
					} else {
						URI p = getPredicateByLocalName(shape, name);
						if (p == null) {

							Namespace ns = nsManager.findByPrefix(name);
							if (ns != null) {
								context.add(new Term(name, ns.getName(), Kind.NAMESPACE));
								break;
							}

							throw new SpreadsheetException(
									"On Shape <" + shape.getId() + "> property not found: " + name);
						}
						String namespace = p.getNamespace();
						Namespace ns = nsManager.findByName(namespace);
						if (ns == null) {
							context.add(new Term(name, p.stringValue(), Kind.PROPERTY));
						} else {
							String prefix = ns.getPrefix();
							Term nsTerm = context.getTerm(prefix);
							if (nsTerm == null) {
								context.add(new Term(prefix, namespace, Kind.NAMESPACE));
							}
							context.add(new Term(name, prefix + ":" + name, Kind.PROPERTY));
						}
					}
					break;
				}
			}
			context.sort();

			return iriTemplate;
		}

		private URI getPredicateByLocalName(Shape shape, String name) {
			for (PropertyConstraint p : shape.getProperty()) {
				URI predicate = p.getPredicate();
				if (predicate != null && name.equals(predicate.getLocalName())) {
					return predicate;
				}
			}
			return null;
		}
	}

	private static class ImportInfo {
		URI targetOntology;
		List<String> importList;

		public ImportInfo(URI targetOntology, List<String> importList) {
			this.targetOntology = targetOntology;
			this.importList = importList;
		}

		public URI getTargetOntology() {
			return targetOntology;
		}

		public List<String> getImportList() {
			return importList;
		}
	}

	private static class PathHandler {

		protected Resource constraint;
		protected URI pathField;
		protected String pathText;

		public PathHandler(Resource propertyConstraint, URI pathField, String pathText) {
			this.constraint = propertyConstraint;
			this.pathField = pathField;
			this.pathText = pathText;
		}

		public void execute(Graph graph, PathFactory pathFactory) throws SpreadsheetException {
			Vertex property = graph.getVertex(constraint);
			if (property == null) {
				throw new SpreadsheetException("PropertyConstraint not found");
			}
			Path path = pathFactory.createPath(pathText);
			Literal pathValue = new LiteralImpl(path.toString());
			property.addProperty(pathField, pathValue);
		}
	}

	private static class PropertyPathHandler extends PathHandler {

		public PropertyPathHandler(Resource propertyConstraint, URI pathField, String pathText) {
			super(propertyConstraint, pathField, pathText);
		}

		public void execute(Graph graph, PathFactory pathFactory) throws SpreadsheetException {
			Vertex property = graph.getVertex(constraint);
			if (property == null) {
				throw new SpreadsheetException("PropertyConstraint not found");
			}
			String text = pathText;
			int c = pathText.charAt(0);
			if (c != '?' && c != '.' && c != '^') {
				text = "." + text;
			}
			Path path = pathFactory.createPath(text);
			PropertyPath propertyPath = PropertyPathUtil.create(path);

			edge(property, pathField, propertyPath);

		}

		private void edge(Vertex subject, URI predicate, PropertyPath object) throws SpreadsheetException {

			if (object instanceof PredicatePath) {
				PredicatePath pp = (PredicatePath) object;
				URI value = pp.getPredicate();
				subject.addProperty(predicate, value);
			} else if (object instanceof SequencePath) {
				SequencePath sequence = (SequencePath) object;
				Vertex priorList = null;
				Graph graph = subject.getGraph();
				for (PropertyPath p : sequence) {
					Vertex list = graph.vertex();
					edge(list, RDF.FIRST, p);
					if (priorList == null) {
						subject.addProperty(predicate, list.getId());
					} else {
						priorList.addProperty(RDF.REST, list.getId());
					}
					priorList = list;
				}
				if (priorList != null) {
					priorList.addProperty(RDF.REST, RDF.NIL);
				}
			} else {
				throw new SpreadsheetException("Unspported PropertyPath type: " + object.getClass().getName());
			}

		}
	}

	private interface FormulaHandler {
		public void execute() throws RDFParseException, IOException, KonigException;
	}

	private class ShapeFormulaHandler implements FormulaHandler {

		private URI shapeId;
		private String formula;

		public ShapeFormulaHandler(URI shapeId, String formula) {
			this.shapeId = shapeId;
			this.formula = formula;
		}

		@Override
		public void execute() throws RDFParseException, IOException, KonigException {

			ShapeManager shapeManager = getShapeManager();
			Shape shape = shapeManager.getShapeById(shapeId);
			if (shape == null) {
				throw new KonigException("Shape not found: " + shapeId);
			}
			propertyOracle.setShape(shape);

			FormulaParser parser = new FormulaParser(propertyOracle, localNameService);
			QuantifiedExpression expression = parser.quantifiedExpression(formula);
			String text = expression.toString();
			Literal literal = vf.createLiteral(text);

			Graph graph = getGraph();
			graph.edge(shapeId, Konig.iriFormula, literal);

		}

	}

	private class PropertyFormulaHandler implements FormulaHandler {
		private URI shapeId;
		private Vertex propertyConstraint;
		private String formula;

		public PropertyFormulaHandler(URI shapeId, Vertex propertyConstraint, String formula) {
			this.shapeId = shapeId;
			this.propertyConstraint = propertyConstraint;
			this.formula = formula;
		}

		public void execute() throws KonigException {
			ShapeManager shapeManager = getShapeManager();
			Shape shape = shapeManager.getShapeById(shapeId);
			if (shape == null) {
				throw new KonigException("Shape not found: " + shapeId);
			}
			propertyOracle.setShape(shape);
			NamespaceMap nsMap = new NamespaceMapAdapter(getNamespaceManager());
			FormulaParser parser = new FormulaParser(propertyOracle, localNameService, nsMap);

			QuantifiedExpression expression = null;
			try {
				expression = parser.quantifiedExpression(formula);
			} catch (Throwable oops) {
				String message = "Failed to parse formula...\n" + "   Shape: <" + shapeId + ">\n" + "   Property: "
						+ propertyConstraint.getValue(SH.path).stringValue() + "\n" + "   Formula: " + formula;
				throw new KonigException(message, oops);
			}

			String text = expression.toString();
			Literal literal = vf.createLiteral(text);

			Graph graph = propertyConstraint.getGraph();

			// PrimaryExpression primary = expression.asPrimaryExpression();
			// if (primary instanceof PathExpression) {
			// PathExpression path = (PathExpression) primary;
			// List<PathStep> stepList = path.getStepList();
			// if (stepList.size()==1) {
			// PathStep step = stepList.get(0);
			// if (step instanceof DirectionStep) {
			// DirectionStep dirStep = (DirectionStep) step;
			// if (dirStep.getDirection() == Direction.OUT) {
			// URI predicate = dirStep.getTerm().getIri();
			// graph.edge(propertyConstraint.getId(), SH.equals, predicate);
			// return;
			// }
			// }
			// }
			// }

			graph.edge(propertyConstraint.getId(), Konig.formula, literal);

		}
		

	}

	

}
