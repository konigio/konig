package io.konig.core.delta;

/*
 * #%L
 * konig-core
 * %%
 * Copyright (C) 2015 - 2016 Gregory McFall
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
