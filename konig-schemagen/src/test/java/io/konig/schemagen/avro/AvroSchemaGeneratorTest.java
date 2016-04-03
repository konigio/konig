package io.konig.schemagen.avro;

import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.XMLSchema;

import io.konig.core.GraphBuilder;
import io.konig.core.NamespaceManager;
import io.konig.core.Vertex;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.MemoryNamespaceManager;
import io.konig.core.io.ResourceFile;
import io.konig.core.vocab.SH;
import io.konig.core.vocab.Schema;
import io.konig.schemagen.avro.impl.SimpleAvroNamer;

public class AvroSchemaGeneratorTest {
	

	@Test
	public void test() throws Exception {
		
		NamespaceManager nsManager = new MemoryNamespaceManager();
		nsManager.add("schema", "http://schema.org/");
		nsManager.add("institution", "http://www.konig.io/institution/");
		
		AvroNamer namer = new SimpleAvroNamer();
		
		URI addressShapeId = uri("http://www.konig.io/shapes/v1/schema/Address");
		URI shapeId = uri("http://www.konig.io/shapes/v1/schema/Person");
		
		MemoryGraph graph = new MemoryGraph();
		GraphBuilder builder = new GraphBuilder(graph);
		builder.beginSubject(uri("http://www.konig.io/institution/Stanford"))
			.addProperty(RDF.TYPE, Schema.Organization)
		.endSubject()
		.beginSubject(uri("http://www.konig.io/institution/Princeton"))
			.addProperty(RDF.TYPE, Schema.Organization)
		.endSubject()
		.beginSubject(shapeId)
			.addProperty(RDF.TYPE, SH.Shape)
			.beginBNode(SH.property)
				.addProperty(SH.predicate, RDF.TYPE)
				.addProperty(SH.hasValue, uri("http://schema.org/Person"))
				.addProperty(SH.maxCount, 1)
				.addProperty(SH.minCount, 1)
			.endSubject()
			.beginBNode(SH.property)
				.addProperty(SH.predicate, Schema.givenName)
				.addProperty(SH.datatype, XMLSchema.STRING)
				.addProperty(SH.minCount, 1)
				.addProperty(SH.maxCount, 1)
			.endSubject()
			.beginBNode(SH.property)
				.addProperty(SH.predicate, Schema.address)
				.addProperty(SH.valueShape, addressShapeId)
				.addProperty(SH.minCount, 1)
				.addProperty(SH.maxCount, 1)
			.endSubject()
			.beginBNode(SH.property)
				.addProperty(SH.predicate, Schema.memberOf)
				.addProperty(SH.valueClass, Schema.Organization)
				.addProperty(SH.nodeKind, SH.IRI)
				.addProperty(SH.minCount, 0)
				.addProperty(SH.maxCount, 1)
			.endSubject()
		.endSubject();
		
		Vertex shapeVertex = graph.vertex(shapeId);
		
		AvroSchemaGenerator avro = new AvroSchemaGenerator(namer, nsManager);
		
		ResourceFile file = avro.generateSchema(shapeVertex);
		
		System.out.println("Content-Location: " + file.getContentLocation());
		System.out.println();
		
		System.out.println(file.asText());
		
	}
	
	private URI uri(String value) {
		return new URIImpl(value);
	}

}
