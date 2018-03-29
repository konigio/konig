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
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.openrdf.model.vocabulary.RDF;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.konig.aws.cloudformation.Resource;
import io.konig.aws.datasource.CloudFormationTemplate;
import io.konig.aws.datasource.DbCluster;
import io.konig.core.Graph;
import io.konig.core.Vertex;
import io.konig.core.pojo.SimplePojoFactory;
import io.konig.core.vocab.AWS;

public class CloudFormationTemplateWriter {
	private File cloudFormationDir;
	private Graph graph;

	public CloudFormationTemplateWriter(File cloudFormationDir, Graph graph) {
		this.cloudFormationDir=cloudFormationDir;
		this.graph=graph;
	}
	public void write() throws IOException {
		writeTemplates();
		writeDbClusters();
		
	}
	private void writeDbClusters() throws IOException {
		List<Vertex> list = graph.v(AWS.DbCluster).in(RDF.TYPE).toVertexList();
		if (!list.isEmpty()) {			
			for (Vertex v : list) {
				SimplePojoFactory pojoFactory = new SimplePojoFactory();
				DbCluster instance = pojoFactory.create(v, DbCluster.class);
				String dbClusterTemplate=getDbClusterTemplate(instance);
				AWSCloudFormationUtil.writeCloudFormationTemplate(cloudFormationDir,dbClusterTemplate);
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
			}
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
		resource.addProperties("StorageEncrypted", new Boolean(instance.getStorageEncrypted()));
		resource.addProperties("AvailabilityZones", instance.getAvailabilityZones());
		resource.addProperties("BackupRetentionPeriod", Integer.parseInt(instance.getBackupRetentionPeriod()));
		resource.addProperties("DatabaseName", instance.getDatabaseName().replace("-", ""));
		resource.addProperties("DBClusterIdentifier", instance.getDbClusterId());
		resource.addProperties("DBSubnetGroupName", instance.getDbSubnetGroupName());
		resource.addProperties("EngineVersion", instance.getEngineVersion());
		resource.addProperties("PreferredBackupWindow", instance.getPreferredBackupWindow());
		resource.addProperties("PreferredMaintenanceWindow", instance.getPreferredMaintenanceWindow());
		if(instance.getReplicationSourceIdentifier()!=null)
			resource.addProperties("ReplicationSourceIdentifier", instance.getReplicationSourceIdentifier());
		return resource;
	}
}
