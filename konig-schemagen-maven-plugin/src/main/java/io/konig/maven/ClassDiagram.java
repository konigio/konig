package io.konig.maven;

/*
 * #%L
 * Konig Schema Generator Maven Plugin
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
import java.util.HashSet;
import java.util.Set;

import org.openrdf.model.URI;

import io.konig.core.NamespaceManager;
import io.konig.core.impl.RdfUtil;
import io.konig.schemagen.plantuml.PlantumlClassDiagramGenerator;

public class ClassDiagram {
	private File file = null;
	private boolean showAssociations=true;
	private boolean showSubClassOf = true;
	private boolean showAttributes = true;
	private boolean showOwlThing = false;
	private boolean showEnumerationClasses = false;
	private Set<String> includeClass = null;
	private Set<String> excludeClass = null;
	private NamespaceManager nsManager;
	
	
	public ClassDiagram() {
	}
	
	public NamespaceManager getNamespaceManager() {
		return nsManager;
	}

	public void setNamespaceManager(NamespaceManager nsManager) {
		this.nsManager = nsManager;
	}

	public File getFile() {
		return file;
	}

	public void setFile(File classDiagramFile) {
		this.file = classDiagramFile;
	}

	public boolean isShowEnumerationClasses() {
		return showEnumerationClasses;
	}

	public void setShowEnumerationClasses(boolean showEnumerationClasses) {
		this.showEnumerationClasses = showEnumerationClasses;
	}

	public boolean isShowAssociations() {
		return showAssociations;
	}
	public void setShowAssociations(boolean showAssociations) {
		this.showAssociations = showAssociations;
	}
	public boolean isShowSubClassOf() {
		return showSubClassOf;
	}
	public void setShowSubClassOf(boolean showSubclassOf) {
		this.showSubClassOf = showSubclassOf;
	}
	public boolean isShowAttributes() {
		return showAttributes;
	}
	public void setShowAttributes(boolean showAttributes) {
		this.showAttributes = showAttributes;
	}
	public boolean isShowOwlThing() {
		return showOwlThing;
	}
	public void setShowOwlThing(boolean showOwlThing) {
		this.showOwlThing = showOwlThing;
	}
	public Set<String> getIncludeClass() {
		return includeClass;
	}
	public void setIncludeClass(Set<String> includeClass) {
		this.includeClass = includeClass;
	}
	public Set<String> getExcludeClass() {
		return excludeClass;
	}
	public void setExcludeClass(Set<String> excludeClass) {
		this.excludeClass = excludeClass;
	}
	
	public void configure(PlantumlClassDiagramGenerator generator) {
		generator.setExcludeClass(uriSet(excludeClass));
		generator.setIncludeClass(uriSet(includeClass));
		generator.setShowAssociations(showAssociations);
		generator.setShowAttributes(showAttributes);
		generator.setShowEnumerationClasses(showEnumerationClasses);
		generator.setShowOwlThing(showOwlThing);
		generator.setShowSubclassOf(showSubClassOf);
	}
	private Set<URI> uriSet(Set<String> source) {
		Set<URI> result = null;
		if (source!=null && !source.isEmpty()) {
			result = new HashSet<>();
			for (String curie : source) {
				result.add(RdfUtil.expand(nsManager, curie));
			}
		}
		return result;
	}
}
