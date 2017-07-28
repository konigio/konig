package io.konig.maven;

import java.util.Map.Entry;
import java.util.Properties;

import org.apache.maven.model.Plugin;

/*
 * #%L
 * Konig GCP Deployment Maven Plugin
 * %%
 * Copyright (C) 2015 - 2017 Gregory McFall
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import io.konig.deploy.gcp.DeploymentException;
import io.konig.deploy.gcp.GcpDeployRunnable;

@Mojo( name = "gcpDeploy")
public class KonigGcpDeploymentMojo extends AbstractMojo {

	@Parameter(property="konig.deploy.action", defaultValue="preview")
	private String action;
	
	@Parameter(property="konig.deploy.timestamp")
	private String timestamp;
	
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
			runnable.setModifiedTimestamp(timestamp);
			runnable.setGoogleCloudPlatform(gcp);
		
			runnable.run();
		} catch (ConfigurationException | DeploymentException e) {
			throw new MojoExecutionException("Deployment failed", e);
		}

	}

	private void configure() throws ConfigurationException {
		if (gcp == null) {
			gcp = new GoogleCloudPlatformInfo();
		}
		Properties properties = new Properties(System.getProperties());
		
		for (Entry<Object,Object> e : project.getProperties().entrySet()) {
			String key = e.getKey().toString();
			String value = e.getValue().toString();
			properties.put(key, value);
		}
		
		if (!properties.containsKey("konig.version")) {
			Plugin plugin = project.getPlugin("io.konig:konig-gcp-deploy-maven-plugin");
			if (plugin != null) {
				properties.setProperty("konig.version", plugin.getVersion());
			}
		}
		
		Configurator configurator = new Configurator(properties);
		configurator.configure(gcp);
	}

}
