package io.konig.schemagen.maven;

import java.io.Closeable;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;

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
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;

import com.sun.codemodel.JCodeModel;

import io.konig.core.ContextManager;
import io.konig.core.Graph;
import io.konig.core.NamespaceManager;
import io.konig.core.OwlReasoner;
import io.konig.core.impl.MemoryContextManager;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.MemoryNamespaceManager;
import io.konig.core.impl.RdfUtil;
import io.konig.core.util.BasicJavaDatatypeMapper;
import io.konig.core.util.SimpleValueFormat;
import io.konig.gae.datastore.CodeGeneratorException;
import io.konig.gae.datastore.FactDaoGenerator;
import io.konig.gae.datastore.SimpleDaoNamer;
import io.konig.gae.datastore.impl.SimpleEntityNamer;
import io.konig.gcp.datasource.GcpShapeConfig;
import io.konig.schemagen.AllJsonldWriter;
import io.konig.schemagen.OntologySummarizer;
import io.konig.schemagen.SchemaGeneratorException;
import io.konig.schemagen.ShapeMediaTypeLinker;
import io.konig.schemagen.avro.AvroNamer;
import io.konig.schemagen.avro.AvroSchemaGenerator;
import io.konig.schemagen.avro.impl.SimpleAvroNamer;
import io.konig.schemagen.avro.impl.SmartAvroDatatypeMapper;
import io.konig.schemagen.gcp.BigQueryDatasetGenerator;
import io.konig.schemagen.gcp.BigQueryEnumGenerator;
import io.konig.schemagen.gcp.BigQueryEnumShapeGenerator;
import io.konig.schemagen.gcp.BigQueryTableMapper;
import io.konig.schemagen.gcp.DataFileMapperImpl;
import io.konig.schemagen.gcp.DatasetMapper;
import io.konig.schemagen.gcp.EnumShapeVisitor;
import io.konig.schemagen.gcp.GoogleCloudResourceGenerator;
import io.konig.schemagen.gcp.LocalNameTableMapper;
import io.konig.schemagen.gcp.NamespaceDatasetMapper;
import io.konig.schemagen.gcp.SimpleDatasetMapper;
import io.konig.schemagen.java.BasicJavaNamer;
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
import io.konig.schemagen.jsonschema.impl.SmartJsonSchemaTypeMapper;
import io.konig.schemagen.plantuml.PlantumlClassDiagramGenerator;
import io.konig.schemagen.plantuml.PlantumlGeneratorException;
import io.konig.shacl.ClassStructure;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeManager;
import io.konig.shacl.ShapeMediaTypeNamer;
import io.konig.shacl.ShapeNamer;
import io.konig.shacl.ShapeVisitor;
import io.konig.shacl.impl.MemoryShapeManager;
import io.konig.shacl.impl.SimpleShapeMediaTypeNamer;
import io.konig.shacl.impl.TemplateShapeNamer;
import io.konig.shacl.io.ShapeFileGetter;
import io.konig.shacl.io.ShapeLoader;
import io.konig.shacl.jsonld.ContextNamer;
import io.konig.showl.WorkbookToTurtleTransformer;
import net.sourceforge.plantuml.SourceFileReader;

/**
 * Goal which generates Avro schemas from SHACL data shapes
 *
 */
@Mojo( name = "generate")
public class KonigSchemagenMojo  extends AbstractMojo {
	
	private static final String BIGQUERY = "bigquery";
	private static final String CLOUD_STORAGE = "cloudstorage";
	private static final String SCHEMA = "schema";
	private static final String DATA = "data";
	private static final String DATASET = "dataset";
	
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
    private File domainModelPngFile;
    
    @Parameter
    private JavaCodeGeneratorConfig java;
    
    @Parameter
    private WorkbookProcessor workbook;
    
    @Parameter
    private GoogleCloudPlatformConfig googleCloudPlatform;
    
    
    @Parameter
    private HashSet<String> excludeNamespace;
    
	

