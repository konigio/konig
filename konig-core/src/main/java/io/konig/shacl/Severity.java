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


import org.openrdf.model.URI;

import io.konig.core.vocab.SH;

public enum Severity {

	INFO(SH.Info),
	WARNING(SH.Warning),
	VIOLATION(SH.Violation);
	
	private URI uri;
	
	private Severity(URI uri) {
		this.uri = uri;
	}
	
	public URI getURI() {
		return uri;
	}
	

	public static Severity fromURI(URI uri) {
		if (INFO.uri.equals(uri)) {
			return INFO;
		}
		if (WARNING.uri.equals(uri)) {
			return WARNING;
		}
		if (VIOLATION.uri.equals(uri)) {
			return VIOLATION;
		}
		
		return null;
	}
	
}
