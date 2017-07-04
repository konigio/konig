package io.konig.openapi.model;

public class Response {
	
	private String statusCode;
	private String description;
	private MediaTypeMap content;
	
	public Response(String statusCode) {
		this.statusCode = statusCode;
	}

	public MediaTypeMap getContent() {
		return content;
	}

	public void setContent(MediaTypeMap content) {
		this.content = content;
	}
	
	public String statusCode() {
		return statusCode;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}


}
