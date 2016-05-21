package io.konig.ldp.maven;

import java.io.File;

public class Put {
	
	private File file;
	private String url;
	private String contentType;
	
	public File getFile() {
		return file;
	}
	public void setFile(File file) {
		this.file = file;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String targetURL) {
		this.url = targetURL;
	}
	public String getContentType() {
		return contentType;
	}
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

}
