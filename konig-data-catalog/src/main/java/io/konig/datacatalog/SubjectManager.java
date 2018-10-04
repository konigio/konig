package io.konig.datacatalog;

/*
 * #%L
 * Konig Data Catalog
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


import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openrdf.model.BNode;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.SKOS;

import io.konig.core.Graph;
import io.konig.core.Vertex;
import io.konig.core.vocab.Schema;

public class SubjectManager {
	
	private Map<Resource,ClassifiedName> nameMap = new HashMap<>();
	
	public void load(Graph graph) {

		List<Vertex> classList = graph.v(OWL.CLASS).in(RDF.TYPE).toVertexList();
		for (Vertex v : classList) {
			
			Set<Vertex> subjectSet = v.getVertexSet(SKOS.BROADER);
			for (Vertex subjectVertex : subjectSet) {
				String name = name(subjectVertex);
				String className = className(subjectVertex);
				
				ClassifiedName cname = new ClassifiedName(name, className);
				nameMap.put(subjectVertex.getId(), cname);
			}
			
		}
	}
	
	public ClassifiedName getSubjectName(Resource subjectId) {
		return nameMap.get(subjectId);
	}
	
	public boolean isEmpty() {
		return nameMap.isEmpty();
	}
	
	public Collection<ClassifiedName> listSubjectNames() {
		return nameMap.values();
	}

	private String className(Vertex v) {
		String suffix = localName(v.getId());
		return "subject-" + suffix;
	}

	private String name(Vertex v) {
		Value name = v.getValue(Schema.name);
		if (name != null) {
			return name.stringValue();
		}
		
		return localName(v.getId());
	}
	
	private String localName(Resource id) {
		return id instanceof URI ? ((URI)id).getLocalName() : ((BNode)id).getID();	
	}

}
