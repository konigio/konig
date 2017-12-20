package io.konig.estimator;
import static org.junit.Assert.assertNotEquals;

import java.io.File;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.openrdf.model.URI;

import io.konig.transform.factory.TransformTest;

public class SizeEstimatorTest extends TransformTest {
	
	@Test
	public void testBasicProductShapeForBigQuery() throws Exception {
		
		load("src/test/resources/Product-BasicShapes");
		
		URI shapeId = iri("http://example.com/shapes/BqProductShape");
			
		SizeEstimator estimator = new SizeEstimator(shapeManager);
		List<DataSourceSizeEstimate> list = estimator.averageSize(new SizeEstimateRequest(shapeId, new File("src/test/resources/Product-BasicShapes/input")));
		
		
		StringBuffer buffer = new StringBuffer();
		buffer.append("BigQuery Table: ");
		buffer.append(shapeManager.getShapeById(shapeId).getTargetClass().getLocalName());
		buffer.append("\n");
		
		buffer.append("Number of Records in sample Files: ");
		buffer.append(list.get(0).getRecordCount());
		buffer.append("\n");
		
		buffer.append("Total size (in bytes): ");
		buffer.append(list.get(0).getSizeSum());
		buffer.append("\n");
		
		buffer.append("Average record size (in bytes): ");
		buffer.append(list.get(0).averageSize());
		buffer.append("\n");
		
		System.out.println(buffer);
		
		assertNotEquals(0, list.get(0).averageSize());
	}
	
	
	@Ignore
	public void testProductShapeForBigQuery() throws Exception {
		
		load("src/test/resources/Product-Shapes");
		
		URI shapeId = iri("http://example.com/shapes/BqProductShape");
			
		SizeEstimator estimator = new SizeEstimator(shapeManager);
		List<DataSourceSizeEstimate> list = estimator.averageSize(new SizeEstimateRequest(shapeId, new File("src/test/resources/Product-Shapes/input")));
		
		
		StringBuffer buffer = new StringBuffer();
		buffer.append("BigQuery Table: ");
		buffer.append(shapeManager.getShapeById(shapeId).getTargetClass().getLocalName());
		buffer.append("\n");
		
		buffer.append("Number of Records in sample Files: ");
		buffer.append(list.get(0).getRecordCount());
		buffer.append("\n");
		
		buffer.append("Total size (in bytes): ");
		buffer.append(list.get(0).getSizeSum());
		buffer.append("\n");
		
		buffer.append("Average record size (in bytes): ");
		buffer.append(list.get(0).averageSize());
		buffer.append("\n");
		
		System.out.println(buffer);
		
		assertNotEquals(0, list.get(0).averageSize());
	}
}
