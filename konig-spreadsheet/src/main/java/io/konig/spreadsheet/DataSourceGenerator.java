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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
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
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.runtime.RuntimeInstance;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.parser.ParseException;
import org.openrdf.model.Namespace;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;

import info.aduna.io.IOUtil;
import io.konig.core.Graph;
import io.konig.core.KonigException;
import io.konig.core.NamespaceManager;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.RdfUtil;
import io.konig.core.vocab.SH;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeManager;
import io.konig.shacl.io.ShapeLoader;

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
	private File velocityLog;
	private DatasourceVelocityContext context;
	private List<RegexRule> regexRuleList = new ArrayList<>();
	
	private File editedTemplateDir=null;
	private RuntimeServices runtime;
	
	
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
		this.context = new DatasourceVelocityContext(properties);
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
		runtime = new RuntimeInstance();
		if (velocityLog != null) {
			runtime.addProperty("runtime.log", velocityLog.getAbsolutePath());
		}

	}
	
	
	private String merge(String templateName) {
		String templateText =  getTemplateText(templateName);
		if(!regexRuleList.isEmpty()) {
			templateText = addRegex(templateName, templateText);
		}
		return mergeTemplate(templateName, templateText);
	}
	
	private String addRegex(String templateName, String templateText) {
		StringWriter out = null;
			for (RegexRule rule : regexRuleList) {
				String variable = "${" + rule.getPropertyName() + "}";
				
				if (templateText.contains(variable)) {
					if (out == null) {
						out = new StringWriter();
					}

					String pattern = rule.getPattern();
					String replacement = rule.getReplacement();
					out.write("#set ( $");
					out.write(rule.getPropertyName());
					out.write(" = \"");
					out.write(rule.getSourceExpression());
					out.write("\" )");
					out.write("#set ( $");
					out.write(rule.getPropertyName());
					out.write(" = $");
					out.write(rule.getPropertyName());
					out.write(".replaceAll(\"");
					out.write(pattern);
					out.write("\", \"");
					out.write(replacement);
					out.write("\") )");
					out.write("\n");
				}
			}
			if (out != null) {
				out.write(templateText);
			}
			
		if (out != null) {
			try {
				out.close();
			} catch (IOException e) {
				throw new KonigException("Failed to add regex to template: " + templateName, e);
			}
			templateText = out.toString();
		}
			
		return templateText;
	}

	private static final int MAX_ITERATIONS = 20;
	
	private String mergeTemplate(String templateName, String templateText) {
		String priorText = null;
		int count = 0;
		while (!templateText.equals(priorText) && count < MAX_ITERATIONS) {
			count++;
			priorText = templateText;
			StringWriter result = new StringWriter();
			try {
				Template template = new Template();

				StringReader reader = new StringReader(templateText);
				template.setRuntimeServices(runtime);
				template.setData(runtime.parse(reader, templateName));
				template.initDocument();
				template.merge(context, result);
				templateText = result.toString();
			} catch (ParseException e) {
				throw new KonigException("Failed to parse template", e);
			}
		}
		return templateText;
	}
//	private String mergeWithDefaultProperties(String templateName, String templateText){
//		put(defaultProperties);	
//		return mergeTemplate(templateName, templateText); 
//	}
//	
//	private String mergeDefaultMap(String templateName, String templateText) {
//		return mergeTemplate(templateName, templateText); 
//	}
	
//	public void removeDefaultProperties(Properties properties) {
//		Set<Entry<Object, Object>> entries = properties.entrySet();
//		for (Entry<Object, Object> e : entries) {
//			context.remove(e.getKey());
//		}
//	}
	
	private String getTemplateText(String templateName) {
		
		String text = getCustomTemplate(templateName);
		
		if (text == null) {
			String path = "WorkbookLoader/" + templateName + ".ttl";
			InputStream input = getClass().getClassLoader().getResourceAsStream(path);
			if (input == null) {
				throw new KonigException("Template not found: " + templateName);
			}
			try (InputStreamReader reader = new InputStreamReader(input)) {
				
				text = org.apache.commons.io.IOUtils.toString(reader);
				
			} catch (IOException e) {
				throw new KonigException("Failed to load template: " + templateName, e);
			}
		}
		
		return text;
	}
	
	
	private String getCustomTemplate(String templateName) {
		String text = null;
		if (templateDir != null) {
			String fileName = templateName + ".ttl";
			File file = new File(templateDir, fileName);
			if (file.exists()) {
				try {
					text = IOUtil.readString(file);
				} catch (IOException e) {
					throw new KonigException("Failed to read template: " + templateName, e);
				}
			}
		}
		return text;
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
	
	
	

	public void put(Properties properties) {
		Set<Entry<Object, Object>> entries = properties.entrySet();
		for (Entry<Object, Object> e : entries) {
			String key = e.getKey().toString();
			String value = e.getValue().toString();
			context.put(key, value);
		}
	}
	

	@SuppressWarnings("rawtypes")
	public void generate(Shape shape, Function func, ShapeManager shapeManager) {
		context.clearParameters();
		String templateName = func.getName();
		HashMap params = (HashMap) func.getParameters();
		Iterator it = params.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pair = (Map.Entry) it.next();
			context.putParameter(pair.getKey().toString(), pair.getValue().toString());
		}

		defaultMap(shape);
		String result = merge(templateName);
		
		for (Object entry : params.entrySet()) {
			Map.Entry pair = (Map.Entry)entry;
			context.remove(pair.getKey());
		}
		
		ByteArrayInputStream input = new ByteArrayInputStream(result.getBytes());

		try {
			Graph graph = new MemoryGraph(nsManager);
			RdfUtil.loadTurtle(graph, input, "");
			graph.edge(shape.getId(), RDF.TYPE, SH.Shape);
			ShapeLoader loader = new ShapeLoader(shapeManager);
			loader.load(graph);
			
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

	public void generate(Shape shape, String templateName, ShapeManager shapeManager) {
		defaultMap(shape);
		String result = merge(templateName);
		ByteArrayInputStream input = new ByteArrayInputStream(result.getBytes());

		try {
			Graph graph = new MemoryGraph(nsManager);
			RdfUtil.loadTurtle(graph, input, "");
			graph.edge(shape.getId(), RDF.TYPE, SH.Shape);
			ShapeLoader loader = new ShapeLoader(shapeManager);
			loader.load(graph);
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
