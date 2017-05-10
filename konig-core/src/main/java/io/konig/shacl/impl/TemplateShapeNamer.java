package io.konig.shacl.impl;

/*
 * #%L
 * Konig Core
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


import org.openrdf.model.Namespace;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

import io.konig.core.NamespaceManager;
import io.konig.core.util.SimpleValueMap;
import io.konig.core.util.ValueFormat;
import io.konig.shacl.ShapeNamer;

public class TemplateShapeNamer implements ShapeNamer {
	private NamespaceManager nsManager;
	private ValueFormat format;

	public TemplateShapeNamer(NamespaceManager nsManager, ValueFormat format) {
		this.nsManager = nsManager;
		this.format = format;
	}



	@Override
	public URI shapeName(URI scopeClass) {
		SimpleValueMap map = new SimpleValueMap();
		map.put("targetClass.localName", scopeClass.getLocalName());
		map.put("targetClassLocalName", scopeClass.getLocalName());
		Namespace ns = nsManager.findByName(scopeClass.getNamespace());
		if (ns != null) {
			map.put("targetClass.namespacePrefix", ns.getPrefix());
			map.put("targetClassNamespacePrefix", ns.getPrefix());
		}
		String value = format.format(map);
		return new URIImpl(value);
	}

}
