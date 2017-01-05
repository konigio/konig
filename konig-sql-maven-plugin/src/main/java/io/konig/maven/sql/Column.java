package io.konig.maven.sql;

public class Column {

	private String name;
	private String iri;
	private String equivalentPath;
	
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
	public String getEquivalentPath() {
		return equivalentPath;
	}
	public void setEquivalentPath(String equivalentPath) {
		this.equivalentPath = equivalentPath;
	}
}
