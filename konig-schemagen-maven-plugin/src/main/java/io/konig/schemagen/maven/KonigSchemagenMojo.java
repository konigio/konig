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
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
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
import io.konig.schemagen.AllJsonldWriter;
import io.konig.schemagen.OntologySummarizer;
import io.konig.schemagen.SchemaGeneratorException;
import io.konig.schemagen.ShapeMediaTypeLinker;
import io.konig.schemagen.SimpleShapeNamer;
import io.konig.schemagen.avro.ShapeToAvro;
import io.konig.schemagen.avro.impl.SmartAvroDatatypeMapper;
import io.konig.schemagen.gcp.BigQueryTableGenerator;
import io.konig.schemagen.gcp.BigQueryTableMapper;
import io.konig.schemagen.gcp.GoogleCloudConfig;
import io.konig.schemagen.gcp.LocalNameTableMapper;
import io.konig.schemagen.gcp.MemoryGoogleCloudManager;
import io.konig.schemagen.gcp.NamespaceDatasetMapper;
import io.konig.schemagen.gcp.SimpleProjectMapper;
import io.konig.schemagen.java.BasicJavaNamer;
import io.konig.schemagen.java.JavaClassBuilder;
import io.konig.schemagen.java.JavaNamer;
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
import net.sourceforge.plantuml.SourceFileReader;

/**
 * Goal which generates Avro schemas from SHACL data shapes
 *
 */
@Mojo( name = "generate", defaultPhase = LifecyclePhase.GENERATE_SOURCES )
public class KonigSchemagenMojo  extends AbstractMojo {
    /**
     * Location of the file.
     */
    @Parameter( defaultValue = "${basedir}/src/main/avro", property = "avroDir", required = true )
    private File avroDir;
    
    
    @Parameter( defaultValue="${basedir}/src/main/jsonld", property="jsonldDir", required=true)
    private File jsonldDir;
    
    @Parameter( defaultValue="${basedir}/src/main/jsonschema", property="jsonSchemaDir", required=true)
    private File jsonSchemaDir;
    
    @Parameter( defaultValue="${basedir}/src/main/rdf", property="sourceDir", required=true)
    private File sourceDir;
    
    @Parameter (defaultValue="${basedir}/src/main/summary", property="summaryDir", required=true)
    private File summaryDir;
    

    @Parameter (defaultValue="${basedir}/src/main/summary/domain.plantuml", property="plantUMLDomainModelFile")
    private File plantUMLDomainModelFile;

    @Parameter (defaultValue="${basedir}/src/main/summary/domain.png", property="domain.png")
    private File domainModelPngFile;
    
    @Parameter (property="skipPlantUMLDomainModel")
    private boolean skipPlantUMLDomainModel=false;
    
    @Parameter(property="javaDir")
    private File javaDir;
    
    @Parameter(property="javaPackageRoot")
    private String javaPackageRoot;
    
    @Parameter
    private HashSet<String> excludeNamespace;
    
    @Parameter
    private String bqShapeBaseURL;
    
    @Parameter(defaultValue="${basedir}/src/main/bigquery", property="bqOutDir")
    private File bqOutDir;
    
    @Parameter
    private File bqSourceDir;

	 @Parameter (defaultValue="${basedir}/target/rdf/shapes", property="shapesOutDir", required=false)
	 private File shapesOutDir;
    
    private NamespaceManager nsManager;
    private ClassManager classManager;
    private OwlReasoner owlReasoner;
    private LogicalShapeNamer logicalShapeNamer;
    private ShapeManager shapeManager;

    public void execute() throws MojoExecutionException   {
    	
    	try {
    		
    		File avscDir = new File(avroDir, "avsc");
    		File avroImports = new File(avroDir, "imports");
			
			shapeManager = new MemoryShapeManager();
			nsManager = new MemoryNamespaceManager();
			ContextNamer contextNamer = new SuffixContextNamer("/context");
			ShapeMediaTypeNamer mediaTypeNamer = new SimpleShapeMediaTypeNamer();
			Graph owlGraph = new MemoryGraph();
			ContextManager contextManager = new MemoryContextManager();

			RdfUtil.loadTurtle(sourceDir, owlGraph, nsManager);
			ShapeLoader shapeLoader = new ShapeLoader(contextManager, shapeManager, nsManager);
			shapeLoader.load(owlGraph);
			

			
			owlReasoner = new OwlReasoner(owlGraph);
			
			
			
			if (bqShapeBaseURL != null) {
				generateBigQueryTables(owlGraph);
			}
			
			
			ShapeToJsonldContext jsonld = new ShapeToJsonldContext(shapeManager, nsManager, contextNamer, mediaTypeNamer, owlGraph);
			jsonld.generateAll(jsonldDir);
			
			SmartAvroDatatypeMapper avroMapper = new SmartAvroDatatypeMapper(owlReasoner);
			ShapeToAvro avro = new ShapeToAvro(avroMapper);
			avro.generateAvro(sourceDir, avscDir, avroImports, owlGraph);
			
			JsonSchemaTypeMapper jsonSchemaTypeMapper = new SmartJsonSchemaTypeMapper(owlReasoner);
			JsonSchemaNamer jsonSchemaNamer = new SimpleJsonSchemaNamer("/jsonschema", mediaTypeNamer);
			JsonSchemaGenerator jsonSchemaGenerator = new JsonSchemaGenerator(jsonSchemaNamer, nsManager, jsonSchemaTypeMapper);
			ShapeToJsonSchema jsonSchema = new ShapeToJsonSchema(jsonSchemaGenerator);
			jsonSchema.setListener(new ShapeToJsonSchemaLinker(owlGraph));
			jsonSchema.generateAll(shapeManager.listShapes(), jsonSchemaDir);
			
			
			
			ShapeMediaTypeLinker linker = new ShapeMediaTypeLinker(mediaTypeNamer);
			linker.assignAll(shapeManager.listShapes(), owlGraph);
			
			owlReasoner.inferClassFromSubclassOf();
			writeSummary(nsManager, shapeManager, owlGraph);
			
			if (!skipPlantUMLDomainModel) {
				generatePlantUMLDomainModel();
			}
			
			if (javaDir != null && javaPackageRoot!=null) {
				generateJavaCode(shapeManager);
			}
			
		
			
			
		} catch (IOException | SchemaGeneratorException | RDFParseException | RDFHandlerException | PlantumlGeneratorException e) {
			throw new MojoExecutionException("Failed to convert shapes to Avro", e);
		}
      
    }


