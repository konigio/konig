package io.konig.deploy.gcp;

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
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.Properties;

import org.junit.Test;

public class ConfiguratorTest {

	@Test
	public void test() throws Exception {
		
		Properties properties = new Properties();
		properties.put("konig.deploy.gcp.cloudstorage.bucketSuffix", "dev");
		Configurator config = new Configurator(properties);
		
		File gcpDir = new File("src/gcp");
		GoogleCloudPlatformInfo info = new GoogleCloudPlatformInfo();
		info.setDirectory(gcpDir);
		
		config.configure(info);
		
		BigQueryInfo bigquery = info.getBigQuery();
		File bigqueryDir = new File(gcpDir, "bigquery");
		assertEquals(bigqueryDir, bigquery.getDirectory());
		assertEquals(new File(bigqueryDir, "dataset"), bigquery.getDataset());
		assertEquals(new File(bigqueryDir, "schema"), bigquery.getSchema());
		
		CloudStorageInfo cloudstorage = info.getCloudstorage();
		File cloudstorageDir = new File(gcpDir, "cloudstorage");
		assertEquals(cloudstorageDir, cloudstorage.getDirectory());
		assertEquals("dev", cloudstorage.getBucketSuffix());
		
		
	}

}
