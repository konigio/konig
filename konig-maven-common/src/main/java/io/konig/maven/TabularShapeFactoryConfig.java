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


public class TabularShapeFactoryConfig {

	@Parameter(property="konig.tabularPropertyNamespace", required=false)
	private String tabularPropertyNamespace;
	
	@Parameter(property="konig.tabularShapeFactory.computeMaxRowSize", required=false, defaultValue="true")
	private boolean computeMaxRowSize=true;

	@Parameter(property="konig.tabularShapeFactory.linkingStrategy", required=false, defaultValue="io.konig.schemagen.sql.SyntheticKeyLinkingStrategy")
	private String linkingStrategy;

	public String getTabularPropertyNamespace() {
		return tabularPropertyNamespace;
	}

	public void setTabularPropertyNamespace(String tabularPropertyNamespace) {
		this.tabularPropertyNamespace = tabularPropertyNamespace;
	}

	public boolean getComputeMaxRowSize() {
		return computeMaxRowSize;
	}

	public void setComputeMaxRowSize(boolean computeMaxRowSize) {
		this.computeMaxRowSize = computeMaxRowSize;
	}

	public String getLinkingStrategy() {
		return linkingStrategy;
	}

	public void setLinkingStrategy(String linkingStrategy) {
		this.linkingStrategy = linkingStrategy;
	}
	
	

}
