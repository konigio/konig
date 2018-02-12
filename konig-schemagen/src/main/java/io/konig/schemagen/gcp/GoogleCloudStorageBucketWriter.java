package io.konig.schemagen.gcp;

/*
 * #%L
 * Konig Schema Generator
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
import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

import io.konig.core.KonigException;
import io.konig.datasource.DataSource;
import io.konig.gcp.datasource.GoogleCloudStorageBucket;
import io.konig.gcp.datasource.NotificationConfig;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeVisitor;

public class GoogleCloudStorageBucketWriter implements ShapeVisitor {
	
	private File outDir;

	public GoogleCloudStorageBucketWriter(File outDir) {
		this.outDir = outDir;
	}

	public void write(GoogleCloudStorageBucket bucket, JsonGenerator json) throws IOException {
		json.writeStartObject();
		json.writeStringField("name", bucket.getName());
		json.writeStringField("location", bucket.getLocation());
		json.writeStringField("storageClass", bucket.getStorageClass());
		if(bucket.getNotificationInfo() != null) {
			json.writeArrayFieldStart("notificationInfo");
			for(NotificationConfig config : bucket.getNotificationInfo()) {
				json.writeStartObject();
				json.writeStringField("topic", config.getTopic());
				if(config.getEventTypes() != null) {
					json.writeArrayFieldStart("eventTypes");
					for(String eventType : config.getEventTypes()) {
						json.writeString(eventType);
					}
					json.writeEndArray();
				}
				json.writeEndObject();
			}
			json.writeEndArray();
		}
		json.writeEndObject();
	}

	@Override
	public void visit(Shape shape) {
		try {
			List<DataSource> list = shape.getShapeDataSource();
			if (list != null) {
				for (DataSource source : list) {
					if (source instanceof GoogleCloudStorageBucket) {
						GoogleCloudStorageBucket bucket = (GoogleCloudStorageBucket) source;
						String fileName = bucket.getName() + ".json";
						outDir.mkdirs();
						File file = new File(outDir, fileName);
						
						JsonFactory factory = new JsonFactory();
						JsonGenerator json = factory.createGenerator(file, JsonEncoding.UTF8);
						json.useDefaultPrettyPrinter();
						try {
							write(bucket, json);
						} finally {
							json.close();
						}
					}
				}
			}
		} catch (Throwable oops) {
			throw new KonigException(oops);
		}
		
	}

}
