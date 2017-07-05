package io.konig.schemagen.maven;

import java.io.File;

public class GoogleCloudPlatformConfig {
	

    private String bqShapeBaseURL;
    private File gcpDir;
	private String bigQueryDatasetId;
	private File enumShapeDir;
	private String enumShapeNameTemplate;
    private File credentials;
	private DataServicesConfig dataServices;
	
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

	public File gcpDir(RdfConfig defaults) {
		if (gcpDir == null) {
			File root = defaults.getRootDir();
			if (root != null) {
				gcpDir = new File(root, "gcp");
			}
		}
		return gcpDir;
	}

	public File enumShapeDir(RdfConfig defaults) {
		if (enumShapeDir == null) {
			enumShapeDir = defaults.getShapesDir();
		}
		return enumShapeDir;
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
	
	
	
}
