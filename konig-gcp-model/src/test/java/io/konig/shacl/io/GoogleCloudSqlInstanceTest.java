package io.konig.shacl.io;

/*
 * #%L
 * Konig Google Cloud Platform Model
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

import java.io.StringReader;
import java.io.StringWriter;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

import io.konig.core.Vertex;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.RdfUtil;
import io.konig.core.pojo.EmitContext;
import io.konig.core.pojo.SimplePojoEmitter;
import io.konig.core.pojo.SimplePojoFactory;
import io.konig.gcp.datasource.GoogleCloudSqlBackendType;
import io.konig.gcp.datasource.GoogleCloudSqlInstance;
import io.konig.gcp.datasource.GoogleCloudSqlInstanceType;
import io.konig.gcp.datasource.GoogleCloudSqlRegion;
import io.konig.gcp.datasource.GoogleCloudSqlSettings;
import io.konig.gcp.datasource.GoogleCloudSqlTier;
import io.konig.gcp.datasource.GoogleCloudSqlVersion;
import io.konig.gcp.io.GoogleCloudSqlJsonUtil;

public class GoogleCloudSqlInstanceTest {
	
	private GoogleCloudSqlInstance instance;
	private GoogleCloudSqlSettings settings;
	
	@Before
	public void setUp() {
		instance = new GoogleCloudSqlInstance();
		settings = new GoogleCloudSqlSettings();
		
		instance.setId(uri("https://www.googleapis.com/sql/v1beta4/projects/exampleProject/instances/exampleInstance"));
		instance.setBackendType(GoogleCloudSqlBackendType.SECOND_GEN);
		instance.setDatabaseVersion(GoogleCloudSqlVersion.MYSQL_5_7);
		instance.setInstanceType(GoogleCloudSqlInstanceType.CLOUD_SQL_INSTANCE);
		instance.setName("exampleInstance");
		instance.setRegion(GoogleCloudSqlRegion.us_central);
		settings.setTier(GoogleCloudSqlTier.db_f1_micro);
		instance.setSettings(settings);
	}

	@Test
	public void testJson() throws Exception {
		StringWriter writer = new StringWriter();
		GoogleCloudSqlJsonUtil.writeJson(instance, writer);
		writer.close();
		String text = writer.toString();
		StringReader reader = new StringReader(text);
		GoogleCloudSqlInstance actual = GoogleCloudSqlJsonUtil.readJson(reader);
		assertInstanceEquals(instance, actual);
	}

	@Test
	public void testTurtle() throws Exception {
		MemoryGraph graph = new MemoryGraph();
		SimplePojoEmitter emitter = new SimplePojoEmitter();
		EmitContext context = new EmitContext(graph);
		
		MemoryGraph sink = new MemoryGraph();
		
		emitter.emit(context, instance, sink);
		
		StringWriter writer = new StringWriter();
		RdfUtil.prettyPrintTurtle(sink, writer);
		
		writer.close();
		
		Vertex vertex = sink.getVertex(instance.getId());
		
		SimplePojoFactory pojoFactory = new SimplePojoFactory();
		GoogleCloudSqlInstance actual = pojoFactory.create(vertex, GoogleCloudSqlInstance.class);
		
		assertInstanceEquals(instance, actual);
	}
	
	private void assertInstanceEquals(GoogleCloudSqlInstance instance2, GoogleCloudSqlInstance actual) {

		assertEquals(instance.getBackendType(), actual.getBackendType());
		assertEquals(instance.getDatabaseVersion(), actual.getDatabaseVersion());
		assertEquals(instance.getId(), actual.getId());
		assertEquals(instance.getInstanceType(), actual.getInstanceType());
		assertEquals(instance.getName(), actual.getName());
		assertEquals(instance.getRegion(), actual.getRegion());
		
	}

	private URI uri(String value) {
		return new URIImpl(value);
	}

}
