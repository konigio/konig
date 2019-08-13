package io.konig.maven;

import java.io.BufferedReader;

/*
 * #%L
 * Konig GCP Deployment Maven Plugin
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


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeoutException;

import org.json.JSONArray;
import org.json.JSONObject;

import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.BigQueryError;
import com.google.cloud.bigquery.QueryRequest;
import com.google.cloud.bigquery.QueryResponse;
import com.google.cloud.bigquery.TableId;

import io.konig.gcp.common.GoogleCloudService;

public class InsertBigQueryDataAction {

	private KonigDeployment deployment;

	public InsertBigQueryDataAction(KonigDeployment deployment) {
		this.deployment = deployment;
	}

	public KonigDeployment from(String path) throws IOException, InterruptedException, TimeoutException {

		GoogleCloudService service = deployment.getService();
		File file = deployment.file(path);
		BigQuery bigquery = service.bigQuery();
		String fileName = file.getName();
		String[] tableReference  = fileName.split("\\.");
		String datasetName = tableReference[0];
		String tableName =  tableReference[1];
		TableId tableId = TableId.of(datasetName, tableName);		
		JSONArray jsonArray = new JSONArray();
		try (FileInputStream fis = new FileInputStream(file)) {
			BufferedReader buf = new BufferedReader(new InputStreamReader(fis));
			String line = buf.readLine();
			while (line != null) {
				JSONObject jsonObject = new JSONObject(line);
				jsonArray.put(jsonObject);
				line = buf.readLine();
			}
			buf.close();
		}
		
		String mergeStatement = new MergeStatementBuilder(jsonArray, tableId).build().toQuery();
		System.out.print("mergeStatement ==" + mergeStatement);
		QueryRequest request = QueryRequest.of(mergeStatement);
		QueryResponse response = bigquery.query(request);
		while (!response.jobCompleted()) {
			Thread.sleep(1000);
			response = bigquery.getQueryResults(response.getJobId());
		}
		if (response.hasErrors()) {
			List<BigQueryError> errors = response.getExecutionErrors();
			StringBuilder errorBuilder = new StringBuilder();
			for(BigQueryError error :errors) {
				errorBuilder.append(error.getReason());
				errorBuilder.append("\n");
			}
			deployment.setResponse(datasetName+"."+tableName + " - Deployment failed " + errorBuilder.toString());
		}else {
			deployment.setResponse(datasetName+"."+tableName + " - " + response.getResult().getTotalRows() +" Rows Inserted");
		}
		return deployment;
		
	}
	
	class MergeStatementBuilder {
		private JSONArray jsonArray;
		private TableId tableId;
		private StringBuilder stringBuilder;
		
		public MergeStatementBuilder(JSONArray sourceJsonArray, TableId tableId) {
			this.jsonArray = sourceJsonArray;
			this.tableId = tableId;
			stringBuilder = new StringBuilder();
		}
		
		private Set keySet() {
			for(Object jsonObject : jsonArray) {
				JSONObject json = (JSONObject)jsonObject;
				if(json != null) {
					return json.keySet();
				}
			}
			return Collections.emptySet();
		}
		
		private void addSource() {
			Set keys = keySet();
			String comma = "";
			String unionall = "";
			stringBuilder.append("USING (");
			
			stringBuilder.append("Select ");
			for(Object key : keys) {
				stringBuilder.append(comma);
				stringBuilder.append(key.toString());
				comma = ",";
			}
			stringBuilder.append(" from(  \n");
			
			for(Object jsonObject : jsonArray) {
				JSONObject json = (JSONObject)jsonObject;
				comma = "";
				if(!unionall.equals("")) {
					stringBuilder.append("\n");
					stringBuilder.append(unionall);
					stringBuilder.append("\n");
				}
				stringBuilder.append("Select ");
				for(Object key : keys) {
					stringBuilder.append(comma);
					stringBuilder.append("\"");
					stringBuilder.append(json.get(key.toString()).toString());
					stringBuilder.append("\"");
					stringBuilder.append(" as ");
					stringBuilder.append(key.toString());
					comma = ",";
				}
				unionall = "union all";
			}
			stringBuilder.append(" ) \n");
			stringBuilder.append(" ) S \n");
		}
		
		private void addUpdate() {
			Set keys = keySet();
			String comma = "";
			stringBuilder.append(" UPDATE SET ");
			for(Object key : keys) {
				if(!key.toString().equals("id")) {
					stringBuilder.append(comma);
					stringBuilder.append(key);
					stringBuilder.append("=");
					stringBuilder.append("S."+key);
					comma = ",";
				}
			}
			stringBuilder.append(" \n");
		}
		
		private void addDelete() {
			stringBuilder.append(" DELETE \n ");
		}
		
		private void addInsert() {
			stringBuilder.append(" INSERT Row \n ");
		}
		
		private void addWhenClause(String whenClause) {
			stringBuilder.append(" " + whenClause + " \n ");
		}
		
		private void addTarget() {
			stringBuilder.append("`");
			stringBuilder.append(tableId.getDataset()); 
			stringBuilder.append(".");
			stringBuilder.append(tableId.getTable());
			stringBuilder.append("`");
			stringBuilder.append(" T");
			stringBuilder.append("\n");
		}
		
		private void addJoinsOn(String on) {
			stringBuilder.append(" ON T."+on);
			stringBuilder.append(" = ");
			stringBuilder.append("S."+on);
			stringBuilder.append("\n");
		}
		
		public MergeStatementBuilder build() {
			stringBuilder.append("#standardSQL");
			stringBuilder.append("\n");
			stringBuilder.append("MERGE ");
			addTarget();
			addSource();
			addJoinsOn("id");
			addWhenClause("WHEN MATCHED THEN");		
			addUpdate();
			addWhenClause("WHEN NOT MATCHED BY SOURCE THEN");
			addDelete();
			addWhenClause("WHEN NOT MATCHED BY TARGET THEN");
			addInsert();
			return this;
		}
		
		public String toQuery() {
			return stringBuilder.toString();
		}
		
		public String toString() {
			return stringBuilder.toString();
		}
	}

}
