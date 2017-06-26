package io.konig.deploy.gcp;

import java.io.File;

import io.konig.yaml.Yaml;

public class CloudStorageInfo {
	
	@Parameter(property="konig.deploy.gcp.cloudstorage.directory", defaultValue="${konig.deploy.gcp.directory}/cloudstorage")
	private File directory;

	@Parameter(property="konig.deploy.gcp.cloudstorage.bucketSuffix")
	private String bucketSuffix;
	
	

	public File getDirectory() {
		return directory;
	}

	public void setDirectory(File directory) {
		this.directory = directory;
	}

	public String getBucketSuffix() {
		return bucketSuffix;
	}

	public void setBucketSuffix(String bucketSuffix) {
		this.bucketSuffix = bucketSuffix;
	}

	public String toString() {
		return Yaml.toString(this);
	}

}
