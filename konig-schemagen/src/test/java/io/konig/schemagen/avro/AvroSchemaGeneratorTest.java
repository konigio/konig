package io.konig.schemagen.avro;

import org.junit.Test;
import static org.junit.Assert.*;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.XMLSchema;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.konig.core.NamespaceManager;
import io.konig.core.impl.MemoryNamespaceManager;
import io.konig.core.vocab.Schema;
import io.konig.schemagen.avro.impl.SimpleAvroDatatypeMapper;
import io.konig.schemagen.avro.impl.SimpleAvroNamer;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeBuilder;
import io.konig.shacl.ShapeManager;

public class AvroSchemaGeneratorTest {
	

	@Test
	public void testOrConstraint() throws Exception {
		
	
		URI partyShapeId = uri("http://example.com/shapes/v1/schema/PartyShape");
		URI personShapeId = uri("http://example.com/shapes/v1/schema/PersonShape");
		URI orgShapeId = uri("http://example.com/shapes/v1/schema/OrganizationShape");
		
		URI personShapeId2 = uri("http://example.com/shapes/v2/schema/PersonShape");
		
		ShapeBuilder shapeBuilder = new ShapeBuilder();
		
		shapeBuilder
		
			.beginShape(partyShapeId)
				.or(personShapeId, orgShapeId)
			.endShape()
			
			.beginShape(personShapeId)
				.beginProperty(Schema.familyName)
					.datatype(XMLSchema.STRING)
					.minCount(1)
					.maxCount(1)
				.endProperty()
			.endShape()
			
			.beginShape(orgShapeId)
				.beginProperty(Schema.name)
					.datatype(XMLSchema.STRING)
					.minCount(1)
					.maxCount(1)				
				.endProperty()
			.endShape()
			
			
			.beginShape(personShapeId2)
				.beginProperty(Schema.sponsor)
					.valueShape(partyShapeId)
					.maxCount(1)
					.minCount(0)
				.endProperty()
			.endShape()
			
			;
		
		ShapeManager shapeManager = shapeBuilder.getShapeManager();

		NamespaceManager nsManager = new MemoryNamespaceManager();
		nsManager.add("schema", "http://schema.org/");
		AvroDatatypeMapper datatypeMapper = new SimpleAvroDatatypeMapper();
		AvroNamer namer = new SimpleAvroNamer();
		
		Shape shape = shapeManager.getShapeById(personShapeId2);
		
		AvroSchemaGenerator avroGenerator = new AvroSchemaGenerator(datatypeMapper, namer, nsManager);
		
		ObjectNode json = avroGenerator.generateSchema(shape);
		
		
		JsonNode fields = json.get("fields");
		JsonNode sponsor = fields.get(0);
		
		assertEquals("sponsor", sponsor.get("name").asText());
		
		JsonNode sponsorType = sponsor.get("type");
		
		assertTrue(sponsorType.get(0).isNull());
		
		JsonNode personShape = sponsorType.get(1);
		
		fields = personShape.get("fields");
		assertEquals("familyName", fields.get(0).get("name").asText());
		assertEquals("string", fields.get(0).get("type").asText());
		
		
		JsonNode orgShape = sponsorType.get(2);
		
		fields = orgShape.get("fields");
		assertEquals("name", fields.get(0).get("name").asText());
		assertEquals("string", fields.get(0).get("type").asText());	
		
	}
	
	private URI uri(String value) {
		return new URIImpl(value);
	}

}
