package io.konig.abbrev;

/*
 * #%L
 * Konig Core
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


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.vocabulary.SKOS;

import io.konig.core.Graph;
import io.konig.core.KonigException;
import io.konig.core.Vertex;
import io.konig.core.vocab.Konig;

/**
 * An AbbreviationManager that holds the AbbreviationSchemes in memory.
 * @author Greg McFall
 *
 */
public class MemoryAbbreviationManager implements AbbreviationManager {

	private Graph graph;
	private Map<URI, AbbreviationScheme> map = new HashMap<>();
	private AbbreviationConfig config;
	

	public MemoryAbbreviationManager(Graph graph, AbbreviationConfig config) {
		this.graph = graph;
		this.config = config;
	}


	@Override
	public AbbreviationScheme getSchemeById(URI schemeId) {
		AbbreviationScheme scheme = map.get(schemeId);
		if (scheme == null) {
			scheme = new AbbreviationScheme(schemeId, config);
			map.put(schemeId, scheme);
			load(scheme);
		}
		return scheme;
	}


	private void load(AbbreviationScheme scheme) {
		Vertex schemeVertex = graph.getVertex(scheme.getId());
		if (schemeVertex != null) {
			List<Vertex> conceptList = schemeVertex.asTraversal().in(SKOS.IN_SCHEME).toVertexList();
			for (Vertex concept : conceptList) {
				Abbreviation abbrev = parseAbbreviation(concept);
				scheme.add(abbrev);
			}
		}
		
	}


	private Abbreviation parseAbbreviation(Vertex concept) {
	
		Value prefLabel = concept.getValue(SKOS.PREF_LABEL);
		if (prefLabel == null) {
			throw new KonigException("skos:prefLabel is not defined for abbreviation <" + concept.getId().stringValue() + ">");
		}
		Value abbrevLabel = concept.getValue(Konig.abbreviationLabel);
		if (abbrevLabel == null) {
			throw new KonigException("konig:abbreviationLabel is not defined for abbreviation <" + concept.getId().stringValue() + ">");
		}
		
		if (!(concept.getId() instanceof URI)) {
			throw new KonigException("Abbreviation concept '" + prefLabel + "' must be identified by a URI");
		}
		
		URI id = (URI) concept.getId();
		
		Abbreviation result = new Abbreviation(id);
		result.setPrefLabel(prefLabel.stringValue());
		result.setAbbreviationLabel(abbrevLabel.stringValue());
		
		return result;
	}


}
