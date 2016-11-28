package io.konig.showl;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.RDF;

import io.konig.core.Graph;
import io.konig.core.NamespaceManager;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.MemoryNamespaceManager;
import io.konig.core.vocab.SH;
import io.konig.core.vocab.Schema;
import io.konig.shacl.io.ShapeFileGetter;

public class ShapeWriterTest {

	@Test
	public void test() throws Exception {
		
		NamespaceManager nsManager = new MemoryNamespaceManager();
		nsManager.add("schema", "http://schema.org/");
		nsManager.add("schema1", "http://example.com/shapes/v1/schema/");
		nsManager.add("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
		nsManager.add("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
		nsManager.add("owl", "http://www.w3.org/2002/07/owl#");
		nsManager.add("sh", "http://www.w3.org/ns/shacl#");
		
		URI personShape = uri("http://example.com/shapes/v1/schema/Person");
		
		Graph graph = new MemoryGraph();
		graph.setNamespaceManager(nsManager);
		graph.builder()
			.beginSubject(personShape)
				.addProperty(RDF.TYPE, SH.Shape)
				.addProperty(SH.targetClass, Schema.Person)
			.endSubject()
			;
		
		File baseDir = new File("target/test/ShapeWriter").getAbsoluteFile();
		baseDir.mkdirs();
		
		ShapeFileGetter fileGetter = new ShapeFileGetter(baseDir, nsManager);
		ShapeWriter writer = new ShapeWriter(fileGetter);
		
		writer.writeShapes(graph);
		
		String expected = "@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\n" + 
				"@prefix schema: <http://schema.org/> .\n" + 
				"@prefix schema1: <http://example.com/shapes/v1/schema/> .\n" + 
				"@prefix sh: <http://www.w3.org/ns/shacl#> .\n" + 
				"\n" + 
				"schema1:Person a sh:Shape ; \n" + 
				"	sh:targetClass schema:Person .";
		
		String actual = readFile("target/test/ShapeWriter/schema1_Person.ttl").replace("\r", "").trim();
		
		assertEquals(expected, actual);
	}

	
	private String readFile(String path) throws IOException {
		byte[] data = Files.readAllBytes(Paths.get(path));
		return new String(data, "UTF-8");
	}

	private URI uri(String value) {
		return new URIImpl(value);
	}

}
