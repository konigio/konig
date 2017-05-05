package io.konig.datacatalog;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.vocabulary.RDFS;

import io.konig.core.Vertex;
import io.konig.core.vocab.VANN;

public class OntologyIndexPage {

	private static final String ONTOLOGY_LIST_TEMPLATE = "data-catalog/velocity/ontologyIndex.vm";
	private static final String ONTOLOGY_LIST = "OntologyList";

	public void render(PageRequest request, PageResponse response) throws DataCatalogException {
		
		List<Vertex> source = DataCatalogUtil.ontologyList(request);
		
		List<Link> ontologyList = new ArrayList<>();
		for (Vertex v : source) {
			if (v.getId() instanceof URI) {
				URI ontologyId = (URI) v.getId();
				String name = ontologyName(v);
				URI summaryResource = DataCatalogUtil.ontologySummary(ontologyId.stringValue());
				String href = DataCatalogUtil.path(request, summaryResource);
				ontologyList.add(new Link(name, href));
			}
		}
		Collections.sort(ontologyList);
		
		VelocityEngine engine = request.getEngine();
		VelocityContext context = request.getContext();
		context.put(ONTOLOGY_LIST, ontologyList);
		
		Template template = engine.getTemplate(ONTOLOGY_LIST_TEMPLATE);
		
		PrintWriter out = response.getWriter();
		template.merge(context, out);
		out.flush();
		
	}

	private String ontologyName(Vertex v) {
		Value value = v.getValue(RDFS.LABEL);
		if (value == null) {
			value = v.getValue(VANN.preferredNamespacePrefix);
		}
		if (value == null) {
			value = v.getId();
		}
		return value.stringValue();
	}

}
