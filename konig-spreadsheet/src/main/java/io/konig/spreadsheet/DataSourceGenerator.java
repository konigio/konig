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
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.commons.io.FileUtils;
import org.apache.poi.util.IOUtils;
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

	private static final String EDITED = "edited";
	private NamespaceManager nsManager;
	private File templateDir;
	private VelocityEngine velocityEngine;
	private File velocityLog;
	private VelocityContext context;
	private Properties defaultProperties;
	private List<RegexRule> regexRuleList = new ArrayList<>();
	
	private File editedTemplateDir=null;
	
	
	public static class VelocityFunctions {
		private NamespaceManager nsManager;
		
		private VelocityFunctions(NamespaceManager nsManager) {
			this.nsManager = nsManager;
		}
		public String spaceToComma(String text) {
			
			StringBuilder builder = new StringBuilder();
			StringTokenizer tokens = new StringTokenizer(text, " \t\n\r");
			while (tokens.hasMoreTokens()) {
				String token = tokens.nextToken();
				if (builder.length()>0) {
					builder.append(", ");
				}
				builder.append(token);
			}
			
			return builder.toString();
		}
		
		public List<Namespace> listNamespaces(String text) {
			List<Namespace> result = null;
			if (text != null) {
				StringTokenizer tokens = new StringTokenizer(text, " \t\r\n");
				while (tokens.hasMoreTokens()) {
					String id = tokens.nextToken();
					int colon = id.indexOf(':');
					String prefix = id.substring(0,  colon);
					Namespace ns = nsManager.findByPrefix(prefix);
					if (ns == null) {
						ns = nsManager.findByName(id);
					}
					
					if (ns == null) {
						throw new RuntimeException("Namespace prefix not found: " + id);
					}
					if (result == null) {
						result = new ArrayList<>();
					}
					result.add(ns);
				}
			}
			return result;
		}
	}
	public DataSourceGenerator(NamespaceManager nsManager, File templateDir, Properties properties) {
		this.nsManager = nsManager;
		this.templateDir = templateDir;
		this.context = new VelocityContext();
		this.defaultProperties = properties;
		context.put("templateException", new TemplateException());
		context.put("functions", new VelocityFunctions(nsManager));
		context.put("beginVar", "${");
		context.put("endVar", "}");
		createVelocityEngine();
	}


	public void addRegexRule(RegexRule rule) {
		regexRuleList.add(rule);
	}

	public File getTemplateDir() {
		return templateDir;
	}


	public File getVelocityLog() {
		return velocityLog;
	}

	public void setVelocityLog(File velocityLog) {
		this.velocityLog = velocityLog;
	}

	private void createVelocityEngine() {
		Properties properties = new Properties();
		properties.put("resource.loader", "file,class");
		properties.setProperty("file.resource.loader.path", templateDir.getPath());
		properties.put("class.resource.loader.class", ClasspathResourceLoader.class.getName());
		if (velocityLog != null) {
			properties.put("runtime.log", velocityLog.getAbsolutePath());
		}

		velocityEngine = new VelocityEngine(properties);
	}
	
	
	private String merge(String templateName) {
		String templateText =  simpleMerge(templateName);
		if(!regexRuleList.isEmpty()) {
			templateText = mergeWithRegex(templateName, templateText);
		}
		if(defaultProperties != null) {
			templateText = mergeWithDefaultProperties(templateName, templateText);
			removeDefaultProperties(defaultProperties);
			templateText = mergeDefaultMap(templateName, templateText);
		}
		return templateText;
	}
	
	
	private String mergeTemplate(String templateName, String templateText) {
		StringWriter result = new StringWriter();
		try {
			Template template = new Template();
			RuntimeServices runtimeServices = RuntimeSingleton.getRuntimeServices();
			StringReader reader = new StringReader(templateText);
			template.setRuntimeServices(runtimeServices);
			template.setData(runtimeServices.parse(reader, templateName));
			template.initDocument();
			template.merge(context, result);
			templateText = result.toString();
		} catch (ParseException e) {
			throw new KonigException("Failed to parse template", e);
		}
		return templateText;
	}
	private String mergeWithDefaultProperties(String templateName, String templateText){
		put(defaultProperties);	
		return mergeTemplate(templateName, templateText); 
	}
	
	private String mergeDefaultMap(String templateName, String templateText) {
		return mergeTemplate(templateName, templateText); 
	}
	
	public void removeDefaultProperties(Properties properties) {
		Set<Entry<Object, Object>> entries = properties.entrySet();
		for (Entry<Object, Object> e : entries) {
			context.remove(e.getKey());
		}
	}
	private String simpleMerge(String templateName){
			templateName = "WorkbookLoader/" + templateName + ".ttl";
			StringWriter tempresult = new StringWriter();
			Template template = velocityEngine.getTemplate(templateName, "UTF-8");
			template.merge(context, tempresult);
			return mergeTemplate(templateName, tempresult.toString());
	}
	
	private String mergeWithRegex(String templateName, String templateText) {
		try {			
			if (templateText == null) {
				throw new KonigException("Template not found: " + templateName);
			}
			String editedTemplatePath = editTemplate(templateName, templateText);
			if (editedTemplatePath == null) {
				return simpleMerge(templateName);
			} 
			StringWriter tempresult = new StringWriter();
			Template template = velocityEngine.getTemplate(editedTemplatePath, "UTF-8");
			template.merge(context, tempresult);
			return mergeTemplate(templateName, tempresult.toString());
			
		} catch (Exception e) {
			throw new KonigException("Failed to parse template", e);
		}
	}
	
	public void close()  {
		if (editedTemplateDir != null && editedTemplateDir.exists()) {
			try {
				FileUtils.deleteDirectory(editedTemplateDir);
				editedTemplateDir = null;
			} catch (IOException e) {
				throw new KonigException(e);
			}
		}
	}
	
	private String editTemplate(String templateName, String templateText) {
		String templatePath = null;
		PrintWriter out = null;
		try {
			for (RegexRule rule : regexRuleList) {
				String variable = "${" + rule.getPropertyName() + "}";
				
				if (templateText.contains(variable)) {
					context.put(rule.getPropertyName(),  rule.getPattern());
					if (out == null) {
						
						if (editedTemplateDir == null) {
							editedTemplateDir = new File(templateDir, EDITED);
							editedTemplateDir.mkdirs();
						}
						templatePath = EDITED + "/" + templateName + ".ttl";
						File outFile = new File(templateDir, templatePath);
						
						if (outFile.exists()) {
							return templatePath;
						}
						
						try {
							out = new PrintWriter(new FileWriter(outFile));
						} catch (IOException e) {
							throw new KonigException(e);
						}
					}

					String pattern = rule.getPattern();
					String replacement = rule.getReplacement();
					out.print("#set ( $");
					out.print(rule.getPropertyName());
					out.print(" = \"");
					out.print(rule.getSourceExpression());
					out.println("\" )");
					out.print("#set ( $");
					out.print(rule.getPropertyName());
					out.print(" = $");
					out.print(rule.getPropertyName());
					out.print(".replaceAll(\"");
					out.print(pattern);
					out.print("\", \"");
					out.print(replacement);
					out.println("\") )");
				}
			}
			if (out != null) {
				out.print(templateText);
			}
		} finally {
			if (out != null) {
				out.close();
			}
		}
			
		return templatePath;
	}


	private String loadTemplate(String templateName) throws IOException {
		File templateFile = new File(templateDir, templateName + ".ttl");
		if (templateFile.exists()) {
			byte[] array = Files.readAllBytes(templateFile.toPath());
			return new String(array);
		}
		
		String resource = "WorkbookLoader/" + templateName + ".ttl";
		InputStream input = getClass().getClassLoader().getResourceAsStream(resource);
		if (input != null) {
			byte[] array = IOUtils.toByteArray(input);
			return new String(array);
		}
		
		return null;
	}

	public void put(Properties properties) {
		Set<Entry<Object, Object>> entries = properties.entrySet();
		for (Entry<Object, Object> e : entries) {
			String key = e.getKey().toString();
			String value = e.getValue().toString();
			context.put(key, value);
		}
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
		
		for (Object entry : params.entrySet()) {
			Map.Entry pair = (Map.Entry)entry;
			context.remove(pair.getKey());
		}
		
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
	
	public VelocityContext getContext(){
		return context;
	}
}
