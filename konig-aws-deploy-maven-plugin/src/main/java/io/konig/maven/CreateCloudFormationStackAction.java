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
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.cloudformation.AmazonCloudFormation;
import com.amazonaws.services.cloudformation.AmazonCloudFormationClientBuilder;
import com.amazonaws.services.cloudformation.model.Capability;
import com.amazonaws.services.cloudformation.model.CreateStackRequest;
import com.amazonaws.services.cloudformation.model.CreateStackResult;
import com.amazonaws.services.cloudformation.model.DescribeStackEventsRequest;
import com.amazonaws.services.cloudformation.model.DescribeStacksRequest;
import com.amazonaws.services.cloudformation.model.Output;
import com.amazonaws.services.cloudformation.model.Stack;
import com.amazonaws.services.cloudformation.model.StackEvent;
import com.amazonaws.services.cloudformation.model.StackStatus;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.konig.aws.common.StackCreationException;
import io.konig.aws.datasource.CloudFormationTemplate;
import io.konig.schemagen.aws.AWSCloudFormationUtil;

public class CreateCloudFormationStackAction {
	private AwsDeployment deployment;
	public CreateCloudFormationStackAction(AwsDeployment deployment){
		this.deployment=deployment;
	}
	public AwsDeployment from(String path) throws Exception {
			File jsonFile=deployment.file(path);
			ObjectMapper mapper = new ObjectMapper();
			CloudFormationTemplate cfTemplate = mapper.readValue(jsonFile, CloudFormationTemplate.class);
			Properties properties=System.getProperties();
			StringWriter result = new StringWriter();
			properties.setProperty("file.resource.loader.path", jsonFile.getParent());
			
			VelocityEngine engine = new VelocityEngine(properties);
			Template template = engine.getTemplate(cfTemplate.getTemplate(), "UTF-8");
			VelocityContext context=new VelocityContext();
			context.put("beginVar", "${");
			context.put("endVar", "}");
			for(Object key:properties.keySet()){
				String k=(String)key;
				context.put(k, properties.getProperty(k));
			}
			template.merge(context, result);
			String strResult=result.toString();
			
			CreateStackRequest request=new CreateStackRequest()
					.withTemplateBody(strResult)
					.withStackName(cfTemplate.getStackName())
					.withCapabilities(Capability.CAPABILITY_IAM);
			
			AmazonCloudFormation amazonClient=AmazonCloudFormationClientBuilder.standard()
		             .withCredentials(AWSCloudFormationUtil.getCredential())
		             .withRegion(Regions.fromName(cfTemplate.getRegion()))
		             .build();
			CreateStackResult stackResult=amazonClient.createStack(request);
			List<Output> outputs=getOutputForRequest(cfTemplate.getStackName(),amazonClient);
			deployment.setResponse("Stack creation complete. Outputs::"+(outputs==null?"":outputs.toString()));
			return deployment;
		
	}
	private List<Output> getOutputForRequest(String stackName, AmazonCloudFormation client) throws InterruptedException, StackCreationException {
	    int tried = 0;
	    String maxTime= System.getProperty("stackMaxTime");
	    while (tried < (maxTime==null?1800:Integer.parseInt(maxTime))) {
	        DescribeStacksRequest describeStacksRequest = new DescribeStacksRequest();
	        describeStacksRequest.withStackName(stackName);
	        Stack resultStack = client.describeStacks(describeStacksRequest).getStacks().get(0);
	        StackStatus stackStatus = StackStatus.valueOf(resultStack.getStackStatus());
	        if (("CREATE_COMPLETE").equals(stackStatus.toString())) {
	           return resultStack.getOutputs();
	        } else if (stackStatus.toString().endsWith("IN_PROGRESS")) {
	        	 Thread.sleep(10000);
	        }	
	        else {
	        	DescribeStackEventsRequest describeStackEventsRequest=new DescribeStackEventsRequest();
	        	describeStackEventsRequest.withStackName(stackName);
	        	List<StackEvent> stackEvents=client.describeStackEvents(describeStackEventsRequest).getStackEvents();
	        	List<StackEvent> errorEvents=new ArrayList<StackEvent>();
	        	for(StackEvent stackEvent:stackEvents){
	        		if(stackEvent.getResourceStatus().equals("CREATE_FAILED")){
	        			errorEvents.add(stackEvent);
	        		}
	        	}
	        	throw new StackCreationException(errorEvents.toString());
	        }
	        tried++;
	    }
	    throw new RuntimeException("stack creation/deletion timed out");
	}

	
	
}
