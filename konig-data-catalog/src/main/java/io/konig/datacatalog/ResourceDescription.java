package io.konig.datacatalog;

public class ResourceDescription {

	private String href;
	private String name;
	private String description;
	
	public ResourceDescription(String href, String name, String description) {
		this.href = href;
		this.name = name;
		this.description = description;
	}

	public String getHref() {
		return href;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}
	
	

}
