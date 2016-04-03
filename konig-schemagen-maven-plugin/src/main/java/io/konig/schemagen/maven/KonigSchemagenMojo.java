package io.konig.schemagen.maven;

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
import org.apache.maven.plugins.annotations.ResolutionScope;

import io.konig.schemagen.avro.ShapeToAvro;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Goal which generates Avro schemas from SHACL data shapes
 *
 */
@Mojo( name = "generate", defaultPhase = LifecyclePhase.GENERATE_SOURCES )
public class KonigSchemagenMojo  extends AbstractMojo {
    /**
     * Location of the file.
     */
    @Parameter( defaultValue = "${basedir}/target/generated/avro", property = "targetDir", required = true )
    private File targetDir;
    
    @Parameter( defaultValue="${basedir}/src/main/resources/shapes", property="sourceDir", required=true)
    private File sourceDir;

    public void execute() throws MojoExecutionException   {
    	
    	try {
			ShapeToAvro worker = new ShapeToAvro(null);
			
			worker.generateAvro(sourceDir, targetDir);
		} catch (IOException e) {
			throw new MojoExecutionException("Failed to convert shapes to Avro", e);
		}
      
    }
}
