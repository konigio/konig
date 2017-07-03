package io.konig.openapi.model;

import javax.xml.validation.Schema;

import io.konig.jsonschema.model.JsonSchema;

public class Parameter {

	private String name;
	private String description;
	private ParameterLocation in;
	private Boolean required;
	private Boolean deprecated;
	private Boolean allowEmptyValue;
	private String style;
	private Boolean explode;
	private Boolean allowReserved;
	private JsonSchema schema;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public ParameterLocation getIn() {
		return in;
	}
	public void setIn(ParameterLocation in) {
		this.in = in;
	}
	public String getStyle() {
		return style;
	}
	public void setStyle(String style) {
		this.style = style;
	}
	public JsonSchema getSchema() {
		return schema;
	}
	public void setSchema(JsonSchema schema) {
		this.schema = schema;
	}
	public Boolean getRequired() {
		return required;
	}
	public void setRequired(Boolean required) {
		this.required = required;
	}
	public Boolean getDeprecated() {
		return deprecated;
	}
	public void setDeprecated(Boolean deprecated) {
		this.deprecated = deprecated;
	}
	public Boolean getAllowEmptyValue() {
		return allowEmptyValue;
	}
	public void setAllowEmptyValue(Boolean allowEmptyValue) {
		this.allowEmptyValue = allowEmptyValue;
	}
	public Boolean getExplode() {
		return explode;
	}
	public void setExplode(Boolean explode) {
		this.explode = explode;
	}
	public Boolean getAllowReserved() {
		return allowReserved;
	}
	public void setAllowReserved(Boolean allowReserved) {
		this.allowReserved = allowReserved;
	}
	
	
	

}
