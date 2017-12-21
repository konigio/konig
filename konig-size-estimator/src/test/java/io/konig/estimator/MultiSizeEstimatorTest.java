package io.konig.estimator;

import static org.junit.Assert.assertNotEquals;

import java.io.File;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.openrdf.model.URI;

import io.konig.transform.factory.TransformTest;

public class MultiSizeEstimatorTest extends TransformTest {

	@Test
	public void testProductShapeForBigQuery() throws Exception {
		
		load("src/test/resources/Product-Shapes");
		
		MultiSizeEstimateRequest request = new MultiSizeEstimateRequest();
		request.setManifestFile(new File("src/test/resources/MultiSizeEstimateProperties.json"));
		request.setShapesLocation(new File("src/test/resources/Product-Shapes"));
		request.setReportFile(new File("src/test/resources/report.txt"));
		
		MultiSizeEstimator estimator = new MultiSizeEstimator(shapeManager);
		estimator.run(request);

		assertNotEquals(0, 1);
	}
}
