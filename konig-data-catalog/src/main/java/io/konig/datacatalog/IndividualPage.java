package io.konig.datacatalog;

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
