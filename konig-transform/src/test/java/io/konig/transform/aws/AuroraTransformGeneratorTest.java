package io.konig.transform.aws;

/*
 * #%L
 * Konig Transform
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


import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openrdf.model.URI;

import io.konig.aws.datasource.AwsShapeConfig;
import io.konig.core.OwlReasoner;
import io.konig.core.impl.RdfUtil;
import io.konig.core.io.ShapeFileFactory;
import io.konig.shacl.Shape;
import io.konig.sql.query.InsertStatement;
import io.konig.transform.factory.ShapeRuleFactory;
import io.konig.transform.factory.TransformTest;
import io.konig.transform.proto.AwsAuroraChannelFactory;
import io.konig.transform.proto.DataChannelFactory;
import io.konig.transform.proto.ShapeModelFactory1;
import io.konig.transform.proto.ShapeModelToShapeRule;
import io.konig.transform.rule.ShapeRule;
import io.konig.transform.sql.factory.SqlFactory;

public class AuroraTransformGeneratorTest extends TransformTest {
	
	private AuroraTransformGenerator generator;
	private SqlFactory sqlFactory;
	private ShapeRuleFactory shapeRuleFactory;
	
	@Before
	public void setUp() {
		AwsShapeConfig.init();
		ShapeFileFactory fileFactory = new MockFileFactory();
		sqlFactory = new SqlFactory();
		ShapeModelFactory1 shapeModelFactory;
		OwlReasoner reasoner = new OwlReasoner(graph);
		ShapeModelToShapeRule shapeModelToShapeRule = new ShapeModelToShapeRule();
		DataChannelFactory dataChannelFactory = new AwsAuroraChannelFactory();
		shapeModelFactory = new ShapeModelFactory1(shapeManager, dataChannelFactory, reasoner);
		shapeRuleFactory = new ShapeRuleFactory(shapeManager, shapeModelFactory, shapeModelToShapeRule);
		generator = new AuroraTransformGenerator(shapeRuleFactory, sqlFactory, fileFactory,new File("target/test/AuroraTransformGeneratorTest"));
	}

	@Test
	public void testVisit() throws Exception {
		
		load("src/test/resources/konig-transform/aurora-transform");
		
		URI shapeId = iri("http://example.com/shapes/TargetPersonShape");
		Shape shape = shapeManager.getShapeById(shapeId);
		
		generator.visit(shape);
		
		ShapeRule shapeRule = shapeRuleFactory.createShapeRule(shape);
		InsertStatement statement = sqlFactory.insertStatement(shapeRule);
		String path="target/test/AuroraTransformGeneratorTest/" + RdfUtil.localName(shape.getId()) + ".sql";		
		String out = String.join("\n", Files.readAllLines(Paths.get(path)));
		assertTrue(!out.isEmpty());
		assertTrue(out.equals(statement.toString()));
	}
	
	private static class MockFileFactory implements ShapeFileFactory {

		@Override
		public File createFile(Shape shape) {
			
			String path = "target/test/AuroraTransformGeneratorTest/" + RdfUtil.localName(shape.getId()) + ".sql";
			return new File(path);
		}
		
	}

}
