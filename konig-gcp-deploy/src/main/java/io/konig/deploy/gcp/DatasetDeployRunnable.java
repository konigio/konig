package io.konig.deploy.gcp;

import io.konig.deploy.DeployAction;

public class DatasetDeployRunnable implements Runnable {
	private DeployAction action;
	private GoogleCloudPlatformInfo googleCloudPlatform;

	@Override
	public void run() {
		
		System.out.println("action: " + action);
		
		
	}

	public DeployAction getAction() {
		return action;
	}

	public void setAction(DeployAction action) {
		this.action = action;
	}

	public GoogleCloudPlatformInfo getGoogleCloudPlatform() {
		return googleCloudPlatform;
	}

	public void setGoogleCloudPlatform(GoogleCloudPlatformInfo googleCloudPlatform) {
		this.googleCloudPlatform = googleCloudPlatform;
	}

}
