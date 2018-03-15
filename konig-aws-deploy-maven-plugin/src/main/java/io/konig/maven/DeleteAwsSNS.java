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

import org.codehaus.plexus.util.StringUtils;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.policy.Policy;
import com.amazonaws.auth.policy.Principal;
import com.amazonaws.auth.policy.Resource;
import com.amazonaws.auth.policy.Statement;
import com.amazonaws.auth.policy.Statement.Effect;
import com.amazonaws.auth.policy.actions.SNSActions;
import com.amazonaws.auth.policy.conditions.ArnCondition;
import com.amazonaws.auth.policy.conditions.ConditionFactory;
import com.amazonaws.auth.policy.conditions.ArnCondition.ArnComparisonType;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.sns.model.CreateTopicResult;
import com.amazonaws.services.sns.model.DeleteTopicRequest;
import com.amazonaws.services.sns.model.SetTopicAttributesRequest;
//import com.amazonaws.services.sns.AmazonSNSClient;
//import com.amazonaws.services.sns.model.DeleteTopicRequest;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.konig.aws.common.InvalidAWSCredentialsException;
import io.konig.aws.datasource.S3Bucket;
import io.konig.aws.datasource.Topic;
import io.konig.aws.datasource.TopicConfiguration;

public class DeleteAwsSNS {
	private AwsDeployment deployment;

	public DeleteAwsSNS(AwsDeployment deployment) {
		this.deployment = deployment;
	}
	
	public AwsDeployment from(String path) throws Exception {
		try{
			File file = deployment.file(path);
			ObjectMapper mapper=new ObjectMapper();
			S3Bucket bucket = mapper.readValue(file, S3Bucket.class);
			verifyAWSCredentials();
			TopicConfiguration notificationConfig = bucket.getNotificationConfiguration().getTopicConfiguration();
			if(notificationConfig!=null && notificationConfig.getTopic()!=null){
				Topic topic=notificationConfig.getTopic();				
				Regions regions=Regions.fromName(topic.getRegion());
				AmazonSNS sns = AmazonSNSClientBuilder.standard().withRegion(regions).build();
				CreateTopicResult result=sns.createTopic(topic.getResourceName());
				sns.deleteTopic(result.getTopicArn());
				deployment.setResponse("Topic with ARN : "+result.getTopicArn()+" is deleted");
			}
			else{
				deployment.setResponse("No topic is configured to the S3 Bucket");
			}
			
		}
		catch(Exception e){
			e.printStackTrace();
			throw e;
		}
	    return deployment;
	}

	private void verifyAWSCredentials() throws InvalidAWSCredentialsException {
		String accessKeyId=System.getProperty("aws.accessKeyId");
		String secretKey=System.getProperty("aws.secretKey");
		if(accessKeyId == null || secretKey==null)
			throw new InvalidAWSCredentialsException();		
	}
}
