package io.konig.gcp.deployment;

import java.util.ArrayList;
import java.util.List;

public class DeploymentConfig {
	private List<GcpResource<?>> resources = new ArrayList<>();
	
	public void addResource(GcpResource<?> resource) {
		resources.add(resource);
	}

	public List<GcpResource<?>> getResources() {
		return resources;
	}
	
	

}
