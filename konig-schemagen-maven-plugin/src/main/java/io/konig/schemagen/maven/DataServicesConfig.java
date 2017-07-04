package io.konig.schemagen.maven;

import java.io.File;

public class DataServicesConfig {
	
	private File basedir;
	private File infoFile;
	private File openApiFile;
	private File configFile;
	private File webappDir;
	
	public DataServicesConfig() {
		
	}
	
	

	public File getBasedir() {
		return basedir==null ?
			new File("target/generated/dataservices") :
			basedir;
	}

	public void setBasedir(File basedir) {
		this.basedir = basedir;
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
			new File(getWebappDir(), "openapi.yaml") :
			openApiFile;
	}

	public void setOpenApiFile(File openApiFile) {
		this.openApiFile = openApiFile;
	}

	public File getConfigFile() {
		return configFile == null ?
			new File(getWebappDir(), "WEB-INF/classes/app.yaml") :
			configFile;
	}

	public void setConfigFile(File configFile) {
		this.configFile = configFile;
	}



	public File getWebappDir() {
		if (webappDir == null) {
			webappDir = new File(getBasedir(), "src/main/webapp");
		}
		return webappDir;
	}



	public void setWebappDir(File webappDir) {
		this.webappDir = webappDir;
	}
	
	

	
}
