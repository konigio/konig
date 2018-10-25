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

import io.konig.validation.CaseStyleConventions;
import io.konig.validation.CommentConventions;
import io.konig.validation.NamespaceValidationConfig;

public class ModelValidationConfig {
	
	@Parameter(property="konig.validation.textReportFile", defaultValue="${project.basedir}/target/generated/rdf/validation.txt")
	private File textReportFile;
	
	private CaseStyleConventions namingConventions;
	private CommentConventions commentConventions;
	private NamespaceValidationConfig[] namespaces;

	public File getTextReportFile() {
		return textReportFile;
	}

	public void setTextReportFile(File textReportFile) {
		this.textReportFile = textReportFile;
	}

	public CaseStyleConventions getNamingConventions() {
		return namingConventions;
	}

	public void setNamingConventions(CaseStyleConventions namingConventions) {
		this.namingConventions = namingConventions;
	}

	public CommentConventions getCommentConventions() {
		return commentConventions;
	}

	public void setCommentConventions(CommentConventions commentConventions) {
		this.commentConventions = commentConventions;
	}

	public NamespaceValidationConfig[] getNamespaces() {
		return namespaces;
	}

	public void setNamespaces(NamespaceValidationConfig[] namespaces) {
		this.namespaces = namespaces;
	}
	
	
	

}
