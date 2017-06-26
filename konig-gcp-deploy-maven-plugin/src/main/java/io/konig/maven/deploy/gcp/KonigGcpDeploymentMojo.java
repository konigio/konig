package io.konig.maven.deploy.gcp;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import io.konig.deploy.DeployAction;
import io.konig.deploy.gcp.Configurator;
import io.konig.deploy.gcp.DeploymentException;
import io.konig.deploy.gcp.GcpDeployRunnable;
import io.konig.deploy.gcp.GoogleCloudPlatformInfo;

@Mojo( name = "gcpDeploy")
public class KonigGcpDeploymentMojo extends AbstractMojo {

	@Parameter(property="konig.deploy.action", defaultValue="upsert")
	private String action;
	
	@Parameter
	private GoogleCloudPlatformInfo gcp;

	@Parameter(defaultValue="${project}", readonly=true, required=true)
	private MavenProject project;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {

		try {
			configure();
			
			DeployAction deployAction = Enum.valueOf(DeployAction.class, action.toUpperCase());
			GcpDeployRunnable runnable = new GcpDeployRunnable();
			runnable.setAction(deployAction);
			runnable.setGoogleCloudPlatform(gcp);
		
			runnable.run();
		} catch (DeploymentException e) {
			throw new MojoExecutionException("Deployment failed", e);
		}

	}

	private void configure() throws DeploymentException {
		if (gcp == null) {
			gcp = new GoogleCloudPlatformInfo();
		}
		Configurator configurator = new Configurator(System.getProperties());
		configurator.configure(gcp);
	}

}
