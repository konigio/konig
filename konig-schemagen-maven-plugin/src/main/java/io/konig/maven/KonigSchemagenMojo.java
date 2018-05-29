package io.konig.maven;

/*
 * #%L
 * Konig Schema Generator Maven Plugin
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


import static org.twdata.maven.mojoexecutor.MojoExecutor.artifactId;
import static org.twdata.maven.mojoexecutor.MojoExecutor.configuration;
import static org.twdata.maven.mojoexecutor.MojoExecutor.element;
import static org.twdata.maven.mojoexecutor.MojoExecutor.executeMojo;
import static org.twdata.maven.mojoexecutor.MojoExecutor.executionEnvironment;
import static org.twdata.maven.mojoexecutor.MojoExecutor.goal;
import static org.twdata.maven.mojoexecutor.MojoExecutor.groupId;
import static org.twdata.maven.mojoexecutor.MojoExecutor.name;
import static org.twdata.maven.mojoexecutor.MojoExecutor.plugin;
import static org.twdata.maven.mojoexecutor.MojoExecutor.version;

import java.io.Closeable;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Plugin;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
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
 */

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.apache.velocity.VelocityContext;
import org.codehaus.plexus.util.FileUtils;
import org.konig.omcs.common.GroovyOmcsDeploymentScriptWriter;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;

import com.sun.codemodel.JCodeModel;

import io.konig.aws.common.GroovyAwsDeploymentScriptWriter;
import io.konig.aws.common.GroovyAwsTearDownScriptWriter;
import io.konig.aws.datasource.AwsShapeConfig;
import io.konig.core.ContextManager;
import io.konig.core.Graph;
import io.konig.core.KonigException;
import io.konig.core.NamespaceManager;
import io.konig.core.OwlReasoner;
import io.konig.core.Vertex;
import io.konig.core.impl.MemoryContextManager;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.MemoryNamespaceManager;
import io.konig.core.impl.RdfUtil;
import io.konig.core.impl.SimpleLocalNameService;
import io.konig.core.path.NamespaceMapAdapter;
import io.konig.core.util.BasicJavaDatatypeMapper;
import io.konig.core.util.SimpleValueFormat;
import io.konig.core.vocab.Konig;
import io.konig.data.app.common.DataApp;
import io.konig.data.app.generator.DataAppGenerator;
import io.konig.data.app.generator.DataAppGeneratorException;
import io.konig.data.app.generator.EntityStructureWorker;
import io.konig.datasource.DatasourceFileLocator;
import io.konig.datasource.DdlFileLocator;
import io.konig.estimator.MultiSizeEstimateRequest;
import io.konig.estimator.MultiSizeEstimator;
import io.konig.estimator.SizeEstimateException;
import io.konig.etl.aws.EtlRouteBuilder;
import io.konig.formula.FormulaParser;
import io.konig.formula.ShapePropertyOracle;
import io.konig.gae.datastore.CodeGeneratorException;
import io.konig.gae.datastore.FactDaoGenerator;
import io.konig.gae.datastore.SimpleDaoNamer;
import io.konig.gae.datastore.impl.SimpleEntityNamer;
import io.konig.gcp.common.GoogleCloudService;
import io.konig.gcp.common.GoogleCredentialsNotFoundException;
import io.konig.gcp.common.GroovyDeploymentScriptWriter;
import io.konig.gcp.common.GroovyTearDownScriptWriter;
import io.konig.gcp.common.InvalidGoogleCredentialsException;
import io.konig.gcp.datasource.GcpShapeConfig;
import io.konig.jsonschema.generator.SimpleJsonSchemaTypeMapper;
import io.konig.maven.project.generator.EtlModelGenerator;
import io.konig.maven.project.generator.MavenProjectConfig;
import io.konig.maven.project.generator.MavenProjectGeneratorException;
import io.konig.maven.project.generator.MultiProject;
import io.konig.maven.project.generator.ParentProjectGenerator;
import io.konig.omcs.datasource.OracleShapeConfig;
import io.konig.openapi.generator.OpenApiGenerateRequest;
import io.konig.openapi.generator.OpenApiGenerator;
import io.konig.openapi.generator.OpenApiGeneratorException;
import io.konig.openapi.generator.ShapeLocalNameJsonSchemaNamer;
import io.konig.openapi.generator.TableDatasourceFilter;
import io.konig.openapi.model.OpenAPI;
import io.konig.rio.turtle.NamespaceMap;
import io.konig.schemagen.AllJsonldWriter;
import io.konig.schemagen.OntologySummarizer;
import io.konig.schemagen.SchemaGeneratorException;
import io.konig.schemagen.ShapeMediaTypeLinker;
import io.konig.schemagen.ViewShapeGenerator;
import io.konig.schemagen.avro.AvroNamer;
import io.konig.schemagen.avro.AvroSchemaGenerator;
import io.konig.schemagen.avro.impl.SimpleAvroNamer;
import io.konig.schemagen.avro.impl.SmartAvroDatatypeMapper;
import io.konig.schemagen.aws.AWSAuroraShapeFileCreator;
import io.konig.schemagen.aws.AWSS3BucketWriter;
import io.konig.schemagen.aws.AwsAuroraTableWriter;
import io.konig.schemagen.aws.AwsAuroraViewWriter;
import io.konig.schemagen.aws.AwsResourceGenerator;
import io.konig.schemagen.aws.CloudFormationTemplateWriter;
import io.konig.schemagen.gcp.BigQueryDatasetGenerator;
import io.konig.schemagen.gcp.BigQueryEnumGenerator;
import io.konig.schemagen.gcp.BigQueryEnumShapeGenerator;
import io.konig.schemagen.gcp.BigQueryLabelGenerator;
import io.konig.schemagen.gcp.BigQueryTableMapper;
import io.konig.schemagen.gcp.CloudSqlJsonGenerator;
import io.konig.schemagen.gcp.CloudSqlTableWriter;
import io.konig.schemagen.gcp.DataFileMapperImpl;
import io.konig.schemagen.gcp.DatasetMapper;
import io.konig.schemagen.gcp.EnumShapeVisitor;
import io.konig.schemagen.gcp.GoogleAnalyticsShapeFileCreator;
import io.konig.schemagen.gcp.GoogleAnalyticsUdfGenerator;
import io.konig.schemagen.gcp.GoogleCloudResourceGenerator;
import io.konig.schemagen.gcp.GooglePubSubTopicListGenerator;
import io.konig.schemagen.gcp.LocalNameTableMapper;
import io.konig.schemagen.gcp.NamespaceDatasetMapper;
import io.konig.schemagen.gcp.SimpleDatasetMapper;
import io.konig.schemagen.java.BasicJavaNamer;
import io.konig.schemagen.java.Filter;
import io.konig.schemagen.java.JavaClassBuilder;
import io.konig.schemagen.java.JavaNamer;
import io.konig.schemagen.java.JsonReaderBuilder;
import io.konig.schemagen.java.JsonWriterBuilder;
import io.konig.schemagen.jsonld.ShapeToJsonldContext;
import io.konig.schemagen.jsonschema.JsonSchemaGenerator;
import io.konig.schemagen.jsonschema.JsonSchemaNamer;
import io.konig.schemagen.jsonschema.JsonSchemaTypeMapper;
import io.konig.schemagen.jsonschema.ShapeToJsonSchema;
import io.konig.schemagen.jsonschema.ShapeToJsonSchemaLinker;
import io.konig.schemagen.jsonschema.TemplateJsonSchemaNamer;
import io.konig.schemagen.jsonschema.impl.SmartJsonSchemaTypeMapper;
import io.konig.schemagen.ocms.OracleCloudResourceGenerator;
import io.konig.schemagen.ocms.OracleTableWriter;
import io.konig.schemagen.plantuml.PlantumlClassDiagramGenerator;
import io.konig.schemagen.plantuml.PlantumlGeneratorException;
import io.konig.schemagen.sql.OracleDatatypeMapper;
import io.konig.schemagen.sql.RdbmsShapeGenerator;
import io.konig.schemagen.sql.RdbmsShapeHandler;
import io.konig.schemagen.sql.SqlTableGenerator;
import io.konig.shacl.ClassStructure;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeFilter;
import io.konig.shacl.ShapeManager;
import io.konig.shacl.ShapeMediaTypeNamer;
import io.konig.shacl.ShapeNamer;
import io.konig.shacl.ShapeVisitor;
import io.konig.shacl.SimpleMediaTypeManager;
import io.konig.shacl.impl.MemoryShapeManager;
import io.konig.shacl.impl.ShapeInjector;
import io.konig.shacl.impl.SimpleShapeMediaTypeNamer;
import io.konig.shacl.impl.TemplateShapeNamer;
import io.konig.shacl.io.ShapeFileGetter;
import io.konig.shacl.io.ShapeLoader;
import io.konig.shacl.io.ShapeWriter;
import io.konig.shacl.jsonld.ContextNamer;
import io.konig.showl.WorkbookToTurtleRequest;
import io.konig.showl.WorkbookToTurtleTransformer;
import io.konig.transform.aws.AuroraTransformGenerator;
import io.konig.transform.bigquery.BigQueryTransformGenerator;
import io.konig.transform.factory.ShapeRuleFactory;
import io.konig.transform.mysql.MySqlTransformGenerator;
import io.konig.transform.proto.AwsAuroraChannelFactory;
import io.konig.transform.proto.ShapeModelFactory;
import io.konig.transform.proto.ShapeModelToShapeRule;
import io.konig.transform.sql.factory.SqlFactory;
import io.konig.yaml.Yaml;
import io.konig.yaml.YamlParseException;
import net.sourceforge.plantuml.SourceFileReader;

