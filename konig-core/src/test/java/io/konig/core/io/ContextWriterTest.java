package io.konig.core.io;

import static org.junit.Assert.fail;

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
