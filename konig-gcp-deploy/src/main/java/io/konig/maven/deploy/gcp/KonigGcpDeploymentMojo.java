package io.konig.maven.deploy.gcp;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import io.konig.deploy.DeployAction;
import io.konig.deploy.gcp.GcpDeployRunnable;
import io.konig.deploy.gcp.GoogleCloudPlatformInfo;

@Mojo( name = "gcpDeploy")
public class KonigGcpDeploymentMojo extends AbstractMojo {

	@Parameter(property="konig.deploy.action", defaultValue="upsert")
	private String action;
	
	@Parameter
	private GoogleCloudPlatformInfo googleCloudPlatform;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		
		DeployAction deployAction = Enum.valueOf(DeployAction.class, action.toUpperCase());
		GcpDeployRunnable runnable = new GcpDeployRunnable();
		runnable.setAction(deployAction);
		runnable.setGoogleCloudPlatform(googleCloudPlatform);
		
		runnable.run();

	}

}