/**
 * Goal which generates Avro schemas from SHACL data shapes
 *
 */
@Mojo( name = "generate")
public class KonigSchemagenMojo  extends AbstractMojo {
	
	private static final String DEV_NULL = "/dev/null";
	
	@Parameter
	private RdfConfig defaults;
	
	@Parameter
	private RdfConfig rdfOutput;
	
    /**
     * Location of the file.
     */
    @Parameter
    private File avroDir;
    
    
    @Parameter
    private JsonldConfig jsonld;
    
    @Parameter
    private JsonSchemaConfig jsonSchema;
    
    @Parameter
    private File rdfSourceDir;
    
    @Parameter
    private MultiSizeEstimateRequest sizeEstimate;
    

    @Parameter
    private File domainModelPngFile;
    
    @Parameter
    private JavaCodeGeneratorConfig java;
    
    @Parameter
    private WorkbookProcessor workbook;
    
    @Parameter
    private GoogleCloudPlatformConfig googleCloudPlatform;
    
    @Parameter
    private OracleManagedCloudConfig oracleManagedCloud;
    
    @Parameter
    private AmazonWebServicesConfig amazonWebServices;
    
    @Parameter
    private ViewShapeGeneratorConfig viewShapeGenerator;
    
    @Parameter
    private HashSet<String> excludeNamespace;
	

    @Parameter
    private ClassDiagram[] plantUML;

	@Parameter    
	private File namespacesFile;
	 
	@Parameter
	private File projectJsonldFile;
	 
	@Parameter
	private MultiProject multiProject;
	 
    
    private NamespaceManager nsManager;
    private OwlReasoner owlReasoner;
    private ShapeManager shapeManager;
    private ShapeInjector shapeInjector;
    private DatasetMapper datasetMapper;
    private ShapeMediaTypeNamer mediaTypeNamer;
    private Graph owlGraph;
    private ContextManager contextManager;
    private ClassStructure structure;
    private VelocityContext context;
    private SimpleLocalNameService localNameService;
    private FormulaParser formulaParser;

	@Component
	private MavenProject mavenProject;

	@Component
	private MavenSession mavenSession;

	@Component
	private BuildPluginManager pluginManager;
	
	private Configurator configurator;

