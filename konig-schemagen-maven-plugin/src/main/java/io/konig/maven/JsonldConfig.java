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
