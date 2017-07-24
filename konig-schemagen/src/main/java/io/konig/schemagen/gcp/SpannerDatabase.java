package io.konig.schemagen.gcp;

/*
 * #%L
 * Konig Schema Generator
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

public class SpannerDatabase {
	
	private String databaseName;
	private GoogleCloudProject databaseProject;
	private List<SpannerTable> tableList = new ArrayList<>();
	
	public String getDatabaseName() {
		return databaseName;
	}
	public void setDatabaseName(String databaseName) {
		this.databaseName = databaseName;
	}
	
	public GoogleCloudProject getDatabaseProject() {
		return databaseProject;
	}
	public void setDatabaseProject(GoogleCloudProject project) {
		this.databaseProject = project;
	}
	
	public void addDatasetTable(SpannerTable table) {
		tableList.add(table);
		//table.setTableDatabase(this);
	}
	
	public List<SpannerTable> getDatasetTable() {
		return tableList;
	}
	
	public SpannerTable findDatasetTable(String tableId) {
		for (SpannerTable t : tableList) {
			if (tableId.equals(t.getTableName())) {
				return t;
			}
		}
		return null;
	}
	
}
