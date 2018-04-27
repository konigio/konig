package io.konig.schemagen.aws;

/*
 * #%L
 * Konig Schema Generator
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
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.openrdf.model.vocabulary.RDF;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;

import io.konig.aws.cloudformation.AwsvpcConfiguration;
import io.konig.aws.cloudformation.ContainerDefinition;
import io.konig.aws.cloudformation.LoadBalancer;
import io.konig.aws.cloudformation.LogConfiguration;
import io.konig.aws.cloudformation.NetworkConfiguration;
import io.konig.aws.cloudformation.Options;
import io.konig.aws.cloudformation.PolicyDocument;
import io.konig.aws.cloudformation.PortMapping;
import io.konig.aws.cloudformation.Principal;
import io.konig.aws.cloudformation.Resource;
import io.konig.aws.cloudformation.Statement;
import io.konig.aws.datasource.CloudFormationTemplate;
import io.konig.aws.datasource.DbCluster;
import io.konig.core.Graph;
import io.konig.core.Vertex;
import io.konig.core.pojo.SimplePojoFactory;
import io.konig.core.vocab.AWS;

public class CloudFormationTemplateWriter {
	private File cloudFormationDir;
	private Graph graph;
	private List<ContainerDefinition> containerDefinitions = null;
	
	public CloudFormationTemplateWriter(File cloudFormationDir, Graph graph) {
		this.cloudFormationDir=cloudFormationDir;
		this.graph=graph;
	}
	public void write() throws IOException {
		writeTemplates();
		if(cloudFormationDir!=null && cloudFormationDir.exists()){
			writeDbClusters();
			//writeECR();
		}
	}
	
	public void updateTemplate() throws IOException {
		if(cloudFormationDir!=null && cloudFormationDir.exists()){
			writeTaskDefinition();
			writeService();
		}
	}
	
	private void writeService() throws IOException {
		Resource resource = new Resource();
		resource.setType("AWS::ECS::Service");
		resource.setDependsOn("LoadBalancerRule");
		resource.addProperties("Cluster", "!Ref ECSCluster");
		resource.addProperties("DesiredCount", "!Ref DesiredCount");
		resource.addProperties("LaunchType", "FARGATE");
		List<LoadBalancer> loadBalancers = new ArrayList<>();
		for(ContainerDefinition containerDefinition : containerDefinitions) {
			LoadBalancer loadBalancer = new LoadBalancer();
			loadBalancer.setContainerName(containerDefinition.getName());
			loadBalancer.setContainerPort(containerDefinition.getPortMappings().get(0).getContainerPort());
			loadBalancer.setTargetGroupArn("!Ref TargetGroup");
			loadBalancers.add(loadBalancer);
		}
		resource.addProperties("LoadBalancers", loadBalancers);
		NetworkConfiguration networkConfig = new NetworkConfiguration();
		AwsvpcConfiguration configuration = new AwsvpcConfiguration();
		configuration.setAssignPublicIp("ENABLED");
		String[] securityGroups = {"!Ref FargateContainerSecurityGroup"};
		configuration.setSecurityGroups(securityGroups);
		String[] subnets = {"!Ref PublicSubnetOne","!Ref PublicSubnetTwo"};
		configuration.setSubnets(subnets);		
		networkConfig.setAwsvpcConfiguration(configuration);
		resource.addProperties("NetworkConfiguration", networkConfig);
		
		resource.addProperties("TaskDefinition", "!Ref TaskDefinition");
		Map<String,Object> resources = new LinkedHashMap<String,Object>();
		resources.put("Service", resource);

		String ecsService = AWSCloudFormationUtil.getResourcesAsString(resources);
		AWSCloudFormationUtil.writeCloudFormationTemplate(cloudFormationDir,ecsService, false);	
	}
	
	private void writeTaskDefinition() throws IOException {
		Resource resource = new Resource();
		resource.setType("AWS::ECS::TaskDefinition");
		resource.addProperties("Cpu", "!Ref ContainerCpu");
		resource.addProperties("Memory", "!Ref ContainerMemory");
		resource.addProperties("NetworkMode", "awsvpc");
		String[] compatibilities = {"FARGATE"};
		resource.addProperties("RequiresCompatibilities", compatibilities);
		resource.addProperties("ExecutionRoleArn", "!Ref ECSTaskExecutionRole");
		resource.addProperties("TaskRoleArn", "!Ref ECSTaskExecutionRole");
		containerDefinitions = new ArrayList<ContainerDefinition>();
		List<String> images = FileUtils.readLines(new File(cloudFormationDir, "ImageList.txt"), "utf-8");
		for (String image : images) {
			ContainerDefinition containerDefinition = new ContainerDefinition();
			containerDefinition.setName(image.replaceAll("[^a-zA-Z0-9]", "-"));
			containerDefinition.setImage("${aws-account-id}.dkr.ecr.${aws-region}.amazonaws.com/"+ image);
			containerDefinition.setCpu("!Ref ContainerCpu");
			containerDefinition.setMemoryReservation("!Ref ContainerMemory");
			List<PortMapping> portMappings = new ArrayList<>();
			PortMapping portMapping = new PortMapping();
			portMapping.setContainerPort("8080");
			portMappings.add(portMapping);
			containerDefinition.setPortMappings(portMappings);
			LogConfiguration logConfiguration = new LogConfiguration();
			logConfiguration.setLogDriver("awslogs");
			Options options = new Options();
			options.setAwslogRegion("${aws-region}");
			options.setAwslogsGroup("!Ref 'AWS::StackName'");
			options.setAwslogsStreamPrefix(containerDefinition.getName());
			logConfiguration.setOptions(options);
			containerDefinition.setLogConfiguration(logConfiguration);
			containerDefinitions.add(containerDefinition);
		}
		resource.addProperties("ContainerDefinitions", containerDefinitions);
		Map<String,Object> resources = new LinkedHashMap<String,Object>();
		resources.put("TaskDefinition", resource);
		String taskDefinition = AWSCloudFormationUtil.getResourcesAsString(resources);
		AWSCloudFormationUtil.writeCloudFormationTemplate(cloudFormationDir,taskDefinition, false);	
	}
	
	private void writeECR() throws IOException {
		String repositoryName=System.getProperty("ECRRepositoryName");
		if(repositoryName!=null){
			String ecrTemplate=getECRTemplate(repositoryName);		
			AWSCloudFormationUtil.writeCloudFormationTemplate(cloudFormationDir,ecrTemplate, false);		
		}		
	}
	private String getECRTemplate(String repositoryName) throws JsonProcessingException {		
		Map<String,Object> resources=new LinkedHashMap<String,Object>();
		Resource resource=new Resource();
		resource.setType("AWS::ECR::Repository");
		resource.addProperties("RepositoryName", repositoryName);
		PolicyDocument repPolicy = new PolicyDocument();
		repPolicy.setVersion("2008-10-17");
		List<Statement> statements = new ArrayList<Statement>();
		Statement statement = new Statement();
		statement.setSid(repositoryName+"Statement");
		statement.setEffect("Allow");
		Principal principal=new Principal();
		principal.setAws("*");
		statement.setPrincipal(principal);
		String[] actions={"ecr:GetDownloadUrlForLayer","ecr:BatchGetImage","ecr:BatchCheckLayerAvailability",
				"ecr:PutImage","ecr:InitiateLayerUpload","ecr:UploadLayerPart","ecr:CompleteLayerUpload"};
		statement.setAction(actions);
		statements.add(statement);
		repPolicy.setStatements(statements);
		resource.addProperties("RepositoryPolicyText", repPolicy);
		
		resources.put(repositoryName+"Template", resource);
		return AWSCloudFormationUtil.getResourcesAsString(resources);

	}
	private void writeDbClusters() throws IOException {
		List<Vertex> list = graph.v(AWS.DbCluster).in(RDF.TYPE).toVertexList();
		if (!list.isEmpty() && cloudFormationDir!=null && cloudFormationDir.exists()) {			
			for (Vertex v : list) {
				SimplePojoFactory pojoFactory = new SimplePojoFactory();
				DbCluster instance = pojoFactory.create(v, DbCluster.class);
				String dbClusterTemplate=getDbClusterTemplate(instance);
				AWSCloudFormationUtil.writeCloudFormationTemplate(cloudFormationDir,dbClusterTemplate, false);
			}
		}
	}
	private void writeTemplates() throws IOException {
		
		List<Vertex> list = graph.v(AWS.CloudFormationTemplate).in(RDF.TYPE).toVertexList();
		if (!list.isEmpty()) {
			if (!cloudFormationDir.exists()) {
				cloudFormationDir.mkdirs();
			}
			for (Vertex v : list) {
				SimplePojoFactory pojoFactory = new SimplePojoFactory();
				CloudFormationTemplate cloudFormation = pojoFactory.create(v, CloudFormationTemplate.class);
				
				File yamlFile = new File(cloudFormationDir, cloudFormation.getStackName() + "_template.yml");
				try (PrintWriter out = new PrintWriter(yamlFile)) {
				    out.println(cloudFormation.getTemplate());
				}
				
				String fileName = MessageFormat.format("{0}.json", cloudFormation.getStackName());
				File file = new File(cloudFormationDir, fileName);
				try (FileWriter writer = new FileWriter(file)) {
					writeCloudFormationJson(writer,cloudFormation);
				}

				
				
			}
		}
		
	}
	
	private void writeCloudFormationJson(FileWriter writer, CloudFormationTemplate cloudFormation) throws IOException {
		JsonFactory factory = new JsonFactory();
		JsonGenerator json = factory.createGenerator(writer);
		json.useDefaultPrettyPrinter();
		
		json.writeStartObject();
		writeString(json,"stackName", cloudFormation.getStackName());		
		writeString(json, "region", cloudFormation.getRegion());
		writeString(json, "template", cloudFormation.getStackName() + "_template.yml");
		
		json.writeEndObject();
		
		json.flush();
		
		
	}
	private void writeString(JsonGenerator json, String fieldName, String value) throws IOException {
		
		if (value != null) {
			json.writeStringField(fieldName, value);
		}
	}
	private String getDbClusterTemplate(DbCluster instance) throws JsonProcessingException {		
		Map<String,Object> resources=new LinkedHashMap<String,Object>();
		resources.put(instance.getDbClusterName()+"DBCluster", getDbClusterResource(instance));
		resources.put(instance.getDbClusterName()+"DBInstance", getDbInstanceResource(instance));
		return AWSCloudFormationUtil.getResourcesAsString(resources);
	}
	private Resource getDbInstanceResource(DbCluster instance) {
		Resource resource=new Resource();
		resource.setType("AWS::RDS::DBInstance");
		resource.addProperties("DBClusterIdentifier", "!Ref "+instance.getDbClusterName()+"DBCluster");
		resource.addProperties("DBInstanceClass", instance.getInstanceClass());
		resource.addProperties("Engine", instance.getEngine());
		return resource;
	}
	private Resource getDbClusterResource(DbCluster instance) {
		Resource resource=new Resource();
		resource.setType("AWS::RDS::DBCluster");
		resource.addProperties("Engine", instance.getEngine());
		resource.addProperties("MasterUsername", "${AuroraMasterUsername}");
		resource.addProperties("MasterUserPassword", "${AuroraMasterUserPassword}");
		resource.addProperties("StorageEncrypted", parseBoolean(instance.getStorageEncrypted()));
		resource.addProperties("AvailabilityZones", instance.getAvailabilityZones());
		resource.addProperties("BackupRetentionPeriod", parseInteger(instance.getBackupRetentionPeriod()));
		resource.addProperties("DatabaseName", databaseName(instance));
		resource.addProperties("DBClusterIdentifier", instance.getDbClusterId());
		resource.addProperties("DBSubnetGroupName", instance.getDbSubnetGroupName());
		resource.addProperties("EngineVersion", instance.getEngineVersion());
		resource.addProperties("PreferredBackupWindow", instance.getPreferredBackupWindow());
		resource.addProperties("PreferredMaintenanceWindow", instance.getPreferredMaintenanceWindow());
		if(instance.getReplicationSourceIdentifier()!=null)
			resource.addProperties("ReplicationSourceIdentifier", instance.getReplicationSourceIdentifier());
		return resource;
	}
	
	private Object databaseName(DbCluster instance) {
		String name = instance.getDatabaseName();
		return name==null ? null : name.replace("-", "");
	}
	private Object parseBoolean(String value) {
		return value==null? null : new Boolean(value);
	}
	
	private Integer parseInteger(String value) {
		return value==null ? null : new Integer(value);
	}
}
