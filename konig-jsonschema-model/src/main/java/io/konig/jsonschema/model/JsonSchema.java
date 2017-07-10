package io.konig.jsonschema.model;

/*
 * #%L
 * Konig JSON Schema model
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


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.annotation.JsonSerialize.Inclusion;

import io.konig.yaml.YamlProperty;

public class JsonSchema {
	
	private String id;
	private String type;
	private String format;
	private String description;
	private String title;
	private Integer multipleOf;
	private Number maximum;
	private Boolean exclusiveMaximum;
	private Number minimum;
	private Boolean exclusiveMinimum;
	private Integer maxLength;
	private Integer minLength;
	private String pattern;
	private Long maxItems;
	private Long minItems;
	private Boolean uniqueItems;
	private Integer maxProperties;
	private Integer minProperties;
	private List<String> required;
	private List<Object> enumList;
	private List<JsonSchema> allOf;
	private List<JsonSchema> oneOf;
	private List<JsonSchema> anyOf;
	private JsonSchema not;
	private JsonSchema items;
	private PropertyMap properties;
	private String ref;
	
	public JsonSchema() {
	}
	
	public JsonSchema(String ref) {
		this.ref = ref;
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getType() {
		return type;
	}

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getTitle() {
		return title;
	}
	
	public void addEnum(Object enumValue) {
		if (enumList == null) {
			enumList = new ArrayList<>();
		}
		enumList.add(enumValue);
	}
	
	public void addRequired(String requiredField) {
		if (required == null) {
			required = new ArrayList<>();
		}
		required.add(requiredField);
	}
	
	public void addProperty(String name, JsonSchema schema) {
		if (properties == null) {
			properties = new PropertyMap();
		}
		properties.put(name, schema);
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Integer getMultipleOf() {
		return multipleOf;
	}

	public void setMultipleOf(Integer multipleOf) {
		this.multipleOf = multipleOf;
	}

	public Number getMaximum() {
		return maximum;
	}

	public void setMaximum(Number maximum) {
		this.maximum = maximum;
	}

	public Number getMinimum() {
		return minimum;
	}

	public void setMinimum(Number minimum) {
		this.minimum = minimum;
	}

	public Boolean getExclusiveMaximum() {
		return exclusiveMaximum;
	}

	public void setExclusiveMaximum(Boolean exclusiveMaximum) {
		this.exclusiveMaximum = exclusiveMaximum;
	}

	public Boolean getExclusiveMinimum() {
		return exclusiveMinimum;
	}

	public void setExclusiveMinimum(Boolean exclusiveMinimum) {
		this.exclusiveMinimum = exclusiveMinimum;
	}

	public Integer getMaxLength() {
		return maxLength;
	}

	public void setMaxLength(Integer maxLength) {
		this.maxLength = maxLength;
	}

	public Integer getMinLength() {
		return minLength;
	}

	public void setMinLength(Integer minLength) {
		this.minLength = minLength;
	}

	public String getPattern() {
		return pattern;
	}

	public void setPattern(String pattern) {
		this.pattern = pattern;
	}

	public Long getMaxItems() {
		return maxItems;
	}

	public void setMaxItems(Long maxItems) {
		this.maxItems = maxItems;
	}

	public Long getMinItems() {
		return minItems;
	}

	public void setMinItems(Long minItems) {
		this.minItems = minItems;
	}

	public Boolean getUniqueItems() {
		return uniqueItems;
	}

	public void setUniqueItems(Boolean uniqueItems) {
		this.uniqueItems = uniqueItems;
	}

	public Integer getMaxProperties() {
		return maxProperties;
	}

	public void setMaxProperties(Integer maxProperties) {
		this.maxProperties = maxProperties;
	}

	public Integer getMinProperties() {
		return minProperties;
	}

	public void setMinProperties(Integer minProperties) {
		this.minProperties = minProperties;
	}

	public List<String> getRequired() {
		return required;
	}

	public void setRequired(List<String> required) {
		this.required = required;
	}

	public List<Object> getEnumList() {
		return enumList;
	}

	public void setEnumList(List<Object> enumList) {
		this.enumList = enumList;
	}

	public List<JsonSchema> getAllOf() {
		return allOf;
	}

	public void setAllOf(List<JsonSchema> allOf) {
		this.allOf = allOf;
	}

	public List<JsonSchema> getOneOf() {
		return oneOf;
	}

	public void setOneOf(List<JsonSchema> oneOf) {
		this.oneOf = oneOf;
	}

	public List<JsonSchema> getAnyOf() {
		return anyOf;
	}

	public void setAnyOf(List<JsonSchema> anyOf) {
		this.anyOf = anyOf;
	}

	public JsonSchema getNot() {
		return not;
	}

	public void setNot(JsonSchema not) {
		this.not = not;
	}

	public JsonSchema getItems() {
		return items;
	}

	public void setItems(JsonSchema items) {
		this.items = items;
	}

	public PropertyMap getProperties() {
		return properties;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setProperties(PropertyMap properties) {
		this.properties = properties;
	}

	public static class PropertyMap extends HashMap<String,JsonSchema> {
		private static final long serialVersionUID = 1L;
		
	}
	
	@YamlProperty("$ref")
	public String getRef() {
		return ref;
	}

	@YamlProperty("$ref")
	public void setRef(String ref) {
		this.ref = ref;
	}

	public String toString() {
		ObjectMapper mapper = new ObjectMapper();
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
		mapper.setSerializationInclusion(Include.NON_NULL);
		
		try {
			return mapper.writeValueAsString(this);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
		
	}

}
