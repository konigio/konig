package com.google.cloud.bigquery;

/*
 * #%L
 * Konig GCP Common
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

import java.io.File;
import java.io.FileReader;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.jackson2.JacksonFactory;

import io.konig.gcp.common.ReplaceStringsReader;

public class KonigBigQueryUtilTest {
	
	
	@Test
	public void testCreateTableInfo() throws Exception {
		JsonFactory factory = JacksonFactory.getDefaultInstance();
		JsonObjectParser parser = factory.createJsonObjectParser();
		File file = new File("src/test/resources/KonigBigQueryUtilTest/schema.OriginMusicAlbumShape.json");
		
		FileReader reader = new FileReader(file);
		ReplaceStringsReader input = new ReplaceStringsReader(
			reader, "${gcpProjectId}", "my-project", "${gcpBucketSuffix}", "dev");
		com.google.api.services.bigquery.model.Table model = 
			parser.parseAndClose(input, com.google.api.services.bigquery.model.Table.class);
		
		

		TableInfo info = KonigBigQueryUtil.createTableInfo(model);
		
		TableDefinition definition = info.getDefinition();
		
		assertTrue(definition instanceof ExternalTableDefinition);
		ExternalTableDefinition external = (ExternalTableDefinition) definition;
		
		List<String> list = external.getSourceUris();
		assertEquals(1, list.size());
		
		
		String uri = list.get(0);
		assertEquals("gs://originmusicalbumshape-dev/*", uri);

		List<Field> fieldList = definition.getSchema().getFields();
		assertEquals(3, fieldList.size());
		
		Field artistId = fieldList.get(2);
		assertEquals("INTEGER", artistId.getType().getValue().name());
	}

}
