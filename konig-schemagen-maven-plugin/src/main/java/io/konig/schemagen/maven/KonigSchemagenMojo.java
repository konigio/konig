package io.konig.schemagen.maven;

import java.io.Closeable;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;

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
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;

import com.sun.codemodel.JCodeModel;

import io.konig.core.ContextManager;
import io.konig.core.Graph;
import io.konig.core.NamespaceManager;
import io.konig.core.OwlReasoner;
import io.konig.core.Vertex;
import io.konig.core.impl.MemoryContextManager;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.MemoryNamespaceManager;
import io.konig.core.impl.RdfUtil;
import io.konig.gae.datastore.CodeGeneratorException;
import io.konig.gae.datastore.FactDaoGenerator;
import io.konig.gae.datastore.SimpleDaoNamer;
import io.konig.gae.datastore.impl.SimpleEntityNamer;
import io.konig.schemagen.AllJsonldWriter;
import io.konig.schemagen.OntologySummarizer;
import io.konig.schemagen.SchemaGeneratorException;
import io.konig.schemagen.ShapeMediaTypeLinker;
import io.konig.schemagen.SimpleShapeNamer;
import io.konig.schemagen.avro.ShapeToAvro;
import io.konig.schemagen.avro.impl.SmartAvroDatatypeMapper;
import io.konig.schemagen.gcp.BigQueryTableGenerator;
import io.konig.schemagen.gcp.BigQueryTableMapper;
import io.konig.schemagen.gcp.DatasetMapper;
import io.konig.schemagen.gcp.GoogleCloudConfig;
import io.konig.schemagen.gcp.LocalNameTableMapper;
import io.konig.schemagen.gcp.MemoryGoogleCloudManager;
import io.konig.schemagen.gcp.NamespaceDatasetMapper;
import io.konig.schemagen.gcp.SimpleDatasetMapper;
import io.konig.schemagen.gcp.SimpleProjectMapper;
import io.konig.schemagen.java.BasicJavaNamer;
import io.konig.schemagen.java.JavaClassBuilder;
import io.konig.schemagen.java.JavaNamer;
import io.konig.schemagen.java.JsonWriterBuilder;
import io.konig.schemagen.java.ShapeHandler;
import io.konig.schemagen.jsonld.ShapeToJsonldContext;
import io.konig.schemagen.jsonschema.JsonSchemaGenerator;
import io.konig.schemagen.jsonschema.JsonSchemaNamer;
import io.konig.schemagen.jsonschema.JsonSchemaTypeMapper;
import io.konig.schemagen.jsonschema.ShapeToJsonSchema;
import io.konig.schemagen.jsonschema.ShapeToJsonSchemaLinker;
import io.konig.schemagen.jsonschema.impl.SimpleJsonSchemaNamer;
import io.konig.schemagen.jsonschema.impl.SmartJsonSchemaTypeMapper;
import io.konig.schemagen.plantuml.PlantumlClassDiagramGenerator;
import io.konig.schemagen.plantuml.PlantumlGeneratorException;
import io.konig.shacl.ClassManager;
import io.konig.shacl.LogicalShapeBuilder;
import io.konig.shacl.LogicalShapeNamer;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeManager;
import io.konig.shacl.ShapeMediaTypeNamer;
import io.konig.shacl.impl.BasicLogicalShapeNamer;
import io.konig.shacl.impl.MemoryClassManager;
import io.konig.shacl.impl.MemoryShapeManager;
import io.konig.shacl.impl.SimpleShapeMediaTypeNamer;
import io.konig.shacl.io.ShapeFileGetter;
import io.konig.shacl.io.ShapeLoader;
import io.konig.shacl.jsonld.ContextNamer;
import io.konig.shacl.jsonld.SuffixContextNamer;
import io.konig.showl.WorkbookToTurtleTransformer;
import net.sourceforge.plantuml.SourceFileReader;

/**
 * Goal which generates Avro schemas from SHACL data shapes
 *
 */
@Mojo( name = "generate", defaultPhase = LifecyclePhase.GENERATE_SOURCES )
public class KonigSchemagenMojo  extends AbstractMojo {
	
	private static final String SCHEMA = "schema";
	private static final String DATA = "data";
	
    /**
     * Location of the file.
     */
    @Parameter
    private File avroDir;
    
    
    @Parameter
    private File jsonldDir;
    
    @Parameter
    private File jsonSchemaDir;
    
    @Parameter(property="sourceDir", required=true)
    private File sourceDir;
    

    @Parameter
    private File domainModelPngFile;
    
    @Parameter
    private File javaDir;
    
    @Parameter
    private String javaPackageRoot;
    
    @Parameter
    private HashSet<String> excludeNamespace;
    
    @Parameter
    private String bqShapeBaseURL;
    
    @Parameter
    private File bqOutDir;
    
    @Parameter
    private File bqSourceDir;

