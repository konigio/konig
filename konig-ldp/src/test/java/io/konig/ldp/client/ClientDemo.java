package io.konig.ldp.client;

/*
 * #%L
 * Konig Linked Data Platform
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


import io.konig.ldp.ResourceFile;

public class ClientDemo {

	public static void main(String[] args) throws Exception {
		String resourceId = "http://localhost:8888/resources/sample.ttl";
		String contentType = "application/ld+json";

		LdpClient client = new LdpClient();
		
		ResourceFile file = client.getResourceBuilder()
			.contentLocation(resourceId)
			.contentType(contentType)
			.body(
				  "{ \"@context\" : {"
				+ "    \"schema\" : \"http://schema.org/\""
				+ "  },"
				+ "  \"@id\" : \"http://example.com/alice\", "
				+ "  \"@type\" : \"schema:Human\" "
				+ "}"
			)
			.rdfSource();
		
		client.put(file);
		
		ResourceFile loaded = client.get(resourceId);
		
		System.out.println(loaded.getEntityBodyText());
	}

}
