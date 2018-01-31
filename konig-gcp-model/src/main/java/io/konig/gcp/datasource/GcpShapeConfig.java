package io.konig.gcp.datasource;

/*
 * #%L
 * Konig Google Cloud Platform Model
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


import io.konig.core.pojo.PojoContext;
import io.konig.core.vocab.GCP;
import io.konig.core.vocab.Konig;
import io.konig.shacl.io.ShapeLoader;

public class GcpShapeConfig {
	
	public static void init() {

		PojoContext context = ShapeLoader.CONTEXT;
		context.mapClass(Konig.GoogleBigQueryView, GoogleBigQueryView.class);
		context.mapClass(Konig.GoogleBigQueryTable, GoogleBigQueryTable.class);
		context.mapClass(Konig.GoogleCloudStorageBucket, GoogleCloudStorageBucket.class);
		context.mapClass(Konig.GoogleCloudSqlTable, GoogleCloudSqlTable.class);
		context.mapClass(GCP.GoogleCloudSqlInstance, GoogleCloudSqlInstance.class);
	
	}

}
