package io.konig.schemagen.gcp;

import java.io.File;

/*
 * #%L
 * Konig Schema Generator
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
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

import io.konig.core.impl.MemoryNamespaceManager;
import io.konig.datasource.DataSource;
import io.konig.gcp.datasource.BigQueryTableReference;
import io.konig.gcp.datasource.GoogleBigQueryTable;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeManager;
import io.konig.shacl.impl.MemoryShapeManager;
import io.konig.shacl.impl.SimpleShapeMediaTypeNamer;
import io.konig.shacl.io.ShapeLoader;

public class GoogleAnalyticsUdfGeneratorTest {
	@Test
	public void testUDF() {
		MemoryShapeManager shapeManager = new MemoryShapeManager();
		MemoryNamespaceManager nsManager = new MemoryNamespaceManager();
		
		ShapeLoader shapeLoader = new ShapeLoader(null, shapeManager, nsManager);
		shapeLoader.loadTurtle(resource("shapes/Organization-x1.ttl"));
		List<Shape> shapes = shapeLoader.getShapeManager().listShapes();
		Shape shape = shapes.get(0);
		
		List<DataSource> shapeDataSource = new ArrayList<DataSource>();
		DataSource ds = new DataSource();
		ds.setId(new URIImpl("http://www.konig.io/ns/core/GoogleAnalytics"));
		URI tableId = new URIImpl("https://example.com/bigquery/PersonTable");
		GoogleBigQueryTable table = new GoogleBigQueryTable();
		table.setId(tableId);
		BigQueryTableReference tableReference = new BigQueryTableReference();
		tableReference.setDatasetId("mypedia-dev-55669");
		tableReference.setTableId("StartAssessment");
		table.setTableReference(tableReference);
		shapeDataSource.add(table);
		shapeDataSource.add(ds);
		shape.setShapeDataSource(shapeDataSource);
		GoogleAnalyticsUdfGenerator generator = new GoogleAnalyticsUdfGenerator(new GoogleAnalyticsShapeFileCreator(new File("src/test/resources/fuctions")),shapeManager);
		generator.visit(shape);
		
	}
	
	private InputStream resource(String path) {
		return getClass().getClassLoader().getResourceAsStream(path);
	}

}
