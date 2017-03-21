package io.konig.spreadsheet;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.openrdf.model.Namespace;
import org.openrdf.model.URI;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;

import io.konig.core.Graph;
import io.konig.core.KonigException;
import io.konig.core.NamespaceManager;
import io.konig.core.impl.RdfUtil;
import io.konig.core.util.LinkedValueMap;
import io.konig.core.util.RecursiveValueFormat;
import io.konig.core.util.SimpleValueMap;
import io.konig.core.util.ValueFormat;
import io.konig.core.util.ValueMap;
import io.konig.shacl.Shape;

/**
 * A utility that generates DataSource definitions associated with Shapes based on a template.
 * @author Greg McFall
 *
 */
public class DataSourceGenerator {
	
	private NamespaceManager nsManager;
	private Map<String,ValueFormat> variableMap = new HashMap<String, ValueFormat>();
	private Map<String, RecursiveValueFormat> templateMap = new HashMap<>();
	private File templateDir;
	
	public DataSourceGenerator(NamespaceManager nsManager, File templateDir, Properties properties) {
		this.nsManager = nsManager;
		this.templateDir = templateDir;
		put(properties);
		
		this.templateDir = templateDir;
	}
	
	public void put(Properties properties) {

		RecursiveValueFormat tmp = new RecursiveValueFormat(variableMap);
		tmp.put(properties);
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

		RecursiveValueFormat template = null;

		try {
			template = getTemplate(templateName);
		} catch (IOException e) {
			throw new KonigException(e);
		}
		
		if (template == null) {
			throw new KonigException("Template not found: " + templateName);
		}
		
		SimpleValueMap defaultMap = defaultMap(shape);
	
		LinkedValueMap map = new LinkedValueMap(params, defaultMap);
		

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
		
		
		RecursiveValueFormat template = null;
		
		try {
			template = getTemplate(templateName);
		} catch (IOException e) {
			throw new KonigException(e);
		}
		
		if (template == null) {
			throw new KonigException("Template not found: " + templateName);
		}
		
		SimpleValueMap map = defaultMap(shape);
		

		String result = template.format(map);
		ByteArrayInputStream input = new ByteArrayInputStream(result.getBytes());
		
		try {
			RdfUtil.loadTurtle(graph, input, "");
		} catch (RDFParseException | RDFHandlerException | IOException e) {
			throw new KonigException("Failed to render template", e);
		}
		
		
	}

	private RecursiveValueFormat getTemplate(String templateName) throws IOException {
		RecursiveValueFormat template = templateMap.get(templateName);
		if (template == null) {
			template = new RecursiveValueFormat(variableMap);
			
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
				template.compile(text);
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
