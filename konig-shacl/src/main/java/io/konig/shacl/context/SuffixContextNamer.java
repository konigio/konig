package io.konig.shacl.context;

/*
 * #%L
 * konig-shacl
 * %%
 * Copyright (C) 2015 Gregory McFall
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

/**
 * A ContextNamer that appends a suffix to the URI for a data shape to form
 * the URI for the corresponding JSON-LD context.
 * @author Greg McFall
 *
 */
public class SuffixContextNamer implements ContextNamer {
	
	private String suffix;

	public SuffixContextNamer(String suffix) {
		this.suffix = suffix;
	}

	public URI forShape(URI shapeId) {
		String value = shapeId.stringValue() + suffix;
		return new URIImpl(value);
	}


}
