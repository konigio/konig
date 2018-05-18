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
import java.io.IOException;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;

import io.konig.aws.common.InvalidAWSCredentialsException;
import io.konig.schemagen.java.SystemConfig;

public class AwsDeployment {

	private File baseDir;
	private String response;
	
	public AwsDeployment(String baseDir) throws IOException {
		SystemConfig.init();
		this.baseDir = new File(baseDir).getAbsoluteFile();	
	}
	
	public Object create(AwsResourceType type) {
		switch (type) {
		case AwsAurora :
			return new CreateAuroraTableAction(this);
		case AwsS3Bucket :
			return new CreateAwsS3BucketAction(this);
		case AwsSnsTopic:
			return new CreateAwsSnsTopicAction(this);
		case AwsSqsQueue:
			return new CreateAwsSqsQueueAction(this);
		case CloudFormationStack:
			return new CreateCloudFormationStackAction(this);
		case AwsAuroraView:
			return new CreateAuroraViewAction(this);
		default:
			break;
		}
		return null;
	}
	
	public Object delete(AwsResourceType type) {
		switch (type) {
		case AwsAurora:
			return new DeleteAwsTable(this);

		case AwsSnsTopic:
			return new DeleteAwsSNS(this);

		case AwsS3Bucket:
			return new DeleteAwsS3BucketAction(this);
			
		case AwsSqsQueue:
			return new DeleteAwsSQS(this);
		
		case CloudFormationStack:
			return new DeleteCloudFormationStackAction(this);
		case AwsAuroraView:
			return new DeleteAuroraViewAction(this);
		default:
			break;

		}
		return null;
	}
	
	public File file(String path) {
		return new File(baseDir, path);
	}
	
	public void setResponse(String response) {
		this.response = response;
	}

	public String getResponse() {
		return this.response;
	}
	
	public void verifyAWSCredentials() throws InvalidAWSCredentialsException {
		String accessKeyId = System.getProperty("aws.accessKeyId");
		String secretKey = System.getProperty("aws.secretKey");
		if (accessKeyId == null || secretKey == null)
			throw new InvalidAWSCredentialsException();
	}
	
	AWSStaticCredentialsProvider getCredential() throws InvalidAWSCredentialsException {
		verifyAWSCredentials();
		return new AWSStaticCredentialsProvider(
				new BasicAWSCredentials(System.getProperty("aws.accessKeyId"), System.getProperty("aws.secretKey")));

	}
}
