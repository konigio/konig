package io.konig.schemagen.rdb;

import static org.junit.Assert.*;

import org.junit.Ignore;
import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.RDF;

import io.konig.core.Graph;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.vocab.KOL;
import io.konig.core.vocab.SH;
import io.konig.core.vocab.Schema;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeManager;
import io.konig.shacl.impl.MemoryShapeManager;
import io.konig.shacl.io.ShapeLoader;

public class RdbShapeGeneratorTest {

	@Ignore
	public void test() {
		
		Graph graph = new MemoryGraph();
		URI shapeId = uri("http://example.com/shape/v1/schema/Person");
		URI rdbShapeId = uri("http://example.com/shape/v2/schema/Person");
		
		graph.builder()
			.beginSubject(shapeId)
				.addProperty(RDF.TYPE, SH.Shape)
				.addProperty(SH.scopeClass, Schema.Person)
				.addProperty(KOL.equivalentRelationalShape, rdbShapeId)
				.beginBNode(SH.property)
					.addProperty(SH.predicate, Schema.memberOf)
					.addProperty(SH.valueClass, Schema.Organization)
					.addProperty(SH.nodeKind, SH.IRI)
				.endSubject()
			.endSubject();
		
		ShapeManager shapeManager = new MemoryShapeManager();
		
		ShapeLoader loader = new ShapeLoader(null, shapeManager);
		loader.load(graph);
		
		Shape sourceShape = shapeManager.getShapeById(shapeId);
		RdbShapeGenerator generator = new RdbShapeGenerator();
		
		Shape rdbShape = generator.toRelationalShape(sourceShape, graph, null);
		
		
		assertEquals(rdbShapeId, rdbShape.getId());
		
	}

	private URI uri(String value) {
		return new URIImpl(value);
	}

}
