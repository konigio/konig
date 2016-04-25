package io.konig.schemagen.maven;

import java.io.File;
import java.io.IOException;

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

import com.sun.codemodel.JCodeModel;

import io.konig.core.ContextManager;
import io.konig.core.Graph;
import io.konig.core.NamespaceManager;
import io.konig.core.impl.MemoryContextManager;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.MemoryNamespaceManager;
import io.konig.core.io.GraphLoadHandler;
import io.konig.schemagen.AllJsonldWriter;
import io.konig.schemagen.OntologySummarizer;
import io.konig.schemagen.ShapeMediaTypeLinker;
import io.konig.schemagen.avro.ShapeToAvro;
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
import io.konig.schemagen.jsonschema.impl.SimpleJsonSchemaTypeMapper;
import io.konig.shacl.LogicalShapeBuilder;
import io.konig.shacl.LogicalShapeNamer;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeManager;
import io.konig.shacl.ShapeMediaTypeNamer;
import io.konig.shacl.impl.BasicLogicalShapeNamer;
import io.konig.shacl.impl.MemoryClassManager;
import io.konig.shacl.impl.MemoryShapeManager;
import io.konig.shacl.impl.SimpleShapeMediaTypeNamer;
import io.konig.shacl.io.ShapeLoader;
import io.konig.shacl.jsonld.ContextNamer;
import io.konig.shacl.jsonld.SuffixContextNamer;

/**
 * Goal which generates Avro schemas from SHACL data shapes
 *
 */
@Mojo( name = "generate", defaultPhase = LifecyclePhase.GENERATE_SOURCES )
public class KonigSchemagenMojo  extends AbstractMojo {
    /**
     * Location of the file.
     */
    @Parameter( defaultValue = "${basedir}/main/avro", property = "avroDir", required = true )
    private File avroDir;
    
    
    @Parameter( defaultValue="${basedir}/main/jsonld", property="jsonldDir", required=true)
    private File jsonldDir;
    
    @Parameter( defaultValue="${basedir}/main/jsonschema", property="jsonSchemaDir", required=true)
    private File jsonSchemaDir;
    
    @Parameter( defaultValue="${basedir}/src/main/shapes", property="sourceDir", required=true)
    private File sourceDir;
    
    @Parameter (defaultValue="${basedir}/main/summary", property="summaryDir", required=true)
    private File summaryDir;
    
    @Parameter(property="javaDir")
    private File javaDir;
    
    @Parameter(property="javaPackageRoot")
    private String javaPackageRoot;

    public void execute() throws MojoExecutionException   {
    	
    	try {
    		
    		File avscDir = new File(avroDir, "avsc");
    		File avroImports = new File(avroDir, "imports");
			
			ShapeManager shapeManager = new MemoryShapeManager();
			NamespaceManager nsManager = new MemoryNamespaceManager();
			ContextNamer contextNamer = new SuffixContextNamer("/context");
			ShapeMediaTypeNamer mediaTypeNamer = new SimpleShapeMediaTypeNamer();
			Graph owlGraph = new MemoryGraph();
			ContextManager contextManager = new MemoryContextManager();
			
			ShapeLoader shapeLoader = new ShapeLoader(contextManager, shapeManager, nsManager);
			shapeLoader.setListener(new GraphLoadHandler(owlGraph));
			shapeLoader.loadAll(sourceDir);
			
			ShapeToJsonldContext jsonld = new ShapeToJsonldContext(shapeManager, nsManager, contextNamer, mediaTypeNamer, owlGraph);
			jsonld.generateAll(jsonldDir);
			
			ShapeToAvro avro = new ShapeToAvro(null);
			avro.generateAvro(sourceDir, avscDir, avroImports, owlGraph);
			
			
			
			JsonSchemaTypeMapper jsonSchemaTypeMapper = new SimpleJsonSchemaTypeMapper();
			JsonSchemaNamer jsonSchemaNamer = new SimpleJsonSchemaNamer("/jsonschema", mediaTypeNamer);
			JsonSchemaGenerator jsonSchemaGenerator = new JsonSchemaGenerator(jsonSchemaNamer, nsManager, jsonSchemaTypeMapper);
			ShapeToJsonSchema jsonSchema = new ShapeToJsonSchema(jsonSchemaGenerator);
			jsonSchema.setListener(new ShapeToJsonSchemaLinker(owlGraph));
			jsonSchema.generateAll(shapeManager.listShapes(), jsonSchemaDir);
			
			ShapeMediaTypeLinker linker = new ShapeMediaTypeLinker(mediaTypeNamer);
			linker.assignAll(shapeManager.listShapes(), owlGraph);
			
			writeSummary(nsManager, owlGraph);
			
			if (javaDir != null && javaPackageRoot!=null) {
				generateJavaCode(owlGraph, nsManager, shapeManager);
			}
			
			
		} catch (IOException e) {
			throw new MojoExecutionException("Failed to convert shapes to Avro", e);
		}
      
    }

	private void generateJavaCode(Graph owlGraph, NamespaceManager nsManager, ShapeManager shapeManager) throws IOException {
		

		MemoryClassManager classManager = new MemoryClassManager();
		LogicalShapeNamer namer = new BasicLogicalShapeNamer("http://example.com/shapes/logical/", nsManager);
		
		LogicalShapeBuilder builder = new LogicalShapeBuilder(namer);
		builder.buildLogicalShapes(shapeManager, classManager);
		
		JCodeModel model = new JCodeModel();
		JavaNamer javaNamer = new BasicJavaNamer(javaPackageRoot, nsManager);
		JavaClassBuilder classBuilder = new JavaClassBuilder(classManager, namer, javaNamer, owlGraph);
		
		for (Shape shape : classManager.list()) {
			classBuilder.buildClass(shape, model);
		}
		
		javaDir.mkdirs();
		model.build(javaDir);
		
	}

	private void writeSummary(NamespaceManager nsManager, Graph owlGraph) throws IOException  {
		
		summaryDir.mkdirs();
		
		File namespacesFile = new File(summaryDir, "namespaces.ttl");
		File projectFile = new File(summaryDir, "project.jsonld");

		OntologySummarizer summarizer = new OntologySummarizer();
		summarizer.summarize(nsManager, owlGraph, namespacesFile);
		
		AllJsonldWriter all = new AllJsonldWriter();
		all.writeJSON(nsManager, owlGraph, projectFile);
		
	}
}
