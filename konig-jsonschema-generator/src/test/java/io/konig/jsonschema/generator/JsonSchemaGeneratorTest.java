package io.konig.jsonschema.generator;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import io.konig.core.Graph;
import io.konig.core.NamespaceManager;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.MemoryNamespaceManager;
import io.konig.core.impl.RdfUtil;
import io.konig.core.io.impl.JsonUtil;
import io.konig.jsonschema.model.JsonSchema;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeManager;
import io.konig.shacl.impl.MemoryShapeManager;

public class JsonSchemaGeneratorTest {
	private NamespaceManager nsManager = new MemoryNamespaceManager();
	private Graph graph = new MemoryGraph(nsManager);
	private ShapeManager shapeManager = new MemoryShapeManager();
	private SimpleJsonSchemaNamer namer = new SimpleJsonSchemaNamer(".jsonschema");
	private SimpleJsonSchemaTypeMapper typeMapper = new SimpleJsonSchemaTypeMapper();
	private JsonSchemaGenerator generator = new JsonSchemaGenerator(nsManager, namer, typeMapper);

	@Test
	public void test() throws Exception {
		load("src/test/resources/json-io");
		URI shapeId = uri("http://example.com/shapes/PersonShape");
		Shape shape = shapeManager.getShapeById(shapeId);
		
		JsonSchema schema = generator.asJsonSchema(shape);
		
		String expected =
			"{\n" + 
			"  \"id\" : \"http://example.com/shapes/PersonShape.jsonschema\",\n" + 
			"  \"type\" : \"object\",\n" + 
			"  \"properties\" : {\n" + 
			"    \"address\" : {\n" + 
			"      \"id\" : \"http://example.com/shapes/PostalAddressShape.jsonschema\",\n" + 
			"      \"type\" : \"object\",\n" + 
			"      \"description\" : \"The person's postal address.\",\n" + 
			"      \"properties\" : {\n" + 
			"        \"streetAddress\" : {\n" + 
			"          \"type\" : \"string\",\n" + 
			"          \"description\" : \"The street address. For example, 1600 Ampitheatre Pkwy.\"\n" + 
			"        },\n" + 
			"        \"addressLocality\" : {\n" + 
			"          \"type\" : \"string\",\n" + 
			"          \"description\" : \"The locality. For example, Mountain View.\"\n" + 
			"        },\n" + 
			"        \"postalCode\" : {\n" + 
			"          \"type\" : \"string\",\n" + 
			"          \"description\" : \"The postal code. For example, 94043.\"\n" + 
			"        },\n" + 
			"        \"addressRegion\" : {\n" + 
			"          \"type\" : \"string\",\n" + 
			"          \"description\" : \"The region. For example, CA.\"\n" + 
			"        }\n" + 
			"      }\n" + 
			"    },\n" + 
			"    \"email\" : {\n" + 
			"      \"type\" : \"array\",\n" + 
			"      \"description\" : \"The person's email address.\",\n" + 
			"      \"items\" : {\n" + 
			"        \"type\" : \"string\"\n" + 
			"      }\n" + 
			"    },\n" + 
			"    \"name\" : {\n" + 
			"      \"type\" : \"string\",\n" + 
			"      \"description\" : \"The full name of the person.\"\n" + 
			"    },\n" + 
			"    \"familyName\" : {\n" + 
			"      \"type\" : \"string\",\n" + 
			"      \"description\" : \"Family name. In the U.S., the last name of an Person.\"\n" + 
			"    },\n" + 
			"    \"gender\" : {\n" + 
			"      \"type\" : \"string\",\n" + 
			"      \"format\" : \"uri\",\n" + 
			"      \"description\" : \"Gender of the person.\"\n" + 
			"    },\n" + 
			"    \"givenName\" : {\n" + 
			"      \"type\" : \"string\",\n" + 
			"      \"description\" : \"Given name. In the U.S., the first name of a Person.\"\n" + 
			"    }\n" + 
			"  }\n" + 
			"}";
		
		ObjectMapper mapper = new ObjectMapper();
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
		mapper.setSerializationInclusion(Include.NON_NULL);
		String actual = mapper.writeValueAsString(schema).replace("\r", "");
		
		assertEquals(expected, actual);
	}

	private URI uri(String stringValue) {
		return new URIImpl(stringValue);
	}

	private void load(String path) throws RDFParseException, RDFHandlerException, IOException {
		
		File file = new File(path);
		RdfUtil.loadTurtle(file, graph, shapeManager);
	}

}
