package io.konig.sql.runtime;

/*
 * #%L
 * Konig DAO SQL Runtime
 * %%
 * Copyright (C) 2015 - 2017 Gregory McFall
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


import java.util.ArrayList;
import java.util.List;

import io.konig.yaml.Yaml;
import io.konig.yaml.YamlProperty;

/**
 * A light-weight list of fields within some object.
 * Each field has a name and an optional nested structure.
 * This class is used by {@link BigQueryShapeReadService} 
 * @author Greg McFall
 *
 */
public class EntityStructure {

	private String name;
	private String comment;
	private List<FieldInfo> fields = new ArrayList<>();
	
	public EntityStructure() {
		
	}
	
	public EntityStructure(String name) {
		this.name = name;
	}
	
	public void addField(String name) {
		fields.add(new FieldInfo(name));
	}
	
	public void addField(String name, EntityStructure struct) {
		fields.add(new FieldInfo(name, struct));
	}

	@YamlProperty("fields")
	public void addField(FieldInfo field) {
		fields.add(field);
	}

	public List<FieldInfo> getFields() {
		return fields;
	}

	public String getName() {
		return name;
	}
	
	public FieldInfo findFieldByName(String name) {
		if (fields != null) {
			for (FieldInfo field : fields) {
				if (name.equals(field.getName())) {
					return field;
				}
			}
		}
		return null;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String toString() {
		return Yaml.toString(this);
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}
	
	
}
