package io.konig.aws.datasource;

import java.util.List;

/*
 * #%L
 * Konig AWS Model
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


import org.openrdf.model.URI;

import io.konig.annotation.RdfProperty;
import io.konig.core.vocab.AWS;
import io.konig.core.vocab.Konig;
import io.konig.datasource.DataSource;

public class S3Bucket extends DataSource{
	private String bucketKey;
	private String bucketName;
	private String region;
	private String bucketMediaType;
	private TopicConfiguration notificationConfiguration;
	
	public S3Bucket() {
		addType(Konig.S3Bucket);
	}
	
	public void addType(URI type) {
		this.getType().add(type);
	}
	
	@RdfProperty(AWS.BUCKET_KEY)
	public String getBucketKey() {
		return bucketKey;
	}

	public void setBucketKey(String bucketKey) {
		this.bucketKey = bucketKey;
	}
	
	@RdfProperty(AWS.BUCKET_NAME)
	public String getBucketName() {
		return bucketName;
	}

	public void setBucketName(String bucketName) {
		this.bucketName = bucketName;
	}
	
	@RdfProperty(AWS.REGION)
	public String getRegion() {
		return region;
	}

	public void setRegion(String region) {
		this.region = region;
	}
	
	@RdfProperty(AWS.BUCKET_MEDIA_TYPE)
	public String getBucketMediaType() {
		return bucketMediaType;
	}

	public void setBucketMediaType(String bucketMediaType) {
		this.bucketMediaType = bucketMediaType;
	}
	
	@RdfProperty(AWS.NOTIFICATION_CONFIGURATION)
	public TopicConfiguration getNotificationConfiguration() {
		return notificationConfiguration;
	}

	public void setNotificationConfiguration(TopicConfiguration notificationConfiguration) {
		this.notificationConfiguration = notificationConfiguration;
	}
	
}
