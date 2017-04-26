package io.konig.maven.content.system;

import java.io.File;
import java.io.IOException;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import io.konig.content.ContentAccessException;
import io.konig.content.client.ContentPublisher;

@Mojo( name = "publish")
public class ContentSystemMojo extends AbstractMojo {
	
	@Parameter(required=true)
	private File baseDir;
	
	@Parameter(required=true)
	private String baseURL;
	
	@Parameter(required=true)
	private String bundleName;
	
	@Parameter(required=true)
	private String bundleVersion;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		
		ContentPublisher publisher = new ContentPublisher();
		try {
			publisher.publish(baseDir, baseURL, bundleName, bundleVersion);
		} catch (IOException | ContentAccessException e) {
			throw new MojoExecutionException("Failed to publish /" + bundleName + "/" + bundleVersion, e);
		}

	}

}
