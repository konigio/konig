package io.konig.schemagen.gcp;

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


import static org.junit.Assert.*;

import java.io.File;

import org.junit.Test;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.RDFS;

import info.aduna.io.IOUtil;
import io.konig.core.Graph;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.vocab.Schema;
import io.konig.gcp.datasource.BigQueryTableReference;
import io.konig.gcp.datasource.GoogleBigQueryTable;
import io.konig.maven.FileUtil;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeBuilder;

public class BigQueryLabelGeneratorTest {

	@Test
	public void test() throws Exception {
		
		ShapeBuilder builder = new ShapeBuilder();
		URI shapeId = uri("http://example.com/shapes/PersonShape");
		
		builder.beginShape(shapeId)
			.beginProperty(Schema.givenName)
			.endProperty()
			.beginProperty(Schema.address)
				.beginValueShape(uri("http://example.com/shapes/PostalAddressShape"))
					.beginProperty(Schema.postalCode)
					.endProperty()
				.endValueShape()
			.endProperty()
		.endShape();
		
		Graph graph = new MemoryGraph();
		graph.edge(Schema.givenName, RDFS.LABEL, langString("Given Name", "en"));
		graph.edge(Schema.givenName, RDFS.LABEL, langString("Nombre de Pila", "es"));
		graph.edge(Schema.address, RDFS.LABEL, langString("Address", "en"));
		graph.edge(Schema.address, RDFS.LABEL, langString("Direcci\u00F3n", "es"));
		graph.edge(Schema.postalCode, RDFS.LABEL, langString("Postal Code", "en"));
		graph.edge(Schema.postalCode, RDFS.LABEL, langString("C\u00F3digo Postal", "es"));
		
		File baseDir =  new File("target/test/BigQueryLabelGeneratorTest");
		File schemaFile = new File(baseDir, "schema.json");
		File dataFile = new File(baseDir, "data.json");
		FileUtil.delete(baseDir);
		
		Shape shape = builder.getShape(shapeId);
		GoogleBigQueryTable table = new GoogleBigQueryTable();
		table.setTableReference(new BigQueryTableReference("{gcpProjectId}", "schema", "Person"));
		shape.addShapeDataSource(table);
		
		BigQueryLabelGenerator generator = new BigQueryLabelGenerator(graph, schemaFile, dataFile, "metadata");
		generator.beginShapeTraversal();
		generator.visit(shape);
		generator.endShapeTraversal();
		
		String actual = IOUtil.readString(dataFile).replace("\r", "");
		String expected = 
			"{\"tableName\":\"schema.Person\",\"fieldName\":\"givenName\",\"label\":\"Given Name\",\"language\":\"en\"}\n" + 
			" {\"tableName\":\"schema.Person\",\"fieldName\":\"givenName\",\"label\":\"Nombre de Pila\",\"language\":\"es\"}\n" + 
			" {\"tableName\":\"schema.Person\",\"fieldName\":\"address\",\"label\":\"Address\",\"language\":\"en\"}\n" + 
			" {\"tableName\":\"schema.Person\",\"fieldName\":\"address\",\"label\":\"Direcci\\u00F3n\",\"language\":\"es\"}\n" + 
			" {\"tableName\":\"schema.Person\",\"fieldName\":\"postalCode\",\"label\":\"Postal Code\",\"language\":\"en\"}\n" + 
			" {\"tableName\":\"schema.Person\",\"fieldName\":\"postalCode\",\"label\":\"C\\u00F3digo Postal\",\"language\":\"es\"}\n";
		
		assertEquals(expected, actual);
		
	}

	private Literal langString(String label, String language) {
		return new LiteralImpl(label, language);
	}

	private URI uri(String value) {
		
		return new URIImpl(value);
	}

}
