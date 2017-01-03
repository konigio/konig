package io.konig.sql;

public class SQLColumnType {
	private SQLDatatype datatype;
	Integer size;

	public SQLColumnType() {
	}

	public SQLColumnType(SQLDatatype datatype, Integer size) {
		this.datatype = datatype;
		this.size = size;
	}

	public SQLDatatype getDatatype() {
		return datatype;
	}

	public void setDatatype(SQLDatatype datatype) {
		this.datatype = datatype;
	}

	public Integer getSize() {
		return size;
	}

	public void setSize(Integer size) {
		this.size = size;
	}
	
	

}
