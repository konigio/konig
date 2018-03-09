package io.konig.maven;

/*
 * #%L
 * Konig Maven Common
 * %%
 * Copyright (C) 2015 - 2018 Gregory McFall
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

public class AmazonWebServicesConfig {
	
	@Parameter(property="konig.aws.directory", defaultValue="${project.basedir}/target/generated/aws")
	private File directory;
	
	@Parameter(property="konig.aws.tables", defaultValue="${konig.aws.directory}/tables")
	private File tables;
	
	@Parameter(property="konig.aws.s3bucket", defaultValue="${konig.aws.directory}/s3buckets")
	private File s3buckets;

	@Parameter(property="konig.aws.deployment.script.file", defaultValue="${konig.aws.directory}/scripts/deploy.groovy")
	private File awsScriptFile;
	
	@Parameter(property="konig.aws.teardown.script.file", defaultValue="${konig.aws.directory}/scripts/tear-down.groovy")
	private File tearDownScriptFile;
	
	@Parameter(property="konig.aws.deployment.version", defaultValue="${konig.version}")
	private String konigVersion;
	
	@Parameter(property="konig.aws.aurora.transform", defaultValue="${konig.aws.directory}/aurora/transform")
	private File transforms;

	
	public AmazonWebServicesConfig() {
			
	}

	public File getDirectory() {
		return directory;
	}

	public void setDirectory(File directory) {
		this.directory = directory;
	}
	
	public File getTables() {
		return tables;
	}

	public void setTables(File tables) {
		this.tables = tables;
	}
	
	public String getKonigVersion() {
		return konigVersion;
	}

	public void setKonigVersion(String konigVersion) {
		this.konigVersion = konigVersion;
	}

	public File getAwsScriptFile() {
		return awsScriptFile;
	}

	public void setAwsScriptFile(File awsScriptFile) {
		this.awsScriptFile = awsScriptFile;
	}

	
	public File getS3buckets() {
		return s3buckets;
	}

	public void setS3buckets(File s3buckets) {
		this.s3buckets = s3buckets;
	}

	public File getTearDownScriptFile() {
		return tearDownScriptFile;
	}

	public void setTearDownScriptFile(File tearDownScriptFile) {
		this.tearDownScriptFile = tearDownScriptFile;
	}

	public File getTransforms() {
		return transforms;
	}

	public void setTransforms(File transforms) {
		this.transforms = transforms;
	}
	
	
}
