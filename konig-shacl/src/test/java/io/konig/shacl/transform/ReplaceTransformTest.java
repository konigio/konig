package io.konig.shacl.transform;

/*
 * #%L
 * konig-shacl
 * %%
 * Copyright (C) 2015 Gregory McFall
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


import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.openrdf.model.URI;

import io.konig.core.Graph;
import io.konig.core.KonigTest;
import io.konig.core.Traversal;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.vocab.Schema;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeBuilder;

public class ReplaceTransformTest extends KonigTest {

	@Test
	public void test() {
		
		URI shapeId = uri("http://example.com/shape/Person");
		
		URI allie = uri("http://example.com/alison");
		URI bob = uri("http://example.com/bob");
		URI carol = uri("http://example.com/carol");
		URI denis = uri("http://example.com/denis");
		URI eddie = uri("http://example.com/eddie");
		
		ShapeBuilder builder = new ShapeBuilder(shapeId);
		
		Shape shape = builder
			.property(Schema.givenName)
			.property(Schema.familyName)
			.property(Schema.parent)
			.property(Schema.email)
			.shape();
		
		Graph source = new MemoryGraph();
		Graph target = new MemoryGraph();
		
		target.v(allie)
			.addLiteral(Schema.givenName, "Allie")
			.addLiteral(Schema.familyName, "Smith")
			.addProperty(Schema.parent, bob)
			.addProperty(Schema.parent, carol)
			.addLiteral(Schema.email, "allie@example.com")
			.addProperty(Schema.knows, denis);
		
		source.v(allie)
			.addLiteral(Schema.givenName, "Alison")
			.addLiteral(Schema.familyName, "Smith")
			.addProperty(Schema.parent, carol)
			.addLiteral(Schema.email, "allie@example.com")
			.addLiteral(Schema.email, "as@gmail.com")
			.addProperty(Schema.knows, eddie);
		
		ReplaceTransform replace = new ReplaceTransform(source.vertex(allie), target.vertex(allie), shape);
		replace.execute();
		
		Traversal allieT = target.v(allie);
		
		assertTrue(allieT.hasValue(Schema.givenName, "Alison").size()==1);
		assertTrue(allieT.hasValue(Schema.familyName, "Smith").size()==1);
		assertTrue(allieT.hasValue(Schema.parent, carol).size()==1);
		assertTrue(allieT.out(Schema.parent).size()==1);
		assertTrue(allieT.hasValue(Schema.email, "allie@example.com").size()==1);
		assertTrue(allieT.hasValue(Schema.email, "as@gmail.com").size()==1);
		assertTrue(allieT.hasValue(Schema.knows, denis).size()==1);
		assertTrue(allieT.out(Schema.knows).size()==1);
		
		
		
	}

}
