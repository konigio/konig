package io.konig.sql;

public class SQLColumnType {
	
	public static final Integer MAX = new Integer(-1);
	
	private SQLDatatype datatype;
	Integer size;
	Integer precision;

	public SQLColumnType() {
	}

	public SQLColumnType(SQLDatatype datatype, Integer size) {
		this.datatype = datatype;
		this.size = size;
	}

	public SQLColumnType(SQLDatatype datatype, Integer size, Integer precision) {
		this.datatype = datatype;
		this.size = size;
		this.precision = precision;
	}
	
	@Override 
	public boolean equals(Object other) {
		if (other instanceof SQLColumnType) {
			SQLColumnType c = (SQLColumnType) other;
			return (
				(datatype==null && c.datatype==null) ||
				(datatype!=null) && datatype.equals(c.datatype)
			) && (
				(size==null && c.size==null) ||
				(size!=null && size.equals(c.size))
			) && (
				(precision==null && c.precision==null) ||
				(precision!=null && precision==c.precision)
			);
		}
		return false;
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

	public Integer getPrecision() {
		return precision;
	}

	public void setPrecision(Integer precision) {
		this.precision = precision;
	}
	
	

}
