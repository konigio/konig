package io.konig.core.util;

/*
 * #%L
 * Konig Core
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


import static org.junit.Assert.*;

import org.junit.Ignore;
import org.junit.Test;

public class RecursiveValueFormatTest {

	@Test
	public void test() {
		
		RecursiveValueFormat template = new RecursiveValueFormat();
		
		template.put("bigQueryTableId", "{classLocalName}");
		template.compile("Staging{bigQueryTableId}Bucket");
		
		SimpleValueMap map = new SimpleValueMap();
		map.put("classLocalName", "Person");
		
		String value = template.format(map);
		
		assertEquals("StagingPersonBucket", value);
		
	}
	

	@Test
	public void test2() {
		
		RecursiveValueFormat template = new RecursiveValueFormat();
		
		template.put("gcpBucketName", "{shapeLocalNameLowercase}.{gcpBucketSuffix}");
		template.put("gcpBucketSuffix", "example.com");
		template.compile("<gs://{gcpBucketName}>");
		
		SimpleValueMap map = new SimpleValueMap();
		map.put("classLocalName", "Person");
		map.put("shapeLocalNameLowercase", "personshape");
		
		String value = template.format(map);
		
		assertEquals("<gs://personshape.example.com>", value);
		
	}

}
