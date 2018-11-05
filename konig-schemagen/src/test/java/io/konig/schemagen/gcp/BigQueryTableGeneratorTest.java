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
import static org.junit.Assert.fail;

import java.util.List;
import java.util.Optional;

import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.RDF;

import com.google.api.services.bigquery.model.TableFieldSchema;
import com.google.api.services.bigquery.model.TableSchema;

import io.konig.core.vocab.Schema;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeBuilder;

public class BigQueryTableGeneratorTest {
	
	private ShapeBuilder builder = new ShapeBuilder();
	private BigQueryTableGenerator generator = new BigQueryTableGenerator();

	@Test
	public void testLangString() {
		
		URI shapeId = uri("http://example.com/shapes/ProductShape");
		
		builder
		.beginShape(shapeId)
			.beginProperty(Schema.name)
				.maxCount(1)
				.datatype(RDF.LANGSTRING)
			.endProperty()
		.endShape();
		
		Shape shape = builder.getShape(shapeId);
		
		TableSchema table = generator.toTableSchema(shape);
		
		TableFieldSchema name = field(table.getFields(), "name");
		
		assertEquals("RECORD", name.getType());
		
		TableFieldSchema stringValue = field(name.getFields(), "stringValue");
		assertEquals("STRING", stringValue.getType());
		assertEquals("REQUIRED", stringValue.getMode());
		
		TableFieldSchema languageCode = field(name.getFields(), "languageCode");
		assertEquals("STRING", languageCode.getType());
		assertEquals("REQUIRED", languageCode.getMode());
	}

	private TableFieldSchema field(List<TableFieldSchema> fieldList, String fieldName) {
		Optional<TableFieldSchema> result = fieldList.stream().filter(f -> fieldName.equals(f.getName())).findAny();
		assertTrue(result.isPresent());
		return result.get();
	}

	private URI uri(String value) {
		return new URIImpl(value);
	}

}
