package io.konig.schemagen.sql;

/*
 * #%L
 * Konig Schema Generator
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


import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

import io.konig.core.util.StringUtil;
import io.konig.shacl.ShapeManager;

public class TabularShapeContext {
	
	private String tabularPropertyNamespace;
	private ShapeManager shapeManager;

	public String getTabularPropertyNamespace() {
		return tabularPropertyNamespace;
	}

	public void setTabularPropertyNamespace(String tabularPropertyNamespace) {
		this.tabularPropertyNamespace = tabularPropertyNamespace;
	}
	
	URI snakeCaseName(URI predicate) throws TabularShapeException {
		if (tabularPropertyNamespace == null) {
			throw new TabularShapeException("tabularPropertyNamespace must be defined");
		}
		
		String newLocalName = StringUtil.SNAKE_CASE(predicate.getLocalName());
		return new URIImpl(tabularPropertyNamespace + newLocalName);
	}
	
	

}
