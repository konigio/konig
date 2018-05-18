package io.konig.datacatalog;

/*
 * #%L
 * Konig Data Catalog
 * %%
 * Copyright (C) 2015 - 2018 Gregory McFall
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


public class DataSourceInfo implements Comparable<DataSourceInfo> {
	
	private String type;
	private String tableName;
	private String providedBy;
	
	public DataSourceInfo(String type, String tableName, String providedBy) {
		this.type = type;
		this.tableName = tableName;
		this.providedBy = providedBy;
	}
	
	public String getType() {
		return type;
	}
	public String getTableName() {
		return tableName;
	}
	public String getProvidedBy() {
		return providedBy;
	}

	@Override
	public int compareTo(DataSourceInfo o) {
		int result = type.compareTo(o.type);
		if (result == 0) {
			result = tableName.compareTo(o.tableName);
		}
		if (result == 0) {
			result = providedBy.compareTo(o.providedBy);
		}
		return result;
	}
	
	
	

}
