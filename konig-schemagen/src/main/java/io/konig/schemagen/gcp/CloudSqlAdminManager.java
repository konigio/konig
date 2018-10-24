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

import java.util.List;

import org.openrdf.model.vocabulary.RDF;

import com.google.api.services.sqladmin.model.Database;
import com.google.api.services.sqladmin.model.DatabaseInstance;

import io.konig.core.Graph;
import io.konig.core.Vertex;
import io.konig.core.pojo.SimplePojoFactory;
import io.konig.core.vocab.GCP;
import io.konig.gcp.common.CloudSqlDatabaseVisitor;
import io.konig.gcp.common.CloudSqlInstanceVisitor;

public class CloudSqlAdminManager  {
	


	private CloudSqlInstanceVisitor instanceVisitor;
	private CloudSqlDatabaseVisitor databaseVisitor;

	public CloudSqlAdminManager(CloudSqlInstanceVisitor instanceVisitor, CloudSqlDatabaseVisitor databaseVisitor) {
		this.instanceVisitor = instanceVisitor;
		this.databaseVisitor = databaseVisitor;
	}


	public void load(Graph graph) {
		SimplePojoFactory pojoFactory = new SimplePojoFactory();
		loadCloudSqlInstances(graph, pojoFactory);
		loadCloudSqlDatabases(graph, pojoFactory);
	}
	
	private void loadCloudSqlDatabases(Graph graph, SimplePojoFactory pojoFactory) {
		if (instanceVisitor != null) {
			List<Vertex> list = graph.v(GCP.GoogleCloudSqlDatabase).in(RDF.TYPE).toVertexList();
			for (Vertex v : list) {
				Database database= pojoFactory.create(v, Database.class);
				databaseVisitor.visit(database);
			}
		}
	}

	private void loadCloudSqlInstances(Graph graph, SimplePojoFactory pojoFactory) {
		if (instanceVisitor != null) {
			List<Vertex> list = graph.v(GCP.GoogleCloudSqlInstance).in(RDF.TYPE).toVertexList();
			for (Vertex v : list) {
				DatabaseInstance instance= pojoFactory.create(v, DatabaseInstance.class);
				instanceVisitor.visit(instance);
			}
		}
	}


}
