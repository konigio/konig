package io.konig.schemagen.sql;

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
import java.io.IOException;

import org.junit.Test;
import static org.junit.Assert.*;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;

import io.konig.core.Graph;
import io.konig.core.NamespaceManager;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.MemoryNamespaceManager;
import io.konig.core.impl.RdfUtil;
import io.konig.gcp.datasource.GcpShapeConfig;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeManager;
import io.konig.shacl.impl.MemoryShapeManager;

public class SqlTableGeneratorTest {

	protected NamespaceManager nsManager = new MemoryNamespaceManager();
	protected Graph graph = new MemoryGraph(nsManager);
	protected ShapeManager shapeManager = new MemoryShapeManager();
	
	private SqlTableGenerator generator = new SqlTableGenerator();

	

	protected URI iri(String value) {
		return new URIImpl(value);
	}

	protected void load(String path) throws RDFParseException, RDFHandlerException, IOException {

		GcpShapeConfig.init();
		File sourceDir = new File(path);
		RdfUtil.loadTurtle(sourceDir, graph, shapeManager);
		
	}


	@Test
	public void testKitchenSink() throws Exception {
		load("src/test/resources/sql-generator/sql-kitchen-sink");
		
		URI shapeId = iri("http://example.com/shapes/ThingShape");
		Shape shape = shapeManager.getShapeById(shapeId);
		assertTrue(shape!=null);
		
		SqlTable table = generator.generateTable(shape);
		
		assertTrue(table!=null);
		
		assertField(table, "booleanProperty", FacetedSqlDatatype.BOOLEAN);
		assertField(table, "bitProperty", FacetedSqlDatatype.BIT);
		assertField(table, "unsignedTinyInt", FacetedSqlDatatype.UNSIGNED_TINYINT);
		assertField(table, "signedTinyInt", FacetedSqlDatatype.SIGNED_TINYINT);
		assertField(table, "unsignedSmallInt", FacetedSqlDatatype.UNSIGNED_SMALLINT);
		assertField(table, "signedSmallInt", FacetedSqlDatatype.SIGNED_SMALLINT);
		assertField(table, "unsignedMediumInt", FacetedSqlDatatype.UNSIGNED_MEDIUMINT);
		assertField(table, "signedMediumInt", FacetedSqlDatatype.SIGNED_MEDIUMINT);
		assertField(table, "unsignedInt", FacetedSqlDatatype.UNSIGNED_INT);
		assertField(table, "signedInt", FacetedSqlDatatype.SIGNED_INT);
		assertField(table, "date", FacetedSqlDatatype.DATE);
		assertField(table, "dateTime", FacetedSqlDatatype.DATETIME);
		assertStringField(table, "text", 100000);
		assertStringField(table, "char", 32);
		assertStringField(table, "varchar", 200);
		assertField(table, "float", FacetedSqlDatatype.SIGNED_FLOAT);
		assertField(table, "double", FacetedSqlDatatype.SIGNED_DOUBLE);
		
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
