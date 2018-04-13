package io.konig.aws.datasource;

/*
 * #%L
 * Konig AWS Model
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


import io.konig.annotation.RdfProperty;
import io.konig.core.vocab.AWS;

public class CloudFormationTemplate {
	private String stackName;
	private String region;
	private String template;
	
	@RdfProperty(AWS.STACK_NAME)
	public String getStackName() {
		return stackName;
	}
	public void setStackName(String stackName) {
		this.stackName = stackName;
	}
	
	@RdfProperty(AWS.AWS_REGION)
	public String getRegion(){
		return region;
	}
	
	public void setRegion(String region){
		this.region=region;
	}
	@RdfProperty(AWS.TEMPLATE)
	public String getTemplate() {
		return template;
	}
	public void setTemplate(String template) {
		this.template = template;
	}
	
}
