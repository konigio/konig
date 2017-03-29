package io.konig.datacatalog;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Properties;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;

import io.konig.core.NamespaceManager;
import io.konig.core.OwlReasoner;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.MemoryNamespaceManager;
import io.konig.core.impl.RdfUtil;
import io.konig.core.vocab.Schema;
import io.konig.gcp.datasource.GcpShapeConfig;
import io.konig.shacl.ClassManager;
import io.konig.shacl.LogicalShapeBuilder;
import io.konig.shacl.LogicalShapeNamer;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeManager;
import io.konig.shacl.impl.BasicLogicalShapeNamer;
import io.konig.shacl.impl.MemoryClassManager;
import io.konig.shacl.impl.MemoryShapeManager;
import io.konig.shacl.io.ShapeLoader;

public class ClassPageTest {
	private NamespaceManager nsManager= new MemoryNamespaceManager();
	private MemoryGraph graph = new MemoryGraph(nsManager);
	private ShapeManager shapeManager = new MemoryShapeManager();
	private VelocityEngine engine;
	private VelocityContext context;
	private ClassRequest request;
	private StringWriter buffer = new StringWriter();
	private PageResponse response = new PageResponseImpl(buffer);
	private ClassPage page = new ClassPage();
	private ClassManager classManager = new MemoryClassManager();

	
	
	@Before
	public void setUp() {
		GcpShapeConfig.init();

		Properties properties = new Properties();
		properties.put("resource.loader", "class");
		properties.put("class.resource.loader.class", ClasspathResourceLoader.class.getName());
		
		engine = new VelocityEngine(properties);
		context = new VelocityContext();

		request = new ClassRequest(new PageRequest(engine, context, graph, shapeManager), classManager);
		
	}

	@Test
	public void testClass() throws Exception {
		
		load("ClassPageTest/testClass.ttl");
		
		request.setOwlClass(graph.getVertex(Schema.Person));
		page.render(request, response);
		
		String actual = buffer.toString();
		actual = actual.replace("\r", "");
		
		String expected = 
			"<html>\r\n" + 
			"<head>\r\n" + 
			"<title>Person</title>\r\n" + 
			"<link rel=\"stylesheet\" type=\"text/css\" href=\"http://schema.org/docs/schemaorg.css\">\r\n" + 
			"</head>\r\n" + 
			"<body>\r\n" + 
			"<div id=\"mainContent\" prefix=\"sh: http://www.w3.org/ns/shacl#\"  typeof=\"sh:Shape\" resource=\"http://schema.org/Person\">\r\n" + 
			"	<h1 property=\"rdfs:label\" class=\"page-title\">Person</h1>\r\n" + 
			"	<table class=\"definition-table\">\r\n" + 
			"		<thead>\r\n" + 
			"			<tr>\r\n" + 
			"				<th>Property</th>\r\n" + 
			"				<th>Type</th>\r\n" + 
			"				<th>Description</th>\r\n" + 
			"			</tr>\r\n" + 
			"		</thead>\r\n" + 
			"		<tbody class=\"supertype\">\r\n" + 
			"			<tr typeof=\"sh:PropertyConstraint\" resource=\"http://schema.org/givenName\">\r\n" + 
			"				<th class=\"prop-nam\">givenName</th>\r\n" + 
			"				<td class=\"prop-ect\">\r\n" + 
			"					string\r\n" + 
			"				</td>	\r\n" + 
			"				<td class=\"prop-desc\" property=\"rdfs:comment\">Given name. In the U.S., the first name of a Person. This can be used along with familyName instead of the name property.</td>\r\n" + 
			"			</tr>\r\n" + 
			"			<tr typeof=\"sh:PropertyConstraint\" resource=\"http://schema.org/familyName\">\r\n" + 
			"				<th class=\"prop-nam\">familyName</th>\r\n" + 
			"				<td class=\"prop-ect\">\r\n" + 
			"					string\r\n" + 
			"				</td>	\r\n" + 
			"				<td class=\"prop-desc\" property=\"rdfs:comment\"></td>\r\n" + 
			"			</tr>\r\n" + 
			"		</tbody>\r\n" + 
			"	</table>\r\n" + 
			"	<table class=\"definition-table\">\r\n" + 
			"		<thead>\r\n" + 
			"			<tr>\r\n" + 
			"				<th>Shapes based on this Class</th>\r\n" + 
			"			</tr>\r\n" + 
			"		</thead>\r\n" + 
			"		<tbody>\r\n" + 
			"			<tr>\r\n" + 
			"				<td>\r\n" + 
			"					<code>\r\n" + 
			"						<a href=\"../shape/PersonShape.html\">PersonShape</a>\r\n" + 
			"					</code>\r\n" + 
			"					<br>\r\n" + 
			"				</td>\r\n" + 
			"			</tr>\r\n" + 
			"		</tbody>\r\n" + 
			"	</table>\r\n" + 
			"</div>\r\n" + 
			"</body>\r\n" + 
			"</html>";
		
		expected = expected.replace("\r", "");
		assertEquals(expected, actual);
		
	}
	

	private void load(String resource) throws RDFParseException, RDFHandlerException, IOException {
		
		InputStream input = getClass().getClassLoader().getResourceAsStream(resource);
		RdfUtil.loadTurtle(graph, input, "");
		ShapeLoader shapeLoader = new ShapeLoader(shapeManager);
		shapeLoader.load(graph);
		OwlReasoner reasoner = new OwlReasoner(graph);
		LogicalShapeNamer shapeNamer = new BasicLogicalShapeNamer("http://example.com/shapes/", nsManager);
		LogicalShapeBuilder builder = new LogicalShapeBuilder(reasoner, shapeNamer);
		builder.buildLogicalShapes(shapeManager, classManager);
	}

}
