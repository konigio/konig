package io.konig.transform;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.XMLSchema;

import io.konig.core.NamespaceManager;
import io.konig.core.Path;
import io.konig.core.impl.MemoryNamespaceManager;
import io.konig.core.path.PathFactory;
import io.konig.core.path.PathImpl;
import io.konig.core.vocab.Schema;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeBuilder;
import io.konig.shacl.ShapeManager;

public class ShapeTransformModelBuilderTest {
	
	@Test
	public void testFilterEmbeddedResource() throws Exception {
		URI sourceShapeId = uri("http://example.com/shape/v1/PersonShape");
		URI targetShapeId = uri("http://example.com/shape/v2/PersonShape");
		
		URI aliasStreet = uri("http://example.com/alias/address_street");
		URI aliasLocality = uri("http://example.com/alias/address_locality");
		URI aliasRegion = uri("http://example.com/alias/address_region");
		URI aliasCountry = uri("http://example.com/alias/address_country");
		
		URI addressShape = uri("http://example.com/shape/v1/AddressShape");
		
		NamespaceManager nsManager = new MemoryNamespaceManager();
		nsManager.add("schema", Schema.NAMESPACE);
		
		
		ShapeBuilder shapeBuilder = new ShapeBuilder();
		shapeBuilder.beginShape(sourceShapeId) 
			.targetClass(Schema.Person)
			
			.beginProperty(aliasStreet)
				.datatype(XMLSchema.STRING)
				.equivalentPath("/schema:address/schema:streetAddress")
			.endProperty()

			.beginProperty(aliasLocality)
				.datatype(XMLSchema.STRING)
				.equivalentPath("/schema:address/schema:addressLocality")
			.endProperty()

			.beginProperty(aliasRegion)
				.datatype(XMLSchema.STRING)
				.equivalentPath("/schema:address/schema:addressRegion")
			.endProperty()

			.beginProperty(aliasCountry)
				.datatype(XMLSchema.STRING)
				.equivalentPath("/schema:address/schema:addressCountry")
			.endProperty()
			
		.endShape()
		.beginShape(targetShapeId)
			.targetClass(Schema.Person)
		
			.beginProperty(Schema.givenName)
				.datatype(XMLSchema.STRING)
			.endProperty()
	
			.beginProperty(Schema.familyName)
				.datatype(XMLSchema.STRING)
			.endProperty()
			
			.beginProperty(Schema.address)
				.valueShape(addressShape)
			.endProperty()
		
		.endShape()
		.beginShape(addressShape)
			.targetClass(Schema.PostalAddress)
			
			.beginProperty(Schema.addressLocality)
				.datatype(XMLSchema.STRING)
			.endProperty()

			.beginProperty(Schema.addressCountry)
				.datatype(XMLSchema.STRING)
			.endProperty()
		
		.endShape()
		;
		
		ShapeManager shapeManager = shapeBuilder.getShapeManager();
		
		PathFactory pathFactory = new PathFactory(nsManager);
		
		ShapeTransformModelBuilder builder = new ShapeTransformModelBuilder(shapeManager, pathFactory);
		
		Shape targetShape = shapeManager.getShapeById(targetShapeId);
		
		ShapeTransformModel model = builder.build(targetShape);
		
		List<TransformElement> elements = new ArrayList<>(model.getElements());
		
		assertEquals(1, elements.size());
		
		TransformElement e = elements.get(0);
		
		List<PropertyTransform> eList = e.getPropertyTransformList();
		
		assertEquals(2, eList.size());
		
		Path path = new PathImpl().out(Schema.address).out(Schema.addressLocality);
		
		PropertyTransform p = e.getPropertyTansformByTargetPath(path);
		
		assertTrue(p != null);
		assertEquals(new PathImpl().out(aliasLocality), p.getSourcePath());
		
		path = new PathImpl().out(Schema.address).out(Schema.addressCountry);
		
		p = e.getPropertyTansformByTargetPath(path);
		assertTrue(p != null);
		assertEquals(new PathImpl().out(aliasCountry), p.getSourcePath());
		
	}

	@Test
	public void testFilter() throws Exception {
		
		URI sourceShapeId = uri("http://example.com/shape/v1/PersonShape");
		URI targetShapeId = uri("http://example.com/shape/v2/PersonShape");
		
		
		
		ShapeBuilder shapeBuilder = new ShapeBuilder();
		shapeBuilder.beginShape(sourceShapeId) 
			.targetClass(Schema.Person)
			
			.beginProperty(Schema.givenName)
				.datatype(XMLSchema.STRING)
			.endProperty()

			.beginProperty(Schema.familyName)
				.datatype(XMLSchema.STRING)
			.endProperty()

			.beginProperty(Schema.email)
				.datatype(XMLSchema.STRING)
			.endProperty()

			.beginProperty(Schema.telephone)
				.datatype(XMLSchema.STRING)
			.endProperty()
			
		.endShape()
		.beginShape(targetShapeId)
			.targetClass(Schema.Person)
		
			.beginProperty(Schema.givenName)
				.datatype(XMLSchema.STRING)
			.endProperty()
	
			.beginProperty(Schema.familyName)
				.datatype(XMLSchema.STRING)
			.endProperty()
		
		.endShape()
		;
		
		ShapeManager shapeManager = shapeBuilder.getShapeManager();
		
		ShapeTransformModelBuilder builder = new ShapeTransformModelBuilder(shapeManager, null);
		
		Shape targetShape = shapeManager.getShapeById(targetShapeId);
		
		ShapeTransformModel model = builder.build(targetShape);
		
		List<TransformElement> elements = new ArrayList<>(model.getElements());
		
		assertEquals(1, elements.size());
		
		TransformElement e = elements.get(0);
		Shape sourceShape = shapeManager.getShapeById(sourceShapeId);
		
		assertEquals(sourceShape, e.getSourceShape());
		
		List<PropertyTransform> pList = e.getPropertyTransformList();
		assertEquals(2, pList.size());
		
		// Verify that the TransformElement includes the following paths:
		//    "/schema:givenName"
		//    "/schema:familyName"
		
		Path path = new PathImpl().out(Schema.givenName);
		
		PropertyTransform p = e.getPropertyTansformByTargetPath(path);
		assertTrue(p != null);
		
		assertEquals(path, p.getSourcePath());
		
		path = new PathImpl().out(Schema.familyName);
		p = e.getPropertyTansformByTargetPath(path);
		assertTrue(p != null);
		assertEquals(path, p.getSourcePath());
		
		
		
		
		
		
	}
	
	private URI uri(String value) {
		return new URIImpl(value);
	}

}
