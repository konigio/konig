package io.konig.datacatalog;

/*
 * #%L
 * Konig Data Catalog
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


import io.konig.core.NamespaceManager;
import io.konig.core.OwlReasoner;

public class DataCatalogPathFactory extends PathFactory {

	private String rootPrefix;
	
	public DataCatalogPathFactory(OwlReasoner reasoner, NamespaceManager nsManager, String rootPrefix) {
		super(reasoner, nsManager);
		this.rootPrefix = rootPrefix;
	}

	@Override
	protected boolean excludePrefix(String prefix) {
		return rootPrefix.equals(prefix);
	}

}
