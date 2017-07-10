package io.konig.maven.content.system;

/*
 * #%L
 * Konig Content System Maven Plugin
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
