package io.konig.openapi.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.List;

import org.junit.Test;

import io.konig.yaml.Yaml;

public class OpenAPITest {

	@Test
	public void test() throws Exception {
		
		File file = new File("src/test/resources/openapi-test/kitchen-sink.yaml");
		OpenAPI api = Yaml.read(OpenAPI.class, file);
		
		assertEquals("3.0.0", api.getOpenapi());
		Info info = api.getInfo();
		assertTrue(info!=null);
		assertEquals("Kitchen Sink API", info.getTitle());
		assertEquals("1.0.0", info.getVersion());
		
		List<Server> serverList = api.getServerList();
		assertEquals(1, serverList.size());
		
		Server server = serverList.get(0);
		assertEquals("http://example.com:{port}/{basePath}", server.getUrl());
		assertEquals("The production server", server.getDescription());
		
		ServerVariable portVar = server.getVariable("port");
		assertEquals("port", portVar.getName());
		assertEquals("8443", portVar.getDefault());
		List<String> enumList = portVar.getEnum();
		assertEquals(2, enumList.size());
		assertEquals("8443", enumList.get(0));
		assertEquals("443", enumList.get(1));
		
		ServerVariable basePath = server.getVariable("basePath");
		assertEquals("v2", basePath.getDefault());
		
		Path path = api.getPath("/person");
		assertTrue(path != null);
		assertEquals("The container holding all Person instances", path.getSummary());
		
		
	}

}
