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
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.codehaus.plexus.util.StringUtils;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.DeleteBucketRequest;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.konig.aws.common.InvalidAWSCredentialsException;
import io.konig.aws.datasource.AwsAuroraDefinition;
import io.konig.aws.datasource.AwsAuroraTableReference;
import io.konig.aws.datasource.S3Bucket;

public class DeleteAwsS3BucketAction {
	private AwsDeployment deployment;

	public DeleteAwsS3BucketAction(AwsDeployment deployment) {
		this.deployment = deployment;
	}
	
	public AwsDeployment from(String path) throws Exception {
		try{
			File file = deployment.file(path);
			ObjectMapper mapper=new ObjectMapper();
			S3Bucket bucket = mapper.readValue(file, S3Bucket.class);
			deployment.verifyAWSCredentials();
			Regions regions=Regions.fromName(bucket.getRegion());
			AmazonS3 s3client = AmazonS3ClientBuilder.standard()
					.withCredentials(deployment.getCredential())
					.withRegion(regions).build();
			String envtName="";
			if(System.getProperty("environmentName") != null) {
				envtName = System.getProperty("environmentName");
			}
			String bucketName=StringUtils.replaceOnce(bucket.getBucketName(), "${environmentName}", envtName);
			s3client.deleteBucket(bucketName);	
		boolean status=s3client.doesBucketExistV2(bucketName);
		if(!status)
			deployment.setResponse("AWS S3 Bucket is deleted ::"+bucketName);
				
		}	
		catch(Exception e){
			throw e;
		}
	    return deployment;
	}
}
