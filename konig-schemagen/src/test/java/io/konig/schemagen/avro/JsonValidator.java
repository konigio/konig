package io.konig.schemagen.avro;

/*
 * #%L
 * Konig Schema Generator
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

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class JsonValidator {

	private List<JsonElement> stack = new ArrayList<>();
	
	public JsonValidator() {
	}
	
	public JsonValidator(ObjectNode root) {
		beginObject(root);
	}
	
	public JsonValidator beginObject(ObjectNode node) {
		stack.add(new JsonObject("root", node));
		return this;
	}
	
	public JsonValidator endObject() {
		pop();
		return this;
	}
	
	public JsonValidator assertField(String fieldName, String expectedValue) {
		JsonObject object = peekObject();
		object.assertField(fieldName, expectedValue);
		return this;
	}

	
	public JsonValidator assertField(String fieldName, int expectedValue) {
		JsonObject object = peekObject();
		object.assertField(fieldName, expectedValue);
		return this;
	}
	

	

	private JsonObject peekObject() {
		JsonElement top = top();
		if (top instanceof JsonObject) {
			return (JsonObject) top;
		}
		throw new RuntimeException("Top element in stack is not a JsonObject");
	}

	private JsonElement top() {
		if (stack.isEmpty()) {
			throw new RuntimeException("Stack is empty");
		}
		
		return stack.get(stack.size()-1);
	}

	private JsonElement pop() {
		JsonElement result = null;
		if (!stack.isEmpty()) {
			result = stack.remove(stack.size()-1);
		}
		return result;
		
	}


	static private class JsonElement {
		String path;

		private JsonElement(String path) {
			this.path = path;
		}
	}
	
	static private class JsonArray extends JsonElement {
		private ArrayNode node;
		private int index;
		private JsonArray(String path, ArrayNode node) {
			super(path);
			this.node = node;
		}
		public String itemPath() {
			StringBuilder builder = new StringBuilder();
			builder.append(path);
			builder.append('[');
			builder.append(index);
			builder.append(']');
			return builder.toString();
		}
		
		private JsonNode next() {
			if (index >= node.size()) {
				fail("Item not found " + itemPath());
			}
			
			return node.get(index++);
		}
	}
	
	static private class JsonObject extends JsonElement{
		private ObjectNode node;

		private JsonObject(String path, ObjectNode node) {
			super(path);
			this.node = node;
		}
		
		private void assertField(String name, String expected) {
			String fieldPath = fieldPath(name);
			JsonNode value = node.get(name);
			if (value == null) {
				fail("Field not found: " + fieldPath);
			}
			String actual = node.get(name).asText();
			assertEquals(fieldPath, expected, actual);
			
		}
		
		private void assertField(String name, int expected) {

			String fieldPath = fieldPath(name);
			JsonNode value = node.get(name);
			if (value == null) {
				fail("Field not found: " + fieldPath);
			}
			int actual = node.get(name).asInt();
			assertEquals(fieldPath, expected, actual);
		}

		private String fieldPath(String name) {
			return path + "." + name;
		}
		
	}

	public JsonValidator beginArray(String fieldName) {
		
		JsonObject top = peekObject();
		String path = top.fieldPath(fieldName);
		JsonNode node = top.node.get(fieldName);
		if (node == null) {
			fail("Field not found: " + path);
		}
		if (node instanceof ArrayNode) {
			JsonArray array = new JsonArray(path, (ArrayNode)node);
			stack.add(array);
		} else {
			fail("Expected array: " + path);
		}
		return this;
	}

	public JsonValidator endArray() {
		pop();
		return this;
	}
	
	public JsonValidator firstObject() {
		return beginObject();
	}
	
	public JsonValidator nextObject() {
		endObject();
		return beginObject();
	}

	public JsonValidator beginObject() {
		JsonArray array = peekArray();
		ArrayNode node = array.node;
		String path = array.itemPath();
		if (array.index >= node.size()) {
			fail("Array item not found: " + path);
		}
		JsonNode child = array.node.get(array.index++);
		if (child instanceof ObjectNode) {
			stack.add(new JsonObject(path, (ObjectNode)child));
		} else {
			fail("Expected object node: " + path);
		}
		
		
		return this;
	}

	private JsonArray peekArray() {
		JsonElement top = top();
		if (top instanceof JsonArray) {
			return (JsonArray) top;
		}
		throw new RuntimeException("Top element is not an array");
	}

	public JsonValidator beginObject(String fieldName) {
		JsonObject top = peekObject();
		JsonNode fieldValue = top.node.get(fieldName);
		String path = top.fieldPath(fieldName);
		
		if (fieldValue instanceof ObjectNode) {
			stack.add(new JsonObject(path, (ObjectNode)fieldValue));
		} else {
			fail("Expected object node: " + path);
		}
		return this;
	}

	public JsonValidator firstString(String expectedValue) {
		JsonArray array = peekArray();
		array.index=0;
		String path = array.itemPath();
		JsonNode item = array.next();
		
		String actualValue = item.asText();
		assertEquals(path, expectedValue, actualValue);
		
		return this;
	}
	public JsonValidator nextString(String expectedValue) {
		JsonArray array = peekArray();
		String path = array.itemPath();
		JsonNode item = array.next();
		
		String actualValue = item.asText();
		assertEquals(path, expectedValue, actualValue);
		
		return this;
	}
}
