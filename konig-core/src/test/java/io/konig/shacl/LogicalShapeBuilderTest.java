package io.konig.shacl;

/*
 * #%L
 * konig-shacl
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

import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.XMLSchema;

import io.konig.core.NamespaceManager;
import io.konig.core.OwlReasoner;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.MemoryNamespaceManager;
import io.konig.core.vocab.Schema;
import io.konig.shacl.impl.BasicLogicalShapeNamer;
import io.konig.shacl.impl.MemoryClassManager;

public class LogicalShapeBuilderTest {

	@Test
	public void test() {
		
		ShapeBuilder shapeBuilder = new ShapeBuilder();
		
		URI personShapeId = uri("http://example.com/shapes/v1/schema/Person");
		
		shapeBuilder
			.beginShape("http://example.com/shapes/v1/schema/Organization")
				.targetClass(Schema.Organization)
			.endShape()
			.beginShape(personShapeId)
				.targetClass(Schema.Person)
				.beginProperty(Schema.givenName)
					.datatype(XMLSchema.STRING)
					.maxCount(1)
					.minCount(0)
				.endProperty()
				.beginProperty(Schema.memberOf)
					.valueClass(Schema.Organization)
				.endProperty()
			.endShape()
			.beginShape("http://example.com/shapes/v2/schema/Person")
				.targetClass(Schema.Person)
				.beginProperty(Schema.givenName)
					.maxCount(2)
					.minCount(1)
				.endProperty()
				.beginProperty(Schema.memberOf)
					.valueShape(uri("http://example.com/shapes/v1/schema/Organization"))
				.endProperty()
			.endShape()
		;
		
		ShapeManager shapeManager = shapeBuilder.getShapeManager();
		NamespaceManager nsManager = new MemoryNamespaceManager();
		nsManager.add("schema", "http://schema.org/");
		MemoryClassManager classManager = new MemoryClassManager();
		LogicalShapeNamer namer = new BasicLogicalShapeNamer("http://example.com/shapes/logical/", nsManager);
		OwlReasoner reasoner = new OwlReasoner(new MemoryGraph());
		LogicalShapeBuilder builder = new LogicalShapeBuilder(reasoner, namer);
		builder.buildLogicalShapes(shapeManager, classManager);
		
		Shape personShape = classManager.getLogicalShape(Schema.Person);
		
		assertTrue(personShape != null);
		assertEquals(Schema.Person, personShape.getTargetClass());
		
		PropertyConstraint p = personShape.getPropertyConstraint(Schema.givenName);
		assertEquals(p.getMinCount(), new Integer(0));
		assertEquals(p.getMaxCount(), new Integer(2));
		assertEquals(p.getDatatype(), XMLSchema.STRING);
		
		p = personShape.getPropertyConstraint(Schema.memberOf);
		assertTrue(p != null);
		assertEquals(p.getValueClass(), Schema.Organization);
		
		
	}
	

	private URI uri(String value) {
		return new URIImpl(value);
	}
}
