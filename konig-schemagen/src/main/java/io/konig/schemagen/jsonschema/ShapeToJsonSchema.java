package io.konig.schemagen.jsonschema;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.openrdf.model.URI;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.konig.schemagen.SchemaGeneratorException;
import io.konig.shacl.Shape;

public class ShapeToJsonSchema {
	
	private JsonSchemaGenerator generator;
	private ObjectMapper mapper;
	private JsonSchemaListener listener;
	
	public ShapeToJsonSchema(JsonSchemaGenerator generator) {
		this.generator = generator;
		mapper = new ObjectMapper();
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
	}


	public JsonSchemaListener getListener() {
		return listener;
	}


	public void setListener(JsonSchemaListener listener) {
		this.listener = listener;
	}


	/**
	 * Generate JSON Schema for a list of Shapes and store those schemas in a given directory.
	 * @param list The list of shapes for which JSON Schemas will be generated.
	 * @param outDir The directory in which the JSON Schema files will be stored
	 * @throws SchemaGeneratorException
	 */
	public void generateAll(List<Shape> list, File outDir) throws SchemaGeneratorException {
		outDir.mkdirs();
		JsonSchemaNamer namer = generator.getNamer();
		for (Shape shape : list) {
			if (shape.getId() instanceof URI) {
				File file = new File(outDir, namer.jsonSchemaFileName(shape));
				generateJsonSchema(shape, file);
			}
		}
	}
	
	/**
	 * Generate the JSON Schema for a given shape and store it in a specified file.
	 * @param shape The shape for which a JSON Schema will be generated
	 * @param jsonSchemaFile The file where the JSON Schema will be stored
	 */
	public void generateJsonSchema(Shape shape, File jsonSchemaFile) throws SchemaGeneratorException {
		ObjectNode json = generator.generateJsonSchema(shape);
		try {
			if (listener != null) {
				listener.handleJsonSchema(shape, json);
			}
			mapper.writeValue(jsonSchemaFile, json);
		} catch (IOException e) {
			throw new SchemaGeneratorException(e);
		}
		
	}
	


}
