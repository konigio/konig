package io.konig.core.json;

/*
 * #%L
 * Konig Core
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


import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.konig.core.KonigException;

/**
 * A fluent builder used to construct JSON objects
 * @author Greg McFall
 *
 */
public class JsonBuilder {
	
	private JsonNodeFactory factory;
	private List<JsonNode> stack = new ArrayList<>();
	

	public JsonBuilder() {
		factory = JsonNodeFactory.instance;
	}
	
	public JsonBuilder beginObject() {
		ObjectNode object = factory.objectNode();
		
		JsonNode top = top();
		if (top instanceof ArrayNode) {
			ArrayNode array = (ArrayNode) top;
			array.add(object);
		}
		
		stack.add(object);
		
		return this;
	}
	
	public JsonBuilder beginObject(String fieldName) {
		ObjectNode subject = (ObjectNode) top();
		ObjectNode object = subject.putObject(fieldName);
		stack.add(object);
		return this;
		
	}
	
	public ObjectNode pop() {
		ObjectNode result = (ObjectNode) top();
		endObject();
		return result;
	}
	
	private JsonNode top() {
		
		return stack.isEmpty() ? null : stack.get(stack.size()-1);
	}

	public JsonBuilder endObject() {
		stack.remove(stack.size()-1);
		return this;
	}
	
	public JsonBuilder put(String fieldName, JsonNode value) {
		ObjectNode node = peekObject();
		node.set(fieldName, value);
		return this;
	}
	
	public JsonBuilder put(String fieldName, String value) {
		ObjectNode node = peekObject();
		node.set(fieldName, factory.textNode(value));
		return this;
	}
	
	public JsonBuilder put(String fieldName, int value) {
		peekObject().set(fieldName, factory.numberNode(value));
		return this;
	}
	
	public JsonBuilder put(String fieldName, double value) {
		peekObject().set(fieldName, factory.numberNode(value));
		return this;
	}
	
	public JsonBuilder beginArray(String fieldName) {
		ArrayNode array = factory.arrayNode();
		peekObject().set(fieldName, array);
		stack.add(array);
		return this;
	}
	
	public JsonBuilder endArray() {
		return endObject();
	}
	

	private ObjectNode peekObject() {
		
		return (ObjectNode) stack.get(stack.size()-1);
	}
	
	private JsonNode peekNode() {
		return stack.get(stack.size()-1);
	}
	
	public String toString() {
		JsonNode node = peekNode();
		if (node != null) {
			ObjectMapper mapper = new ObjectMapper();
			try {
				return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(node);
			} catch (JsonProcessingException e) {
				throw new KonigException(e);
			}
		}
		return "null";
	}
	
	public Reader toReader() {

		JsonNode node = peekNode();
		String text = node==null ? "" : toString();
		
		return new StringReader(text);
	}

}
