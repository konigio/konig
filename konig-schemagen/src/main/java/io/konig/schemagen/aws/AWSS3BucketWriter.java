package io.konig.schemagen.aws;

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
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.amazonaws.services.cloudformation.AmazonCloudFormationClient;
import com.amazonaws.services.cloudformation.model.ValidateTemplateRequest;
import com.amazonaws.services.cloudformation.model.ValidateTemplateResult;
import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;

import io.konig.aws.cloudformation.Condition;
import io.konig.aws.cloudformation.NotifConfig;
import io.konig.aws.cloudformation.PolicyDocument;
import io.konig.aws.cloudformation.Principal;
import io.konig.aws.cloudformation.Resource;
import io.konig.aws.cloudformation.Statement;
import io.konig.aws.cloudformation.TopicConfig;
import io.konig.aws.datasource.NotificationConfiguration;
import io.konig.aws.datasource.Queue;
import io.konig.aws.datasource.QueueConfiguration;
import io.konig.aws.datasource.S3Bucket;
import io.konig.aws.datasource.Topic;
import io.konig.aws.datasource.TopicConfiguration;
import io.konig.core.KonigException;
import io.konig.datasource.DataSource;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeVisitor;

public class AWSS3BucketWriter implements ShapeVisitor {
	
	private File outDir;
	private File cfDir;

	public AWSS3BucketWriter(File outDir,File cfDir) {
		this.outDir = outDir;
		this.cfDir = cfDir;
	}

	public void write(S3Bucket bucket, JsonGenerator json) throws IOException {
		
		json.writeStartObject();
		json.writeStringField("bucketKey", bucket.getBucketKey());
		json.writeStringField("bucketName", bucket.getBucketName());
		json.writeStringField("region", bucket.getRegion());
		json.writeStringField("bucketMediaType", bucket.getBucketMediaType());
		if(bucket.getNotificationConfiguration() != null){
			json.writeObjectFieldStart("notificationConfiguration");
			
			if(bucket.getNotificationConfiguration().getTopicConfiguration() != null) {
				json.writeObjectFieldStart("topicConfiguration");
				TopicConfiguration topicConfig = bucket.getNotificationConfiguration().getTopicConfiguration();
				Topic topic = topicConfig.getTopic();
				if(topic!=null){			
						json.writeObjectFieldStart("topic");
						json.writeStringField("resourceName", topic.getResourceName());
						json.writeStringField("region", topic.getRegion());
						json.writeStringField("accountId", topic.getAccountId());
						json.writeEndObject();
				}
				json.writeStringField("topicArn", topicConfig.getTopicArn());
				json.writeStringField("eventType", topicConfig.getEventType());
				json.writeEndObject();
			}
			
			if(bucket.getNotificationConfiguration().getQueueConfiguration() != null){
				json.writeObjectFieldStart("queueConfiguration");
				QueueConfiguration queueConfig = bucket.getNotificationConfiguration().getQueueConfiguration();
				Queue queue = queueConfig.getQueue();
				if(queue != null){	
					json.writeObjectFieldStart("queue");
					json.writeStringField("resourceName", queue.getResourceName());
					json.writeStringField("region", queue.getRegion());
					json.writeStringField("accountId", queue.getAccountId());
					json.writeEndObject();
				}
				json.writeStringField("queueArn", queueConfig.getQueueArn());
				json.writeStringField("eventType", queueConfig.getEventType());
				json.writeEndObject();
			}
			
			json.writeEndObject();
		}
		json.writeEndObject();				
	}

	@Override
	public void visit(Shape shape) {
		try {
			List<DataSource> list = shape.getShapeDataSource();
			if (list != null) {
				for (DataSource source : list) {
					if (source instanceof S3Bucket) {
						S3Bucket bucket = (S3Bucket) source;
						String fileName = bucket.getBucketKey() + ".json";
						outDir.mkdirs();
						File file = new File(outDir, fileName);
						
						JsonFactory factory = new JsonFactory();
						JsonGenerator json = factory.createGenerator(file, JsonEncoding.UTF8);
						json.useDefaultPrettyPrinter();
						try {
							write(bucket, json);
							
						} catch(Exception ex){
							ex.printStackTrace();
							
						}finally {
							json.close();
						}
						if(cfDir!=null && cfDir.exists()){
							String s3BucketTemplate=getS3BucketTemplate(bucket);
							AWSCloudFormationUtil.writeCloudFormationTemplate(cfDir,s3BucketTemplate);
						}
					}
				}
			}
		} catch (Throwable oops) {
			throw new KonigException(oops);
		}
		
	}
	
