package io.konig.gcp.deployment;

public class BaseGcpResource<T> implements GcpResource<T> {
	private String name;
	private String type;
	private T properties;
	private GcpMetadata metadata;

	public BaseGcpResource(String type) {
		this.type = type;
	}



	@Override
	public String getName() {
		return name;
	}
	
	

	public void setProperties(T properties) {
		this.properties = properties;
	}



	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getType() {
		return type;
	}

	@Override
	public T getProperties() {
		return properties;
	}
	
	public GcpMetadata getMetadata() {
		return metadata;
	}
	
	public void setMetadata(GcpMetadata metadata) {
		this.metadata = metadata;
	}
	
	

}
