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

import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDFS;

import io.konig.core.Vertex;
import io.konig.core.vocab.Konig;

public class OwlRestrictionKeyFactory implements BNodeKeyFactory {

	@Override
	public BNodeKey createKey(URI predicate, Vertex object) {
		if (RDFS.SUBCLASSOF.equals(predicate)) {
			URI onProperty = object.getURI(OWL.ONPROPERTY);
			if (onProperty != null) {
				URI constraint = getConstraint(object);
				if (constraint != null) {
					StringBuilder builder = new StringBuilder();
					builder.append(onProperty.stringValue());
					builder.append('!');
					builder.append(constraint.stringValue());
					
					String text = builder.toString();
					String hash = ShaBNodeHasher.SHA1(text);
					Map<String, URI> map = new HashMap<>();
					map.put(OWL.ONPROPERTY.stringValue(), Konig.KeyValue);
					map.put(constraint.stringValue(), Konig.KeyTerm);
					
					return new BNodeKey(hash, map, this);
				}
			}
		}
		return null;
	}

	private URI getConstraint(Vertex object) {
		
		return 
			(object.getValue(OWL.MINCARDINALITY) != null) ? OWL.MINCARDINALITY :
			(object.getValue(OWL.MAXCARDINALITY) != null) ? OWL.MAXCARDINALITY :
			(object.getValue(OWL.CARDINALITY) != null)    ? OWL.CARDINALITY :
			(object.getValue(OWL.HASVALUE) != null)       ? OWL.HASVALUE :
			(object.getValue(OWL.ALLVALUESFROM) != null)  ? OWL.ALLVALUESFROM :
			(object.getValue(OWL.SOMEVALUESFROM) != null) ? OWL.SOMEVALUESFROM :
			null;
	}

}
