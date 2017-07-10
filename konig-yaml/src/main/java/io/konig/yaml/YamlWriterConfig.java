package io.konig.yaml;

/*
 * #%L
 * Konig YAML
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
