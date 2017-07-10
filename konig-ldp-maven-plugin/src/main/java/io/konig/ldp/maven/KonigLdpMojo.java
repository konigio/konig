package io.konig.ldp.maven;

/*
 * #%L
 * Konig LDP Maven Plugin
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
import java.nio.file.Files;
import java.nio.file.Path;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
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
 */

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import io.konig.ldp.LdpException;
import io.konig.ldp.ResourceFile;
import io.konig.ldp.client.LdpClient;

/**
 * Goal which publishes artifacts to a Linked Data Platform server
 *
 */
@Mojo( name = "publish" )
public class KonigLdpMojo  extends AbstractMojo {
	
    
    @Parameter
    private Put[] files;
   
    private LdpClient client = new LdpClient();
    

    public void execute() throws MojoExecutionException   {
    	
    	if (files != null) {
    		for (Put pub : files) {
    			doPublish(pub);
    		}
    	}
    }



	private void doPublish(Put pub) throws MojoExecutionException {
		
		File file = pub.getFile();
		String url = pub.getUrl();
		String contentType = pub.getContentType();
		
		if (file == null) {
			throw new MojoExecutionException("file parameter must be defined");
		}
		
		if (url == null) {
			throw new MojoExecutionException("url must be defined for file " + file.getAbsolutePath());
		}
		
		if (contentType == null) {
			throw new MojoExecutionException("contentType must be defined for file " + file.getAbsolutePath());
		}

		try {
			Path path = file.toPath();
			byte[] body = Files.readAllBytes(path);
			
			ResourceFile resource = client.getResourceBuilder()
				.contentLocation(url)
				.contentType(contentType)
				.entityBody(body)
				.resource();
		
			client.put(resource);
		} catch (IOException | LdpException e) {
			throw new MojoExecutionException("Failed to put file " + file.getName(), e);
		}
		
		
	}


}
