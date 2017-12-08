package io.konig.estimator;

import java.io.File;

import org.openrdf.model.URI;

public class SizeEstimateRequest {

	private URI shapeId;	
	private File sampleDataDir;
	
	public SizeEstimateRequest(URI shapeId, File sampleDataDir) {
		this.shapeId = shapeId;
		this.sampleDataDir = sampleDataDir;
	}

	public URI getShapeId() {
		return shapeId;
	}

	public File getSampleDataDir() {
		return sampleDataDir;
	}
	
	
	
	

}
