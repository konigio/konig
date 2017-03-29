package io.konig.datacatalog;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Properties;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;

import io.konig.core.NamespaceManager;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.MemoryNamespaceManager;
import io.konig.core.impl.RdfUtil;
import io.konig.gcp.datasource.GcpShapeConfig;
import io.konig.shacl.ShapeManager;
import io.konig.shacl.impl.MemoryShapeManager;

public class ClassIndexPageTest {

	private NamespaceManager nsManager = new MemoryNamespaceManager();
	private MemoryGraph graph = new MemoryGraph(nsManager);
	private ShapeManager shapeManager = new MemoryShapeManager();
	private VelocityEngine engine;
	private VelocityContext context;
	private PageRequest request;
	private StringWriter buffer = new StringWriter();
	private PageResponse response = new PageResponseImpl(buffer);
	private ClassIndexPage page = new ClassIndexPage();

	
	
	@Before
	public void setUp() {
		GcpShapeConfig.init();

		Properties properties = new Properties();
		properties.put("resource.loader", "class");
		properties.put("class.resource.loader.class", ClasspathResourceLoader.class.getName());
		
		engine = new VelocityEngine(properties);
		context = new VelocityContext();

		request = new PageRequest(engine, context, graph, shapeManager);

	}
	
	@Test
	public void testAllClasses() throws Exception {
		load("ClassIndexPageTest/testAllClasses.ttl");
		
		page.render(request, response);

		String actual = buffer.toString();
		actual = actual.replace("\r", "");
		
		String expected = 
			"<html>\n" + 
			"<head>\n" + 
			"<title>Class Index</title>\n" + 
			"<link rel=\"stylesheet\" type=\"text/css\" href=\"http://schema.org/docs/schemaorg.css\">\n" + 
			"</head>\n" + 
			"<body>\n" + 
			"	<div><a href=\"schema/CreativeWork.html\">CreativeWork</a></div>\n" + 
			"	<div><a href=\"schema/Organization.html\">Organization</a></div>\n" + 
			"	<div><a href=\"schema/Person.html\">Person</a></div>\n" + 
			"</body>\n" + 
			"</html>";
		
		assertEquals(expected, actual);
		
	}

	private void load(String resource) throws RDFParseException, RDFHandlerException, IOException {
		
		InputStream input = getClass().getClassLoader().getResourceAsStream(resource);
		RdfUtil.loadTurtle(graph, input, "");
	}
}