    public void execute() throws MojoExecutionException   {
    	
    	try {
    		init();
			shapeManager = new MemoryShapeManager();
			shapeInjector = new ShapeInjector((MemoryShapeManager)shapeManager);
			nsManager = new MemoryNamespaceManager();
			mediaTypeNamer = new SimpleShapeMediaTypeNamer();
			owlGraph = new MemoryGraph();
			contextManager = new MemoryContextManager();
			owlReasoner = new OwlReasoner(owlGraph);
			owlGraph.setNamespaceManager(nsManager);

			
			loadResources();
			preprocessResources();
			generateGoogleCloudPlatform();
			generateOracleManagedCloudServices();
			generateAmazonWebServices();
			deleteAmazonWebServices();
			generateJsonld();
			generateAvro();
			generateJsonSchema();
			
			ShapeMediaTypeLinker linker = new ShapeMediaTypeLinker(mediaTypeNamer);
			linker.assignAll(shapeManager.listShapes(), owlGraph);
			
			writeSummary(nsManager, shapeManager, owlGraph);
			
			generatePlantUMLDomainModel();
			generateJava();
			generateDataServices();
			generateMultiProject();
			
			updateRdf();
			
			computeSizeEstimates();
			generateViewShape();
			
		} catch (IOException | SchemaGeneratorException | RDFParseException | RDFHandlerException | 
				PlantumlGeneratorException | CodeGeneratorException | OpenApiGeneratorException | 
				YamlParseException | DataAppGeneratorException | MavenProjectGeneratorException | 
				ConfigurationException | GoogleCredentialsNotFoundException | InvalidGoogleCredentialsException | 
				SizeEstimateException | SQLException e) {
			throw new MojoExecutionException("Schema generation failed", e);
		}
      
    }
    
    private void preprocessResources() throws MojoExecutionException, IOException, RDFParseException, RDFHandlerException {
    	String shapeIriPattern=null;
    	String shapeIriReplacement=null;
    	String propertyNameSpace = null;
    	AuroraInfo aurora=null;
    	BigQueryInfo bigQuery=null;
    	CloudSqlInfo cloudSql=null; 	
	
		
    	if(amazonWebServices!=null && amazonWebServices.getAurora()!=null){
    		aurora=amazonWebServices.getAurora();
    		shapeIriPattern=aurora.getShapeIriPattern();
    		shapeIriReplacement=aurora.getShapeIriReplacement();
    		propertyNameSpace=aurora.getPropertyNameSpace();
    	}
    	else if(googleCloudPlatform!=null && googleCloudPlatform.getBigquery()!=null){
    		bigQuery=googleCloudPlatform.getBigquery();
    		shapeIriPattern=bigQuery.getShapeIriPattern();
    		shapeIriReplacement=bigQuery.getShapeIriReplacement();
    		propertyNameSpace=bigQuery.getPropertyNameSpace();
    		
    	}
    	else if(googleCloudPlatform!=null && googleCloudPlatform.getCloudsql()!=null){
    		cloudSql=googleCloudPlatform.getCloudsql();
    		shapeIriPattern=cloudSql.getShapeIriPattern();
    		shapeIriReplacement=cloudSql.getShapeIriReplacement();
    		propertyNameSpace=cloudSql.getPropertyNameSpace();
    	}
    		
    	
    	if (rdfSourceDir != null && (aurora!=null || bigQuery!=null || cloudSql!=null)) {
    		File shapesDir = new File(rdfSourceDir.getPath()+"/shapes");    		
			if (shapesDir != null) {
				ShapeFileGetter fileGetter = new ShapeFileGetter(shapesDir, nsManager);
				RdbmsShapeGenerator generator = new RdbmsShapeGenerator(formulaParser(), shapeIriPattern, shapeIriReplacement,propertyNameSpace, owlReasoner);
				ShapeWriter shapeWriter = new ShapeWriter();
				RdbmsShapeHandler handler = new RdbmsShapeHandler(shapeInjector, generator, fileGetter, shapeWriter, nsManager);
				handler.visitAll(shapeManager.listShapes());
			}
    	}
	}

	private FormulaParser formulaParser() {
		if (formulaParser == null) {
			SimpleLocalNameService nameService = getLocalNameService();
			NamespaceMap nsMap = new NamespaceMapAdapter(owlGraph.getNamespaceManager());
			ShapePropertyOracle oracle = new ShapePropertyOracle();
			formulaParser = new FormulaParser(oracle, nameService, nsMap);
		}
		
		return formulaParser;
	}

	private SimpleLocalNameService getLocalNameService() {
		if (localNameService == null) {
			localNameService = new SimpleLocalNameService();
			localNameService.addAll(owlGraph);
		}
		return localNameService;
	}

	private void init() throws MojoExecutionException, IOException {
    	GcpShapeConfig.init();
    	OracleShapeConfig.init();
    	AwsShapeConfig.init();
    }

	private void generateDeploymentScript() throws MojoExecutionException, GoogleCredentialsNotFoundException, InvalidGoogleCredentialsException, IOException, SQLException {
		
		GroovyDeploymentScript deploy = googleCloudPlatform.getDeployment();
		if (deploy != null) {
			
			GoogleCloudService googleCloudService = new GoogleCloudService();
			String konigVersion = deploy.getKonigVersion();
			File scriptFile = deploy.getScriptFile();
			
			GroovyDeploymentScriptWriter scriptWriter = new GroovyDeploymentScriptWriter(
					konigVersion, googleCloudPlatform, googleCloudService, scriptFile);
			
			scriptWriter.run();

			GroovyTearDownScriptWriter teardownScriptWriter = new GroovyTearDownScriptWriter(
					konigVersion, googleCloudPlatform, googleCloudService, deploy.getTearDownScriptFile());
			
			teardownScriptWriter.run();
			
		}
			
		
	}


	private void generateMultiProject() throws MavenProjectGeneratorException, IOException {
		if (multiProject != null) {
			ParentProjectGenerator generator = multiProject.run();
			if (multiProject.isAutoBuild()) {
				List<String> goalList = mavenSession.getGoals();
				
				try {
					generator.buildChildren(goalList);
				} catch (MavenInvocationException e) {
					throw new MavenProjectGeneratorException(e);
				}
			}
			
		}
		
	}

	private void generateDataServices() throws IOException, OpenApiGeneratorException, YamlParseException, DataAppGeneratorException, MojoExecutionException {
		DataServicesConfig dataServices = googleCloudPlatform==null ? null : googleCloudPlatform.getDataServices();
    	if (dataServices != null) {
		
			File openapiFile = dataServices.getOpenApiFile();
			File infoFile = dataServices.getInfoFile();
			File configFile = dataServices.getConfigFile();
			
			generateOpenApiSpecification(openapiFile, infoFile);
			generateAppConfigFile(openapiFile, configFile);
			copyDataAppWar(dataServices.getWebappDir());
			copyCredentials(dataServices.getWebappDir());
			
			generateEntityStructure(dataServices.getWebappDir());
			generateGoogleAnalyticsExport(dataServices.getWebappDir());
			
		}
		
	}

