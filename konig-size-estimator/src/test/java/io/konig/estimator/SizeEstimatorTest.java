package io.konig.estimator;
import static org.junit.Assert.assertNotEquals;

import java.io.File;
import java.util.List;

import org.junit.Test;
import org.openrdf.model.URI;

import io.konig.transform.factory.TransformTest;

public class SizeEstimatorTest extends TransformTest {
	
	@Test
	public void testCSVForProduct() throws Exception {
		
		load("src/test/resources/Product-Shapes");
		
		URI shapeId = iri("http://example.com/shapes/BqProductShape");
				
		SizeEstimator estimator = new SizeEstimator(shapeManager);
		List<DataSourceSizeEstimate> list = estimator.averageSize(new SizeEstimateRequest(shapeId, new File("src/test/resources/Product-Shapes/input")));
		
		assertNotEquals(0, list.get(0).averageSize());
	}
}
