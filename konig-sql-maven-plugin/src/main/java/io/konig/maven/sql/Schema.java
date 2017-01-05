package io.konig.maven.sql;

import java.util.ArrayList;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;

public class Schema {

	private String name;
	private String iri;
	private List<Table> tables=new ArrayList<>();

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Table> getTables() {
		return tables;
	}

	public void setTables(List<Table> tables) {
		this.tables = tables;
	}

	public String getIri() {
		return iri;
	}

	public void setIri(String iri) {
		this.iri = iri;
	}
	
	public Table getTableByName(String name)  {
		
		for (Table table : tables) {
			if (name.equals(table.getName())) {
				return table;
			}
		}

		return null;
	}
	

}
