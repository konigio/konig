package io.konig.schemagen.java;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;

import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JFieldVar;

import io.konig.core.Graph;
import io.konig.core.NamespaceManager;
import io.konig.core.OwlReasoner;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.MemoryNamespaceManager;
import io.konig.core.impl.RdfUtil;
import io.konig.core.util.IOUtil;
import io.konig.core.util.SimpleValueFormat;
import io.konig.core.vocab.Schema;
import io.konig.shacl.ClassHierarchy;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeManager;
import io.konig.shacl.ShapeNamer;
import io.konig.shacl.SimpleShapeNamer;
import io.konig.shacl.impl.MemoryShapeManager;
import io.konig.shacl.io.ShapeLoader;

public class JsonReaderBuilderTest {

	private NamespaceManager nsManager;
	private Graph graph;
	private ShapeManager shapeManager;
	private JavaNamer javaNamer;
	private JavaDatatypeMapper datatypeMapper;
	private OwlReasoner owlReasoner;
	private ClassHierarchy hierarchy;
	private JsonReaderBuilder builder;
	
	
	@Before
	public void setUp() {
		nsManager = new MemoryNamespaceManager();
		graph = new MemoryGraph(nsManager);
		shapeManager = new MemoryShapeManager();
		javaNamer = new BasicJavaNamer("com.example", nsManager);
		datatypeMapper = new BasicJavaDatatypeMapper();
		owlReasoner = new OwlReasoner(graph);
		SimpleValueFormat iriTemplate = new SimpleValueFormat("http://example.com/shapes/canonical/{targetClassNamespacePrefix}/{targetClassLocalName}");
		hierarchy = new ClassHierarchy(iriTemplate);
	}
	
	@Test
	public void testSubclasses() throws Exception {

		load("JsonReaderBuilderTest/model.ttl");
		
		JCodeModel model = new JCodeModel();
		
		JDefinedClass jclass = builder.produceJsonReader(Schema.CreativeWork, model);
		
		Map<String, JFieldVar> fieldMap = jclass.fields();
		
		JFieldVar instance = fieldMap.get("INSTANCE");
		assertTrue(instance != null);
		

		File file = new File("target/test/JsonReaderBuilderTest/subclasses");
		IOUtil.recursiveDelete(file);
		file.mkdirs();
		model.build(file);
	}

	@Ignore
	public void testSimpleShape() throws Exception {
		
		load("JsonReaderBuilderTest/model.ttl");
		URI shapeId = uri("http://example.com/shapes/PersonShape");
		Shape shape = shapeManager.getShapeById(shapeId);
		
		JCodeModel model = new JCodeModel();
		
		JDefinedClass jclass = builder.buildJsonReader(shape, model);
		
		Map<String, JFieldVar> fieldMap = jclass.fields();
		
		JFieldVar instance = fieldMap.get("INSTANCE");
		assertTrue(instance != null);
		

		File file = new File("target/test/JavaClassBuilderTest/person");
		file.mkdirs();
		model.build(file);
	}

	private URI uri(String value) {
		return new URIImpl(value);
	}

	private void load(String resource) throws RDFParseException, RDFHandlerException, IOException {
		
		InputStream input = getClass().getClassLoader().getResourceAsStream(resource);
		
		RdfUtil.loadTurtle(graph, input, "");
		ShapeLoader shapeLoader = new ShapeLoader(shapeManager);
		shapeLoader.load(graph);
		hierarchy.init(shapeManager, owlReasoner);

		builder = new JsonReaderBuilder(hierarchy, javaNamer, datatypeMapper, owlReasoner);
		
	}

}
