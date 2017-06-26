package io.konig.deploy.gcp;

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
