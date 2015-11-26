package io.konig.shacl.context;

import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.XMLSchema;

import io.konig.core.Context;
import io.konig.core.NamespaceManager;
import io.konig.core.impl.MemoryNamespaceManager;
import io.konig.core.io.ContextWriter;
import io.konig.core.vocab.Schema;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeBuilder;

public class ContextGeneratorTest {

	@Test
	public void test() throws Exception {
		
		ContextNamer namer = new SuffixContextNamer("/context");
		
		NamespaceManager namespaceManager = new MemoryNamespaceManager();
		namespaceManager
			.add("ss", "http://www.konig.io/shape/schema/")
			.add("schema", "http://schema.org/")
			.add("xsd", XMLSchema.NAMESPACE);
		
		URI addressShape = new URIImpl("http://www.konig.io/shape/schema/PostalAddress-v1");
		ShapeBuilder builder = new ShapeBuilder(addressShape);
		
		builder
			.scopeClass(Schema.PostalAddress)
			.property(Schema.streetAddress).datatype(XMLSchema.STRING)
			.property(Schema.addressLocality).datatype(XMLSchema.STRING)
			.property(Schema.addressRegion).datatype(XMLSchema.STRING)
			.shape("http://www.konig.io/shape/schema/Person-v1")
			.scopeClass(Schema.Person)
			.property(Schema.email).datatype(XMLSchema.STRING)
			.property(Schema.givenName).datatype(XMLSchema.STRING).maxCount(1)
			.property(Schema.familyName).datatype(XMLSchema.STRING).maxCount(1)
			.property(Schema.address).valueShape(addressShape);
		
		
		Shape shape = builder.shape();
		
		ContextGenerator generator = new ContextGenerator(
			builder.getShapeManager(), namespaceManager, namer);
		
		Context context = generator.forShape(shape);
		
		ContextWriter writer = new ContextWriter();
		writer.write(context, System.out);
		
//		fail("Not yet implemented");
	}

}
