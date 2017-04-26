package io.konig.datacatalog;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;

import io.konig.core.Vertex;
import io.konig.core.impl.RdfUtil;

public class OverviewPage {
	private static final String ONTOLOGY_LIST = "OntologyList";
	private static final String OVERVIEW_FILE = "data-catalog/velocity/overview.vm";
	
	public void render(PageRequest request, PageResponse response) throws DataCatalogException {
		
		request.setPageId(DataCatalogBuilder.OVERVIEW_URI);
		request.setActiveLink(DataCatalogBuilder.OVERVIEW_URI);
		List<Vertex> list = DataCatalogUtil.ontologyList(request);
		List<ResourceDescription> ontologyList = new ArrayList<>();
		for (Vertex v : list) {
			Resource id = v.getId();
			if (id instanceof URI) {
				URI ontologyId = (URI) id;

				String name = DataCatalogUtil.ontologyName(v);
				String description = RdfUtil.getDescription(v);
				URI pageId = DataCatalogUtil.ontologySummary(ontologyId.getNamespace());
				String href = DataCatalogUtil.path(request, pageId);
				
				ontologyList.add(new ResourceDescription(href, name, description));
			}
		}
		
		DataCatalogUtil.sortResourceList(ontologyList);
		
		VelocityContext context = request.getContext();
		context.put(ONTOLOGY_LIST, ontologyList);
		VelocityEngine engine = request.getEngine();
		Template template = engine.getTemplate(OVERVIEW_FILE);
		
		PrintWriter writer = response.getWriter();
		template.merge(context, writer);
		writer.flush();
		

	}

}
