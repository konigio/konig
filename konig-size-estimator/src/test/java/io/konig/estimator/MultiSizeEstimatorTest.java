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
import static org.junit.Assert.assertTrue;

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

		assertTrue(request.getReportFile().exists());
	}
}
