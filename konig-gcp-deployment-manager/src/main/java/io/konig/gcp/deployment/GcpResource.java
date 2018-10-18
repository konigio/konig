package io.konig.gcp.deployment;

public interface GcpResource<T> {
	
	String getName();
	String getType();
	T getProperties();
	

}
