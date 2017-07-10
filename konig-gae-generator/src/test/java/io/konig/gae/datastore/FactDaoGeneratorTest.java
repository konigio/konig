package io.konig.gae.datastore;

/*
 * #%L
 * Konig GAE Generator
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


import java.io.File;

import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.XMLSchema;

import com.sun.codemodel.JCodeModel;

import io.konig.core.NamespaceManager;
import io.konig.core.impl.MemoryNamespaceManager;
import io.konig.core.util.BasicJavaDatatypeMapper;
import io.konig.core.util.JavaDatatypeMapper;
import io.konig.core.vocab.Konig;
import io.konig.core.vocab.Schema;
import io.konig.core.vocab.TIME;
import io.konig.gae.datastore.impl.SimpleEntityNamer;
import io.konig.schemagen.java.BasicJavaNamer;
import io.konig.schemagen.java.JavaNamer;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeBuilder;
import io.konig.shacl.ShapeManager;

public class FactDaoGeneratorTest {

	@Test
	public void testGenerateDao() throws Exception {
		
		NamespaceManager nsManager = new MemoryNamespaceManager();
		nsManager.add("fact", "http://example.com/ns/fact/");
		nsManager.add("schema", Schema.NAMESPACE);
		nsManager.add("time", TIME.NAMESPACE);
		nsManager.add("konig", Konig.NAMESPACE);
		
		EntityNamer entityNamer = new SimpleEntityNamer();
		JavaNamer javaNamer = new BasicJavaNamer("com.example", nsManager);
		DaoNamer daoNamer = new SimpleDaoNamer("com.example.gae.datastore", nsManager);
		URI shapeId = uri("http://example.com/shape/v1/fact/LoginTotalCountBySchoolShape");
		URI factClass = uri("http://example.com/ns/fact/LoginTotalCountBySchool");
		URI schoolProperty = uri("http://example.com/ns/alias/school");
		URI intervalShapeId = uri("http://example.com/shape/v1/konig/TimeIntervalShape");
		
		
		ShapeBuilder builder = new ShapeBuilder();
		builder.beginShape(shapeId)
			.targetClass(factClass)
			.beginProperty(Konig.totalCount)
				.datatype(XMLSchema.INT)
				.stereotype(Konig.measure)
				.minCount(1)
				.maxCount(1)
			.endProperty()
			.beginProperty(schoolProperty)
				.valueClass(Schema.School)
				.stereotype(Konig.dimension)
				.minCount(1)
				.maxCount(1)
			.endProperty()
			.beginProperty(Konig.timeInterval)
				.valueShape(intervalShapeId)
				.stereotype(Konig.dimension)
				.minCount(1)
				.maxCount(1)
			.endProperty()
		.endShape()
		.beginShape(intervalShapeId)
			.targetClass(Konig.TimeInterval)
			
			.beginProperty(Konig.intervalStart)
				.datatype(XMLSchema.DATE)
				.stereotype(Konig.dimension)
				.minCount(1)
				.maxCount(1)
			.endProperty()

			.beginProperty(Konig.durationUnit)
				.valueClass(TIME.TemporalUnit)
				.stereotype(Konig.dimension)
				.minCount(1)
				.maxCount(1)
			.endProperty()
			
		.endShape();
		
		ShapeManager shapeManager = builder.getShapeManager();
		Shape shape = shapeManager.getShapeById(shapeId);
		JavaDatatypeMapper datatypeMapper = new BasicJavaDatatypeMapper();

		JCodeModel model = new JCodeModel();
		
		FactDaoGenerator generator = new FactDaoGenerator()
			.setDaoNamer(daoNamer)
			.setDatatypeMapper(datatypeMapper)
			.setEntityNamer(entityNamer)
			.setJavaNamer(javaNamer)
			.setFindByDimensionOnly(false)
			.setShapeManager(shapeManager);
		
		generator.generateDao(shape, model);
		


		File file = new File("target/test/DatastoreDaoGenerator/generateDao");
		file.mkdirs();
		model.build(file);
		
	}
	
	private URI uri(String value) {
		return new URIImpl(value);
	}

}
