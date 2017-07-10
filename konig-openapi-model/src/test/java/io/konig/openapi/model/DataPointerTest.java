package io.konig.openapi.model;

import static org.junit.Assert.*;

import org.junit.Test;

import io.konig.jsonschema.model.JsonSchema;

public class DataPointerTest {

	@Test
	public void test() {
		OpenAPI api = new OpenAPI();
		
		Components components = new Components();
		api.setComponents(components);
		
		JsonSchema schema = new JsonSchema("FooSchema");
		components.addSchema(schema);
		
		JsonSchema actual = DataPointer.resolve(JsonSchema.class, api, "#/components/schemas/FooSchema");
		
		assertEquals(schema, actual);
	}

}
