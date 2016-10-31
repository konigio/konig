package io.konig.core.delta;

/*
 * #%L
 * konig-core
 * %%
 * Copyright (C) 2015 - 2016 Gregory McFall
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

import java.util.HashMap;
import java.util.Map;

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;

import io.konig.core.Vertex;
import io.konig.core.vocab.Konig;

public class SimpleKeyFactory implements BNodeKeyFactory {

	private URI accessor;
	private URI keyPredicate;
	private Map<String, URI> part = new HashMap<>();

	

	public SimpleKeyFactory(URI accessor, URI keyPredicate) {
		this.accessor = accessor;
		this.keyPredicate = keyPredicate;
		part.put(keyPredicate.stringValue(), Konig.KeyValue);
	}

	@Override
	public BNodeKey createKey(URI predicate, Vertex object) {
		
		if (this.accessor.equals(predicate)) {
			Value value = object.getValue(keyPredicate);
			StringBuilder builder = new StringBuilder();
			builder.append(predicate.stringValue());
			builder.append('!');
			if (value instanceof BNode) {
				builder.append("_:");
			}
			builder.append(value.stringValue());
			if (value instanceof Literal) {
				Literal literal = (Literal) value;
				String lang = literal.getLanguage();
				if (lang != null) {
					builder.append('@');
					builder.append(lang);
				}
				// If the literal has a declared datatype should we include it in the key?
				
			}
			String text = builder.toString();
			String hash = ShaBNodeHasher.SHA1(text);
			
			return new BNodeKey(hash, part, this);
			
		}
		
		return null;
	}

}
