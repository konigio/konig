package io.konig.shacl;

/*
 * #%L
 * Konig SHACL
 * %%
 * Copyright (C) 2015 - 2016 Gregory McFall
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


import org.openrdf.model.Resource;
import org.openrdf.model.URI;

public class ValidationResult {

	private Resource id;
	private Resource focusNode;
	private URI path;
	private String message;
	private Resource sourceShape;
	private Severity severity;
	
	public Resource getId() {
		return id;
	}
	public void setId(Resource id) {
		this.id = id;
	}
	public Resource getFocusNode() {
		return focusNode;
	}
	public void setFocusNode(Resource focusNode) {
		this.focusNode = focusNode;
	}
	public URI getPath() {
		return path;
	}
	public void setPath(URI path) {
		this.path = path;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public Resource getSourceShape() {
		return sourceShape;
	}
	public void setSourceShape(Resource sourceShape) {
		this.sourceShape = sourceShape;
	}
	public Severity getSeverity() {
		return severity;
	}
	public void setSeverity(Severity severity) {
		this.severity = severity;
	}
	

}
