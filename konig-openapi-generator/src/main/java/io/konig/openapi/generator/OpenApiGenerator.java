package io.konig.openapi.generator;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;

import io.konig.jsonschema.generator.JsonSchemaGenerator;
import io.konig.jsonschema.generator.JsonSchemaGeneratorException;
import io.konig.jsonschema.generator.JsonSchemaNamer;
import io.konig.jsonschema.model.JsonSchema;
import io.konig.openapi.model.Components;
import io.konig.openapi.model.OpenAPI;
import io.konig.openapi.model.SchemaMap;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeFilter;
import io.konig.shacl.ShapeManager;
import io.konig.yaml.AnchorFeature;
import io.konig.yaml.Yaml;
import io.konig.yaml.YamlWriterConfig;

public class OpenApiGenerator {
	private static final String OPENAPI_VERSION = "3.0.0";
	private static final String VELOCITY_TEMPLATE = "openapi-generator/openapi.yaml";
	private static final String ENTITY_LIST = "entityList";
	
	private JsonSchemaGenerator jsonSchemaGenerator;
	private JsonSchemaNamer jsonSchemaNamer;
	private ShapeFilter shapeFilter;
	private File velocityLog;


	public OpenApiGenerator(
		JsonSchemaNamer jsonSchemaNamer, 
		JsonSchemaGenerator jsonSchemaGenerator, 
		ShapeFilter shapeFilter
	) {
		this.jsonSchemaNamer = jsonSchemaNamer;
		this.jsonSchemaGenerator = jsonSchemaGenerator;
		this.shapeFilter = shapeFilter;
	}
	

	public File getVelocityLog() {
		return velocityLog;
	}

	public void setVelocityLog(File velocityLog) {
		this.velocityLog = velocityLog;
	}

	public void generate(OpenApiGenerateRequest request) throws OpenApiGeneratorException {
	
		try {
			Worker worker = new Worker();
			worker.generate(request);
		} catch (IOException | JsonSchemaGeneratorException e) {
			throw new OpenApiGeneratorException(e);
		}
		
	}
	
	private class Worker {
		
		private ShapeManager shapeManager;
		private Map<URI,Entity> entityMap = new HashMap<>();
		private VelocityEngine engine;
		private VelocityContext context;
		private OpenApiGenerateRequest request;
		private List<Shape> shapeList = new ArrayList<>();
		private PrintWriter out;
		
		private void generate(OpenApiGenerateRequest request) throws OpenApiGeneratorException, IOException, JsonSchemaGeneratorException {
			this.request = request;
			this.shapeManager = request.getShapeManager();
			
			createPrintWriter();
			createVelocityEngine();
			createContext();
			printOpenapiProperty();
			printInfo();
			merge();
			printComponents();
			out.flush();
			
			
		}

		private void printInfo() throws OpenApiGeneratorException, IOException {
			if (request.getOpenApiInfo() == null) {
				throw new OpenApiGeneratorException("OpenApiGenerateRequest.openApiInfo must be defined");
			}
			IOUtils.copy(request.getOpenApiInfo(), out);
			out.println();
			
		}

		private void printOpenapiProperty() {
			out.print("openapi: ");
			out.println(OPENAPI_VERSION);
			
		}

		private void printComponents() throws JsonSchemaGeneratorException {
			OpenAPI api = new OpenAPI();
			Components components = new Components();
			api.setComponents(components);
			
			addSchemas(components);
			YamlWriterConfig config = new YamlWriterConfig()
				.setAnchorFeature(AnchorFeature.NONE)
				.setIncludeClassTag(false);
			
			Yaml.write(out, config, api);
			
			
		}

		private void addSchemas(Components components) throws JsonSchemaGeneratorException {

			components.setSchemas(new SchemaMap());
			for (Shape shape : shapeList) {
				JsonSchema schema = jsonSchemaGenerator.asJsonSchema(shape);
				String name = jsonSchemaNamer.schemaId(shape);
				
				components.getSchemas().put(name, schema);
			}
			
		}

		private void createPrintWriter() {
			
			out = request.getWriter() instanceof PrintWriter ? 
				(PrintWriter) request.getWriter() :
				new PrintWriter(request.getWriter());
			
		}

		private void merge() throws IOException {

			Template template = engine.getTemplate(VELOCITY_TEMPLATE);
			template.merge(context, out);
			
		}

		private void createContext() {
			context = new VelocityContext();
			buildEntityMap();
			List<Entity> entityList = new ArrayList<>(entityMap.values());
			Collections.sort(entityList);
			context.put(ENTITY_LIST, entityList);
		}

		private void createVelocityEngine() {

			Properties properties = new Properties();
			properties.put("resource.loader", "class");
			properties.put("class.resource.loader.class", ClasspathResourceLoader.class.getName());
			if (velocityLog != null) {
				properties.put("runtime.log", velocityLog.getAbsolutePath());
			}
			
			engine = new VelocityEngine(properties);
			
		}


		private void buildEntityMap() {
			for (Shape shape : shapeManager.listShapes()) {
				if (shapeFilter.accept(shape)) {
					
					Resource targetClass = shape.getTargetClass();
					if (targetClass instanceof URI) {
						String refValue = jsonSchemaNamer.schemaId(shape);
						Entity entity = produceEntity((URI)targetClass);
						entity.addShape(shape);
						entity.addMediaType(new MediaType(shape.getMediaTypeBaseName()+"+json", refValue));
						shapeList.add(shape);
					}
				}
			}
			
		}

		private Entity produceEntity(URI targetClass) {
			Entity e = entityMap.get(targetClass);
			if (e == null) {
				e = new Entity(targetClass);
				entityMap.put(targetClass, e);
			}
			return e;
		}
	}
	

}
