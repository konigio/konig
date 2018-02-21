package io.konig.transform.mysql;

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


import java.io.File;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import io.konig.transform.proto.AbstractShapeModelToShapeRuleTest;

public class MySqlTransformGeneratorTest extends AbstractShapeModelToShapeRuleTest  {

	File outDir = new File("target/test/mysql-transform");
	MySqlTransformGenerator generator = new MySqlTransformGenerator(shapeManager, outDir, owlReasoner);
	
	@Test
	public void testLoad() throws Throwable {
		FileUtils.deleteDirectory(outDir);
		load("src/test/resources/konig-transform/mysql-transform");
		generator.generateAll();
		
		List<Throwable> errorList = generator.getErrorList();
		if (errorList != null && !errorList.isEmpty()) {
			throw errorList.get(0);
		}
	}
}
