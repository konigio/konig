package io.konig.core.showl;

/*
 * #%L
 * Konig Core
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


import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;

import io.konig.core.Graph;
import io.konig.core.OwlReasoner;
import io.konig.core.Vertex;
import io.konig.shacl.ShapeManager;

public class ShowlPropertyManager extends ShowlManager {

	public ShowlPropertyManager(ShapeManager shapeManager, OwlReasoner reasoner) {
		super(shapeManager, reasoner);
	}

	public void build() {
		buildClasses();
		buildProperties();
		loadShapes();
		inferTargetClasses();
		inferInverses();
	}

	

	private void buildProperties() {
		
		Graph graph = getReasoner().getGraph();
		
		for (Vertex property : graph.vertex(RDF.PROPERTY).asTraversal().in(RDF.TYPE).toVertexList()) {
			
			if (property.getId() instanceof URI) {
				produceShowlProperty((URI)property.getId());
			}
		}
		
	}

	private void buildClasses() {
		
		Graph graph = getReasoner().getGraph();
		
		
		for (Vertex owlClass : graph.vertex(OWL.CLASS).asTraversal().in(RDF.TYPE).toVertexList()) {
			if (owlClass.getId() instanceof URI) {
				this.produceOwlClass((URI) owlClass.getId());
			}
		}
		
		
		
	}


}
