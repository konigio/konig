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
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.DistributionManagement;
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
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.apache.velocity.runtime.parser.ParseException;
import org.codehaus.plexus.util.FileUtils;
import org.konig.omcs.common.GroovyOmcsDeploymentScriptWriter;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;

import com.sun.codemodel.JCodeModel;

import io.konig.abbrev.AbbreviationConfig;
import io.konig.abbrev.AbbreviationManager;
import io.konig.abbrev.MemoryAbbreviationManager;
import io.konig.aws.common.GroovyAwsDeploymentScriptWriter;
import io.konig.aws.common.GroovyAwsTearDownScriptWriter;
import io.konig.aws.datasource.AwsShapeConfig;
import io.konig.cadl.CubeEmitter;
import io.konig.cadl.CubeManager;
import io.konig.core.ContextManager;
import io.konig.core.Graph;
import io.konig.core.KonigException;
import io.konig.core.NamespaceManager;
import io.konig.core.OwlReasoner;
import io.konig.core.impl.MemoryContextManager;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.MemoryNamespaceManager;
import io.konig.core.impl.RdfUtil;
import io.konig.core.impl.SimpleLocalNameService;
import io.konig.core.io.SkosEmitter;
import io.konig.core.project.Project;
import io.konig.core.project.ProjectFolder;
import io.konig.core.project.ProjectManager;
import io.konig.core.showl.BasicTransformService;
import io.konig.core.showl.CompositeNodeShapeConsumer;
import io.konig.core.showl.CompositeSourceNodeSelector;
import io.konig.core.showl.DataLayerSourceNodeSelector;
import io.konig.core.showl.DataSourceTypeSourceNodeSelector;
import io.konig.core.showl.DestinationTypeTargetNodeShapeFactory;
import io.konig.core.showl.ExplicitDerivedFromSelector;
import io.konig.core.showl.HasDataSourceTypeSelector;
import io.konig.core.showl.LineageShowlNodeShapeConsumer;
import io.konig.core.showl.MappingReport;
import io.konig.core.showl.RawCubeSourceNodeSelector;
import io.konig.core.showl.ReceivesDataFromSourceNodeFactory;
import io.konig.core.showl.ShowlClassProcessor;
import io.konig.core.showl.ShowlManager;
import io.konig.core.showl.ShowlNodeListingConsumer;
import io.konig.core.showl.ShowlNodeShape;
import io.konig.core.showl.ShowlNodeShapeBuilder;
import io.konig.core.showl.ShowlService;
import io.konig.core.showl.ShowlServiceImpl;
import io.konig.core.showl.ShowlSourceNodeFactory;
import io.konig.core.showl.ShowlTargetNodeSelector;
import io.konig.core.showl.ShowlTransformEngine;
import io.konig.core.showl.ShowlTransformService;
import io.konig.core.util.BasicJavaDatatypeMapper;
import io.konig.core.util.SimpleValueFormat;
import io.konig.core.util.ValueFormat;
import io.konig.core.vocab.Konig;
import io.konig.core.vocab.XOWL;
import io.konig.core.vocab.XSD;
import io.konig.data.app.common.DataApp;
import io.konig.data.app.generator.DataAppGenerator;
import io.konig.data.app.generator.DataAppGeneratorException;
import io.konig.data.app.generator.EntityStructureWorker;
import io.konig.datasource.DataSource;
import io.konig.datasource.TableDataSource;
import io.konig.estimator.MultiSizeEstimateRequest;
import io.konig.estimator.MultiSizeEstimator;
import io.konig.estimator.SizeEstimateException;
//import io.konig.etl.aws.EtlRouteBuilder;
//import io.konig.etl.gcp.GcpEtlRouteBuilder;
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
import io.konig.gcp.deployment.DeploymentConfigEmitter;
import io.konig.gcp.deployment.GcpConfigManager;
import io.konig.jsonschema.generator.SimpleJsonSchemaTypeMapper;
import io.konig.lineage.LineageEmitter;
import io.konig.maven.project.generator.MavenProjectGeneratorException;
import io.konig.maven.project.generator.MultiProject;
import io.konig.maven.project.generator.ParentProjectGenerator;
import io.konig.maven.project.generator.XmlSerializer;
import io.konig.omcs.datasource.OracleShapeConfig;
import io.konig.openapi.generator.OpenApiGenerateRequest;
import io.konig.openapi.generator.OpenApiGenerator;
import io.konig.openapi.generator.OpenApiGeneratorException;
import io.konig.openapi.generator.ShapeLocalNameJsonSchemaNamer;
import io.konig.openapi.generator.TableDatasourceFilter;
import io.konig.openapi.model.OpenAPI;
import io.konig.schemagen.AllJsonldWriter;
import io.konig.schemagen.CalculateMaximumRowSize;
import io.konig.schemagen.InvalidDatatypeException;
import io.konig.schemagen.OntologySummarizer;
import io.konig.schemagen.SchemaGeneratorException;
import io.konig.schemagen.ShapeMediaTypeLinker;
import io.konig.schemagen.TabularShapeGenerationException;
import io.konig.schemagen.TabularShapeGenerator;
import io.konig.schemagen.avro.AvroNamer;
import io.konig.schemagen.avro.AvroSchemaGenerator;
import io.konig.schemagen.avro.impl.SimpleAvroNamer;
import io.konig.schemagen.avro.impl.SmartAvroDatatypeMapper;
import io.konig.schemagen.aws.AWSS3BucketWriter;
import io.konig.schemagen.aws.AwsAuroraTableWriter;
import io.konig.schemagen.aws.AwsAuroraViewWriter;
import io.konig.schemagen.aws.AwsResourceGenerator;
import io.konig.schemagen.aws.CloudFormationTemplateWriter;
import io.konig.schemagen.env.EnvironmentGenerationException;
import io.konig.schemagen.env.EnvironmentGenerator;
import io.konig.schemagen.gcp.BigQueryDatasetGenerator;
import io.konig.schemagen.gcp.BigQueryEnumGenerator;
import io.konig.schemagen.gcp.BigQueryEnumShapeGenerator;
import io.konig.schemagen.gcp.BigQueryLabelGenerator;
import io.konig.schemagen.gcp.BigQueryTableGenerator;
import io.konig.schemagen.gcp.BigQueryTableMapper;
import io.konig.schemagen.gcp.CloudSqlTableWriter;
import io.konig.schemagen.gcp.DataFileMapperImpl;
import io.konig.schemagen.gcp.DatasetMapper;
import io.konig.schemagen.gcp.GoogleAnalyticsShapeFileCreator;
import io.konig.schemagen.gcp.GoogleAnalyticsUdfGenerator;
import io.konig.schemagen.gcp.GoogleCloudResourceGenerator;
import io.konig.schemagen.gcp.GooglePubSubTopicListGenerator;
import io.konig.schemagen.gcp.LocalNameTableMapper;
import io.konig.schemagen.gcp.NamespaceDatasetMapper;
import io.konig.schemagen.gcp.SimpleDatasetMapper;
import io.konig.schemagen.io.CompositeEmitter;
import io.konig.schemagen.io.NamedGraphEmitter;
import io.konig.schemagen.io.OntologyEmitter;
import io.konig.schemagen.io.ShapeToFileEmitter;
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
import io.konig.schemagen.packaging.MavenPackage;
import io.konig.schemagen.packaging.MavenPackagingProjectGenerator;
import io.konig.schemagen.packaging.PackagingProjectRequest;
import io.konig.schemagen.packaging.ResourceKind;
import io.konig.schemagen.plantuml.PlantumlClassDiagramGenerator;
import io.konig.schemagen.plantuml.PlantumlGeneratorException;
import io.konig.schemagen.sql.KonigIdLinkingStrategy;
import io.konig.schemagen.sql.OracleDatatypeMapper;
import io.konig.schemagen.sql.SqlTableGenerator;
import io.konig.schemagen.sql.SyntheticKeyLinkingStrategy;
import io.konig.schemagen.sql.TabularLinkingStrategy;
import io.konig.schemagen.sql.TabularShapeException;
import io.konig.schemagen.sql.TabularShapeFactory;
import io.konig.shacl.ClassStructure;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeFilter;
import io.konig.shacl.ShapeManager;
import io.konig.shacl.ShapeMediaTypeNamer;
import io.konig.shacl.ShapeNamer;
import io.konig.shacl.ShapeReasoner;
import io.konig.shacl.SimpleMediaTypeManager;
import io.konig.shacl.impl.MemoryShapeManager;
import io.konig.shacl.impl.ShapeInjector;
import io.konig.shacl.impl.SimpleShapeMediaTypeNamer;
import io.konig.shacl.impl.TemplateShapeNamer;
import io.konig.shacl.io.DdlFileEmitter;
import io.konig.shacl.io.ShapeAuxiliaryWriter;
import io.konig.shacl.io.ShapeLoader;
import io.konig.shacl.jsonld.ContextNamer;
import io.konig.spreadsheet.GcpDeploymentSheet;
import io.konig.spreadsheet.SettingsSheet;
import io.konig.spreadsheet.SpreadsheetException;
import io.konig.spreadsheet.WorkbookProcessorImpl;
import io.konig.transform.beam.BeamTransformGenerationException;
import io.konig.transform.beam.BeamTransformGenerator;
import io.konig.transform.beam.BeamTransformRequest;
import io.konig.transform.model.ShapeTransformException;
import io.konig.transform.mysql.RoutedSqlTransformVisitor;
import io.konig.transform.mysql.SqlTransformGenerator;
import io.konig.transform.proto.AwsAuroraChannelFactory;
import io.konig.transform.proto.ShapeModelFactory;
import io.konig.validation.IdNamePair;
import io.konig.validation.MavenProjectId;
import io.konig.validation.ModelValidationReport;
import io.konig.validation.ModelValidationRequest;
import io.konig.validation.ModelValidationSummaryWriter;
import io.konig.validation.ModelValidator;
import io.konig.validation.PlainTextModelValidationReportWriter;
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
    
    private static final String GCP_PATTERN = "$'{'basedir'}'/src/main/resources/env/{0}/gcp";
	
	@Parameter(defaultValue="${basedir}/target/generated/rdf")
	private File rdfDir;
	
	@Parameter
	private RdfModelConfig rdfModel;

    /**
     * Location of the file.
     */
    @Parameter
    private File avroDir;
    
    @Parameter 
    private File settingsSourceDir;
    
    @Parameter(defaultValue="${basedir}/target/generated/settings")
    private File settingsDir;
    
    @Parameter(defaultValue="${basedir}/target/deploy/src/main/resources/env/")
    private File envDir;
    
    @Parameter
    private JsonldConfig jsonld;
    
    @Parameter
    private JsonSchemaConfig jsonSchema;
    
    @Parameter
    private TabularShapeFactoryConfig tabularShapes;
    
    @Parameter
    private File rdfSourceDir;
    
    @Parameter
    private MultiSizeEstimateRequest sizeEstimate;
    
    @Parameter
    private ModelValidationConfig modelValidation;

    @Parameter
    private File domainModelPngFile;
    
    @Parameter
    private JavaCodeGeneratorConfig java;
    
    @Parameter
    private WorkbookProcessorConfig workbook;
    
    @Parameter
    private GoogleCloudPlatformConfig googleCloudPlatform;
    
    @Parameter
    private OracleManagedCloudConfig oracleManagedCloud;
    
    @Parameter
    private AmazonWebServicesConfig amazonWebServices;
    
    @Parameter
    private TabularShapeGeneratorConfig config;
    
    @Parameter
    private HashSet<String> excludeNamespace;
	
    @Parameter
	private OwlProfile[] profiles;
    
    @Parameter
    private OwlInference[] inferences;

    @Parameter
    private ClassDiagram[] plantUML;

	@Parameter    
	private File namespacesFile;
	 
	@Parameter
	private File projectJsonldFile;
	 
	@Parameter
	private MultiProject multiProject;
	
	@Parameter
	private BuildTarget buildTarget;
	
	@Parameter
	private Dependency[] dependencies;
	
	@Parameter
	private boolean failOnError = true;
	 

	private RdfConfig rdf;
    private NamespaceManager nsManager;
    private OwlReasoner owlReasoner;
    private ShapeManager shapeManager;
    private ShapeInjector shapeInjector;
    private DatasetMapper datasetMapper;
    private ShapeMediaTypeNamer mediaTypeNamer;
    private Graph owlGraph;
    private AbbreviationManager abbrevManager;
    private ContextManager contextManager;
    private ClassStructure structure;
    private SimpleLocalNameService localNameService;
    private CompositeEmitter emitter;
    private Project project;
    private SqlTransformGenerator mysqlTransformGenerator;
    private RoutedSqlTransformVisitor sqlTransformVisitor;
    private File mavenHome;
    private boolean anyError;
    private CubeManager cubeManager;
    private LineageEmitter lineageEmitter;

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
			owlGraph = new MemoryGraph(nsManager);
			contextManager = new MemoryContextManager();
			owlReasoner = new OwlReasoner(owlGraph);
			
			emitter = new CompositeEmitter();
			copySettings();
			createProject();
			loadResources();
			preprocessResources();
			generateModelValidationReport();
			generateGoogleCloudPlatform();
			generateOracleManagedCloudServices();
			generateAmazonWebServices();
			deleteAmazonWebServices();
			generateJsonld();
			generateAvro();
			generateJsonSchema();
			generateMappingReport();
			generateSqlTransforms();
			
			ShapeMediaTypeLinker linker = new ShapeMediaTypeLinker(mediaTypeNamer);
			linker.assignAll(shapeManager.listShapes(), owlGraph);
			
			writeSummary(nsManager, shapeManager, owlGraph);
			
			generatePlantUMLDomainModel();
			generateJava();
			generateDataServices();
			generateMultiProject();
			
			computeSizeEstimates();
			generateTabularShapes();
			computeMaxRowSize();
			
			emit();
			
			buildEnvironments();
			failIfAnyError();
			buildPackagingProject();
			
		} catch (IOException | SchemaGeneratorException | RDFParseException | RDFHandlerException | 
				PlantumlGeneratorException | CodeGeneratorException | OpenApiGeneratorException | 
				YamlParseException | DataAppGeneratorException | MavenProjectGeneratorException | 
				ConfigurationException | GoogleCredentialsNotFoundException | InvalidGoogleCredentialsException | 
				SizeEstimateException | KonigException | SQLException | InvalidDatatypeException | 
				ShapeTransformException | EnvironmentGenerationException | ParseException | MojoExecutionException |
				MavenInvocationException | BeamTransformGenerationException e) {
			
			if (failOnError) {
				
				throw e instanceof MojoExecutionException ? 
					(MojoExecutionException) e :
					new MojoExecutionException("Schema generation failed", e);
			} else {
				getLog().error("Swallowing fatal error since failOnError=false", e);
			}
		}
      
    }
    

	


	private void failIfAnyError() throws MojoExecutionException {
		if (anyError) {
			throw new MojoExecutionException("One or more errors occurred.  See the log for details");
		}
		
	}


	private void buildPackagingProject() throws IOException, ParseException, MavenInvocationException {
		MavenPackagingProjectGenerator generator = new MavenPackagingProjectGenerator();
		PackagingProjectRequest request = new PackagingProjectRequest();
		request.setBasedir(mavenProject.getBasedir());
		
		io.konig.schemagen.packaging.MavenProject deployProject = new io.konig.schemagen.packaging.MavenProject();
		deployProject.setGroupId(mavenProject.getGroupId());
		deployProject.setArtifactId(mavenProject.getArtifactId());
		deployProject.setVersion(mavenProject.getVersion());
		deployProject.setName(mavenProject.getName());
		
		request.setMavenProject(deployProject);
		
		File logDir = new File("target/logs");
		logDir.mkdirs();
		request.setVelocityLogFile(new File(logDir, "velocity.log"));
		
		if (googleCloudPlatform != null) {
			request.addPackage(
				new MavenPackage(ResourceKind.gcp)
					.include(GCP_PATTERN, "/gcp", "deployment/config.yaml")
			);
		}
		
		File pomFile = generator.generate(request);
		
		InvocationRequest invokeRequest = new DefaultInvocationRequest();
		List<String> goalList = mavenSession.getGoals();
		invokeRequest.setPomFile(pomFile);
		invokeRequest.setGoals(goalList);
		
		Invoker invoker = new DefaultInvoker();
		invoker.setMavenHome(mavenHome());
		invoker.execute(invokeRequest);
		
		
		
	}


	/**
	 * Generates environment-specific resource files.
	 * 
	 * For example, if the project generates GCP resources, then the environment-specific
	 * versions of those resources will be found at...
	 * <pre>
	 *   /target/deploy/src/main/resources/env/{environmentName}/gcp/...
	 * </pre>
	 */
	private void buildEnvironments() throws FileNotFoundException, EnvironmentGenerationException, IOException {
		
		File velocityLog = new File(project.getBaseDir(), "target/logs/EnvironmentGenerator/velocity.log");
		velocityLog.getParentFile().mkdirs();
		
		EnvironmentGenerator generator = new EnvironmentGenerator(velocityLog);
		
		// TODO: Find a better way to define the source directory
		File sourceDir = rdf.getOwlDir().getParentFile().getParentFile();
		
		generator.run(settingsDir, sourceDir, envDir);
		
		
	}


	private void copySettings() throws IOException {
		if (settingsSourceDir != null && settingsSourceDir.isDirectory()) {
			settingsDir.mkdirs();
			for (File child : settingsSourceDir.listFiles()) {
				if (child.getName().endsWith(".properties")) {
					File dest = new File(settingsDir, child.getName());
					Files.copy(child.toPath(), dest.toPath());
				}
			}
		}
		
	}


	private void generateMappingReport() throws IOException {
		if (rdfModel!=null && rdfModel.isPrintMappings()) {
			
			ShowlManager showlManager = new ShowlManager(shapeManager, owlReasoner);
			showlManager.load();
			
			MappingReport report = new MappingReport();
			File rdfDir = rdfDir();
			rdfDir.mkdirs();
			File outFile = new File(rdfDir, "mappingReport.txt");
			
			try (FileWriter out = new FileWriter(outFile)) {
				report.write(out, showlManager, nsManager);
			}
		}
		
	}


	private void generateSqlTransforms() throws ShapeTransformException {
//		if (mysqlTransformGenerator != null) {
//			mysqlTransformGenerator.buildAll(shapeManager, owlReasoner);
//		}
		
	}


	private void generateModelValidationReport() throws IOException, ConfigurationException {
		if (modelValidation != null) {
			Configurator configurator = configurator();
			configurator.configure(modelValidation);
			ModelValidationRequest request = new ModelValidationRequest(owlReasoner, shapeManager);
			if (modelValidation.getNamingConventions() != null) {
				request.setCaseStyle(modelValidation.getNamingConventions());
			}
			request.setCommentConventions(modelValidation.getCommentConventions());
			request.addAll(modelValidation.getNamespaces());

			ModelValidator validator = new ModelValidator();
			
			ModelValidationReport report = validator.process(request);
			
			report.setProject(new IdNamePair(
				new MavenProjectId(
					mavenProject.getGroupId(),
					mavenProject.getArtifactId()), 
				mavenProjectName()));
			
			report.setNamespaceManager(nsManager);
			PlainTextModelValidationReportWriter reportWriter = new PlainTextModelValidationReportWriter();
			
			File textFile = modelValidation.getTextReportFile();
			
			textFile.getParentFile().mkdirs();
			try (FileWriter out = new FileWriter(textFile)) {
				reportWriter.writeReport(report, out);
			}
			
			File summaryFile = new File(textFile.getParentFile(), "model-validation-summary.csv");
			try (FileWriter out = new FileWriter(summaryFile)) {
				ModelValidationSummaryWriter summaryWriter = new ModelValidationSummaryWriter(true);
				
				summaryWriter.writeReport(report, out);
			}
		}
		
	}


	private String mavenProjectName() {
		String name = mavenProject.getName().trim();
		if (name == null || name.length()==0) {
			name = mavenProject.getArtifactId();
		}
		return name;
	}


	private void emit() throws KonigException, IOException, RDFHandlerException {
		emitter.emit(owlGraph);
		
		writeDdlFiles(googleCloudPlatform, Konig.GoogleBigQueryTable, Konig.GoogleBigQueryView, Konig.GoogleCloudSqlTable);
		writeDdlFiles(amazonWebServices, Konig.AwsAuroraTable, Konig.AwsAuroraView);

	}

	private void writeDdlFiles(RdfSource rdfSource, URI...datasourceType) throws RDFHandlerException, IOException {
		if (rdfSource != null && anyDdlFiles(datasourceType)) {
			File file = new File(rdfSource.getRdfDirectory(), "ddlFiles.ttl");
			writeDdlFiles(file, datasourceType);
		}
	}

	private void writeDdlFiles(File file, URI... datasourceType) throws RDFHandlerException, IOException {
		ShapeAuxiliaryWriter writer = new ShapeAuxiliaryWriter(file);
		writer.addShapeEmitter(new DdlFileEmitter(datasourceType));
		writer.writeAll(nsManager, shapeManager.listShapes());
	}

	private boolean anyDdlFiles(URI... datasourceTypeId) {
		if (shapeManager != null) {
			List<Shape> shapeList = shapeManager.listShapes();
			if (shapeList != null) {
				for (Shape shape : shapeList) {
					List<DataSource> datasourceList = shape.getShapeDataSource();
					if (datasourceList != null) {
						for (DataSource ds : datasourceList) {
							if (ds instanceof TableDataSource) {
								for (URI typeId : datasourceTypeId) {
									if (ds.getType().contains(typeId)) {
										TableDataSource table = (TableDataSource) ds;
										if (table.getDdlFile() != null) {
											return true;
										}
									}
								}
							}
						}
					}
				}
			}
		}
		return false;
	}


	private void createProject() {
		
		URI projectId = Project.createId(mavenProject.getGroupId(), mavenProject.getArtifactId(), mavenProject.getVersion());
		File baseDir = mavenProject.getBasedir();
		
		project = new Project(projectId, baseDir);
		
		ProjectManager manager = ProjectManager.instance();
		manager.add(project);
		
		if (dependencies != null) {
			for (Dependency dep : dependencies) {
				URI depId = new URIImpl(dep.getId());
				File depDir = new File(baseDir, dep.getBaseDir());
				manager.add(new Project(depId, depDir));
			}
		}
		
	}


	private void preprocessResources() throws MojoExecutionException, IOException, RDFParseException, RDFHandlerException {
    	AuroraInfo aurora=null;
    	BigQueryInfo bigQuery=null;
    	CloudSqlInfo cloudSql=null; 	
	
		if (profiles != null) {
			for (OwlProfile profile : profiles) {
				switch (profile) {
				case XML_SCHEMA_DATATYPE_HIERARCHY :
					XSD.addDatatypeHierarchy(owlGraph);
					break;
				}
			}
		}
		
		if (inferences != null) {
			ShapeReasoner shapeReasoner = new ShapeReasoner(shapeManager);
			for (OwlInference inference : inferences) {
				switch (inference) {
				case OWL_PROPERTY_CLASSIFICATION :
					XOWL.classifyProperties(owlReasoner, shapeReasoner);
				}
			}
		}
		
    	if(amazonWebServices!=null && amazonWebServices.getAurora()!=null){
    		aurora=amazonWebServices.getAurora();
    	}
    	else if(googleCloudPlatform!=null && googleCloudPlatform.getBigquery()!=null && buildTarget==BuildTarget.RDF){
    		generateBigQueryEnumShapes();
    		
    		
    	}
    	else if(googleCloudPlatform!=null && googleCloudPlatform.getCloudsql()!=null){
    		cloudSql=googleCloudPlatform.getCloudsql();
    	}
    		
    	if (tabularShapes != null) {
    		String namespace = tabularShapes.getTabularPropertyNamespace();
    		if (namespace != null) {
    			TabularLinkingStrategy linkingStrategy = linkingStrategy(tabularShapes);
	    		TabularShapeFactory factory = new TabularShapeFactory(shapeManager, namespace, linkingStrategy);
	    		try {
					factory.processAll(shapeManager.listShapes());
				} catch (TabularShapeException e) {
					throw new MojoExecutionException("Failed to generate tabular shapes", e);
				}
    		}
    	}
	}


	private TabularLinkingStrategy linkingStrategy(TabularShapeFactoryConfig config) throws MojoExecutionException {
		String linkingStrategyClassName = config.getLinkingStrategy();
		if (linkingStrategyClassName == null || 
				"io.konig.schemagen.sql.SyntheticKeyLinkingStrategy".equals(linkingStrategyClassName)) { 
			return new SyntheticKeyLinkingStrategy(config.getTabularPropertyNamespace());
		}
		
		if (KonigIdLinkingStrategy.class.getName().equals(linkingStrategyClassName)) {
			URI idPredicate = new URIImpl(config.getTabularPropertyNamespace() + "ID");
			return new KonigIdLinkingStrategy(idPredicate, config.getTabularPropertyNamespace());
		}
		
		throw new MojoExecutionException("Cannot create linking strategy: " + linkingStrategyClassName);
	}


	private void generateBigQueryEnumShapes() {
		
		if (googleCloudPlatform.getBigqueryEnumShapeIriTemplate() != null) {

			ValueFormat format = new SimpleValueFormat(googleCloudPlatform.getBigqueryEnumShapeIriTemplate());
			ShapeNamer shapeNamer = new TemplateShapeNamer(nsManager, format);
			
			BigQueryEnumShapeGenerator generator = new BigQueryEnumShapeGenerator(datasetMapper(), createTableMapper(), shapeNamer, shapeManager);
			generator.setOmitTypeProperty(googleCloudPlatform.isOmitTypeFromEnumTables());
			generator.generateAll(owlReasoner);
		}
		
	}


	private void init() throws MojoExecutionException, IOException, ConfigurationException {
		rdf = new RdfConfig();
		rdf.setRootDir(rdfDir.getParentFile());
    	GcpShapeConfig.init();
    	OracleShapeConfig.init();
    	AwsShapeConfig.init();
    	if (tabularShapes==null) {
    		tabularShapes = new TabularShapeFactoryConfig();
    	}
    	if (rdf == null) {
    		rdf = new RdfConfig();
    		rdf.setRootDir(new File(mavenProject.getBasedir(), "target/generated/rdf"));
    	}

    }

