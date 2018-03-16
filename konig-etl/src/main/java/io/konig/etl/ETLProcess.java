package io.konig.etl;

/*
 * #%L
 * Konig ETL
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


public class ETLProcess {
	
	public void run(ETLRequest request) throws ETLException{
		ETLDatasource datasource = request.getDatasource();
		datasource.createTmpTable(request.getCreateSql());
		datasource.loadTmpTable(request.getTableName(), request.getBucketName(), request.getObjectId());
		datasource.transform(request.getTransformSql());	
		datasource.export(request.getExportRequest());
		datasource.deleteTmpTable(request.getTableName());
	}
}
