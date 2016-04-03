package io.konig.schemagen.avro;

public class FixedAvroDatatype extends AvroDatatype {
	
	int size;
	
	public FixedAvroDatatype(int size) {
		super("fixed");
		this.size = size;
	}
	
	public FixedAvroDatatype(String logicalType, int size) {
		super("fixed", logicalType);
		this.size = size;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

}