	@Parameter
	private File shapesOutDir;
	
	@Parameter
	private String bigQueryDatasetId;
	
	 @Parameter
	 private File workbookFile;
	 
	 @Parameter
	 private File owlOutDir;

    @Parameter
    private File plantUMLDomainModelFile;
	    
	 @Parameter
	 private File namespacesFile;
	 
	 @Parameter
	 private File projectJsonldFile;
	 
	 @Parameter
	 private String daoPackage;

    
    private NamespaceManager nsManager;
    private ClassManager classManager;
    private OwlReasoner owlReasoner;
    private LogicalShapeNamer logicalShapeNamer;
    private ShapeManager shapeManager;
    private DatasetMapper datasetMapper;
    private ShapeMediaTypeNamer mediaTypeNamer;
    private Graph owlGraph;
    private ContextManager contextManager;

    public void execute() throws MojoExecutionException   {
    	
    	try {
    		
			shapeManager = new MemoryShapeManager();
			nsManager = new MemoryNamespaceManager();
			mediaTypeNamer = new SimpleShapeMediaTypeNamer();
			owlGraph = new MemoryGraph();
			contextManager = new MemoryContextManager();
			owlReasoner = new OwlReasoner(owlGraph);
			owlGraph.setNamespaceManager(nsManager);

			loadResources();

			generateBigQueryTables();
			
			generateJsonld();
			generateAvro();
			generateJsonSchema();
			
			ShapeMediaTypeLinker linker = new ShapeMediaTypeLinker(mediaTypeNamer);
			linker.assignAll(shapeManager.listShapes(), owlGraph);
			
			owlReasoner.inferClassFromSubclassOf();
			writeSummary(nsManager, shapeManager, owlGraph);
			
			generatePlantUMLDomainModel();
			generateJava();
			
			
		} catch (IOException | SchemaGeneratorException | RDFParseException | RDFHandlerException | PlantumlGeneratorException | CodeGeneratorException e) {
			throw new MojoExecutionException("Failed to convert shapes to Avro", e);
		}
      
    }
    
    private void loadResources() throws MojoExecutionException, RDFParseException, RDFHandlerException, IOException {

		loadSpreadsheet();
		
		RdfUtil.loadTurtle(sourceDir, owlGraph, nsManager);
		ShapeLoader shapeLoader = new ShapeLoader(contextManager, shapeManager, nsManager);
		shapeLoader.load(owlGraph);
		
	}

	private void generateJava() throws IOException, CodeGeneratorException {

		if (javaDir != null && javaPackageRoot!=null) {
			generateJavaCode(shapeManager);
		}
		
	}

	private void generateJsonSchema() {

		if (jsonSchemaDir != null) {

			JsonSchemaTypeMapper jsonSchemaTypeMapper = new SmartJsonSchemaTypeMapper(owlReasoner);
			JsonSchemaNamer jsonSchemaNamer = new SimpleJsonSchemaNamer("/jsonschema", mediaTypeNamer);
			JsonSchemaGenerator jsonSchemaGenerator = new JsonSchemaGenerator(jsonSchemaNamer, nsManager, jsonSchemaTypeMapper);
			ShapeToJsonSchema jsonSchema = new ShapeToJsonSchema(jsonSchemaGenerator);
			jsonSchema.setListener(new ShapeToJsonSchemaLinker(owlGraph));
			jsonSchema.generateAll(shapeManager.listShapes(), jsonSchemaDir);
		}
		
	}

	private void generateAvro() throws IOException {
    	if (avroDir != null) {
    		File avroImports = new File(avroDir, "imports");
    		File avscDir = new File(avroDir, "avsc");
			SmartAvroDatatypeMapper avroMapper = new SmartAvroDatatypeMapper(owlReasoner);
			ShapeToAvro avro = new ShapeToAvro(avroMapper);
			avro.generateAvro(sourceDir, avscDir, avroImports, owlGraph);
		}
		
	}
	private void generateJsonld() throws SchemaGeneratorException, IOException {

		if (jsonldDir != null) {
			ContextNamer contextNamer = new SuffixContextNamer("/context");
			ShapeToJsonldContext jsonld = new ShapeToJsonldContext(shapeManager, nsManager, contextNamer, mediaTypeNamer, owlGraph);
			jsonld.generateAll(jsonldDir);
		}
		
	}
	private void loadSpreadsheet() throws MojoExecutionException   {
		 try {

			 if (workbookFile!=null && workbookFile.exists()) {
				 WorkbookToTurtleTransformer transformer = new WorkbookToTurtleTransformer(datasetMapper());
				 transformer.transform(workbookFile, owlOutDir, shapesOutDir);
			 }
		 } catch (Throwable oops) {
			 throw new MojoExecutionException("Failed to transform workbook to RDF", oops);
		 }
	 }