	private void generateEntityStructure(File webappDir) throws KonigException, IOException {
		File baseDir = new File(webappDir, "WEB-INF/classes/ClasspathEntityStructureService");
		EntityStructureWorker worker = new EntityStructureWorker(nsManager, shapeManager, baseDir);
		worker.run();
	}
	
	private void generateGoogleAnalyticsExport(File webappDir) throws KonigException, IOException {
		File baseDir = new File(webappDir, "WEB-INF/classes/GoogleAnalyticsExport");
		GoogleAnalyticsShapeFileCreator fileCreator = new GoogleAnalyticsShapeFileCreator(baseDir);
		GoogleAnalyticsUdfGenerator udfGenerator = new GoogleAnalyticsUdfGenerator(fileCreator,shapeManager);		
		udfGenerator.generate(shapeManager.listShapes());
	}

	private void copyCredentials(File webappDir) throws MojoExecutionException, IOException {
		
		File credentials = googleCloudPlatform.getCredentials();
		if (credentials == null) {
			String credentialsPath = System.getenv("GOOGLE_APPLICATION_CREDENTIALS");
			if (credentialsPath == null) {
				String msg =
					"The location of the Google Cloud credentials is not defined. " + 
					"Please define the GOOGLE_APPLICATION_CREDENTIALS " +
					"environment variable, or set the property 'konig.gcp.credentials'.";
				
				throw new MojoExecutionException(msg);
			}
			credentials = new File(credentialsPath);
		}
		
		File target = new File(webappDir, "WEB-INF/classes/konig/gcp/credentials.json");
		
		FileUtils.copyFile(credentials, target);
		
		
	}

	private void copyDataAppWar(File basedir) throws MojoExecutionException {
		
		String konigVersion = mavenProject.getPluginArtifactMap()
				.get("io.konig:konig-schemagen-maven-plugin").getVersion();
		
		
		
		executeMojo(
			plugin(
				groupId("org.apache.maven.plugins"),
				artifactId("maven-dependency-plugin"),
				version("3.0.1")
			),
			goal("unpack"),
			configuration(
				element(
					name("artifactItems"), 
						element(name("artifactItem"),
							element(name("groupId"), "io.konig"),
							element(name("artifactId"), "konig-data-app-gcp"),
							element(name("version"), konigVersion),
							element(name("type"), "war"),
							element(name("overWrite"), "true"),
							element(name("outputDirectory"), basedir.getAbsolutePath())
						)
				)	
			),
			executionEnvironment(
				mavenProject,
				mavenSession,
				pluginManager
			)
		);
		
	}

	private void generateAppConfigFile(File openapiFile, File configFile) throws YamlParseException, IOException, DataAppGeneratorException {
		
		if (!DEV_NULL.equals(configFile) && openapiFile.exists()) {
			OpenAPI api = Yaml.read(OpenAPI.class, openapiFile);
			SimpleMediaTypeManager mediaTypeManager = new SimpleMediaTypeManager(shapeManager);
			DataAppGenerator generator = new DataAppGenerator(mediaTypeManager);
			
			DataApp app = generator.toDataApp(api);
			Yaml.write(configFile, app);
			
		}
	}

	private void generateOpenApiSpecification(File openapiFile, File infoFile) throws IOException, OpenApiGeneratorException {

		openapiFile.getParentFile().mkdirs();
		try (FileReader infoReader = new FileReader(infoFile)) {
		
			try (FileWriter openapiWriter = new FileWriter(openapiFile)) {
			
				OpenApiGenerateRequest request = new OpenApiGenerateRequest()
					.setOpenApiInfo(infoReader)
					.setShapeManager(shapeManager)
					.setWriter(openapiWriter);
				

				io.konig.jsonschema.generator.JsonSchemaNamer namer = new ShapeLocalNameJsonSchemaNamer();
				io.konig.jsonschema.generator.JsonSchemaTypeMapper typeMapper = new SimpleJsonSchemaTypeMapper();
				ShapeFilter shapeFilter = new TableDatasourceFilter();
				io.konig.jsonschema.generator.JsonSchemaGenerator schemaGenerator = 
						new io.konig.jsonschema.generator.JsonSchemaGenerator(nsManager, null, typeMapper);
				
				OpenApiGenerator generator = new OpenApiGenerator(namer, schemaGenerator, shapeFilter);
				
				generator.generate(request);
				
			}	
		}
		
	}

	private void updateRdf() throws RDFHandlerException, IOException {
		if (rdfOutput != null) {
			File shapesDir = rdfOutput.shapesDir(defaults);
			if (shapesDir != null) {
				ShapeFileGetter fileGetter = new ShapeFileGetter(shapesDir, nsManager);
				io.konig.shacl.io.ShapeWriter shapeWriter = new io.konig.shacl.io.ShapeWriter();
				for (Shape shape : shapeManager.listShapes()) {
					Resource shapeId = shape.getId();
					if (shapeId instanceof URI) {
						URI shapeURI = (URI) shapeId;

						Graph graph = new MemoryGraph();
						shapeWriter.emitShape(shape, graph);
						File shapeFile = fileGetter.getFile(shapeURI);
						RdfUtil.prettyPrintTurtle(nsManager, graph, shapeFile);
					}
					
				}
			}
		}
		
	}

	private void loadResources() throws MojoExecutionException, RDFParseException, RDFHandlerException, IOException {

    	if (defaults == null) {
    		defaults = new RdfConfig();
    	}
		loadSpreadsheet();
		
		if (rdfSourceDir == null && 
			(
				jsonld!=null ||
				java != null ||
				plantUML != null ||
				googleCloudPlatform!=null ||
				jsonSchema!=null ||
				oracleManagedCloud != null ||
				amazonWebServices != null
			)
		) {
			rdfSourceDir = defaults.getRdfDir();
		}
		
		if (rdfSourceDir != null) {
			RdfUtil.loadTurtle(rdfSourceDir, owlGraph, nsManager);
			ShapeLoader shapeLoader = new ShapeLoader(contextManager, shapeManager, nsManager);
			shapeLoader.load(owlGraph);
		}
		generateEnumShapes();
		
		
	}

