package io.konig.data.app.generator;

/*
 * #%L
 * Konig Data App Generator
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


import static org.junit.Assert.*;

import java.io.File;

import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

import io.konig.core.NamespaceManager;
import io.konig.core.impl.MemoryNamespaceManager;
import io.konig.core.util.IOUtil;
import io.konig.dao.core.DaoConstants;
import io.konig.gcp.datasource.BigQueryTableReference;
import io.konig.gcp.datasource.GoogleBigQueryTable;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeManager;
import io.konig.shacl.impl.MemoryShapeManager;

public class EntityStructureWorkerTest {

	@Test
	public void test() throws Exception {
		
		NamespaceManager nsManager = new MemoryNamespaceManager();
		nsManager.add("shape", "http://example.com/shapes/");
		ShapeManager shapeManager = new MemoryShapeManager();
		
		GoogleBigQueryTable table = new GoogleBigQueryTable();
		table.setId(uri("http://example.com/bigquery/person"));
		table.setTableReference(new BigQueryTableReference("{gcpProjectId}", "schema", "Person"));
		
		Shape shape = new Shape(uri("http://example.com/shapes/PersonShape"));
		shape.setMediaTypeBaseName("application/vnd.example.person");
		shape.addShapeDataSource(table);
		shapeManager.addShape(shape);
		
		File baseDir = new File("target/test/EntityStructureWorkerTest");
		IOUtil.recursiveDelete(baseDir);
		baseDir.mkdirs();
		
		EntityStructureWorker worker = new EntityStructureWorker(nsManager, shapeManager, baseDir);
		
		worker.run();
		
		File mediaTypeMapFile = new File(baseDir, DaoConstants.MEDIA_TYPE_MAP_FILE_NAME);
		assertTrue(mediaTypeMapFile.exists());
		
		String text = IOUtil.stringContent(mediaTypeMapFile);
		assertTrue(text.contains("application/vnd.example.person,http://example.com/shapes/PersonShape"));
	}

	private URI uri(String value) {
		return new URIImpl(value);
	}

}
