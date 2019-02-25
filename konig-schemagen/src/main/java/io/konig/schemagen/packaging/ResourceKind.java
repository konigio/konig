package io.konig.schemagen.packaging;

import java.text.MessageFormat;

public enum ResourceKind {
	gcp,
	aws;
	
	public String getAssemblyTemplate() {
		return MessageFormat.format("io/konig/schemagen/packaging/{0}/dep.xml", this.name());
	}
	
	public String getPomTemplate() {
		return MessageFormat.format("io/konig/schemagen/packaging/pom.xml", this.name());
	}
	
	
}
