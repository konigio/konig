package io.konig.shacl;

/*
 * #%L
 * Konig Spreadsheet
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

import io.konig.core.HasURI;
import io.konig.core.KonigException;
import io.konig.core.vocab.Konig;

public enum RelationshipDegree implements HasURI{
	OneToOne(Konig.OneToOne),
	OneToMany(Konig.OneToMany),
	ManyToOne(Konig.ManyToOne),
	ManyToMany(Konig.ManyToMany);
	
	private URI uri;
	
	private RelationshipDegree(URI uri) {
		this.uri = uri;
	}

	public URI getURI() {
		return uri;
	}
	
	public static RelationshipDegree fromURI(URI uri) {
		if (uri == null) {
			return null;
		}
		for (RelationshipDegree value : values()) {
			if (value.getURI().equals(uri)) {
				return value;
			}
		}
		throw new KonigException("Value not found: " + uri.stringValue());
	}

	public static RelationshipDegree fromLocalName(String localName) {
		for (RelationshipDegree value : values()) {
			if (value.getURI().getLocalName().equals(localName)) {
				return value;
			}
		}
		throw new KonigException("Value not found: " + localName);
	}

}