	private void generateEnumShapes() throws MojoExecutionException {
		File enumShapeDir = googleCloudPlatform==null ? null : googleCloudPlatform.enumShapeDir(defaults);
		if (enumShapeDir!=null) {
			
			String enumShapeNameTemplate = googleCloudPlatform.getEnumShapeNameTemplate();
			if (enumShapeNameTemplate != null) {
				ShapeFileGetter fileGetter = new ShapeFileGetter(enumShapeDir, nsManager);
				
				ShapeNamer shapeNamer = new TemplateShapeNamer(nsManager, new SimpleValueFormat(enumShapeNameTemplate));
				ShapeVisitor shapeVisitor = new EnumShapeVisitor(fileGetter, shapeManager);
				BigQueryEnumShapeGenerator generator = new BigQueryEnumShapeGenerator(datasetMapper(), 
						createTableMapper(), shapeNamer, shapeManager, shapeVisitor);
				generator.generateAll(owlReasoner);
			}
		}
		
	}

	private void generateJava() throws IOException, CodeGeneratorException {

		if (java!=null) {
			if (java.getJavaDir()==null) {
				throw new CodeGeneratorException("javaCodeGenerator.javaDir must be defined");
			}
			java.getJavaDir().mkdirs();
			if (java.isGenerateCanonicalJsonReaders()) {
				generateCanonicalJsonReaders();
			}
			generateJavaCode();
		}
		
	}
	
	private ClassStructure classStructure() {
		if (structure == null) {
			SimpleValueFormat iriTemplate = new SimpleValueFormat("http://example.com/shapes/canonical/{targetClassNamespacePrefix}/{targetClassLocalName}");
			structure = new ClassStructure(iriTemplate, shapeManager, owlReasoner);
		}
		return structure;
	}

	private void generateCanonicalJsonReaders() throws IOException, CodeGeneratorException {
		if (java.getPackageRoot() == null) {
			throw new CodeGeneratorException("javaCodeGenerator.packageRoot must be defined");
		}
		JavaNamer javaNamer = new BasicJavaNamer(java.getPackageRoot(), nsManager);
		BasicJavaDatatypeMapper datatypeMapper = new BasicJavaDatatypeMapper();
		JsonReaderBuilder builder = new JsonReaderBuilder(classStructure(), javaNamer, datatypeMapper, owlReasoner);
		JCodeModel model = new JCodeModel();
		builder.produceAll(model);
		
		model.build(java.getJavaDir());
	}

	private void generateJsonSchema() {

		if (jsonSchema != null) {

			JsonSchemaTypeMapper jsonSchemaTypeMapper = new SmartJsonSchemaTypeMapper(owlReasoner);
			JsonSchemaNamer jsonSchemaNamer = TemplateJsonSchemaNamer.namer(nsManager, shapeManager, jsonSchema);
			JsonSchemaGenerator jsonSchemaGenerator = new JsonSchemaGenerator(jsonSchemaNamer, nsManager, jsonSchemaTypeMapper);
			ShapeToJsonSchema generator = new ShapeToJsonSchema(jsonSchemaGenerator);
			generator.setListener(new ShapeToJsonSchemaLinker(owlGraph));
			generator.generateAll(shapeManager.listShapes(), jsonSchema.getJsonSchemaDir());
		}
		
	}

	private void generateAvro() throws IOException {
    	if (avroDir != null) {
    		
    		
    		SmartAvroDatatypeMapper avroMapper = new SmartAvroDatatypeMapper(owlReasoner);
    		AvroNamer namer = new SimpleAvroNamer();
    		AvroSchemaGenerator generator = new AvroSchemaGenerator(avroMapper, namer, nsManager);
    		
    		generator.generateAll(shapeManager.listShapes(), avroDir);
    		
		}
		
	}
	private void generateJsonld() throws SchemaGeneratorException, IOException {

		if (jsonld != null) {
			
			ContextNamer contextNamer = jsonld.contextNamer(nsManager, shapeManager);
			ShapeToJsonldContext generator = new ShapeToJsonldContext(shapeManager, nsManager, contextNamer, owlGraph);
			generator.generateAll(jsonld.getJsonldDir());
		}
		
	}
	private void loadSpreadsheet() throws MojoExecutionException   {
		 try {			
			 if (workbook != null) {				 
				 WorkbookToTurtleTransformer transformer = new WorkbookToTurtleTransformer(datasetMapper(), nsManager);
				 transformer.getWorkbookLoader().setFailOnWarnings(workbook.isFailOnWarnings());
				 transformer.getWorkbookLoader().setFailOnErrors(workbook.isFailOnErrors());
				 transformer.getWorkbookLoader().setInferRdfPropertyDefinitions(workbook.isInferRdfPropertyDefinitions());
				 transformer.transform(workbookToTurleRequest());	
				 if(transformer.getWorkbookLoader().getDataSourceGenerator()!=null ){
					 context=transformer.getWorkbookLoader().getDataSourceGenerator().getContext();
					 if(context.get("ECRRepositoryName")!=null)
						 System.setProperty("ECRRepositoryName", (String)context.get("ECRRepositoryName"));
				 }
			 }
			
		 } catch (Throwable oops) {
			 throw new MojoExecutionException("Failed to transform workbook to RDF", oops);
		 }
	 }


	private WorkbookToTurtleRequest workbookToTurleRequest() {
		WorkbookToTurtleRequest request = new WorkbookToTurtleRequest();
		request.setWorkbookFile(workbook.getWorkbookFile());
		request.setWorkbookDir(workbook.getWorkbookDir());
		request.setOwlOutDir(workbook.owlDir(defaults));
		request.setShapesOutDir(workbook.shapesDir(defaults));
		request.setGcpOutDir(workbook.gcpDir(defaults));
		request.setAwsOutDir(workbook.awsDir(defaults));
		return request;
	}

	private void generatePlantUMLDomainModel() throws IOException, PlantumlGeneratorException, MojoExecutionException {
		if (plantUML != null) {
			
			for (ClassDiagram diagram : plantUML) {
				diagram.setNamespaceManager(nsManager);
				
				if (diagram.getFile() == null) {
					throw new MojoExecutionException("plantUML.file parameter must be defined");
				}
				
				diagram.getFile().getParentFile().mkdirs();
				
				PlantumlClassDiagramGenerator generator = new PlantumlClassDiagramGenerator(owlReasoner);
				diagram.configure(generator);
				
				FileWriter writer = new FileWriter(diagram.getFile());
				try {
					generator.generateDomainModel(classStructure(), writer);
				} finally {
					close(writer);
				}
				
				SourceFileReader reader = new SourceFileReader(diagram.getFile());
				reader.getGeneratedImages();
			}

			
		}
		
	}

