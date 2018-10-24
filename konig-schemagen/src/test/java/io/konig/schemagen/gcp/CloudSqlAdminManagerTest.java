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


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.RDF;

import com.google.api.services.sqladmin.model.Database;
import com.google.api.services.sqladmin.model.DatabaseInstance;

import io.konig.core.impl.MemoryGraph;
import io.konig.core.vocab.GCP;
import io.konig.gcp.common.CloudSqlDatabaseVisitor;
import io.konig.gcp.common.CloudSqlInstanceVisitor;

public class CloudSqlAdminManagerTest {
	
	private MemoryGraph graph = new MemoryGraph();
	private MockDatabaseInstanceVisitor instanceVisitor = new MockDatabaseInstanceVisitor();
	private MockDatabaseVisitor databaseVisitor = new MockDatabaseVisitor();
	private CloudSqlAdminManager manager = new CloudSqlAdminManager(instanceVisitor, databaseVisitor);
	
	static class MockDatabaseInstanceVisitor implements CloudSqlInstanceVisitor {

		private DatabaseInstance instance;
		
		@Override
		public void visit(DatabaseInstance instance) {
			this.instance = instance;
		}

		public DatabaseInstance getInstance() {
			return instance;
		}
		
	}
	
	static class MockDatabaseVisitor implements CloudSqlDatabaseVisitor {

		private Database database;
		
		@Override
		public void visit(Database database) {
			this.database = database;
		}

		public Database getDatabase() {
			return database;
		}
		
	}

	@Test
	public void testInstance() {
		
		
		URI instanceId = uri("http://example.com/instance/mock_instance");
		
		Resource settings = graph.vertex().getId();
		graph.edge(instanceId, RDF.TYPE, GCP.GoogleCloudSqlInstance);
		graph.edge(instanceId, GCP.name, literal("mock_instance"));
		graph.edge(instanceId, GCP.settings, settings);
		graph.edge(instanceId, GCP.instanceType, literal("CLOUD_SQL_INSTANCE"));
		graph.edge(settings, GCP.tier, literal("db-f1-micro"));
		
		manager.load(graph);
		
		DatabaseInstance instance = instanceVisitor.getInstance();
		assertTrue(instance != null);
		assertEquals("mock_instance", instance.getName());
		assertEquals("CLOUD_SQL_INSTANCE", instance.getInstanceType());
		assertEquals("db-f1-micro", instance.getSettings().getTier());
		
	}
	
	@Test
	public void testDatabase() {
		URI databaseId = uri("http://example.com/database/mock_db");
		
		graph.edge(databaseId, RDF.TYPE, GCP.GoogleCloudSqlDatabase);
		graph.edge(databaseId, GCP.name, literal("mock_db"));
		graph.edge(databaseId, GCP.charset, literal("utf8"));
		graph.edge(databaseId, GCP.instance, literal("mock_instance"));
		
		manager.load(graph);
		
		Database db = databaseVisitor.getDatabase();
		assertTrue(db != null);
		assertEquals("mock_db", db.getName());
		assertEquals("utf8", db.getCharset());
		assertEquals("mock_instance", db.getInstance());
	}
	
	private Value literal(String value) {
		
		return new LiteralImpl(value);
	}

	private URI uri(String value) {
		return new URIImpl(value);
	}

}
