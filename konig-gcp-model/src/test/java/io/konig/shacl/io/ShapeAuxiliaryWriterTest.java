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


import static org.junit.Assert.*;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.URIImpl;

import io.konig.core.Graph;
import io.konig.core.Vertex;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.MemoryNamespaceManager;
import io.konig.core.impl.RdfUtil;
import io.konig.core.project.Project;
import io.konig.core.project.ProjectFile;
import io.konig.core.vocab.Konig;
import io.konig.gcp.datasource.GcpShapeConfig;
import io.konig.gcp.datasource.GoogleBigQueryTable;
import io.konig.shacl.Shape;

public class ShapeAuxiliaryWriterTest {

	@Test
	public void test() throws Exception {
		GcpShapeConfig.init();
		
		
		URI shapeId = uri("http://example.com/shape/PersonShape");
		Shape shape = new Shape(shapeId);
		GoogleBigQueryTable bigquery = new GoogleBigQueryTable();
		shape.addShapeDataSource(bigquery);
		URI tableId = uri("http://example.com/bigquery/Person");
		bigquery.setId(tableId);
		
		URI projectId = uri("http://example.com/project/Directory");
		File projectBaseDir = new File("target/test/ShapeAuxiliaryWriterTest");
		
		Project project = new Project(projectId, projectBaseDir);
		ProjectFile projectFile = project.createProjectFile("gcp/bigquery/Person-ddl.sql");
		
		bigquery.setDdlFile(projectFile);
		
		ShapeAuxiliaryWriter writer = new ShapeAuxiliaryWriter(new File("target/test/ShapeAuxiliaryWriterTest/rdf/ddlFiles.ttl"));
		
		writer.addShapeEmitter(new DdlFileEmitter(Konig.GoogleBigQueryTable));
		
		List<Shape> shapeList = Arrays.asList(shape);
		
		writer.writeAll(MemoryNamespaceManager.getDefaultInstance(), shapeList);
		
		Graph graph = new MemoryGraph(new MemoryNamespaceManager());
		RdfUtil.loadTurtle(projectBaseDir, graph);
		
		Vertex tableNode = graph.getVertex(tableId);
		assertTrue(tableNode != null);
		
		Vertex fileNode = tableNode.getVertex(Konig.ddlFile);
		assertTrue(fileNode != null);
		
		Value actualProjectId = fileNode.getValue(Konig.baseProject);
		assertTrue(actualProjectId != null);
		assertEquals(projectId.stringValue(), actualProjectId.stringValue());
		
		Value actualRelativePath = fileNode.getValue(Konig.relativePath);
		assertTrue(actualRelativePath != null);
		assertEquals(projectFile.getRelativePath(), actualRelativePath.stringValue());
		
	}

	private URI uri(String value) {
		return new URIImpl(value);
	}

}
