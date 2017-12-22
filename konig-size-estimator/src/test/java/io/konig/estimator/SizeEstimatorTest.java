package io.konig.estimator;

/*
 * #%L
 * Konig Size Estimator
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

import static org.junit.Assert.assertNotEquals;

import java.io.File;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.openrdf.model.URI;

import io.konig.transform.factory.TransformTest;

public class SizeEstimatorTest extends TransformTest {

	@Ignore
	public void testBasicProductShapeForBigQuery() throws Exception {

		load("src/test/resources/Product-BasicShapes");

		URI shapeId = iri("http://example.com/shapes/BqProductShape");

		SizeEstimator estimator = new SizeEstimator(shapeManager);
		List<DataSourceSizeEstimate> list = estimator.averageSize(
				new SizeEstimateRequest(shapeId, new File("src/test/resources/Product-BasicShapes/input")));

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
		List<DataSourceSizeEstimate> list = estimator.averageSize(
				new SizeEstimateRequest(shapeId, new File("src/test/resources/Product-Shapes/ProductInput")));

		StringBuffer buffer = new StringBuffer();
		buffer.append("BigQuery Table: ");
		buffer.append(shapeManager.getShapeById(shapeId).getTargetClass().getLocalName());
		buffer.append("\n");

		for (DataSourceSizeEstimate estimate : list) {

			buffer.append("Number of Records in sample Files: ");
			buffer.append(estimate.getRecordCount());
			buffer.append("\n");

			buffer.append("Total size (in bytes): ");
			buffer.append(estimate.getSizeSum());
			buffer.append("\n");

			buffer.append("Average record size (in bytes): ");
			buffer.append(estimate.averageSize());
			buffer.append("\n");

			System.out.println(buffer);
			
			buffer.append("-------------------------------------------\n");
		}

		assertNotEquals(0, list.get(0).averageSize());
	}
	
	@Test
	public void testCategoryShapeForBigQuery() throws Exception {

		load("src/test/resources/Product-Shapes");

		URI shapeId = iri("http://example.com/shapes/BqProductCategoryShape");

		SizeEstimator estimator = new SizeEstimator(shapeManager);
		List<DataSourceSizeEstimate> list = estimator.averageSize(
				new SizeEstimateRequest(shapeId, new File("src/test/resources/Product-Shapes/ProductCategoryInput")));

		StringBuffer buffer = new StringBuffer();
		buffer.append("BigQuery Table: ");
		buffer.append(shapeManager.getShapeById(shapeId).getTargetClass().getLocalName());
		buffer.append("\n");

		for (DataSourceSizeEstimate estimate : list) {

			buffer.append("Number of Records in sample Files: ");
			buffer.append(estimate.getRecordCount());
			buffer.append("\n");

			buffer.append("Total size (in bytes): ");
			buffer.append(estimate.getSizeSum());
			buffer.append("\n");

			buffer.append("Average record size (in bytes): ");
			buffer.append(estimate.averageSize());
			buffer.append("\n");

			System.out.println(buffer);
			
			buffer.append("-------------------------------------------\n");
		}

		assertNotEquals(0, list.get(0).averageSize());
	}
}
