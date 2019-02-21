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


import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;

import io.konig.core.Graph;
import io.konig.core.vocab.Konig;
import io.konig.core.vocab.VANN;

public class KonigDescriber {

	
	public KonigDescriber(Graph graph) {

		graph.edge(Konig.NAMESPACE_ID, RDF.TYPE, OWL.ONTOLOGY);
		graph.edge(Konig.NAMESPACE_ID, VANN.preferredNamespacePrefix, new LiteralImpl("konig"));
	}

}
