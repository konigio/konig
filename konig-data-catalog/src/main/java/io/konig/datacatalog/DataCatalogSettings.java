package io.konig.datacatalog;

/*
 * #%L
 * Konig Data Catalog
 * %%
 * Copyright (C) 2015 - 2019 Gregory McFall
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


public class DataCatalogSettings {
	
	private ClassDisplayName classDisplayName =  ClassDisplayName.BUSINESS_NAME;
	private ClassOrEntity classOrEntity;
	private boolean mixEntitiesAndEnums = false;
	
	public ClassDisplayName getClassDisplayName() {
		return classDisplayName;
	}

	public void setClassDisplayName(ClassDisplayName classDisplayName) {
		this.classDisplayName = classDisplayName;
	}

	public ClassOrEntity getClassOrEntity() {
		return classOrEntity;
	}

	public void setClassOrEntity(ClassOrEntity classOrEntity) {
		this.classOrEntity = classOrEntity;
	}

	public boolean isMixEntitiesAndEnums() {
		return mixEntitiesAndEnums;
	}

	public void setMixEntitiesAndEnums(boolean mixEntitiesAndEnums) {
		this.mixEntitiesAndEnums = mixEntitiesAndEnums;
	}
	
	
	

}