	private void close(Closeable stream) {
		try {
			stream.close();
		} catch (IOException oops) {
			oops.printStackTrace();
		}
		
	}
	
	private Configurator configurator() {
		if (configurator == null) {
			configurator = new Configurator(createProperties());
		}
		return configurator;
	}


	private void computeSizeEstimates() throws ConfigurationException, SizeEstimateException, IOException {
		if (sizeEstimate != null) {
			Configurator configurator = configurator();
			configurator.configure(sizeEstimate);
			MultiSizeEstimator estimator = new MultiSizeEstimator(shapeManager);
			estimator.run(sizeEstimate);
		}
		
	}
	
	private void generateAmazonWebServices() throws IOException, ConfigurationException, MojoExecutionException {
		if(amazonWebServices != null) {
			Configurator config = configurator();
			config.configure(amazonWebServices);
			AuroraInfo aurora=amazonWebServices.getAurora();
			File tablesDir=null;
			if(aurora!=null){
				tablesDir = Configurator.checkNull(aurora.getTables());
			}
			File bucketsDir = Configurator.checkNull(amazonWebServices.getS3buckets());
			File transformsDir = Configurator.checkNull(amazonWebServices.getTransforms());
			File cloudFormationDir = Configurator.checkNull(amazonWebServices.getCloudFormationTemplates());
			File viewDir = Configurator.checkNull(amazonWebServices.getAurora().getViews());
			AwsResourceGenerator resourceGenerator = new AwsResourceGenerator();
			
			
			if(tablesDir != null) {
				SqlTableGenerator generator = new SqlTableGenerator();
				DatasourceFileLocator sqlFileLocator = new DdlFileLocator(tablesDir);
				AwsAuroraTableWriter awsAuror = new AwsAuroraTableWriter(tablesDir, generator,sqlFileLocator);
			
				resourceGenerator.add(awsAuror);				
			}
			if (viewDir != null) {
				SqlTableGenerator generator = new SqlTableGenerator();
				DatasourceFileLocator sqlFileLocator = new DdlFileLocator(viewDir);
				ShapeModelFactory shapeModelFactory=new ShapeModelFactory(shapeManager, new AwsAuroraChannelFactory(), owlReasoner);
				AwsAuroraViewWriter awsAuror = new AwsAuroraViewWriter(viewDir, generator,sqlFileLocator,shapeModelFactory);
			
				resourceGenerator.add(awsAuror);	
				
			}
			if(transformsDir != null && amazonWebServices.isEnableAuroraTransform()){
				ShapeModelFactory shapeModelFactory=new ShapeModelFactory(shapeManager, new AwsAuroraChannelFactory(), owlReasoner);
				ShapeRuleFactory shapeRuleFactory=new ShapeRuleFactory(shapeManager, shapeModelFactory, new ShapeModelToShapeRule());
				AuroraTransformGenerator generator=new AuroraTransformGenerator(shapeRuleFactory, new SqlFactory(), new AWSAuroraShapeFileCreator(transformsDir), rdfSourceDir);
				resourceGenerator.add(generator);
			}
			
			CloudFormationTemplateWriter templateWriter = null;
			if(cloudFormationDir != null){
				templateWriter = new CloudFormationTemplateWriter(cloudFormationDir,owlGraph);
				templateWriter.write();
			}
			if(bucketsDir != null){
				 AWSS3BucketWriter awsS3=new AWSS3BucketWriter(bucketsDir,cloudFormationDir);
				 resourceGenerator.add(awsS3);
			}
			resourceGenerator.dispatch(shapeManager.listShapes());
			generateCamelEtl();
			if(templateWriter != null) {
				templateWriter.updateTemplate();
			}
			GroovyAwsDeploymentScriptWriter scriptWriter = new GroovyAwsDeploymentScriptWriter(amazonWebServices);
			scriptWriter.run(); 
		}
	}
	
	private void deleteAmazonWebServices() throws IOException, ConfigurationException {
		if(amazonWebServices != null) {
			Configurator config = configurator();
			config.configure(amazonWebServices);
			AuroraInfo aurora=amazonWebServices.getAurora();
			File tablesDir=null;
			if(aurora!=null)
				tablesDir = Configurator.checkNull(aurora.getTables());
			
			AwsResourceGenerator resourceGenerator = new AwsResourceGenerator();
			if(tablesDir != null) {
				SqlTableGenerator generator = new SqlTableGenerator();
				DatasourceFileLocator sqlFileLocator = new DdlFileLocator(tablesDir);
				AwsAuroraTableWriter awsAuror = new AwsAuroraTableWriter(tablesDir, generator,sqlFileLocator);
			
				resourceGenerator.add(awsAuror);
				resourceGenerator.dispatch(shapeManager.listShapes());
				GroovyAwsTearDownScriptWriter scriptWriter = new GroovyAwsTearDownScriptWriter(amazonWebServices);
				scriptWriter.run(); 
			}
		}
	}
	
