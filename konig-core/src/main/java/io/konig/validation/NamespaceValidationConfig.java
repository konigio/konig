package io.konig.validation;

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


public class NamespaceValidationConfig {
	
	private String namespaceName;
	private boolean ignoreRangeConflicts;
	
	public String getNamespaceName() {
		return namespaceName;
	}
	public void setNamespaceName(String namespaceName) {
		this.namespaceName = namespaceName;
	}
	public boolean isIgnoreRangeConflicts() {
		return ignoreRangeConflicts;
	}
	public void setIgnoreRangeConflicts(boolean ignoreRangeConflicts) {
		this.ignoreRangeConflicts = ignoreRangeConflicts;
	}
	
	

}
