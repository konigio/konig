package io.konig.schemagen.maven;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.openrdf.model.URI;

import io.konig.core.NamespaceManager;
import io.konig.core.impl.RdfUtil;
import io.konig.schemagen.plantuml.PlantumlClassDiagramGenerator;

public class PlantumlConfig {
	private File classDiagramFile = null;
	private boolean showAssociations=false;
	private boolean showSubClassOf = false;
	private boolean showAttributes = false;
	private boolean showOwlThing = false;
	private boolean showEnumerationClasses = false;
	private Set<String> includeClass = null;
	private Set<String> excludeClass = null;
	private NamespaceManager nsManager;
	
	
	public PlantumlConfig() {
	}
	
	public NamespaceManager getNamespaceManager() {
		return nsManager;
	}

	public void setNamespaceManager(NamespaceManager nsManager) {
		this.nsManager = nsManager;
	}

	public File getClassDiagramFile() {
		return classDiagramFile;
	}

	public void setClassDiagramFile(File classDiagramFile) {
		this.classDiagramFile = classDiagramFile;
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
