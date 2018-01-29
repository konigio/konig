package io.konig.spreadsheet;

/*
 * #%L
 * Konig Spreadsheet
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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.RuntimeSingleton;
import org.apache.velocity.runtime.parser.ParseException;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.openrdf.model.Namespace;
import org.openrdf.model.URI;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;

import io.konig.core.Graph;
import io.konig.core.KonigException;
import io.konig.core.NamespaceManager;
import io.konig.core.impl.RdfUtil;
import io.konig.shacl.Shape;

/**
 * A utility that generates DataSource definitions associated with Shapes based
 * on a template.
 * 
 * @author Greg McFall
 *
 */
public class DataSourceGenerator {

	private NamespaceManager nsManager;
	private File templateDir;
	private VelocityEngine engine;
	private File velocityLog;
	private VelocityContext context;

	public DataSourceGenerator(NamespaceManager nsManager, File templateDir, Properties properties) {
		this.nsManager = nsManager;
		this.templateDir = templateDir;
		this.context = new VelocityContext();
		put(properties);
		createVelocityEngine();
	}

	public File getVelocityLog() {
		return velocityLog;
	}

	public void setVelocityLog(File velocityLog) {
		this.velocityLog = velocityLog;
	}

	private void createVelocityEngine() {

		Properties properties = new Properties();
		properties.put("resource.loader", "class");
		properties.setProperty("file.resource.loader.path", "WorkbookLoader");
		properties.put("class.resource.loader.class", ClasspathResourceLoader.class.getName());
		if (velocityLog != null) {
			properties.put("runtime.log", velocityLog.getAbsolutePath());
		}

		engine = new VelocityEngine(properties);
	}

	private String merge(String templateName){
		StringWriter result = new StringWriter();
		try {
			templateName = "WorkbookLoader/" + templateName + ".ttl";
			StringWriter tempresult = new StringWriter();
			Template template = engine.getTemplate(templateName, "UTF-8");
			template.merge(context, tempresult);
	
			RuntimeServices runtimeServices = RuntimeSingleton.getRuntimeServices();
			StringReader reader = new StringReader(tempresult.toString());
			template.setRuntimeServices(runtimeServices);
			template.setData(runtimeServices.parse(reader, templateName));
			template.initDocument();
			template.merge(context, result);
		} catch (ParseException e) {
			throw new KonigException("Failed to parse template", e);
		}
		return result.toString();
	}

	public void put(Properties properties) {
		Set<Entry<Object, Object>> entries = properties.entrySet();
		for (Entry<Object, Object> e : entries) {
			String key = e.getKey().toString();
			String value = e.getValue().toString();
			context.put(key, value);
		}
	}

	public File getTemplateDir() {
		return templateDir;
	}

	public void setTemplateDir(File templateDir) {
		this.templateDir = templateDir;
	}

	@SuppressWarnings("rawtypes")
	public void generate(Shape shape, Function func, Graph graph) {

		String templateName = func.getName();
		HashMap params = (HashMap) func.getParameters();
		Iterator it = params.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pair = (Map.Entry) it.next();
			context.put(pair.getKey().toString(), pair.getValue().toString());
		}

		defaultMap(shape);
		String result = merge(templateName);
		ByteArrayInputStream input = new ByteArrayInputStream(result.getBytes());

		try {
			RdfUtil.loadTurtle(graph, input, "");
		} catch (RDFParseException | RDFHandlerException | IOException e) {
			throw new KonigException("Failed to render template", e);
		}

	}

	private void defaultMap(Shape shape) {
		if (shape.getId() instanceof URI) {
			putURI("shape", (URI) shape.getId());
		}
		String mediaType = shape.getMediaTypeBaseName();
		if (mediaType != null) {
			int slash = mediaType.lastIndexOf('/');
			if (slash > 0) {
				String mediaSubtype = mediaType.substring(slash + 1);
				context.put("mediaSubtype", mediaSubtype);
			}
		}
		putURI("class", shape.getTargetClass());
	}

	public void generate(Shape shape, String templateName, Graph graph) {
		defaultMap(shape);
		String result = merge(templateName);
		ByteArrayInputStream input = new ByteArrayInputStream(result.getBytes());

		try {
			RdfUtil.loadTurtle(graph, input, "");
		} catch (RDFParseException | RDFHandlerException | IOException e) {
			throw new KonigException("Failed to render template", e);
		}

	}

	private void putURI(String name, URI value) {

		if (value != null) {
			Namespace ns = nsManager.findByName(value.getNamespace());
			context.put(name + "Id", value.stringValue());
			context.put(name + "LocalName", value.getLocalName());
			context.put(name + "LocalNameLowercase", value.getLocalName().toLowerCase());
			if (ns != null) {
				context.put(name + "NamespacePrefix", ns.getPrefix());
			}
		}

	}

}
