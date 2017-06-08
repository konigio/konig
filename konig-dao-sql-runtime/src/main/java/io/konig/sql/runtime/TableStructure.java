package io.konig.sql.runtime;

import java.util.ArrayList;
import java.util.List;

/**
 * A light-weight list of fields within a given table.
 * Each field has a name and an optional nested structure.
 * This class is used by {@link BigQueryShapeReadService} 
 * @author Greg McFall
 *
 */
public class TableStructure {

	private String name;
	private List<FieldInfo> fields = new ArrayList<>();
	
	public TableStructure() {
		
	}
	
	public TableStructure(String name) {
		this.name = name;
	}
	
	public void addField(String name) {
		fields.add(new FieldInfo(name));
	}
	
	public void addField(String name, TableStructure struct) {
		fields.add(new FieldInfo(name, struct));
	}
	
	public void addField(FieldInfo field) {
		fields.add(field);
	}

	public List<FieldInfo> getFields() {
		return fields;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String toString() {
		return JsonUtil.toString(this);
	}
	
	
}
