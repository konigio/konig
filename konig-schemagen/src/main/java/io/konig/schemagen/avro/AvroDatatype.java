package io.konig.schemagen.avro;

public class AvroDatatype {
	
	public static final AvroDatatype BOOLEAN = new AvroDatatype("boolean");
	public static final AvroDatatype INT = new AvroDatatype("int");
	public static final AvroDatatype LONG = new AvroDatatype("long");
	public static final AvroDatatype FLOAT = new AvroDatatype("float");
	public static final AvroDatatype DOUBLE = new AvroDatatype("double");
	public static final AvroDatatype STRING = new AvroDatatype("string");
	public static final AvroDatatype DATE = new AvroDatatype("int", "date");
	public static final AvroDatatype TIME = new AvroDatatype("int", "time-millis");
	public static final AvroDatatype TIMESTAMP = new AvroDatatype("long", "timestamp-millis");
	public static final AvroDatatype DURATION = new FixedAvroDatatype("duration", 12);
	public static final AvroDatatype BYTE = new FixedAvroDatatype(1);
	
	private String typeName;
	private String logicalType;
	
	protected AvroDatatype(String typeName, String logicalType) {
		this.typeName = typeName;
		this.logicalType = logicalType;
	}

	protected AvroDatatype(String typeName) {
		this.typeName = typeName;
	}

	public String getTypeName() {
		return typeName;
	}

	public String getLogicalType() {
		return logicalType;
	}
	
	

}
