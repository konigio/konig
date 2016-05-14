package io.konig.schemagen.jsonschema;

public class JsonSchemaDatatype {
	
	public static final JsonSchemaDatatype BOOLEAN = new JsonSchemaDatatype("boolean");
	public static final JsonSchemaDatatype INT = new JsonSchemaDatatype("integer");
	public static final JsonSchemaDatatype LONG = new JsonSchemaDatatype("integer");
	public static final JsonSchemaDatatype FLOAT = new JsonSchemaDatatype("number");
	public static final JsonSchemaDatatype DOUBLE = new JsonSchemaDatatype("number");
	public static final JsonSchemaDatatype STRING = new JsonSchemaDatatype("string");
	public static final JsonSchemaDatatype DATE = new JsonSchemaDatatype("string", "date-time");
	public static final JsonSchemaDatatype TIME = new JsonSchemaDatatype("string", "date-time");
	public static final JsonSchemaDatatype TIMESTAMP = new JsonSchemaDatatype("string", "date-time");
	
	private String typeName;
	private String format;
	private Boolean exclusiveMinimum;
	private Boolean exclusiveMaximum;
	private Number minimum;
	private Number maximum;
	
	protected JsonSchemaDatatype(String typeName, String logicalType) {
		this.typeName = typeName;
		this.format = logicalType;
	}
	
	
	
	public JsonSchemaDatatype(String typeName, String format, Boolean exclusiveMinimum, Boolean exclusiveMaximum,
			Number minimum, Number maximum) {
		this.typeName = typeName;
		this.format = format;
		this.exclusiveMinimum = exclusiveMinimum;
		this.exclusiveMaximum = exclusiveMaximum;
		this.minimum = minimum;
		this.maximum = maximum;
	}



	protected JsonSchemaDatatype(String typeName) {
		this.typeName = typeName;
	}

	public String getTypeName() {
		return typeName;
	}

	public String getFormat() {
		return format;
	}

	public Boolean getExclusiveMinimum() {
		return exclusiveMinimum;
	}

	public Boolean getExclusiveMaximum() {
		return exclusiveMaximum;
	}

	public Number getMinimum() {
		return minimum;
	}

	public Number getMaximum() {
		return maximum;
	}
	
	

}
