package io.konig.schemagen.maven;

import java.io.File;

import io.konig.core.NamespaceManager;
import io.konig.core.util.SimpleValueFormat;
import io.konig.core.util.ValueFormat;
import io.konig.schemagen.jsonschema.JsonSchemaNamer;
import io.konig.schemagen.jsonschema.TemplateJsonSchemaNamer;
import io.konig.shacl.ShapeManager;

public class JsonSchemaConfig {

	private String uriTemplate;
	private File jsonSchemaDir;
	
	public String getUriTemplate() {
		return uriTemplate;
	}
	public void setUriTemplate(String uriTemplate) {
		this.uriTemplate = uriTemplate;
	}
	public File getJsonSchemaDir() {
		return jsonSchemaDir;
	}
	public void setJsonSchemaDir(File jsonSchemaDir) {
		this.jsonSchemaDir = jsonSchemaDir;
	}
	
	
	public JsonSchemaNamer namer(NamespaceManager nsManager, ShapeManager shapeManager) {
		String templateText = (uriTemplate==null) ? "{shapeId}/jsonSchema" : uriTemplate;
		ValueFormat format = new SimpleValueFormat(templateText);
		return new TemplateJsonSchemaNamer(nsManager, shapeManager, format);
	}
}