	private void generatePlantUMLDomainModel() throws IOException, PlantumlGeneratorException {
		if (plantUMLDomainModelFile != null) {

			plantUMLDomainModelFile.getParentFile().mkdirs();
			
			ClassManager classManager = getClassManager();
			PlantumlClassDiagramGenerator generator = new PlantumlClassDiagramGenerator(owlReasoner, shapeManager);
			FileWriter writer = new FileWriter(plantUMLDomainModelFile);
			try {
				generator.generateDomainModel(classManager, writer);
			} finally {
				close(writer);
			}
			
			SourceFileReader reader = new SourceFileReader(plantUMLDomainModelFile);
			reader.getGeneratedImages();
		}
		
	}

	private void close(Closeable stream) {
		try {
			stream.close();
		} catch (IOException oops) {
			oops.printStackTrace();
		}
		
	}

	private void generateBigQueryTables() throws SchemaGeneratorException, IOException, RDFParseException, RDFHandlerException {

		if (bqShapeBaseURL != null) {
			if (bqSourceDir != null) {
				RdfUtil.loadTurtle(bqSourceDir, owlGraph, nsManager);
			}
			SimpleShapeNamer shapeNamer = new SimpleShapeNamer(nsManager, bqShapeBaseURL);
			shapeNamer.setPrefixBase("bq");
			BigQueryTableGenerator generator = new BigQueryTableGenerator(shapeManager, shapeNamer, owlReasoner);
			
			MemoryGoogleCloudManager cloudManager = new MemoryGoogleCloudManager();
			
			
			GoogleCloudConfig config = new GoogleCloudConfig(cloudManager, generator);
			
			BigQueryTableMapper tableMapper = createTableMapper();
			
			
			config.load(owlGraph);
			
			generator.generateBigQueryTables(cloudManager);
			ShapeFileGetter shapeFileGetter = new ShapeFileGetter(shapesOutDir, nsManager);
			cloudManager.setProjectMapper(new SimpleProjectMapper("testProject"));
			cloudManager.setDatasetMapper(datasetMapper());
			generator.setTableMapper(tableMapper);
			generator.generateEnumTables(owlGraph, cloudManager);
			config.writeEnumTableShapes(nsManager, shapeFileGetter);
			
			
			File bqSchemaDir = new File(bqOutDir, SCHEMA);
			File bqDataDir = new File(bqOutDir, DATA);
			
			config.writeBigQueryTableDefinitions(bqSchemaDir);
			config.writeBigQueryEnumMembers(owlGraph, bqDataDir);
		}
		
	}
	
	private BigQueryTableMapper createTableMapper() {
		return new LocalNameTableMapper();
	}


	private DatasetMapper datasetMapper() {
		if (datasetMapper == null) {

			if (bigQueryDatasetId != null) {
				datasetMapper = new SimpleDatasetMapper(bigQueryDatasetId);
			} else {
				datasetMapper = new NamespaceDatasetMapper(nsManager);
			}
		}
		return datasetMapper;
	}


	private ClassManager getClassManager() {
		if (classManager == null) {
			classManager = new MemoryClassManager();

			LogicalShapeNamer namer = getLogicalShapeNamer();
			LogicalShapeBuilder builder = new LogicalShapeBuilder(owlReasoner, namer);
			builder.buildLogicalShapes(shapeManager, classManager);
			
		}
		return classManager;
	}
	
	private LogicalShapeNamer getLogicalShapeNamer() {
		if (logicalShapeNamer == null) {
			logicalShapeNamer = new BasicLogicalShapeNamer("http://example.com/shapes/logical/", nsManager);
		}
		return logicalShapeNamer;
	}

	private void generateJavaCode(ShapeManager shapeManager) throws IOException, CodeGeneratorException {
		
		final JCodeModel model = new JCodeModel();
		JavaNamer javaNamer = new BasicJavaNamer(javaPackageRoot, nsManager);
		JavaClassBuilder classBuilder = new JavaClassBuilder(shapeManager, javaNamer, owlReasoner);
		final JsonWriterBuilder writerBuilder = new JsonWriterBuilder(owlReasoner, shapeManager, javaNamer);
		
		classBuilder.buildAllClasses(model);
		writerBuilder.buildAll(shapeManager.listShapes(), model);
		
		if (daoPackage != null) {
			SimpleEntityNamer entityNamer = new SimpleEntityNamer();
			SimpleDaoNamer daoNamer = new SimpleDaoNamer(daoPackage, nsManager);
			FactDaoGenerator daoGenerator = new FactDaoGenerator()
				.setDaoNamer(daoNamer)
				.setDatatypeMapper(classBuilder.getMapper())
				.setEntityNamer(entityNamer)
				.setJavaNamer(javaNamer)
				.setShapeManager(shapeManager);
			
			daoGenerator.generateAllFactDaos(model);
		}
		
		javaDir.mkdirs();
		model.build(javaDir);
		
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
}
