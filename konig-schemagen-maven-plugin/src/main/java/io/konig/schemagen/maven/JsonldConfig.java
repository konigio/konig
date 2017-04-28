package io.konig.schemagen.maven;

import java.io.File;

import io.konig.core.NamespaceManager;
import io.konig.core.util.SimpleValueFormat;
import io.konig.core.util.ValueFormat;
import io.konig.shacl.ShapeManager;
import io.konig.shacl.jsonld.ContextNamer;
import io.konig.shacl.jsonld.SuffixContextNamer;
import io.konig.shacl.jsonld.TemplateContextNamer;

public class JsonldConfig {
	
	private String uriTemplate;
	private File jsonldDir;
	
	public String getUriTemplate() {
		return uriTemplate;
	}
	public void setUriTemplate(String uriTemplate) {
		this.uriTemplate = uriTemplate;
	}
	public File getJsonldDir() {
		return jsonldDir;
	}
	public void setJsonldDir(File jsonldDir) {
		this.jsonldDir = jsonldDir;
	}
	
	public ContextNamer contextNamer(NamespaceManager nsManager, ShapeManager shapeManager) {
		if (uriTemplate != null) {
			ValueFormat template = new SimpleValueFormat(uriTemplate);
			return new TemplateContextNamer(nsManager, shapeManager, template);
		}
		
		return new SuffixContextNamer("/context");
	}

}
