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


import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;
import org.openrdf.model.Resource;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.XMLSchema;

import io.konig.core.vocab.Konig;
import io.konig.core.vocab.Schema;
import io.konig.gcp.datasource.BigQueryTableReference;
import io.konig.gcp.datasource.GoogleBigQueryTable;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;
import io.konig.sql.runtime.EntityStructure;
import io.konig.sql.runtime.FieldInfo;
import io.konig.sql.runtime.Stereotype;
import io.konig.yaml.Yaml;

public class EntityStructureGeneratorTest {

	@Test
	public void test() throws Exception {
		
		Shape shape = new Shape(uri("http://example.com/shape/WorkShape"));
		shape.setComment("Creative Work");
		PropertyConstraint p = new PropertyConstraint(Schema.dateCreated);
		shape.setMediaTypeBaseName("application/vnd.example.work");
		p.setDatatype(XMLSchema.DATETIME);
		p.setStereotype(Konig.dimension);
		shape.add(p);
		
		GoogleBigQueryTable table = new GoogleBigQueryTable();
		table.setTableReference(new BigQueryTableReference(null, "schema", "CreativeWork"));
		shape.addShapeDataSource(table);
		
		EntityStructureGenerator generator = new EntityStructureGenerator();
		
		EntityStructure e = generator.toEntityStructure(shape);
		
		String yaml = Yaml.toString(e);
		
		EntityStructure e2 = Yaml.read(EntityStructure.class, yaml);
		
		assertEquals("Creative Work", e2.getComment());
		assertEquals("application/vnd.example.work", e2.getMediaTypeBaseName());
		List<FieldInfo> fieldList = e2.getFields();
		assertEquals(1, fieldList.size());
		FieldInfo field = fieldList.get(0);
		assertEquals(XMLSchema.DATETIME, field.getFieldType());
		assertEquals(Stereotype.DIMENSION, field.getStereotype());
	}

	private Resource uri(String value) {
		
		return new URIImpl(value);
	}

}
