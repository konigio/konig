package io.konig.schemagen.gcp;

/*
 * #%L
 * Konig Schema Generator
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


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.RDF;

import io.konig.core.Graph;
import io.konig.core.KonigException;
import io.konig.core.Vertex;
import io.konig.core.pojo.SimplePojoFactory;
import io.konig.core.vocab.GCP;
import io.konig.core.vocab.Konig;
import io.konig.gcp.datasource.GoogleCloudSqlDatabase;
import io.konig.gcp.datasource.GoogleCloudSqlInstance;
import io.konig.gcp.datasource.GoogleCloudSqlSettings;
import io.konig.gcp.datasource.GoogleCloudSqlTableInfo;
import io.konig.gcp.io.GoogleCloudSqlJsonUtil;
import io.konig.maven.CloudSqlInfo;

public class CloudSqlJsonGenerator {

	public CloudSqlJsonGenerator() {
		
	}
	
	public void writeAll(CloudSqlInfo cloudSqlInfo, Graph graph) throws IOException {
		writeInstances(cloudSqlInfo.getInstances(), graph);
		List<GoogleCloudSqlDatabase> dbList = writeTables(cloudSqlInfo.getTables(), graph);
		writeDatabases(cloudSqlInfo.getDatabases(), dbList);
	}

	private void writeDatabases(File baseDir, List<GoogleCloudSqlDatabase> dbList) throws IOException {
		if (!dbList.isEmpty()) {

			if (!baseDir.exists()) {
				baseDir.mkdirs();
			}
			
			for (GoogleCloudSqlDatabase db : dbList) {
				String instance = db.getInstance();
				String dbName = db.getName();
				String fileName = MessageFormat.format("{0}_{1}.json", instance, dbName);
				File file = new File(baseDir, fileName);
				try (FileWriter writer = new FileWriter(file)) {
					GoogleCloudSqlJsonUtil.writeJson(db, writer);
				}
			}
			
		}
		
	}

	private List<GoogleCloudSqlDatabase> writeTables(File baseDir, Graph graph) throws KonigException, IOException {
		Map<URI, GoogleCloudSqlDatabase> map = new HashMap<>();
		
		List<Vertex> list = graph.v(Konig.GoogleCloudSqlTable).in(RDF.TYPE).toVertexList();
		if (!list.isEmpty()) {

			if (!baseDir.exists()) {
				baseDir.mkdirs();
			}
			for (Vertex v : list) {

				SimplePojoFactory pojoFactory = new SimplePojoFactory();
				GoogleCloudSqlTableInfo table = pojoFactory.create(v, GoogleCloudSqlTableInfo.class);
				String instance = table.getInstance();
				String database = table.getDatabase();
				String tableName = table.getTableName();
				if (instance == null) {
					throw new KonigException("instance must be defined for table: " + table.getId());
				}
				if (database == null) {
					throw new KonigException("database must be defined for table: " + table.getId());
				}
				if (tableName==null) {
					throw new KonigException("tableName must be defined for table: " + table.getId());
				}
				
				String fileName = MessageFormat.format("{0}_{1}_{2}.json", instance, database, tableName);

				String sqlFileName = MessageFormat.format("{0}_{1}_{2}.sql", instance, database, tableName);
				
				String instanceFileName= MessageFormat.format("{0}.json", instance);
				
				File file = new File(baseDir, fileName);
				table.setDdlFile(new File(baseDir, sqlFileName));
				table.setInstanceFile(new File(baseDir,instanceFileName));
				
				try (FileWriter writer = new FileWriter(file)) {
					GoogleCloudSqlJsonUtil.writeJson(table, writer);
				}
				
				String databaseId = MessageFormat.format("https://www.googleapis.com/sql/v1beta4/projects/$'{'gcpProjectId'}'/instances/{0}/databases/{1}", 
						instance, database);
				URI databaseURI = new URIImpl(databaseId);
				
				if (!map.containsKey(databaseURI)) {
					GoogleCloudSqlDatabase db = new GoogleCloudSqlDatabase();
					db.setId(databaseURI);
					db.setInstance(instance);
					db.setName(database);
					map.put(databaseURI, db);
				}
			}
		}
		
		
		return new ArrayList<>(map.values());
	}
	
	private void writeInstances(File baseDir, Graph graph) throws IOException {
		List<Vertex> list = graph.v(GCP.GoogleCloudSqlInstance).in(RDF.TYPE).toVertexList();
		if (!list.isEmpty()) {
			if (!baseDir.exists()) {
				baseDir.mkdirs();
			}
			for (Vertex v : list) {
				SimplePojoFactory pojoFactory = new SimplePojoFactory();
				GoogleCloudSqlInstance instance = pojoFactory.create(v, GoogleCloudSqlInstance.class);
				File jsonFile = new File(baseDir, instance.getName() + ".json");
				try (FileWriter writer = new FileWriter(jsonFile)) {
					GoogleCloudSqlJsonUtil.writeJson(instance, writer);
				}
			}
		}
	}

}
