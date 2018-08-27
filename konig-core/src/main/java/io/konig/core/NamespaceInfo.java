package io.konig.core;

/*
 * #%L
 * Konig Core
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


import java.util.HashSet;
import java.util.Set;

import org.openrdf.model.URI;

/**
 * A set that holds all the terms for a given Namespace.
 * @author Greg McFall
 *
 */
public class NamespaceInfo {
	
	private String namespaceIri;
	private Set<URI> type = new HashSet<>();
	private Set<URI> terms = new HashSet<>();
	
	
	public NamespaceInfo(String namespaceIri) {
		this.namespaceIri = namespaceIri;
	}
	
	public Set<URI> getType() {
		return type;
	}
	
	public Set<URI> getTerms() {
		return terms;
	}


	public String getNamespaceIri() {
		return namespaceIri;
	}
	
	public String toString() {
		return "NamespaceInfo[" + namespaceIri + "]";
	}

}
