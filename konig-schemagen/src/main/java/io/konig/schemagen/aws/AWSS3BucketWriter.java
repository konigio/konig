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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.amazonaws.services.cloudformation.AmazonCloudFormationClient;
import com.amazonaws.services.cloudformation.model.ValidateTemplateRequest;
import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import io.konig.aws.cloudformation.Resource;
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
				String outputs=contents.substring(contents.lastIndexOf("Outputs:"));
				String resources=contents.substring(0,contents.lastIndexOf("Outputs:"));
				resources=resources+getS3BucketTemplate(bucket);
				contents=resources+outputs;
				
				AmazonCloudFormationClient client = new AmazonCloudFormationClient();
				ValidateTemplateRequest request = new ValidateTemplateRequest();
				request.setTemplateBody(contents);				
				client.validateTemplate(request);
				
				try(FileWriter fileWriter= new FileWriter(file)){
					fileWriter.write(contents);
				}
				
		}
		
	}

	private String getS3BucketTemplate(S3Bucket bucket) throws JsonProcessingException {
		Resource resource=new Resource();
		resource.setType("AWS::S3::Bucket");
		resource.addProperties("BucketName", bucket.getBucketName());
		ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
		Map<String,Object> resources=new HashMap<String,Object>();
		resources.put(bucket.getBucketKey()+"S3Bucket", resource);
		String s3BucketResource=mapper.writeValueAsString(resources);
		String[] s3BucketResourceLines=s3BucketResource.split("\n");
		StringBuffer template=new StringBuffer();
		for(String line:s3BucketResourceLines){
			if(!line.contains("---")){
				template.append("  "+line+"\n");
			}
		}
		return template.toString();
	}

}
