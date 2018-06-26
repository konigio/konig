package io.konig.maven;

public class IriTemplateConfig {

	private String iriPattern;
	private String iriReplacement;
	
	public IriTemplateConfig() {
	}

	
	public String getIriPattern() {
		return iriPattern;
	}


	public void setIriPattern(String iriPattern) {
		this.iriPattern = iriPattern;
	}


	public String getIriReplacement() {
		return iriReplacement;
	}


	public void setIriReplacement(String iriReplacement) {
		this.iriReplacement = iriReplacement;
	}


	public String generateIri(String entityName) {
		return entityName.replaceAll(iriPattern, iriReplacement);
	}

}
