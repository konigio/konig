package io.konig.maven;

/*
 * #%L
 * Konig Maven Common
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
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.openrdf.model.URI;

import io.konig.core.NamespaceManager;
import io.konig.core.impl.RdfUtil;

public class GoogleCloudPlatformConfig implements RdfSource {

	
	

    private String bqShapeBaseURL;
    
    @Parameter(property="konig.gcp.directory", defaultValue="${project.basedir}/target/generated/gcp")
    private File directory;
    
    
	private String bigQueryDatasetId;
    private File credentials;
    
    @Parameter(property="konig.gcp.topicsFile", defaultValue="${konig.gcp.directory}/topics.txt")
    private File topicsFile;
    
    @Parameter(property="konig.gcp.dataServices")
	private DataServicesConfig dataServices;
    
    
	private boolean enableBigQueryTransform=true;
	
	private boolean enableMySqlTransform=true;
	
	@Parameter(property="konig.gcp.bigquery", required=true)
	private BigQueryInfo bigquery;

	
	@Parameter(property="konig.gcp.etlPipeline", required=true)
	private EtlPipelineConfig etlPipeline;
	
	
	@Parameter(property="konig.gcp.cloudstorage", required=true)
	private CloudStorageInfo cloudstorage;

	@Parameter(property="konig.gcp.cloudsql", required=true)
	private CloudSqlInfo cloudsql;
	
	@Parameter(property="konig.gcp.deployment")
	private GroovyDeploymentScript deployment;
	
	@Parameter(property="konig.gcp.omitTypeFromEnumTables", defaultValue="false")
	private boolean omitTypeFromEnumTables;
	
	@Parameter(property="konig.gcp.etl", defaultValue="${konig.gcp.directory}/camel-etl")
	private File camelEtl;
	
	@Parameter(property="konig.gcp.rdf.directory", defaultValue="${konig.gcp.directory}/rdf")
	private File rdfDirectory;

	@Parameter(property="konig.gcp.bigqueryEnumShapeIriTemplate")
	private String bigqueryEnumShapeIriTemplate;
	
	@Parameter(property="konig.gcp.excludeEnumTable")
	private String[] excludeEnumTable;
	
	public GoogleCloudPlatformConfig() {
		
	}
	
	
	
	public boolean isOmitTypeFromEnumTables() {
		return omitTypeFromEnumTables;
	}

	public String[] getExcludeEnumTable() {
		return excludeEnumTable;
	}
	
	public Set<URI> getExcludeEnumTableList(NamespaceManager nsManager) {
		if (excludeEnumTable==null || excludeEnumTable.length==0) {
			return Collections.emptySet();
		}
		
		Set<URI> result = new HashSet<>();
		for (String curie : excludeEnumTable) {
			result.add(RdfUtil.expand(nsManager, curie));
		}
		return result;
	}

	public void setExcludeEnumTable(String[] excludeEnumTable) {
		this.excludeEnumTable = excludeEnumTable;
	}



	public void setOmitTypeFromEnumTables(boolean omitTypeFromEnumTables) {
		this.omitTypeFromEnumTables = omitTypeFromEnumTables;
	}



	public String getBqShapeBaseURL() {
		return bqShapeBaseURL;
	}
	public void setBqShapeBaseURL(String bqShapeBaseURL) {
		this.bqShapeBaseURL = bqShapeBaseURL;
	}
	public File getDirectory() {
		return directory;
	}
	public void setDirectory(File gcpDir) {
		this.directory = gcpDir;
	}
	public String getBigQueryDatasetId() {
		return bigQueryDatasetId;
	}
	public void setBigQueryDatasetId(String bigQueryDatasetId) {
		this.bigQueryDatasetId = bigQueryDatasetId;
	}

	public File gcpDir(RdfConfig defaults) {
		if (directory == null) {
			File root = defaults.getRootDir();
			if (root != null) {
				directory = new File(root, "gcp");
			}
		}
		return directory;
	}

	public DataServicesConfig getDataServices() {
		return dataServices;
	}

	public void setDataServices(DataServicesConfig dataServices) {
		this.dataServices = dataServices;
	}

	public File getCredentials() {
		return credentials;
	}

	public void setCredentials(File credentials) {
		this.credentials = credentials;
	}

	public boolean isEnableBigQueryTransform() {
		return enableBigQueryTransform;
	}

	public void setEnableBigQueryTransform(boolean enableBigQueryTransform) {
		this.enableBigQueryTransform = enableBigQueryTransform;
	}
	
	public boolean isEnableMySqlTransform() {
		return enableMySqlTransform;
	}

	public void setEnableMySqlTransform(boolean enableMySqlTransform) {
		this.enableMySqlTransform = enableMySqlTransform;
	}

	public BigQueryInfo getBigquery() {
		return bigquery;
	}

	public void setBigquery(BigQueryInfo bigQuery) {
		this.bigquery = bigQuery;
	}

	public CloudStorageInfo getCloudstorage() {
		return cloudstorage;
	}

	public void setCloudstorage(CloudStorageInfo cloudStorage) {
		this.cloudstorage = cloudStorage;
	}

	public GroovyDeploymentScript getDeployment() {
		return deployment;
	}

	public void setDeployment(GroovyDeploymentScript deployment) {
		this.deployment = deployment;
	}

	public File getTopicsFile() {
		return topicsFile;
	}

	public void setTopicsFile(File topicsFile) {
		this.topicsFile = topicsFile;
	}

	public CloudSqlInfo getCloudsql() {
		return cloudsql;
	}

	public void setCloudsql(CloudSqlInfo cloudsql) {
		this.cloudsql = cloudsql;
	}

	@Override
	public File getRdfDirectory() {
		return rdfDirectory;
	}
	
	public void setRdfDirectory(File rdfDirectory) {
		this.rdfDirectory = rdfDirectory;
	}
	
	public File getCamelEtl() {
		return camelEtl;
	}

	public void setCamelEtl(File camelEtl) {
		this.camelEtl = camelEtl;
	}


	public String getBigqueryEnumShapeIriTemplate() {
		return bigqueryEnumShapeIriTemplate;
	}

	public void setBigqueryEnumShapeIriTemplate(String bigqueryEnumShapeIriTemplate) {
		this.bigqueryEnumShapeIriTemplate = bigqueryEnumShapeIriTemplate;
	}

	public EtlPipelineConfig getEtlPipeline() {
		return etlPipeline;
	}

	public void setEtlPipeline(EtlPipelineConfig etlPipeline) {
		this.etlPipeline = etlPipeline;
	}
	
}
