package io.konig.maven;

/*
 * #%L
 * Konig Maven Common
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

public class WorkbookProcessorConfig {
	private File workbookFile;
	private File workbookDir;
	
	private File templateDir;
	
	private boolean inferRdfPropertyDefinitions=true;
	private boolean normalizeTerms=true;
	private boolean failOnWarnings = false;
	private boolean failOnErrors = true;
	private int maxErrorCount = 0;
	
	public File getWorkbookFile() {
		return workbookFile;
	}
	public boolean isInferRdfPropertyDefinitions() {
		return inferRdfPropertyDefinitions;
	}
	public void setInferRdfPropertyDefinitions(boolean inferRdfPropertyDefinitions) {
		this.inferRdfPropertyDefinitions = inferRdfPropertyDefinitions;
	}
	public void setWorkbookFile(File workbookFile) {
		this.workbookFile = workbookFile;
	}
	public boolean isFailOnWarnings() {
		return failOnWarnings;
	}
	public void setFailOnWarnings(boolean failOnWarnings) {
		this.failOnWarnings = failOnWarnings;
	}
	public boolean isFailOnErrors() {
		return failOnErrors;
	}
	public void setFailOnErrors(boolean failOnErrors) {
		this.failOnErrors = failOnErrors;
	}
	public File getWorkbookDir() {
		return workbookDir;
	}
	public void setWorkbookDir(File workbookDir) {
		this.workbookDir = workbookDir;
	}
	public boolean isNormalizeTerms() {
		return normalizeTerms;
	}
	public void setNormalizeTerms(boolean normalizeTerms) {
		this.normalizeTerms = normalizeTerms;
	}
	public int getMaxErrorCount() {
		return maxErrorCount;
	}
	public void setMaxErrorCount(int maxErrorCount) {
		this.maxErrorCount = maxErrorCount;
	}
	public File getTemplateDir() {
		return templateDir;
	}
	public void setTemplateDir(File templateDir) {
		this.templateDir = templateDir;
	}
	
}
