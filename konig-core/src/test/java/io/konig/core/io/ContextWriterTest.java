package io.konig.core.io;

/*
 * #%L
 * konig-core
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


import java.io.StringWriter;

import org.junit.Test;
import org.openrdf.model.vocabulary.XMLSchema;

import io.konig.core.Context;
import io.konig.core.ContextBuilder;
import io.konig.core.vocab.Schema;

public class ContextWriterTest {

	@Test
	public void test() throws Exception {
		
		Context context = new ContextBuilder("http://example.com/context")
			.namespace("schema", "http://schema.org/")
			.namespace("xsd", XMLSchema.NAMESPACE)
			.type(Schema.Person)
			.type(Schema.PostalAddress)
			.objectProperty(Schema.address)
			.property(Schema.streetAddress, XMLSchema.STRING)
			.property(Schema.addressLocality, XMLSchema.STRING)
			.property(Schema.addressRegion, XMLSchema.STRING)
			.property(Schema.givenName, XMLSchema.STRING)
			.property(Schema.familyName, XMLSchema.STRING)
			.objectProperty(Schema.children)
			.objectProperty(Schema.parent)
			.property(Schema.email, XMLSchema.STRING)
			.objectProperty(Schema.knows)
			.getContext();
		
		
		
		StringWriter out = new StringWriter();
		ContextWriter writer = new ContextWriter();
		writer.write(context, out);
		
		// TODO: parse the output as JSON and validate it.
	}

}