    @Parameter
    private ClassDiagram[] plantUML;

	    
	 private File namespacesFile;
	 
	 @Parameter
	 private File projectJsonldFile;
	 

    
    private NamespaceManager nsManager;
    private OwlReasoner owlReasoner;
    private ShapeManager shapeManager;
    private DatasetMapper datasetMapper;
    private ShapeMediaTypeNamer mediaTypeNamer;
    private Graph owlGraph;
    private ContextManager contextManager;
    private ClassStructure structure;

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
			
			writeSummary(nsManager, shapeManager, owlGraph);
			
			generatePlantUMLDomainModel();
			generateJava();
			
			updateRdf();
			
			
		} catch (IOException | SchemaGeneratorException | RDFParseException | RDFHandlerException | PlantumlGeneratorException | CodeGeneratorException e) {
			throw new MojoExecutionException("Failed to convert shapes to Avro", e);
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
    	GcpShapeConfig.init();
    	
		loadSpreadsheet();
		
		if (rdfSourceDir == null && 
			(
				jsonld!=null ||
				java != null ||
				plantUML != null ||
				googleCloudPlatform!=null ||
				jsonSchema!=null
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
			generateJavaCode(structure);
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
			JsonSchemaNamer jsonSchemaNamer = jsonSchema.namer(nsManager, shapeManager);
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
				 transformer.getWorkbookLoader().setInferRdfPropertyDefinitions(workbook.isInferRdfPropertyDefinitions());
				 transformer.transform(workbook.getWorkbookFile(), workbook.owlDir(defaults), workbook.shapesDir(defaults));
			 }
		 } catch (Throwable oops) {
			 throw new MojoExecutionException("Failed to transform workbook to RDF", oops);
		 }
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
	
	private void generateBigQueryTables() throws IOException, MojoExecutionException {
		if (googleCloudPlatform != null) {
			
			File gcpDir = googleCloudPlatform.gcpDir(defaults);
			if (gcpDir == null) {
				throw new MojoExecutionException("googleCloudPlatform.gcpDir must be defined");
			}
			
			File bqOutDir = new File(gcpDir, BIGQUERY);
			File bqSchemaDir = new File(bqOutDir, SCHEMA);
			File bqDatasetDir = new File(bqOutDir, DATASET);
			File bucketDir = new File(gcpDir, CLOUD_STORAGE);
			
			GoogleCloudResourceGenerator resourceGenerator = new GoogleCloudResourceGenerator();
	
			resourceGenerator.addBigQueryGenerator(bqSchemaDir);
			resourceGenerator.addCloudStorageBucketWriter(bucketDir);
			resourceGenerator.dispatch(shapeManager.listShapes());
			
			BigQueryEnumGenerator enumGenerator = new BigQueryEnumGenerator(shapeManager);

			
			File bqDataDir = new File(bqOutDir, DATA);
			

			DataFileMapperImpl dataFileMapper = new DataFileMapperImpl(bqDataDir, datasetMapper, createTableMapper());
			enumGenerator.generate(owlGraph, dataFileMapper);
			
			BigQueryDatasetGenerator datasetGenerator = new BigQueryDatasetGenerator(bqSchemaDir, bqDatasetDir);
			datasetGenerator.run();
		}
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

	private void generateJavaCode(ClassStructure structure) throws IOException, CodeGeneratorException {
		
		final JCodeModel model = new JCodeModel();
		if (java.getPackageRoot()==null) {
			throw new CodeGeneratorException("javaCodeGenerator.packageRoot must be defined");
		}
		JavaNamer javaNamer = new BasicJavaNamer(java.getPackageRoot(), nsManager);
		JavaClassBuilder classBuilder = new JavaClassBuilder(structure, javaNamer, owlReasoner);
		final JsonWriterBuilder writerBuilder = new JsonWriterBuilder(owlReasoner, shapeManager, javaNamer);
		
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
}
