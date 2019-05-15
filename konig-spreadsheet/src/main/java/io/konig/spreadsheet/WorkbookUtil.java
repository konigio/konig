package io.konig.spreadsheet;

/*
 * #%L
 * Konig Spreadsheet
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


import java.util.Set;

import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;

import io.konig.core.Graph;
import io.konig.core.OwlReasoner;
import io.konig.core.Vertex;
import io.konig.core.vocab.Schema;

public class WorkbookUtil {

	public static boolean assignValueType(OwlReasoner owlReasoner, URI predicate, SheetColumn c) {
		
		Graph graph = owlReasoner.getGraph();
		Vertex v = graph.getVertex(predicate);
		if (v != null) {
			URI range = v.getURI(RDFS.RANGE);
			if (range == null) {
				Set<Value> set = v.getValueSet(Schema.rangeIncludes);
				if (set.size()==1) {
					Value value = set.iterator().next();
					if (value instanceof URI) {
						range = (URI) value;
					}
				}
			}
			if (range == null) {
				
				if (v.hasProperty(RDF.TYPE, OWL.OBJECTPROPERTY)) {
					c.setObjectType(OWL.THING);
					return true;
				}
				
				if (v.hasProperty(RDF.TYPE, OWL.DATATYPEPROPERTY)) {
					c.setDatatype(RDFS.LITERAL);
					return true;
				}
				
				return false;
			}
			if (owlReasoner.isDatatype(range)) {
				c.setDatatype(range);
			} else {
				c.setObjectType(range);
			}

			return true;
		}
		
		
		
		return false;
		
	}

}
