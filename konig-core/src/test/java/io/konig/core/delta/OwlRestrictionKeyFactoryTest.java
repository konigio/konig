package io.konig.core.delta;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;

import io.konig.core.Vertex;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.vocab.Schema;

public class OwlRestrictionKeyFactoryTest {

	@Test
	public void test() {
		
		
		MemoryGraph source = new MemoryGraph();
		source.builder()
			.beginSubject(Schema.Person)
				.beginBNode(RDFS.SUBCLASSOF)
					.addProperty(RDF.TYPE, OWL.RESTRICTION)
					.addProperty(OWL.ONPROPERTY, Schema.address)
					.addProperty(OWL.MAXCARDINALITY, 1)
				.endSubject()
			.endSubject();
		

		
		MemoryGraph target = new MemoryGraph();
		target.builder()
			.beginSubject(Schema.Person)
				.beginBNode(RDFS.SUBCLASSOF)
					.addProperty(RDF.TYPE, OWL.RESTRICTION)
					.addProperty(OWL.ONPROPERTY, Schema.address)
					.addProperty(OWL.MAXCARDINALITY, 0)
				.endSubject()
			.endSubject();
				
		
		
		Vertex sourceAddress = source.v(Schema.Person).out(RDFS.SUBCLASSOF).toVertexList().iterator().next();
		Vertex targetAddress = target.v(Schema.Person).out(RDFS.SUBCLASSOF).toVertexList().iterator().next();
		
		OwlRestrictionKeyFactory factory = new OwlRestrictionKeyFactory();
		BNodeKey sourceKey = factory.createKey(RDFS.SUBCLASSOF, sourceAddress);
		BNodeKey targetKey = factory.createKey(RDFS.SUBCLASSOF, targetAddress);
		
		assertEquals(sourceKey.getHash(), targetKey.getHash());
				
		
	}
	
}
