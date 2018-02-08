package io.konig.schemagen.omcs;

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

import java.io.File;
import java.io.IOException;

import org.junit.Ignore;
import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;

import io.konig.core.Graph;
import io.konig.core.NamespaceManager;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.MemoryNamespaceManager;
import io.konig.core.impl.RdfUtil;
import io.konig.core.vocab.OMCS;
import io.konig.gcp.datasource.GcpShapeConfig;
import io.konig.omcs.datasource.OracleShapeConfig;
import io.konig.omcs.datasource.OracleTable;
import io.konig.schemagen.sql.FacetedSqlDatatype;
import io.konig.schemagen.sql.SqlColumn;
import io.konig.schemagen.sql.SqlTable;
import io.konig.schemagen.sql.SqlTableGenerator;
import io.konig.schemagen.sql.StringSqlDatatype;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeManager;
import io.konig.shacl.impl.MemoryShapeManager;

public class OracleTableGeneratotTest {

	protected NamespaceManager nsManager = new MemoryNamespaceManager();
	protected Graph graph = new MemoryGraph(nsManager);
	protected ShapeManager shapeManager = new MemoryShapeManager();
	
	private SqlTableGenerator generator = new SqlTableGenerator();

	

	protected URI iri(String value) {
		return new URIImpl(value);
	}

	protected void load(String path) throws RDFParseException, RDFHandlerException, IOException {

		OracleShapeConfig.init();
		File sourceDir = new File(path);
		RdfUtil.loadTurtle(sourceDir, graph, shapeManager);
		
	}
	
	@Test
	public void testKitchenSink() throws Exception {
		load("src/test/resources/sql-generator/omcs-kitchen-sink");
		
		URI shapeId = iri("http://example.com/shapes/PersonShape");
		Shape shape = shapeManager.getShapeById(shapeId);
		
		assertTrue(shape!=null);
		
		SqlTable table = generator.generateTable(shape);
		
		assertTrue(table!=null);
		
		assertField(table, "floatProperty", FacetedSqlDatatype.SIGNED_FLOAT);
	
		
		System.out.println(table.toString());
		
//		fail("Not yet implemented");
	}

	private void assertStringField(SqlTable table, String columnName, int length) {

		SqlColumn c = table.getColumnByName(columnName);
		assertTrue(c != null);
		assertTrue(c.getDatatype() instanceof StringSqlDatatype);
		StringSqlDatatype type = (StringSqlDatatype) c.getDatatype();
		assertEquals(length, type.getMaxLength());
		
	}

	private void assertField(SqlTable table, String columnName, FacetedSqlDatatype datatype) {
		SqlColumn c = table.getColumnByName(columnName);
		assertTrue(c != null);
		assertEquals(datatype, c.getDatatype());
		
	}

}
