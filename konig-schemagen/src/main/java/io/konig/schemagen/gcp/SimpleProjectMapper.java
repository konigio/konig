package io.konig.schemagen.gcp;

import io.konig.core.Vertex;

public class SimpleProjectMapper implements ProjectMapper {

	private String projectId;
	
	
	public SimpleProjectMapper(String projectId) {
		this.projectId = projectId;
	}


	@Override
	public String projectForClass(Vertex owlClass) {
		return projectId;
	}

}
