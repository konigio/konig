package io.konig.transform.aws;

import static org.junit.Assert.fail;

import java.io.File;

import org.junit.Before;
import org.junit.Test;
import org.openrdf.model.URI;

import io.konig.aws.datasource.AwsShapeConfig;
import io.konig.core.OwlReasoner;
import io.konig.core.impl.RdfUtil;
import io.konig.core.io.ShapeFileFactory;
import io.konig.shacl.Shape;
import io.konig.transform.factory.ShapeRuleFactory;
import io.konig.transform.factory.TransformTest;
import io.konig.transform.proto.AwsAuroraChannelFactory;
import io.konig.transform.proto.DataChannelFactory;
import io.konig.transform.proto.ShapeModelFactory;
import io.konig.transform.proto.ShapeModelToShapeRule;
import io.konig.transform.sql.factory.SqlFactory;

public class AuroraTransformGeneratorTest extends TransformTest {
	
	private AuroraTransformGenerator generator;
	
	@Before
	public void setUp() {
		AwsShapeConfig.init();
		ShapeFileFactory fileFactory = new MockFileFactory();
		SqlFactory sqlFactory = new SqlFactory();
		ShapeModelFactory shapeModelFactory;
		ShapeRuleFactory shapeRuleFactory;
		OwlReasoner reasoner = new OwlReasoner(graph);
		ShapeModelToShapeRule shapeModelToShapeRule = new ShapeModelToShapeRule();
		DataChannelFactory dataChannelFactory = new AwsAuroraChannelFactory();
		shapeModelFactory = new ShapeModelFactory(shapeManager, dataChannelFactory, reasoner);
		shapeRuleFactory = new ShapeRuleFactory(shapeManager, shapeModelFactory, shapeModelToShapeRule);
		generator = new AuroraTransformGenerator(shapeRuleFactory, sqlFactory, fileFactory);
	}

	@Test
	public void testVisit() throws Exception {
		
		load("src/test/resources/konig-transform/aurora-transform");
		
		URI shapeId = iri("http://example.com/shapes/TargetPersonShape");
		Shape shape = shapeManager.getShapeById(shapeId);
		
		generator.visit(shape);
		
		// TODO: Add test assertions here
		fail("Not yet implemented");
	}
	
	private static class MockFileFactory implements ShapeFileFactory {

		@Override
		public File createFile(Shape shape) {
			
			String path = "target/test/AuroraTransformGeneratorTest/" + RdfUtil.localName(shape.getId()) + ".sql";
			return new File(path);
		}
		
	}

}