	private String getS3BucketTemplate(S3Bucket bucket) throws JsonProcessingException {
		
		Resource s3BucketResource=new Resource();
		s3BucketResource.setType("AWS::S3::Bucket");
		s3BucketResource.addProperties("BucketName", bucket.getBucketName().toLowerCase());
		NotificationConfiguration config=bucket.getNotificationConfiguration();
		Map<String,Object> resources=new LinkedHashMap<String,Object>();
		resources.put(bucket.getBucketKey()+"S3Bucket", s3BucketResource);
		if(config!=null && config.getTopicConfiguration()!=null){
			
			TopicConfiguration topicConfig=config.getTopicConfiguration();
			Topic topic=topicConfig.getTopic();
			NotifConfig notConfig=new NotifConfig();
			List<TopicConfig> topConfigs=new ArrayList<TopicConfig>();
			TopicConfig topConfig=new TopicConfig();
			topConfig.setEvent(topicConfig.getEventType());
			topConfig.setTopic("!Ref "+bucket.getBucketKey()+"SNSTopic");
			topConfigs.add(topConfig);
			notConfig.setTopConfigs(topConfigs);
			s3BucketResource.addProperties("NotificationConfiguration", notConfig);			
			
			Resource snsTopicResource = new Resource();
			snsTopicResource.setType("AWS::SNS::Topic");
			snsTopicResource.addProperties("TopicName", topic.getResourceName());
			resources.put(bucket.getBucketKey()+"SNSTopic", snsTopicResource);
			
			Resource snsTopicPolicyResource=getPolicy(bucket,"SNSTopic");			
			resources.put(bucket.getBucketKey()+"SNSTopicPolicy", snsTopicPolicyResource);			
			
			if(config.getQueueConfiguration()!=null){				
				QueueConfiguration queueConfig=config.getQueueConfiguration();
				Queue queue=queueConfig.getQueue();
				
				Resource sqsQueueResource = new Resource();
				sqsQueueResource.setType("AWS::SQS::Queue");
				sqsQueueResource.addProperties("QueueName",queue.getResourceName());
				resources.put(bucket.getBucketKey()+"SQSQueue", sqsQueueResource);
				
				Resource snsSubscriptionResource = new Resource();
				snsSubscriptionResource.setType("AWS::SNS::Subscription");
				snsSubscriptionResource.addProperties("Protocol", "sqs");
				snsSubscriptionResource.addProperties("TopicArn", "!Ref "+bucket.getBucketKey()+"SNSTopic");
				Map<String,String[]> function = new HashMap<String,String[]>();
				String[] getAttParams={bucket.getBucketKey()+"SQSQueue","Arn"};
				function.put("Fn::GetAtt", getAttParams);
				snsSubscriptionResource.addProperties("Endpoint", function);
				resources.put(bucket.getBucketKey()+"SNSSubscription", snsSubscriptionResource);
				
				Resource sqsQueuePolicyResource=getPolicy(bucket,"SQSQueue");			
				resources.put(bucket.getBucketKey()+"SQSQueuePolicy", sqsQueuePolicyResource);
			}
		}
		
		return AWSCloudFormationUtil.getResourcesAsString(resources);
		
	}

	private Resource getPolicy(S3Bucket bucket,String resourceType) {
		Resource policyResource =new Resource();
		List<Statement> statements=null;
		List<String> urls=new ArrayList<String>();
		urls.add("!Ref "+bucket.getBucketKey()+resourceType);
		if(resourceType.equals("SQSQueue")){
			policyResource.setType("AWS::SQS::QueuePolicy");
			statements=getQueuePolicyStatement(bucket);
			policyResource.addProperties("Queues", urls);
		}
		else{
			policyResource.setType("AWS::SNS::TopicPolicy");
			statements=getTopicPolicyStatement(bucket);
			policyResource.addProperties("Topics", urls);	
		}
		
		PolicyDocument policyDoc=new PolicyDocument();
		policyDoc.setId(bucket.getBucketKey()+resourceType+"PolicyDoc");
		policyDoc.setVersion("2012-10-17");
		policyDoc.setStatements(statements);	
		policyResource.addProperties("PolicyDocument", policyDoc);		
		return policyResource;
	}


	private List<Statement> getTopicPolicyStatement(S3Bucket bucket) {
		List<Statement> statements=new ArrayList<Statement>();
		Statement statement=new Statement();
		statement.setSid(bucket.getBucketKey()+"TopicPolicyStatementId");
		statement.setEffect("Allow");
		Principal principal=new Principal();
		principal.setAws("*");
		statement.setPrincipal(principal);
		statement.setAction("sns:Publish");
		statement.setResource("!Ref "+bucket.getBucketKey()+"SNSTopic");
		Condition condition=new Condition();
		Map<String,Object> arnCondition=new HashMap<String,Object>();
		arnCondition.put("aws:SourceArn","arn:aws:s3:*:*:"+bucket.getBucketName().toLowerCase());
		condition.setArnEquals(arnCondition);
		statement.setCondition(condition);
		statements.add(statement);
		return statements;	
	}
	private List<Statement> getQueuePolicyStatement(S3Bucket bucket) {
		List<Statement> statements=new ArrayList<Statement>();
		Statement statement=new Statement();
		statement.setSid(bucket.getBucketKey()+"QueuePolicyStatementId");
		statement.setEffect("Allow");
		Principal principal=new Principal();
		principal.setAws("*");
		statement.setPrincipal(principal);
		statement.setAction("sqs:SendMessage");
		Map<String,String[]> function = new HashMap<String,String[]>();
		String[] getAttParams={bucket.getBucketKey()+"SQSQueue","Arn"};
		function.put("Fn::GetAtt", getAttParams);
		statement.setResource(function);
		Condition condition=new Condition();
		Map<String,Object> arnCondition=new HashMap<String,Object>();
		arnCondition.put("aws:SourceArn", "!Ref "+bucket.getBucketKey()+"SNSTopic");
		condition.setArnEquals(arnCondition);
		statement.setCondition(condition);
		statements.add(statement);
		return statements;	
	}

	

}
