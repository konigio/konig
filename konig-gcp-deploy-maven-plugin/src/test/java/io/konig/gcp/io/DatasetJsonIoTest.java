package io.konig.gcp.io;

/*
 * #%L
 * Konig GCP Deployment Maven Plugin
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


import static org.junit.Assert.assertEquals;

import java.io.StringReader;
import java.io.StringWriter;

import org.junit.Test;

import com.google.api.services.bigquery.model.Dataset;
import com.google.api.services.bigquery.model.DatasetReference;

public class DatasetJsonIoTest {

	@Test
	public void test() throws Exception {
		
		// Create a dataset POJO
		Dataset dataset = new Dataset();
		DatasetReference ref = new DatasetReference();
		ref.setDatasetId("someDataset");
		ref.setProjectId("someProject");
		dataset.setDatasetReference(ref);
		dataset.setFriendlyName("My Test Dataset");
	
		
		// Write the dataset instance to a JSON string
		
		DatasetJsonWriter writer = new DatasetJsonWriter();
		StringWriter out = new StringWriter();
		
		writer.writeJson(dataset, out);
		String jsonValue = out.toString();
		
		// Parse the JSON string back into a Dataset POJO
		
		StringReader reader = new StringReader(jsonValue);
		DatasetJsonParser parser = new DatasetJsonParser();
		Dataset parsedDataset = parser.parse(reader);
		
		// Verified that the parsed Dataset has the correct properties.
		
		assertEquals("someProject", parsedDataset.getDatasetReference().getProjectId());
		assertEquals("someDataset", parsedDataset.getDatasetReference().getDatasetId());
		assertEquals("My Test Dataset", parsedDataset.getFriendlyName());
	}

}
