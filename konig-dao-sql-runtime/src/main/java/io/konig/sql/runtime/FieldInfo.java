package io.konig.sql.runtime;

public class FieldInfo {

	private String name;
	private TableStructure struct;
	
	public FieldInfo() {
	}
	public FieldInfo(String fieldName) {
		this.name = fieldName;
	}
	
	public FieldInfo(String name, TableStructure struct) {
		this.name = name;
		this.struct = struct;
	}
	public String getName() {
		return name;
	}
	public void setName(String fieldName) {
		this.name = fieldName;
	}
	public TableStructure getStruct() {
		return struct;
	}
	public void setStruct(TableStructure struct) {
		this.struct = struct;
	}
	
	
}
