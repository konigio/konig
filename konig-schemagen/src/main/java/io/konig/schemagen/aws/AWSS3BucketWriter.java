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
						writeCloudFormationTemplate(bucket);
					}
				}
			}
		} catch (Throwable oops) {
			throw new KonigException(oops);
		}
		
	}

	private void writeCloudFormationTemplate(S3Bucket bucket) throws Exception {
		for(File file:cfDir.listFiles()){
				String contents = new String(Files.readAllBytes(Paths.get(file.getAbsolutePath())));	
				YAMLFactory yamlFactory=new YAMLFactory();
				YAMLMapper mapper = new YAMLMapper(new YAMLFactory());
				JsonNode node = mapper.readTree(contents);
				JsonNode outputNode=node.get("Outputs");
			
				if(outputNode!=null){			
					String outputs=contents.substring(contents.lastIndexOf("Outputs:"));
					String resources=contents.substring(0,contents.lastIndexOf("Outputs:"));
					resources=resources+getS3BucketTemplate(bucket);
					contents=resources+outputs;
				}
				
				AmazonCloudFormationClient client = new AmazonCloudFormationClient();
				ValidateTemplateRequest request = new ValidateTemplateRequest();
				request.setTemplateBody(contents);				
				ValidateTemplateResult result=client.validateTemplate(request);
				
				try(FileWriter fileWriter= new FileWriter(file)){
					fileWriter.write(contents);
				}
				
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
			
			Resource snsTopicPolicyResource = new Resource();
			snsTopicPolicyResource.setType("AWS::SNS::TopicPolicy");
			List<String> topicArns=new ArrayList<String>();
			topicArns.add("!Ref "+bucket.getBucketKey()+"SNSTopic");
			PolicyDocument policyDoc=new PolicyDocument();
			policyDoc.setId(bucket.getBucketKey()+"SNSTopicPolicyDoc");
			policyDoc.setVersion("2012-10-17");
			
			List<Statement> statements=new ArrayList<Statement>();
			Statement statement=new Statement();
			statement.setSid(bucket.getBucketKey()+"StatementId");
			statement.setEffect("Allow");
			Principal principal=new Principal();
			principal.setAws("*");
			statement.setPrincipal(principal);
			statement.setAction("sns:Publish");
			statement.setResource("*");		
			statements.add(statement);
			policyDoc.setStatements(statements);
			snsTopicPolicyResource.addProperties("PolicyDocument", policyDoc);
			snsTopicPolicyResource.addProperties("Topics", topicArns);	
			
			resources.put(bucket.getBucketKey()+"SNSTopicPolicy", snsTopicPolicyResource);
			
		}
		return getResourcesAsString(resources);
		
	}

	private String getResourcesAsString(Map<String, Object> resources) throws JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper(new YAMLFactory());	
		String resource=mapper.writeValueAsString(resources);
		String[] resourceLines=resource.split("\n");
		StringBuffer template=new StringBuffer();
		for(String line:resourceLines){
			if(!line.contains("---")){
				template.append("  "+line+"\n");
			}
		}
		return template.toString();
	}

}
