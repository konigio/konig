package io.konig.schemagen.maven;

import java.io.File;

public class GoogleCloudPlatformConfig {
	

    private String bqShapeBaseURL;
    private File gcpDir;
	private String bigQueryDatasetId;
	private File enumShapeDir;
	private String enumShapeNameTemplate;
	
	public GoogleCloudPlatformConfig() {
		
	}
	
	public String getBqShapeBaseURL() {
		return bqShapeBaseURL;
	}
	public void setBqShapeBaseURL(String bqShapeBaseURL) {
		this.bqShapeBaseURL = bqShapeBaseURL;
	}
	public File getGcpDir() {
		return gcpDir;
	}
	public void setGcpDir(File gcpDir) {
		this.gcpDir = gcpDir;
	}
	public String getBigQueryDatasetId() {
		return bigQueryDatasetId;
	}
	public void setBigQueryDatasetId(String bigQueryDatasetId) {
		this.bigQueryDatasetId = bigQueryDatasetId;
	}

	public File getEnumShapeDir() {
		return enumShapeDir;
	}

	public void setEnumShapeDir(File enumShapeDir) {
		this.enumShapeDir = enumShapeDir;
	}

	public String getEnumShapeNameTemplate() {
		return enumShapeNameTemplate;
	}

	public void setEnumShapeNameTemplate(String enumShapeNameTemplate) {
		this.enumShapeNameTemplate = enumShapeNameTemplate;
	}
	
}
