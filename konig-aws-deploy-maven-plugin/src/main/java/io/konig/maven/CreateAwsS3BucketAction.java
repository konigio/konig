package io.konig.maven;

/*
 * #%L
 * Konig AWS Deployment Maven Plugin
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

import java.io.File;

import org.codehaus.plexus.util.StringUtils;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.BucketNotificationConfiguration;
import com.amazonaws.services.s3.model.CreateBucketRequest;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.konig.aws.common.InvalidAWSCredentialsException;
import io.konig.aws.datasource.NotificationConfiguration;
import io.konig.aws.datasource.S3Bucket;
import io.konig.aws.datasource.Topic;
import io.konig.aws.datasource.TopicConfiguration;

public class CreateAwsS3BucketAction {

	private AwsDeployment deployment;

	public CreateAwsS3BucketAction(AwsDeployment deployment) {
		this.deployment = deployment;
	}

	public AwsDeployment from(String path) throws Exception {
		String cfTemplatePresent=System.getProperty("cfTemplatePresent");
		if(cfTemplatePresent==null || cfTemplatePresent.equals("N")){
			try {
				File file = deployment.file(path);
				ObjectMapper mapper = new ObjectMapper();
				S3Bucket bucket = mapper.readValue(file, S3Bucket.class);
				deployment.verifyAWSCredentials();
				Regions regions = Regions.fromName(bucket.getRegion());
				AmazonS3 s3client = AmazonS3ClientBuilder.standard()
						.withCredentials(deployment.getCredential())
						.withRegion(regions).build();
				String envtName = "";
				if (System.getProperty("environmentName") != null) {
					envtName = System.getProperty("environmentName");
				}
				String bucketName = StringUtils.replaceOnce(bucket.getBucketName(), "${environmentName}", envtName);
				Bucket b = s3client.createBucket(bucketName);
	
				if (bucket.getNotificationConfiguration() != null) {
					NotificationConfiguration notificationConfiguration = bucket.getNotificationConfiguration();
					if (notificationConfiguration.getTopicConfiguration() != null) {
						TopicConfiguration topicConfiguration = notificationConfiguration.getTopicConfiguration();
						BucketNotificationConfiguration s3notificationConfiguration = new BucketNotificationConfiguration();
						String accountId = "";
						if (System.getProperty("aws-account-id") != null) {
							accountId = System.getProperty("aws-account-id");
						}
						String topicArn = StringUtils.replaceOnce(topicConfiguration.getTopicArn(), "${aws-account-id}",
								accountId);
						com.amazonaws.services.s3.model.TopicConfiguration topicConfig = new com.amazonaws.services.s3.model.TopicConfiguration(
								topicArn, topicConfiguration.getEventType());
						s3notificationConfiguration.addConfiguration("snsTopicConfig", topicConfig);
	
						s3client.setBucketNotificationConfiguration(bucketName, s3notificationConfiguration);
					}
				}
				if (b != null)
					deployment.setResponse("AWS S3 Bucket is created ::" + b.getName());
			} catch (Exception e) {
				throw e;
			}
		}
		else{
			deployment.setResponse("S3 Bucket will be created through cloud formation template");
		}
		return deployment;
	}
}
