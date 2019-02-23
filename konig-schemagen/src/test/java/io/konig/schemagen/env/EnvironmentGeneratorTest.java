package io.konig.schemagen.env;

/*
 * #%L
 * Konig Schema Generator
 * %%
 * Copyright (C) 2015 - 2019 Gregory McFall
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


import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import io.konig.maven.FileUtil;

public class EnvironmentGeneratorTest {

	@Test
	public void test() throws Exception {
		
		File settingsDir = new File("src/test/resources/EnvironmentGeneratorTest/settings");
		File sourceDir = new File("src/test/resources/EnvironmentGeneratorTest/source");
		File targetDir = new File("target/test/EnvironmentGeneratorTest");
		File velocityLog = new File("target/test/velocity.log");
		
		FileUtil.delete(targetDir);
		
		EnvironmentGenerator generator = new EnvironmentGenerator(velocityLog);
		generator.run(settingsDir, sourceDir, targetDir);
		
		assertFileContents(new File(targetDir, "test/gcp/deployment/config.yaml"), "edw-test");
		
	}

	private void assertFileContents(File file, String expected) throws IOException {
		
		String actual = FileUtil.readString(file);
		assertEquals(expected, actual);
		
	}

}
