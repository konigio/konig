package io.konig.gcp.deployment;

import java.util.ArrayList;
import java.util.List;

public class GcpMetadata {
	
	private List<String> dependsOn;

	public List<String> getDependsOn() {
		return dependsOn;
	}
	
	public void addDependency(String resourceName) {
		if (dependsOn == null) {
			dependsOn = new ArrayList<>();
		}
		dependsOn.add(resourceName);
	}

}
