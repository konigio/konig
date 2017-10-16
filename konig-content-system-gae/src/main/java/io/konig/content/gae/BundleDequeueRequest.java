package io.konig.content.gae;

/*
 * #%L
 * Konig Content System, Google App Engine implementation
 * %%
 * Copyright (C) 2015 - 2017 Gregory McFall
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


import java.io.IOException;
import java.io.Reader;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class BundleDequeueRequest {

	private String bucketId;
	private String objectId;

	public BundleDequeueRequest(Reader reader) throws JsonProcessingException, IOException, BadRequestException {
		ObjectMapper mapper = new ObjectMapper();
		JsonNode node = mapper.readTree(reader);
		if (node instanceof ObjectNode) {
			
			ObjectNode json = (ObjectNode) node;
			
			node = json.get("bucketId");
			if (node == null) {
				throw new BadRequestException("bucketId must be defined");
			}
			
			bucketId = node.asText();
			
			node = json.get("objectId");
			if (node == null) {
				throw new BadRequestException("objectId must be defined");
			}
			
			objectId = node.asText(); 
		}
	}
	
	public String getBucketId() {
		return bucketId;
	}
	public String getObjectId() {
		return objectId;
	}
	
	
	

}
