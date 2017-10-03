package io.konig.core.pojo.impl;

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


import org.openrdf.model.Resource;
import org.openrdf.model.Value;

import io.konig.core.Graph;
import io.konig.core.KonigException;
import io.konig.core.Vertex;

/**
 * A ValueHandler that wraps a CollectionValueHandler and an RdfListValueHandler.
 * The decision about which handler to use depends on the nature of the object in the
 * PropertyInfo passed to the handleValue method.
 * <p>
 * If the object is an RDF List, then the RdfListValueHandler is used.
 * Otherwise, the CollectionValueHandler is used.
 * </p>
 * @author Greg McFall
 *
 */
public class MultiValueHandler implements ThreePhaseValueHandler {
	
	private static ElementInfo dummyInfo = new ElementInfo(null);
	
	private CollectionValueHandler collectionHandler;
	private RdfListValueHandler rdfListHandler;

	public MultiValueHandler(CollectionValueHandler collectionHandler, RdfListValueHandler rdfListHandler) {
		this.collectionHandler = collectionHandler;
		this.rdfListHandler = rdfListHandler;
	}

	@Override
	public void handleValue(PropertyInfo propertyInfo) throws KonigException {
		Value object = propertyInfo.getObject();
		if (object instanceof Resource) {
			Resource objectId = (Resource)object;
			Graph graph = propertyInfo.getSubject().getVertex().getGraph();
			Vertex objectVertex = graph.getVertex(objectId);
			if (objectVertex.isList()) {
				rdfListHandler.handleValue(propertyInfo);
			} else {
				useCollectionHandler(propertyInfo);
			}
		} else {
			// object is a Literal value
			
			useCollectionHandler(propertyInfo);
		}

	}

	private void useCollectionHandler(PropertyInfo propertyInfo) {
		if (propertyInfo.getElementInfo() == dummyInfo) {
			// This must be the time handleValue is being called for the current set of values.
			// Therefore, we need to invoke the setUp method on the collectionHandler
			
			propertyInfo.setElementInfo(null);
			
			collectionHandler.setUp(propertyInfo);
		}
		collectionHandler.handleValue(propertyInfo);
		
	}

	@Override
	public void setUp(PropertyInfo propertyInfo) throws KonigException {
		// We need to delay calling setUp on the collectionHandler until we
		// get the first value.  So we set a dummy ElementInfo and look for it
		// in the handleValue method.
		
		propertyInfo.setElementInfo(dummyInfo);
	}

	@Override
	public void tearDown(PropertyInfo propertyInfo) throws KonigException {
		propertyInfo.setElementInfo(null);
	}

}
