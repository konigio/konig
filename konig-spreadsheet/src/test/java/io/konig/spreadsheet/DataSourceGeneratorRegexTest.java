package io.konig.spreadsheet;

/*
 * #%L
 * Konig Spreadsheet
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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

public class DataSourceGeneratorRegexTest {

	@Test
	public void test() {
		 Pattern resourceTypePattern = Pattern.compile("application/vnd[.][^.]+[.](.*)");
		 Matcher matcher = resourceTypePattern.matcher("application/vnd.pearson.edw.staging.person");
		 if (matcher.matches()) {
			 String resourceType = matcher.group(1);
			 assertEquals("edw.staging.person", resourceType);
		 } else {
			 fail("No match");
		 }
	}

}