	private void generateOracleManagedCloudServices() throws MojoExecutionException, RDFParseException, RDFHandlerException, IOException, ConfigurationException {
	if(oracleManagedCloud != null) {
			Configurator config = configurator();
			config.configure(oracleManagedCloud);
			File directory = Configurator.checkNull(oracleManagedCloud.getDirectory());
			File tablesDir = Configurator.checkNull(oracleManagedCloud.getTables());
			if(directory != null && tablesDir != null) {
				OracleCloudResourceGenerator resourceGenerator = new OracleCloudResourceGenerator();
				SqlTableGenerator sqlgenerator = new SqlTableGenerator(new OracleDatatypeMapper());
				OracleTableWriter oracle = new OracleTableWriter(tablesDir, sqlgenerator);
				resourceGenerator.add(oracle);
				resourceGenerator.dispatch(shapeManager.listShapes());
				GroovyOmcsDeploymentScriptWriter scriptWriter = new GroovyOmcsDeploymentScriptWriter(oracleManagedCloud);
				scriptWriter.run();
			}
		}
	}
	private void generateGoogleCloudPlatform() throws IOException, MojoExecutionException, ConfigurationException, GoogleCredentialsNotFoundException, InvalidGoogleCredentialsException, SQLException {
		if (googleCloudPlatform != null) {
			
			Configurator config = configurator();
			config.configure(googleCloudPlatform);
			
			File gcpDir = googleCloudPlatform.gcpDir(defaults);
			if (gcpDir == null) {
				throw new MojoExecutionException("googleCloudPlatform.gcpDir must be defined");
			}
			
			BigQueryInfo bigQuery = googleCloudPlatform.getBigquery();
			CloudStorageInfo cloudStorage = googleCloudPlatform.getCloudstorage();
			
			GoogleCloudResourceGenerator resourceGenerator = new GoogleCloudResourceGenerator(shapeManager, owlReasoner);
	
			if (bigQuery != null) {
				resourceGenerator.addBigQueryGenerator(bigQuery.getSchema());
				resourceGenerator.addBigQueryViewGenerator(bigQuery.getView());
				
				resourceGenerator.add(labelGenerator());
			}
			if (cloudStorage != null) {
				resourceGenerator.addCloudStorageBucketWriter(cloudStorage.getDirectory());
			}
			if (googleCloudPlatform.getCloudsql() != null) {
				resourceGenerator.add(cloudSqlTableWriter());
				File mysqlScriptsDir = googleCloudPlatform.getCloudsql().getScripts();
				if(mysqlScriptsDir != null) {
					generateMySqlTransformScripts(mysqlScriptsDir);
				}
			}
			if (googleCloudPlatform.getCloudsql().getInstances()!=null) {
				CloudSqlJsonGenerator instanceWriter = new CloudSqlJsonGenerator();
				instanceWriter.writeAll(googleCloudPlatform.getCloudsql(), owlGraph);
			}
			resourceGenerator.add(new GooglePubSubTopicListGenerator(googleCloudPlatform.getTopicsFile()));
			resourceGenerator.dispatch(shapeManager.listShapes());

			if (bigQuery != null) {

				BigQueryEnumGenerator enumGenerator = new BigQueryEnumGenerator(shapeManager);
				
				File bqDataDir = Configurator.checkNull(bigQuery.getData());
				File bqSchemaDir = Configurator.checkNull(bigQuery.getSchema());
				File bqDatasetDir = Configurator.checkNull(bigQuery.getDataset());
				File bqScriptsDir = Configurator.checkNull(bigQuery.getScripts());
				
				if (bqDataDir != null) {
					DataFileMapperImpl dataFileMapper = new DataFileMapperImpl(bqDataDir, datasetMapper, createTableMapper());
					enumGenerator.generate(owlGraph, dataFileMapper);
				}
				if (bqSchemaDir != null && bqDatasetDir!=null) {
					BigQueryDatasetGenerator datasetGenerator = new BigQueryDatasetGenerator(bqSchemaDir, bqDatasetDir);
					datasetGenerator.run();
				}
				if (bqScriptsDir != null) {
					generateTransformScripts(bqScriptsDir);
				}
			}

			generateDeploymentScript();
		}
	}

	
	private CloudSqlTableWriter cloudSqlTableWriter() {
		CloudSqlInfo info = googleCloudPlatform.getCloudsql();
		SqlTableGenerator generator = new SqlTableGenerator();
		DatasourceFileLocator sqlFileLocator = new DdlFileLocator(info.getTables());
		return new CloudSqlTableWriter(generator, sqlFileLocator);
	}

	private void generateMySqlTransformScripts(File outDir) throws MojoExecutionException {
		if (googleCloudPlatform.isEnableMySqlTransform()) {
			MySqlTransformGenerator generator = new MySqlTransformGenerator(shapeManager, outDir, owlReasoner);
			generator.generateAll();
			List<Throwable> errorList = generator.getErrorList();
			if (errorList != null && !errorList.isEmpty()) {
				Log logger = getLog();
				for (Throwable e : errorList) {
					logger.error(e.getMessage());
				}
				throw new MojoExecutionException("Failed to generate MySql Transform", errorList.get(0));
			}
		}
	}

	private BigQueryLabelGenerator labelGenerator() {
		File schemaDir = googleCloudPlatform.getBigquery().getSchema();
		File dataDir = googleCloudPlatform.getBigquery().getData();
		MetadataInfo metadata = googleCloudPlatform.getBigquery().getMetadata();
		if (metadata.isSkip()) {
			return null;
		}
		
		String metaDatasetId = metadata.getDataset();
		
		File schemaFile = new File(schemaDir, metaDatasetId + ".FieldLabel.json");
		File dataFile = new File(dataDir, metaDatasetId + ".FieldLabel");
		
		return new BigQueryLabelGenerator(owlGraph, schemaFile, dataFile, metaDatasetId);
	}



	private void generateTransformScripts(File outDir) throws MojoExecutionException {
		
		
		if (googleCloudPlatform.isEnableBigQueryTransform()) {
			
		
			BigQueryTransformGenerator generator = new BigQueryTransformGenerator(shapeManager, outDir, owlReasoner,rdfSourceDir);
			generator.generateAll();
			List<Throwable> errorList = generator.getErrorList();
			if (errorList != null && !errorList.isEmpty()) {
				Log logger = getLog();
				for (Throwable e : errorList) {
					logger.error(e.getMessage());
				}
				throw new MojoExecutionException("Failed to generate BigQuery Transform", errorList.get(0));
			}
		}
		
	}
	
	private Properties createProperties() {
		Properties properties = new Properties(System.getProperties());
		
		for (Entry<Object,Object> e : mavenProject.getProperties().entrySet()) {
			String key = e.getKey().toString();
			String value = e.getValue().toString();
			properties.put(key, value);
		}
		properties.put("project", mavenProject);
		
		if (!properties.containsKey("konig.version")) {
			Plugin plugin = mavenProject.getPlugin("io.konig:konig-gcp-deploy-maven-plugin");
			if (plugin != null) {
				properties.setProperty("konig.version", plugin.getVersion());
			}
		}
		return properties;
	}

	private BigQueryTableMapper createTableMapper() {
		return new LocalNameTableMapper();
	}


	private DatasetMapper datasetMapper() {
		if (datasetMapper == null) {

			String bigQueryDatasetId = googleCloudPlatform==null ? null : googleCloudPlatform.getBigQueryDatasetId();
			
			if (bigQueryDatasetId != null) {
				datasetMapper = new SimpleDatasetMapper(bigQueryDatasetId);
			} else {
				datasetMapper = new NamespaceDatasetMapper(nsManager);
			}
		}
		return datasetMapper;
	}

