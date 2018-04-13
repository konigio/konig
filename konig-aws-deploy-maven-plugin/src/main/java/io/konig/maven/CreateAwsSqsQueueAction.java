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
import java.util.HashMap;
import java.util.Map;

import org.codehaus.plexus.util.StringUtils;

import com.amazonaws.auth.policy.Policy;
import com.amazonaws.auth.policy.Principal;
import com.amazonaws.auth.policy.Resource;
import com.amazonaws.auth.policy.Statement;
import com.amazonaws.auth.policy.Statement.Effect;
import com.amazonaws.auth.policy.actions.SNSActions;
import com.amazonaws.auth.policy.actions.SQSActions;
import com.amazonaws.auth.policy.conditions.ArnCondition;
import com.amazonaws.auth.policy.conditions.ConditionFactory;
import com.amazonaws.auth.policy.conditions.ArnCondition.ArnComparisonType;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.sns.model.CreateTopicResult;
import com.amazonaws.services.sns.model.SetTopicAttributesRequest;
import com.amazonaws.services.sns.util.Topics;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.CreateQueueResult;
import com.amazonaws.services.sqs.model.QueueAttributeName;
import com.amazonaws.services.sqs.model.SetQueueAttributesRequest;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.konig.aws.datasource.Queue;
import io.konig.aws.datasource.QueueConfiguration;
import io.konig.aws.datasource.S3Bucket;

public class CreateAwsSqsQueueAction {
	private AwsDeployment deployment;

	public CreateAwsSqsQueueAction(AwsDeployment deployment){
		this.deployment=deployment;
	}
	
	public AwsDeployment from(String path) throws Exception {
		String cfTemplatePresent=System.getProperty("cfTemplatePresent");
		if(cfTemplatePresent==null || cfTemplatePresent.equals("N")){
			try{
				File file = deployment.file(path);
				ObjectMapper mapper=new ObjectMapper();
				S3Bucket bucket = mapper.readValue(file, S3Bucket.class);
				deployment.verifyAWSCredentials();
				
				QueueConfiguration queueConfig = bucket.getNotificationConfiguration().getQueueConfiguration();
				
				if(queueConfig != null && queueConfig.getQueue() != null){
					String accountId = "";
					if (System.getProperty("aws-account-id") != null) {
						accountId = System.getProperty("aws-account-id");
					}
					
					Queue queue = queueConfig.getQueue();				
					Regions regions=Regions.fromName(queue.getRegion());
					AmazonSQS sqs = AmazonSQSClientBuilder.standard()
							.withCredentials(deployment.getCredential())
							.withRegion(regions).build();  
					AmazonSNS sns = AmazonSNSClientBuilder.standard()
							.withCredentials(deployment.getCredential())
							.withRegion(regions).build();  
					
					CreateQueueResult result = sqs.createQueue(queue.getResourceName());
					
					String topicArn = StringUtils.replaceOnce(bucket.getNotificationConfiguration().getTopicConfiguration().getTopicArn(), "${aws-account-id}",
							accountId);
					String queueArn = StringUtils.replaceOnce(bucket.getNotificationConfiguration().getQueueConfiguration().getQueueArn(), "${aws-account-id}",
							accountId);
					
					deployment.setResponse("Queue  "+queueArn+" is created");
					
					Policy policy = new Policy().withStatements(
						    new Statement(Effect.Allow)
								.withPrincipals(Principal.AllUsers)
						        .withActions(SQSActions.SendMessage)
						        .withResources(new Resource(queueArn))
						        .withConditions(ConditionFactory.newSourceArnCondition(topicArn)));
					
					Map<String, String> queueAttributes = new HashMap<String, String>();
					queueAttributes.put(QueueAttributeName.Policy.toString(), policy.toJson());
					 
					deployment.setResponse("Queue Policy Configured : "+policy.toJson());
					
					sqs.setQueueAttributes(
					    new SetQueueAttributesRequest(result.getQueueUrl(), queueAttributes));
					
					Topics.subscribeQueue(sns, sqs, topicArn, result.getQueueUrl());
					
					deployment.setResponse("Subscription is created : Topic ["+topicArn+"], Queue ["+queueArn+"]");
				}
				else{
					deployment.setResponse("Queue Configuration Failed");
				}
				
			}
			catch(Exception e){
				throw e;
			}
		}
		else{
			deployment.setResponse("Queue will be created through cloud formation template");
		}
	    return deployment;
	}

}
