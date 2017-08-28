package io.konig.maven;

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


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.FormatOptions;
import com.google.cloud.bigquery.TableDataWriteChannel;
import com.google.cloud.bigquery.TableId;
import com.google.cloud.bigquery.WriteChannelConfiguration;

import io.konig.gcp.common.GoogleCloudService;

public class InsertBigQueryDataAction {

	private KonigDeployment deployment;

	public InsertBigQueryDataAction(KonigDeployment deployment) {
		this.deployment = deployment;
	}

	public KonigDeployment from(String path) throws FileNotFoundException, IOException {

		GoogleCloudService service = deployment.getService();
		File file = deployment.file(path);
		
		String fileName = file.getName();
		int dot = fileName.indexOf('.');
		String datasetName = fileName.substring(0, dot);
		String tableName = fileName.substring(dot+1);
		TableId tableId = TableId.of(datasetName, tableName);
		
		WriteChannelConfiguration config = 
			WriteChannelConfiguration.newBuilder(tableId)
				.setFormatOptions(FormatOptions.json())
				.build();
		BigQuery bigquery = service.bigQuery();
		
		TableDataWriteChannel writer = bigquery.writer(config);
		
		try (FileInputStream fis = new FileInputStream(file)) {
			FileChannel fci = fis.getChannel();
			ByteBuffer buffer = ByteBuffer.allocate(1024);
			
			for (;;) {
				int read = fci.read(buffer);
				if (read == -1) {
					break;
				}
				buffer.flip();
				writer.write(buffer);
				buffer.clear();
			}
		}
		
		return deployment;
	}
}
