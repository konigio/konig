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
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.openrdf.model.Namespace;
import org.openrdf.model.URI;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;

import io.konig.core.Graph;
import io.konig.core.KonigException;
import io.konig.core.NamespaceManager;
import io.konig.core.impl.RdfUtil;
import io.konig.core.util.CompositeValueMap;
import io.konig.core.util.SimpleValueFormat;
import io.konig.core.util.SimpleValueMap;
import io.konig.core.util.ValueMap;
import io.konig.shacl.Shape;

/**
 * A utility that generates DataSource definitions associated with Shapes based on a template.
 * @author Greg McFall
 *
 */
public class DataSourceGenerator {
	
	private NamespaceManager nsManager;
	private SimpleValueMap settings = new SimpleValueMap();
	private Map<String, SimpleValueFormat> templateMap = new HashMap<>();
	private File templateDir;
	
	public DataSourceGenerator(NamespaceManager nsManager, File templateDir, Properties properties) {
		this.nsManager = nsManager;
		this.templateDir = templateDir;
		put(properties);
		
		this.templateDir = templateDir;
	}
	
	public void put(Properties properties) {

		Set<Entry<Object,Object>> entries = properties.entrySet();
		for (Entry<Object,Object> e : entries) {
			String key = e.getKey().toString();
			String value = e.getValue().toString();
			settings.put(key, value);
		}
	}
	
	public File getTemplateDir() {
		return templateDir;
	}



	public void setTemplateDir(File templateDir) {
		this.templateDir = templateDir;
	}


	public void generate(Shape shape, Function func, Graph graph) {
		String templateName = func.getName();
		ValueMap params = func.getParameters();

		SimpleValueFormat template = null;

		try {
			template = getTemplate(templateName);
		} catch (IOException e) {
			throw new KonigException(e);
		}
		
		if (template == null) {
			throw new KonigException("Template not found: " + templateName);
		}
		
		SimpleValueMap defaultMap = defaultMap(shape);
	
		CompositeValueMap map = new CompositeValueMap(params, defaultMap, settings);
		

		String result = template.format(map);
		ByteArrayInputStream input = new ByteArrayInputStream(result.getBytes());
		
		try {
			RdfUtil.loadTurtle(graph, input, "");
		} catch (RDFParseException | RDFHandlerException | IOException e) {
			throw new KonigException("Failed to render template", e);
		}
	}

	private SimpleValueMap defaultMap(Shape shape) {

		SimpleValueMap map = new SimpleValueMap();
		
		
		if (shape.getId() instanceof URI) {
			putURI(map, "shape", (URI)shape.getId());
		}
		putURI(map, "class", shape.getTargetClass());
		return map;
	}

	public void generate(Shape shape, String templateName, Graph graph) {
		
		
		SimpleValueFormat template = null;
		
		try {
			template = getTemplate(templateName);
		} catch (IOException e) {
			throw new KonigException(e);
		}
		
		if (template == null) {
			throw new KonigException("Template not found: " + templateName);
		}
		
		SimpleValueMap map = defaultMap(shape);
		CompositeValueMap composite = new CompositeValueMap(map, settings);

		String result = template.format(composite);
		ByteArrayInputStream input = new ByteArrayInputStream(result.getBytes());
		
		try {
			RdfUtil.loadTurtle(graph, input, "");
		} catch (RDFParseException | RDFHandlerException | IOException e) {
			throw new KonigException("Failed to render template", e);
		}
		
		
	}

	private SimpleValueFormat getTemplate(String templateName) throws IOException {
		SimpleValueFormat template = templateMap.get(templateName);
		if (template == null) {
			
			InputStream stream = openTemplate(templateName);
			if (stream == null) {
				return null;
			}
			
			try {

				byte[] buffer = new byte[1024];
				int len = -1;
				
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				while ((len=stream.read(buffer)) != -1) {
					out.write(buffer, 0, len);
				}
				
				String text = out.toString("UTF-8");
				template = new SimpleValueFormat(text);
				templateMap.put(templateName, template);
				
			} finally {
				stream.close();
			}
		}
		return template;
	}
	

	
	private InputStream openTemplate(String templateName) throws FileNotFoundException {
		String fileName = templateName + ".ttl";

		InputStream stream = null;
		
		if (templateDir != null) {
			File file = new File(templateDir, fileName);
			if (file.exists()) {
				stream = new FileInputStream(file);
			}
		}
		
		if (stream == null) {
			stream = getClass().getClassLoader().getResourceAsStream("WorkbookLoader/" + fileName);
		}
		return stream;
	}

	private void putURI(SimpleValueMap map, String name, URI value) {
		
		if (value != null) {
			Namespace ns = nsManager.findByName(value.getNamespace());
			
			map.put(name+"Id", value.stringValue());
			map.put(name+"LocalName", value.getLocalName());
			map.put(name+"LocalNameLowercase", value.getLocalName().toLowerCase());
			
			if (ns != null) {
				map.put(name+"NamespacePrefix", ns.getPrefix());
			}
		}
		
	}


}
