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

import com.amazonaws.regions.Regions;
import com.amazonaws.services.cloudformation.AmazonCloudFormation;
import com.amazonaws.services.cloudformation.AmazonCloudFormationClientBuilder;
import com.amazonaws.services.cloudformation.model.DeleteStackRequest;
import com.amazonaws.services.cloudformation.model.DeleteStackResult;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.konig.aws.datasource.CloudFormationTemplate;
import io.konig.schemagen.aws.AWSCloudFormationUtil;

public class DeleteCloudFormationStackAction {
	private AwsDeployment deployment;

	public DeleteCloudFormationStackAction(AwsDeployment deployment) {
		this.deployment = deployment;
	}
	public AwsDeployment from(String path) throws Exception {
		File jsonFile = deployment.file(path);
		ObjectMapper mapper = new ObjectMapper();
		CloudFormationTemplate cfTemplate = mapper.readValue(jsonFile, CloudFormationTemplate.class);
		DeleteStackRequest request=new DeleteStackRequest().withStackName(cfTemplate.getStackName());
		AmazonCloudFormation amazonClient=AmazonCloudFormationClientBuilder.standard()
	             .withCredentials(AWSCloudFormationUtil.getCredential())
	             .withRegion(Regions.fromName(cfTemplate.getRegion()))
	             .build();
		DeleteStackResult stackResult=amazonClient.deleteStack(request);
		deployment.setResponse("Stack deletion complete");
		return deployment;
	}
}
