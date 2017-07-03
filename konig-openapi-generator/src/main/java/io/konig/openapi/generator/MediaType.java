package io.konig.openapi.generator;

public class MediaType {

	private String type;
	private String subtype;
	private String refValue;
	
	public MediaType(String fullName, String refValue) {
		this.type = fullName;
		int slash = fullName.indexOf('/');
		subtype = type.substring(slash+1);
		this.refValue = refValue;
	}

	public String getType() {
		return type;
	}

	public String getSubtype() {
		return subtype;
	}

	public String getRefValue() {
		return refValue;
	}
	

}
