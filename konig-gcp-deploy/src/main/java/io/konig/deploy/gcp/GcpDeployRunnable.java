package io.konig.deploy.gcp;

import io.konig.deploy.DeployAction;

public class GcpDeployRunnable implements Runnable {
	
	private DeployAction action;
	private GoogleCloudPlatformInfo googleCloudPlatform;

	@Override
	public void run() {
		
		// TODO: Remove the following print statements
		//       and replace them with the required functionality.
		
		System.out.println("action: " + action);
		if (googleCloudPlatform != null) {
			System.out.println(googleCloudPlatform);
		}
		
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
