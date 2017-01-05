package io.konig.maven.sql;

import java.util.ArrayList;
import java.util.List;

public class Table {
	private String name;
	private String iri;
	private String targetClass;
	private List<Column> columns = new ArrayList<>();
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getIri() {
		return iri;
	}
	public void setIri(String iri) {
		this.iri = iri;
	}
	public List<Column> getColumns() {
		return columns;
	}
	public void setColumns(List<Column> columns) {
		this.columns = columns;
	}
	public String getTargetClass() {
		return targetClass;
	}
	public void setTargetClass(String targetClass) {
		this.targetClass = targetClass;
	}

}