	private void generateJavaCode() throws IOException, CodeGeneratorException {
		
		final JCodeModel model = new JCodeModel();
		if (java.getPackageRoot()==null) {
			throw new CodeGeneratorException("javaCodeGenerator.packageRoot must be defined");
		}
		
		JavaNamer javaNamer = new BasicJavaNamer(java.getPackageRoot(), nsManager);
		Filter filter = new Filter(java.getFilter());
		ClassStructure structure = classStructure();
		JavaClassBuilder classBuilder = new JavaClassBuilder(structure, javaNamer, owlReasoner, filter);
		final JsonWriterBuilder writerBuilder = new JsonWriterBuilder(structure, owlReasoner, shapeManager, javaNamer, filter);
		
		classBuilder.buildAllClasses(model);
		writerBuilder.buildAll(shapeManager.listShapes(), model);
		
		if (java.getGoogleDatastoreDaoPackage() != null) {
			SimpleEntityNamer entityNamer = new SimpleEntityNamer();
			SimpleDaoNamer daoNamer = new SimpleDaoNamer(java.getGoogleDatastoreDaoPackage(), nsManager);
			FactDaoGenerator daoGenerator = new FactDaoGenerator()
				.setDaoNamer(daoNamer)
				.setDatatypeMapper(classBuilder.getMapper())
				.setEntityNamer(entityNamer)
				.setJavaNamer(javaNamer)
				.setShapeManager(shapeManager);
			
			daoGenerator.generateAllFactDaos(model);
		}
		
		
		model.build(java.getJavaDir());
		
	}

	private void writeSummary(NamespaceManager nsManager, ShapeManager shapeManager, Graph owlGraph) throws IOException  {
		
		OntologySummarizer summarizer = new OntologySummarizer();
		
		if (namespacesFile != null) {
			namespacesFile.getParentFile().mkdirs();

			summarizer.summarize(nsManager, owlGraph, namespacesFile);
		}
		
		
		if (projectJsonldFile != null) {
			projectJsonldFile.getParentFile().mkdirs();
			AllJsonldWriter all = new AllJsonldWriter();
			all.writeJSON(nsManager, owlGraph, excludeNamespace, projectJsonldFile);
		}
		
	}
	private void generateCamelEtl() throws MojoExecutionException {
		File outDir = null;
		Map<String, Object> services=new HashMap<>();
		Shape dockerComposer = null;
		try {
			File derivedDir = new File(rdfSourceDir, "shape-dependencies");
		
			if (derivedDir.exists()) {
				Graph graph = new MemoryGraph();
				NamespaceManager nsManager = new MemoryNamespaceManager();
				RdfUtil.loadTurtle(derivedDir, graph, nsManager);
				
				for (Vertex targetShapeVertex : graph.vertices()) {					
					Shape targetShape = shapeManager.getShapeById(targetShapeVertex.getId());
					dockerComposer=targetShape;
					if (targetShape.hasDataSourceType(Konig.AwsAuroraTable)) {
						if(amazonWebServices != null) {
							outDir = amazonWebServices.getCamelEtl();
						}						
						List<Vertex> sourceList = targetShapeVertex.asTraversal().out(Konig.DERIVEDFROM).toVertexList();
						
						if (!sourceList.isEmpty()) {
							Vertex sourceShapeVertex = sourceList.get(0);
							Resource sourceShapeId = sourceShapeVertex.getId();
							Shape sourceShape = shapeManager.getShapeById(sourceShapeId);

							if (sourceShape.hasDataSourceType(Konig.AwsAuroraTable)
									&& sourceShape.hasDataSourceType(Konig.S3Bucket)) {
								EtlModelGenerator etlModelGenerator=new EtlModelGenerator(new MavenProjectConfig(),new File(amazonWebServices.getBaseDirectory(),"../"),new URIImpl(targetShape.getId().stringValue()).getLocalName());
								etlModelGenerator.run();
								EtlRouteBuilder builder = new EtlRouteBuilder(sourceShape, targetShape, outDir);
								builder.setEtlBaseDir(amazonWebServices.getBaseDirectory());
								builder.generate();
								String serviceName = new URIImpl(targetShape.getId().stringValue()).getLocalName();
								Map<String, Object> service=new HashMap<>();
								service.put("image", "etl-"+serviceName+":latest");
								services.put(serviceName, service);
							}
						}
					}
					
				}
				EtlRouteBuilder builder = new EtlRouteBuilder(null, dockerComposer, outDir);
				
				builder.createDockerComposeFile(services);
				createImageList(services, outDir);
			}
			
		} catch (Exception ex) {
			throw new MojoExecutionException("Failed to generate camel etl routes", ex);
		}
		
	}
	private void createImageList(Map services, File outDir ) throws IOException {
		File cloudformationtemplateDir = new File(outDir.getParent(), "cloudformationtemplate");

		if (!cloudformationtemplateDir.exists())
			cloudformationtemplateDir.mkdirs();
		
		FileWriter writer = null;
		try {
			File imageListFile = new File(cloudformationtemplateDir, "ImageList.txt");
			writer = new FileWriter(imageListFile, true);
			Iterator iterator = services.entrySet().iterator();
			while(iterator.hasNext()) {
		         Map.Entry service = (Map.Entry)iterator.next();	
		         Map images = (HashMap)service.getValue();
		         writer.append(images.get("image").toString().toLowerCase());
		         writer.append("\n");
			}
		} finally {
			writer.flush();
			writer.close();
		}
	}
	
	private void generateViewShape() throws RDFParseException, RDFHandlerException, IOException {
		if(defaults.getShapesDir() != null && viewShapeGenerator != null) {
			RdfUtil.loadTurtle(defaults.getRdfDir(), owlGraph, nsManager);
			ShapeLoader shapeLoader = new ShapeLoader(contextManager, shapeManager, nsManager);
			shapeLoader.load(owlGraph);
			
			File shapesDir = defaults.getShapesDir();
			ViewShapeGenerator shapeGenerator = new ViewShapeGenerator(nsManager, shapeManager, viewShapeGenerator);
			shapeGenerator.generate(shapesDir);
		}
	}
}
