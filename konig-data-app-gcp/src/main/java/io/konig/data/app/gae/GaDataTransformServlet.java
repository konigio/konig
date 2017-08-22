package io.konig.data.app.gae;

/*
 * #%L
 * Konig Data App for Google Cloud Platform
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


import java.io.InputStream;
import java.util.HashMap;

import javax.servlet.ServletException;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.BigQueryOptions;

import io.konig.data.app.common.AppEngineUtil;
import io.konig.data.app.common.DataTransformServlet;
import io.konig.sql.runtime.BigQueryDataTransformService;

public class GaDataTransformServlet extends DataTransformServlet {
	private static final long serialVersionUID = 1L;
	
	@Override
	protected void createDataTransformService(String queryString , HashMap<String,String> queryParams) throws ServletException {
		try (InputStream input = getClass().getClassLoader().getResourceAsStream(AppEngineUtil.CREDENTIALS_FILE)) {
			GoogleCredentials credentials = GoogleCredentials.fromStream(input);
			BigQuery bigQuery = BigQueryOptions.newBuilder()
					.setCredentials(credentials).build().getService();			
			new BigQueryDataTransformService(bigQuery).executeSql(queryString, queryParams);;
		} catch (Exception e) {
			throw new ServletException(e);
		}
		
	}

}
