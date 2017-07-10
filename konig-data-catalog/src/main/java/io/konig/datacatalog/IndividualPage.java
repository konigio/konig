package io.konig.datacatalog;

/*
 * #%L
 * Konig Data Catalog
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


import java.io.PrintWriter;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.openrdf.model.URI;

import io.konig.core.Vertex;
import io.konig.core.impl.RdfUtil;

public class IndividualPage {
	private static final String TEMPLATE = "data-catalog/velocity/individual.vm";

	public void render(IndividualRequest request, PageResponse response) throws DataCatalogException {

		DataCatalogUtil.setSiteName(request);
		VelocityEngine engine = request.getEngine();
		VelocityContext context = request.getContext();
		Vertex individual = request.getIndividual();
		URI individualId = (URI) individual.getId();
		
		request.setPageId(individualId);
		request.setActiveLink(null);
		
		context.put("IndividualName", individualId.getLocalName());
		context.put("IndividualId", individualId.stringValue());
		
		String description = RdfUtil.getDescription(individual);
		if (description == null) {
			description = "";
		}
		context.put("IndividualDescription", description);
		
		Link enumerationClass = new Link(
				request.getEnumerationClass().getLocalName(), 
				request.getEnumerationClass().stringValue());
		
		context.put("EnumerationClass", enumerationClass);
		
		Template template = engine.getTemplate(TEMPLATE);
		
		PrintWriter out = response.getWriter();
		template.merge(context, out);
		out.flush();
	}
	
}
