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

public class JsonSchemaConfig {

	private String uriTemplate;
	private File jsonSchemaDir;
	private boolean additionalProperties=false;
	private boolean generateSchemaId = false;
	
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
	public boolean getAdditionalProperties() {
		return additionalProperties;
	}
	public void setAdditionalProperties(boolean additionalProperties) {
		this.additionalProperties = additionalProperties;
	}
	
	public boolean getGenerateSchemaId() {
		return generateSchemaId;
	}
	public void setGenerateSchemaId(boolean generateSchemaId) {
		this.generateSchemaId = generateSchemaId;
	}
	
	
	
	
	
//	public JsonSchemaNamer namer(NamespaceManager nsManager, ShapeManager shapeManager) {
//		String templateText = (uriTemplate==null) ? "{shapeId}/jsonSchema" : uriTemplate;
//		ValueFormat format = new SimpleValueFormat(templateText);
//		return new TemplateJsonSchemaNamer(nsManager, shapeManager, format);
//	}
}
