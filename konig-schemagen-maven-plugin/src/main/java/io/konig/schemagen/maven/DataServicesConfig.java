package io.konig.schemagen.maven;

import java.io.File;

public class DataServicesConfig {
	
	private File infoFile;
	private File openApiFile;
	private File configFile;
	
	public DataServicesConfig() {
		
	}

	public File getInfoFile() {
		return this.infoFile == null ?
				new File("src/dataservices/openapi-info.yaml") :
				infoFile;
	}

	public void setInfoFile(File infoFile) {
		this.infoFile = infoFile;
	}

	public File getOpenApiFile() {
		return openApiFile == null ?
			new File("target/generated/dataservices/openapi.yaml") :
			openApiFile;
	}

	public void setOpenApiFile(File openApiFile) {
		this.openApiFile = openApiFile;
	}

	public File getConfigFile() {
		return configFile == null ?
			new File("target/generated/dataservices/app.yaml") :
			configFile;
	}

	public void setConfigFile(File configFile) {
		this.configFile = configFile;
	}
	
	

	
}