	private void generatePlantUMLDomainModel() throws IOException, PlantumlGeneratorException {
		
		ClassManager classManager = getClassManager();
		PlantumlClassDiagramGenerator generator = new PlantumlClassDiagramGenerator(owlReasoner, shapeManager);
		FileWriter writer = new FileWriter(plantUMLDomainModelFile);
		try {
			generator.generateDomainModel(classManager, writer);
		} finally {
			close(writer);
		}
		
//		FileOutputStream imageFile = new FileOutputStream(domainModelPngFile);
		SourceFileReader reader = new SourceFileReader(plantUMLDomainModelFile);
		reader.getGeneratedImages();
		
	}

	private void close(Closeable stream) {
		try {
			stream.close();
		} catch (IOException oops) {
			oops.printStackTrace();
		}
		
	}

	private void generateBigQueryTables(Graph graph) throws SchemaGeneratorException, IOException, RDFParseException, RDFHandlerException {
		
		if (bqSourceDir != null) {
			RdfUtil.loadTurtle(bqSourceDir, graph, nsManager);
		}
		SimpleShapeNamer shapeNamer = new SimpleShapeNamer(nsManager, bqShapeBaseURL);
		shapeNamer.setPrefixBase("bq");
		BigQueryTableGenerator generator = new BigQueryTableGenerator(shapeManager, shapeNamer, owlReasoner);
		
		MemoryGoogleCloudManager cloudManager = new MemoryGoogleCloudManager();
		
		GoogleCloudConfig config = new GoogleCloudConfig(cloudManager, generator);
		
		// For now, we hardcode the project and dataset id.
		// We'll fix this later.
		
		config.load(graph);
		
		generator.generateBigQueryTables(cloudManager);
		ShapeFileGetter shapeFileGetter = new ShapeFileGetter(shapesOutDir, nsManager);
		cloudManager.setProjectMapper(new SimpleProjectMapper("testProject"));
		cloudManager.setDatasetMapper(new NamespaceDatasetMapper(nsManager));
		BigQueryTableMapper tableMapper = new LocalNameTableMapper();
		generator.setTableMapper(tableMapper);
		generator.generateEnumTables(graph, cloudManager);
		config.writeEnumTableShapes(nsManager, shapeFileGetter);
		
		config.writeBigQueryTableDefinitions(bqOutDir);
		
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

	private void generateJavaCode(ShapeManager shapeManager) throws IOException {
		

		ClassManager classManager = getClassManager();
		LogicalShapeNamer namer = getLogicalShapeNamer();
		
		
		JCodeModel model = new JCodeModel();
		JavaNamer javaNamer = new BasicJavaNamer(javaPackageRoot, nsManager);
		JavaClassBuilder classBuilder = new JavaClassBuilder(classManager, namer, javaNamer, owlReasoner);
		
		for (Shape shape : classManager.list()) {
			classBuilder.buildClass(shape, model);
		}
		
		javaDir.mkdirs();
		model.build(javaDir);
		
	}

	private void writeSummary(NamespaceManager nsManager, ShapeManager shapeManager, Graph owlGraph) throws IOException  {
		
		summaryDir.mkdirs();
		
		File namespacesFile = new File(summaryDir, "namespaces.ttl");
		File projectFile = new File(summaryDir, "project.jsonld");
		File domainFile = new File(summaryDir, "domain.ttl");
		File prototypeFile = new File(summaryDir, "prototype.ttl");

		OntologySummarizer summarizer = new OntologySummarizer();
		summarizer.summarize(nsManager, owlGraph, namespacesFile);
		summarizer.writeDomainModel(nsManager, owlGraph, shapeManager, domainFile);
		summarizer.writePrototypeModel(nsManager, owlGraph, shapeManager, prototypeFile);
		
		
		AllJsonldWriter all = new AllJsonldWriter();
		all.writeJSON(nsManager, owlGraph, excludeNamespace, projectFile);
		
	}
}
