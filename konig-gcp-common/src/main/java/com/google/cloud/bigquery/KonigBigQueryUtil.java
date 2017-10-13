package com.google.cloud.bigquery;
 
/*
 * #%L
 * Konig GCP Common
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


public class KonigBigQueryUtil {

	public static DatasetInfo createDatasetInfo(com.google.api.services.bigquery.model.Dataset model) {
		return new DatasetInfo.BuilderImpl(model).build();
	}
	
	public static TableInfo createTableInfo(com.google.api.services.bigquery.model.Table model) {
		String type = model.getType();
		if (type == null) {
			model.setType(TableDefinition.Type.TABLE.name());
		}
		return new TableInfo.BuilderImpl(model).build();
	}

	public static TableInfo createViewInfo(com.google.api.services.bigquery.model.Table model) { 
		String type = model.getType();
		if (type == null) {
			model.setType(TableDefinition.Type.VIEW.name()); 
		}
		model.getView().setUseLegacySql(false);
		return new TableInfo.BuilderImpl(model).build();
	}
}
