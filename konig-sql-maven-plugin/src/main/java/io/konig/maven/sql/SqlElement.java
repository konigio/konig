package io.konig.maven.sql;

public class SqlElement {
	
	private String sqlId;
	private String iri;
	private String equivalentPath;

	public SqlElement() {
	}

	public String getSqlId() {
		return sqlId;
	}

	public void setSqlId(String sqlId) {
		this.sqlId = sqlId;
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
