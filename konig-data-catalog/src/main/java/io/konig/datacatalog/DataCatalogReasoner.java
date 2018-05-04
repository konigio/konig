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


import org.openrdf.model.Resource;
import org.openrdf.model.vocabulary.OWL;

import io.konig.core.Graph;
import io.konig.core.OwlReasoner;
import io.konig.core.vocab.Konig;

public class DataCatalogReasoner extends OwlReasoner {

	public DataCatalogReasoner(Graph graph) {
		super(graph);
	}

	public boolean isTypeOf(Resource individual, Resource owlClass) {
		if (Konig.Undefined.equals(individual) && owlClass.equals(OWL.CLASS)) {
			return true;
		}
		return super.isTypeOf(individual, owlClass);
	}
}
