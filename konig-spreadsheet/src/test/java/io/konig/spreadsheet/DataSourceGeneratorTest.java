package io.konig.spreadsheet;

/*
 * #%L
 * Konig Spreadsheet
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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

import io.konig.core.NamespaceManager;
import io.konig.core.impl.MemoryNamespaceManager;
import io.konig.core.vocab.Schema;
import io.konig.gcp.datasource.BigQueryTableReference;
import io.konig.gcp.datasource.GcpShapeConfig;
import io.konig.gcp.datasource.GoogleBigQueryTable;
import io.konig.shacl.Shape;
import io.konig.shacl.impl.MemoryShapeManager;

public class DataSourceGeneratorTest {
	private MemoryShapeManager shapeManager = new MemoryShapeManager();

	@Test
	public void test() throws Exception {
		
		GcpShapeConfig.init();
		
		File templateDir = new File("target/WorkbookLoader");
		
		Properties properties = new Properties();
		properties.load(getClass().getClassLoader().getResourceAsStream("WorkbookLoader/settings.properties"));
		
		properties.put("gcpProjectId", "warehouse");
		
		NamespaceManager nsManager = new MemoryNamespaceManager();
		nsManager.add("schema", Schema.NAMESPACE);

		Shape shape = new Shape(uri("http://example.com/shapes/PersonOriginShape"));
		shape.setTargetClass(Schema.Person);
		
		shapeManager.addShape(shape);
		
		DataSourceGenerator generator = new DataSourceGenerator(nsManager, templateDir, properties);
		generator.generate(shape, "BigQueryTable", shapeManager);
		
		List<GoogleBigQueryTable> tableList = shape.getShapeDataSource().stream()
				.filter(s -> s instanceof GoogleBigQueryTable)
				.map(s -> (GoogleBigQueryTable)s)
				.collect(Collectors.toList());
		
		assertEquals(1, tableList.size());
		
		GoogleBigQueryTable table = tableList.get(0);
		
		assertEquals(uri("https://www.googleapis.com/bigquery/v2/projects/warehouse/datasets/schema/tables/Person"), table.getId());
		
		
		BigQueryTableReference tableRef = table.getTableReference();
		assertTrue(tableRef != null);
		
		assertEquals(tableRef.getProjectId(), "warehouse");
		assertEquals(tableRef.getDatasetId(), "schema");
		assertEquals(tableRef.getTableId(), "Person");
	}


	private URI uri(String value) {
		return new URIImpl(value);
	}

}