//	private void initMySqlTransformGenerator() throws ConfigurationException {
//		
//		RoutedSqlTransformVisitor visitor = new RoutedSqlTransformVisitor();
//		if (googleCloudPlatform != null) {
//
//			
//			if (googleCloudPlatform.isEnableBigQueryTransform()) {
//				visitor = new RoutedSqlTransformVisitor();
//			}
//		}
//		
//	}


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
			configureDistributionManagement();
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
	
	private File mavenHome() {
		if (mavenHome == null) {

			for (String dirname : System.getenv("PATH").split(File.pathSeparator)) {
				File file = new File(dirname, "mvn");
				if (file.isFile()) {
					mavenHome = file.getParentFile().getParentFile();
					return mavenHome;
				}
			}
			throw new RuntimeException("Maven executable not found.");
		}
		return mavenHome;
	}

	private void configureDistributionManagement() {
		
		DistributionManagement pojo = mavenProject.getDistributionManagement();
		if (pojo != null) {
			StringWriter out = new StringWriter();
			XmlSerializer serializer = new XmlSerializer(out);
			serializer.write(pojo, "distributionManagement");
			serializer.flush();
			
			
			ParentProjectConfig config = new ParentProjectConfig();
			config.setDistributionManagement(out.toString());
			multiProject.setParentProject(config);
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

	

	private void loadResources() throws MojoExecutionException, RDFParseException, RDFHandlerException, IOException {

    	if (rdf == null) {
    		rdf = new RdfConfig();
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
			rdfSourceDir = rdf.getRdfDir();
		}
		
		if (rdfSourceDir != null) {
			RdfUtil.loadTurtle(rdfSourceDir, owlGraph, nsManager);
			ShapeLoader shapeLoader = new ShapeLoader(contextManager, shapeManager, nsManager);
			shapeLoader.load(owlGraph);
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
			if (jsonSchema.getGenerateSchemaId()) {
				jsonSchemaGenerator.setIncludeIdValue(true);
				generator.setListener(new ShapeToJsonSchemaLinker(owlGraph));
			}
			File outDir = jsonSchema.getJsonSchemaDir();
			if (outDir == null) {
				outDir = new File(mavenProject.getBasedir(), "target/generated/json-schema");
				jsonSchema.setJsonSchemaDir(outDir);
			}
			generator.generateAll(shapeManager.listShapes(), outDir);
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

	private void loadSpreadsheet() throws MojoExecutionException {
		try {
			if (workbook != null) {
				File templateDir = workbook.getTemplateDir();
				int maxErrorCount = workbook.getMaxErrorCount();

				WorkbookProcessorImpl processor = new WorkbookProcessorImpl(owlGraph, shapeManager, templateDir);
				processor.setMaxErrorCount(maxErrorCount);
				processor.addService(gcpDeploymentSheet(processor));
				processor.init();
				processor.service(SettingsSheet.class).setOutDir(settingsDir);
				
				if (workbook.isApplyProjectFilter()) {
					String acceptProject = mavenProject.getGroupId() + ":" + mavenProject.getArtifactId();
					processor.setAcceptProject(acceptProject);
				}

				// WorkbookLoader workbookLoader = new
				// WorkbookLoader(nsManager);
				// workbookLoader.setShapeManager(shapeManager);
				// workbookLoader.setFailOnWarnings(workbook.isFailOnWarnings());
				// workbookLoader.setFailOnErrors(workbook.isFailOnErrors());
				// workbookLoader.setInferRdfPropertyDefinitions(workbook.isInferRdfPropertyDefinitions());
				// workbookLoader.setNormalizeTerms(workbook.isNormalizeTerms());

				File workbookFile = workbook.getWorkbookFile();
				File workbookDir = workbook.getWorkbookDir();
				List<File> files = new ArrayList<File>();
				if (workbookFile != null) {
					files.add(workbookFile);
				}
				if (workbookDir != null && workbookDir.exists() && workbookDir.isDirectory()) {
					File[] filesArr = workbookDir.listFiles();
					if (filesArr != null && filesArr.length > 0) {
						List<File> filesTmp = Arrays.asList(filesArr);
						files.addAll(filesTmp);
					}
				}
				if (files.isEmpty()) {
					throw new SpreadsheetException("No files available in workbookDir and workbookFile.");
				}
				// for (File file : files) {
				// FileInputStream input = new FileInputStream(file);
				// try {
				//
				// Workbook workbook = new XSSFWorkbook(input);
				//
				// workbookLoader.setDatasetMapper(datasetMapper);
				// getLog().debug("Opening Workbook: " + file.getName());
				// workbookLoader.load(workbook, owlGraph,workbookFile);
				// } finally {
				// input.close();
				// getLog().debug("Closing Workbook: " + file.getName());
				// }
				// }

				processor.processAll(files);
				if (processor.getErrorCount() > 0) {
					anyError = true;
				}
				
				emitter.add(new OntologyEmitter(rdf.getOwlDir()));
				emitter.add(new ShapeToFileEmitter(shapeManager, rdf.getShapesDir()));
				emitter.add(new SkosEmitter(rdf.getOwlDir()));
				emitter.add(new NamedGraphEmitter(rdf.getOwlDir()));
				cubeManager = processor.getServiceManager().getService(CubeManager.class);
				addCadEmitter(cubeManager);


//				VelocityContext context = workbookLoader.getDataSourceGenerator().getContext();
//				File gcpDir = workbook.gcpDir(defaults);
//				File awsDir = workbook.awsDir(defaults);
//				if (context != null) {
//					if (context.get("ECRRepositoryName") != null) {
//						System.setProperty("ECRRepositoryName", (String) context.get("ECRRepositoryName"));
//					}
//					emitter.add(new GenericEmitter(GCP.GoogleCloudSqlInstance, gcpDir, context));
//					emitter.add(new GenericEmitter(AWS.DbCluster, awsDir, context));
//					emitter.add(new GenericEmitter(AWS.CloudFormationTemplate, awsDir, context));
//					emitter.add(new GenericEmitter(AWS.SecurityTag, awsDir, context));
//				}

			}

		} catch (Throwable oops) {
			throw new MojoExecutionException("Failed to transform workbook to RDF", oops);
		}
	}
	
	private Object gcpDeploymentSheet(WorkbookProcessorImpl processor) {
		File yamlFile = globalGcpYamlTemplate();
		if (yamlFile != null) {
			return new GcpDeploymentSheet(processor, yamlFile);
		}
		return null;
	}


	private File globalGcpYamlTemplate() {
		if (googleCloudPlatform != null) {
			File gcpDir = googleCloudPlatform.gcpDir(rdf);
			return new File(gcpDir, "deployment/templates/global.yaml");
		}
		return null;
	}


	private void addCadEmitter(CubeManager cubeManager) {
		if (cubeManager != null) {
			// TODO:  Handle the case where cubes are defined in the same namespace as 
			//        an OWL ontology.  As it stands, this method will overwrite the OWL ontology.
			
			emitter.add(new CubeEmitter(rdf.getOwlDir(), cubeManager));
		}
		
	}


	private File rdfDir() {
		return rdf.getRdfDir();
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
//			File transformsDir = Configurator.checkNull(amazonWebServices.getTransforms());
			File cloudFormationDir = Configurator.checkNull(amazonWebServices.getCloudFormationTemplates());
			File viewDir = Configurator.checkNull(amazonWebServices.getAurora().getViews());
			AwsResourceGenerator resourceGenerator = new AwsResourceGenerator();
			
			
			if(tablesDir != null) {
				SqlTableGenerator generator = new SqlTableGenerator(shapeManager);

				ProjectFolder tablesFolder = new ProjectFolder(project, tablesDir);
				AwsAuroraTableWriter awsAuror = new AwsAuroraTableWriter(tablesDir, generator,tablesFolder,abbrevManager());
				resourceGenerator.add(awsAuror);				
			}
			if (viewDir != null) {
				SqlTableGenerator generator = new SqlTableGenerator();
				ProjectFolder viewFolder = new ProjectFolder(project, viewDir);
				ShapeModelFactory shapeModelFactory=new ShapeModelFactory(shapeManager, new AwsAuroraChannelFactory(), owlReasoner);
		

				AwsAuroraViewWriter awsAuror = new AwsAuroraViewWriter(viewDir, generator,viewFolder,shapeModelFactory,abbrevManager());
			
				resourceGenerator.add(awsAuror);	
				
			}
//			if(transformsDir != null && amazonWebServices.isEnableAuroraTransform()){
//				ShapeModelFactory shapeModelFactory=new ShapeModelFactory(shapeManager, new AwsAuroraChannelFactory(), owlReasoner);
//				ShapeRuleFactory shapeRuleFactory=new ShapeRuleFactory(shapeManager, shapeModelFactory, new ShapeModelToShapeRule());
//				ProjectFolder transformsFolder = new ProjectFolder(project, transformsDir);
//				AuroraTransformGenerator generator=new AuroraTransformGenerator(shapeRuleFactory, new SqlFactory(),transformsFolder, rdfSourceDir);
//				resourceGenerator.add(generator);
//			}
			
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
//			generateCamelEtl();
			if(templateWriter != null) {
				templateWriter.updateTemplate();
			}
			GroovyAwsDeploymentScriptWriter scriptWriter = new GroovyAwsDeploymentScriptWriter(amazonWebServices);
			scriptWriter.run(); 
		}
	}
	
	private AbbreviationManager abbrevManager() {
		if (abbrevManager == null) {
			AbbreviationConfig config = new AbbreviationConfig();
			config.setDelimiters("_ \t\r\n");
			config.setPreferredDelimiter("_");
			abbrevManager = new MemoryAbbreviationManager(owlGraph, config);
		}
		return abbrevManager;
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
				SqlTableGenerator generator = new SqlTableGenerator(shapeManager);
				ProjectFolder tablesFolder = new ProjectFolder(project, tablesDir);
				AwsAuroraTableWriter awsAuror = new AwsAuroraTableWriter(tablesDir, generator,tablesFolder,abbrevManager());
			
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
				SqlTableGenerator sqlgenerator = new SqlTableGenerator(new OracleDatatypeMapper(), shapeManager);
				OracleTableWriter oracle = new OracleTableWriter(tablesDir, sqlgenerator);
				resourceGenerator.add(oracle);
				resourceGenerator.dispatch(shapeManager.listShapes());
				GroovyOmcsDeploymentScriptWriter scriptWriter = new GroovyOmcsDeploymentScriptWriter(oracleManagedCloud);
				scriptWriter.run();
			}
		}
	}
	private void generateGoogleCloudPlatform() throws IOException, MojoExecutionException, ConfigurationException, GoogleCredentialsNotFoundException, InvalidGoogleCredentialsException, SQLException, BeamTransformGenerationException {
		if (googleCloudPlatform != null) {
			
			if (buildTarget!=null && buildTarget!=BuildTarget.GCP) {
				return;
			}

			Configurator config = configurator();
			config.configure(googleCloudPlatform);
			
			File gcpDir = googleCloudPlatform.gcpDir(rdf);
			if (gcpDir == null) {
				throw new MojoExecutionException("googleCloudPlatform.gcpDir must be defined");
			}
			
			BigQueryInfo bigQuery = googleCloudPlatform.getBigquery();
			CloudStorageInfo cloudStorage = googleCloudPlatform.getCloudstorage();
			
//			GcpDeploymentConfigManager deployManager = new GcpDeploymentConfigManager();
			
			GoogleCloudResourceGenerator resourceGenerator = new GoogleCloudResourceGenerator(shapeManager, owlReasoner);
//			resourceGenerator.setBigqueryTableListener(deployManager.createBigQueryTableListener());
	
			if (bigQuery != null) {
				ProjectFolder schemaFolder = project.createFolder(bigQuery.getSchema());
				resourceGenerator.addBigQueryGenerator(schemaFolder);
				if (googleCloudPlatform.isEnableBigQueryTransform()) {
					ProjectFolder viewFolder = project.createFolder(bigQuery.getView());
					resourceGenerator.addBigQueryViewGenerator(viewFolder);
				}
				
				resourceGenerator.add(labelGenerator());
			}
			if (cloudStorage != null) {
				resourceGenerator.addCloudStorageBucketWriter(cloudStorage.getDirectory());
			}
			if (googleCloudPlatform.getCloudsql() != null) {
				resourceGenerator.add(cloudSqlTableWriter());
//				File mysqlScriptsDir = googleCloudPlatform.getCloudsql().getScripts();
//				if(mysqlScriptsDir != null) {
//					generateMySqlTransformScripts(mysqlScriptsDir);
//				}
			}
			if (googleCloudPlatform.isEnableBigQueryTransform()) {
				configureBigQueryTransform();
			}
//			if (googleCloudPlatform.isEnableMySqlTransform()) {
//				configureCloudSqlTransform();
//			}
			// For now, commenting out the old CloudSqlJsonGenerator
			// TODO: delete the code block permanently.
//			if (googleCloudPlatform.getCloudsql().getInstances()!=null) {
//				CloudSqlJsonGenerator instanceWriter = new CloudSqlJsonGenerator();
//				instanceWriter.writeAll(googleCloudPlatform.getCloudsql(), owlGraph);
//			}
			resourceGenerator.add(new GooglePubSubTopicListGenerator(googleCloudPlatform.getTopicsFile()));
			resourceGenerator.dispatch(shapeManager.listShapes());
			
			File deploymentDir = new File(googleCloudPlatform.getDirectory(), "deployment");
			
//			CloudSqlAdminManager sqlAdmin = new CloudSqlAdminManager(
//				deployManager.createCloudSqlInstanceVisitor(),
//				deployManager.createCloudSqlDatabaseVisitor());

//			File deploymentYaml = new File(deploymentDir, "gcp-deployment.yaml");
			
			BigQueryTableGenerator bigQueryTableGenerator = new BigQueryTableGenerator(shapeManager, null, owlReasoner);
			GcpConfigManager configManager = new GcpConfigManager(bigQueryTableGenerator, globalGcpYamlTemplate());
			File gcpConfigFile = new File(deploymentDir, "config.yaml");
			emitter.add(new DeploymentConfigEmitter(shapeManager, configManager, gcpConfigFile));
			
			// TODO: remove the following, obsolete emitter
//			emitter.add(new GoogleDeploymentManagerEmitter(sqlAdmin, deployManager, deploymentYaml));

			if (bigQuery != null) {

				BigQueryEnumGenerator enumGenerator = new BigQueryEnumGenerator(shapeManager);
				
				File bqDataDir = Configurator.checkNull(bigQuery.getData());
				File bqSchemaDir = Configurator.checkNull(bigQuery.getSchema());
				File bqDatasetDir = Configurator.checkNull(bigQuery.getDataset());
				File bqScriptsDir = Configurator.checkNull(bigQuery.getScripts());
				
				if (bqDataDir != null) {
					DataFileMapperImpl dataFileMapper = new DataFileMapperImpl(bqDataDir, datasetMapper(), createTableMapper());
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
			
//			generateCamelEtl();
			generateDeploymentScript();
		}
	}

	


	private void configureBigQueryTransform() throws BeamTransformGenerationException, IOException {
		// We really ought to batch up all the transform jobs and run them all at once.
		// To that end, this method ought to simply configure the batch to include BigQuery transforms.
		// For now, however, we'll go ahead and run the BigQuery transform job by itself.
		

		ShowlService showlService = new ShowlServiceImpl(owlReasoner);
		ShowlNodeShapeBuilder builder = new ShowlNodeShapeBuilder(showlService, showlService);
		DestinationTypeTargetNodeShapeFactory targetNodeFactory = new DestinationTypeTargetNodeShapeFactory(
				Collections.singleton(Konig.GoogleBigQueryTable), builder);
		ShowlSourceNodeFactory sourceNodeFactory = new ReceivesDataFromSourceNodeFactory(builder, owlGraph);
		

		ShowlTransformService transformService = new BasicTransformService(showlService, showlService, sourceNodeFactory);
		

		ShowlClassProcessor classProcessor = new ShowlClassProcessor(showlService, showlService);
		classProcessor.buildAll(shapeManager);

		ShowlNodeListingConsumer consumer = new ShowlNodeListingConsumer();
		ShowlTransformEngine engine = new ShowlTransformEngine(targetNodeFactory, shapeManager, transformService, consumer);
		engine.run();
		
		if (!consumer.getList().isEmpty()) {
		
			File projectDir = new File(mavenProject.getBasedir(), "target/generated/beam");
			BeamTransformRequest request = BeamTransformRequest.builder()
				.groupId(mavenProject.getGroupId())
				.artifactBaseId(mavenProject.getArtifactId())
				.version(mavenProject.getVersion())
				.projectDir(projectDir)
				.nodeList(consumer.getList())
				.build();
	
			
			String basePackage = mavenProject.getGroupId() + ".beam";
			BeamTransformGenerator generator =  new BeamTransformGenerator(basePackage, owlReasoner);
			generator.generateAll(request);

			addLineageEmitter();
		}

	
		
	}
	private void addLineageEmitter() {
		if (lineageEmitter == null) {
			File outFile = new File(rdf.getShapesDir(), "lineage.ttl");
			lineageEmitter = new LineageEmitter(outFile);
			emitter.add(lineageEmitter);
		}
		
	}





	private CompositeSourceNodeSelector nodeSelector(ShapeManager shapeManager) {
		return new CompositeSourceNodeSelector(
				new RawCubeSourceNodeSelector(shapeManager),
				new DataSourceTypeSourceNodeSelector(shapeManager, Konig.GoogleCloudStorageFolder),
				new ExplicitDerivedFromSelector());
	}

	private RoutedSqlTransformVisitor sqlTransformVisitor() {
		if (sqlTransformVisitor==null) {
			sqlTransformVisitor = new RoutedSqlTransformVisitor();
			mysqlTransformGenerator = new SqlTransformGenerator(sqlTransformVisitor, sqlTransformVisitor);
		}
		return sqlTransformVisitor;
	}


	private CloudSqlTableWriter cloudSqlTableWriter() {
		CloudSqlInfo info = googleCloudPlatform.getCloudsql();
		SqlTableGenerator generator = new SqlTableGenerator(shapeManager);
		ProjectFolder folder = new ProjectFolder(project, info.getTables());
		return new CloudSqlTableWriter(generator, folder, abbrevManager());
	}

//	private void generateMySqlTransformScripts(File outDir) throws MojoExecutionException {
//		if (googleCloudPlatform.isEnableMySqlTransform()) {
//			OldMySqlTransformGenerator generator = new OldMySqlTransformGenerator(shapeManager, outDir, owlReasoner);
//			generator.generateAll();
//			List<Throwable> errorList = generator.getErrorList();
//			if (errorList != null && !errorList.isEmpty()) {
//				Log logger = getLog();
//				for (Throwable e : errorList) {
//					logger.error(e.getMessage());
//				}
//				throw new MojoExecutionException("Failed to generate MySql Transform", errorList.get(0));
//			}
//		}
//	}

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
		
		
//		if (googleCloudPlatform.isEnableBigQueryTransform()) {
//			
//		
//			BigQueryTransformGenerator generator = new BigQueryTransformGenerator(shapeManager, outDir, owlReasoner,rdfSourceDir);
//			generator.generateAll();
//			List<Throwable> errorList = generator.getErrorList();
//			if (errorList != null && !errorList.isEmpty()) {
//				Log logger = getLog();
//				for (Throwable e : errorList) {
//					logger.error(e.getMessage());
//				}
//				throw new MojoExecutionException("Failed to generate BigQuery Transform", errorList.get(0));
//			}
//		}
		
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
	
//	private void generateCamelEtl() throws MojoExecutionException {
//		File outDir = null;
//		Map<String, Map<String,String>> services=new HashMap<>();
//		Shape dockerComposer = null;
//		try {
//			File derivedDir = new File(rdfSourceDir, "shape-dependencies");
//		
//			if (derivedDir.exists()) {
//				Graph graph = new MemoryGraph();
//				NamespaceManager nsManager = new MemoryNamespaceManager();
//				RdfUtil.loadTurtle(derivedDir, graph, nsManager);
//				
//				for (Vertex targetShapeVertex : graph.vertices()) {					
//					Shape targetShape = shapeManager.getShapeById(targetShapeVertex.getId());
//					dockerComposer = targetShape;
//					if (targetShape.hasDataSourceType(Konig.AwsAuroraTable)) {
//						if(amazonWebServices != null) {
//							outDir = amazonWebServices.getCamelEtl();
//						}						
//						List<Vertex> sourceList = targetShapeVertex.asTraversal().out(Konig.derivedFrom).toVertexList();
//						
//						if (!sourceList.isEmpty()) {
//							Vertex sourceShapeVertex = sourceList.get(0);
//							Resource sourceShapeId = sourceShapeVertex.getId();
//							Shape sourceShape = shapeManager.getShapeById(sourceShapeId);
//
//							if (sourceShape.hasDataSourceType(Konig.AwsAuroraTable)
//									&& sourceShape.hasDataSourceType(Konig.S3Bucket)) {
//								EtlModelGenerator etlModelGenerator=new EtlModelGenerator(new MavenProjectConfig(),new File(amazonWebServices.getBaseDirectory(),"../"),new URIImpl(targetShape.getId().stringValue()).getLocalName());
//								etlModelGenerator.run();
//								EtlRouteBuilder builder = new EtlRouteBuilder(sourceShape, targetShape, outDir);
//								builder.setEtlBaseDir(amazonWebServices.getBaseDirectory());
//								builder.generate();
//								String serviceName = new URIImpl(targetShape.getId().stringValue()).getLocalName();
//								Map<String, String> service=new HashMap<>();
//								service.put("image", "etl-"+serviceName+":latest");
//								services.put(serviceName, service);
//							}
//						}
//					}
//					if(targetShape.hasDataSourceType(Konig.GoogleBigQueryTable)) {
//						if(googleCloudPlatform != null) {
//							outDir = googleCloudPlatform.getCamelEtl();
//						}						
//						List<Vertex> sourceList = targetShapeVertex.asTraversal().out(Konig.derivedFrom).toVertexList();
//						if (!sourceList.isEmpty()) {
//							Vertex sourceShapeVertex = sourceList.get(0);
//							Resource sourceShapeId = sourceShapeVertex.getId();
//							Shape sourceShape = shapeManager.getShapeById(sourceShapeId);
//
//							if (sourceShape.hasDataSourceType(Konig.GoogleBigQueryTable)
//									&& sourceShape.hasDataSourceType(Konig.GoogleCloudStorageBucket)) {
//								GcpEtlRouteBuilder builder = new GcpEtlRouteBuilder(sourceShape, targetShape, outDir);
//								builder.setEtlBaseDir(googleCloudPlatform.getDirectory());
//								builder.generate();
//								String serviceName = new URIImpl(targetShape.getId().stringValue()).getLocalName();
//								Map<String, String> service=new HashMap<>();
//								service.put("image", "etl-"+serviceName+":latest");
//								services.put(serviceName, service);
//							}
//						}
//					}
//				}
//				EtlRouteBuilder builder = new EtlRouteBuilder(null, dockerComposer, outDir);
//				builder.createDockerComposeFile(services);
//				createImageList(services, outDir);
//			}
//			
//		} catch (Exception ex) {
//			throw new MojoExecutionException("Failed to generate camel etl routes", ex);
//		}
//		
//	}
	private void createImageList(Map<String,Map<String,String>> services, File outDir ) throws IOException {
		File cloudformationtemplateDir = new File(outDir.getParent(), "cloudformationtemplate");

		if (!cloudformationtemplateDir.exists())
			cloudformationtemplateDir.mkdirs();
		
		FileWriter writer = null;
		try {
			File imageListFile = new File(cloudformationtemplateDir, "ImageList.txt");
			writer = new FileWriter(imageListFile, true);
			Iterator<Map.Entry<String, Map<String,String>>> iterator = services.entrySet().iterator();
			while(iterator.hasNext()) {
		         Map.Entry<String, Map<String,String>> service = iterator.next();	
		         Map<String,String> images = service.getValue();
		         writer.append(images.get("image").toString().toLowerCase());
		         writer.append("\n");
			}
		} finally {
			writer.flush();
			writer.close();
		}
	}
	
	private void generateTabularShapes() throws RDFParseException, RDFHandlerException, IOException {
		
		if(rdf.getShapesDir() != null && config != null) {
			RdfUtil.loadTurtle(rdf.getRdfDir(), owlGraph, nsManager);
			ShapeLoader shapeLoader = new ShapeLoader(contextManager, shapeManager, nsManager);
			shapeLoader.load(owlGraph);
			
			File shapesDir = rdf.getShapesDir();
			TabularShapeGenerator tabularShapeGenerator = new TabularShapeGenerator(nsManager, shapeManager);
			try {
				tabularShapeGenerator.generateTabularShapes(shapesDir, config);
			} catch (TabularShapeGenerationException e) {
				e.printStackTrace();
			}
	}
	}
	
	private void computeMaxRowSize() throws InvalidDatatypeException {
		if(tabularShapes.getComputeMaxRowSize()){
			
			CalculateMaximumRowSize calculator = new CalculateMaximumRowSize();
			calculator.addMaximumRowSizeAll(shapeManager.listShapes(), nsManager);
		}
	}
}
