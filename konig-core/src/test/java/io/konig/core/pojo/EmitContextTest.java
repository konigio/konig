package io.konig.core.pojo;

/*
 * #%L
 * Konig Core
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
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;

import org.junit.Test;
import org.openrdf.model.Namespace;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;

import io.konig.core.Graph;
import io.konig.core.NamespaceManager;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.MemoryNamespaceManager;
import io.konig.core.vocab.Schema;

public class EmitContextTest {

	@Test
	public void test() {
		
		Graph graph = new MemoryGraph();
		graph.setNamespaceManager(new MemoryNamespaceManager());
		graph.getNamespaceManager().add("schema", "http://schema.org/");
		
		graph.edge(Schema.name, RDF.TYPE, OWL.DATATYPEPROPERTY);
		graph.edge(Schema.address, RDF.TYPE, OWL.OBJECTPROPERTY);
		
		EmitContext context = new EmitContext(graph);
		context.register(TestPerson.class);
		
		
		Method nameGetter = nameGetter();
		
		URI namePredicate = context.getterPredicate(nameGetter);
		assertEquals(Schema.name, namePredicate);
	}
	
	private Method nameGetter() {
		
		Method[] methodList = TestPerson.class.getDeclaredMethods();
		for (Method m : methodList) {
			if (m.getName().equals("getName")) {
				return m;
			}
		}
		return null;
	}

}
