package io.konig.yaml;

public class YamlWriterConfig {


	protected int indentSpaces = 3;
	protected boolean includeClassTag = true;
	protected AnchorFeature anchorFeature = AnchorFeature.ALL;
	
	public int getIndentSpaces() {
		return indentSpaces;
	}
	
	public YamlWriterConfig setIndentSpaces(int indentSpaces) {
		this.indentSpaces = indentSpaces;
		return this;
	}
	

	
	public boolean isIncludeClassTag() {
		return includeClassTag;
	}

	public YamlWriterConfig setIncludeClassTag(boolean includeClassTag) {
		this.includeClassTag = includeClassTag;
		return this;
	}

	public AnchorFeature getAnchorFeature() {
		return anchorFeature;
	}

	public YamlWriterConfig setAnchorFeature(AnchorFeature anchorFeature) {
		this.anchorFeature = anchorFeature;
		return this;
	}

	public void configure(YamlWriterConfig config) {
		indentSpaces = config.indentSpaces;
		includeClassTag = config.includeClassTag;
		anchorFeature = config.anchorFeature;
	}
	
	

}
