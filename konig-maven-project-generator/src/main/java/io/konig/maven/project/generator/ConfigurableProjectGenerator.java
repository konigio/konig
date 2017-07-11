package io.konig.maven.project.generator;

/*
 * #%L
 * Konig Maven Project Generator
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


import java.io.StringWriter;

import org.apache.velocity.VelocityContext;

public abstract class ConfigurableProjectGenerator<T> extends MavenProjectGenerator {

	protected T config;
	private String tagName;
	
	public ConfigurableProjectGenerator(T config, String tagName) {
		super();
		this.config = config;
		this.tagName = tagName;
	}
	

	protected VelocityContext createVelocityContext() {
		VelocityContext context = super.createVelocityContext();
		putObject(tagName, config);
		return context;
	}
	
	protected void putObject(String tag, Object object) {

		StringWriter buffer = new StringWriter();
		XmlSerializer xml = new XmlSerializer(buffer);
		xml.setIndent(5);
		xml.setIndentWidth(2);
		
		xml.indent();
		xml.write(object, tag);
		xml.flush();
		getContext().put(tag, buffer.toString());
	}
	
	protected void putProperties(int indent, String key, Object object) {
		StringWriter buffer = new StringWriter();
		XmlSerializer xml = new XmlSerializer(buffer);
		xml.setIndent(indent);
		xml.setIndentWidth(2);
		xml.printProperties(object);
		xml.flush();
		
		getContext().put(key, buffer.toString());
	}


	public String getTagName() {
		return tagName;
	}


	public void setTagName(String tagName) {
		this.tagName = tagName;
	}

}
