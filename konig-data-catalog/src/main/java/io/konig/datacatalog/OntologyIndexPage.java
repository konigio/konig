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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.vocabulary.RDFS;

import io.konig.core.NamespaceInfo;
import io.konig.core.NamespaceInfoManager;
import io.konig.core.Vertex;
import io.konig.core.vocab.Konig;
import io.konig.core.vocab.VANN;

public class OntologyIndexPage {

	private static final String ONTOLOGY_LIST_TEMPLATE = "data-catalog/velocity/ontologyIndex.vm";
	private static final String ONTOLOGY_LIST = "OntologyList";

	public void render(PageRequest request, PageResponse response) throws DataCatalogException {

		NamespaceInfoManager nim = request.getBuildRequest().getNamespaceInfoManager();
		List<Vertex> source = DataCatalogUtil.ontologyList(request);
		
		List<OntologyDescription> ontologyList = new ArrayList<>();
		boolean anyEnumNamespace = false;
		for (Vertex v : source) {
			if (v.getId() instanceof URI) {
				URI ontologyId = (URI) v.getId();
				String name = ontologyName(v);
				URI summaryResource = DataCatalogUtil.ontologySummary(ontologyId.stringValue());
				String href = DataCatalogUtil.path(request, summaryResource);
				NamespaceInfo info = nim.getNamespaceInfo(ontologyId.stringValue());
				boolean isEnumNamespace = info==null ? false : info.getType().contains(Konig.EnumNamespace);
				ontologyList.add(new OntologyDescription(href, name, null, isEnumNamespace));
				if (isEnumNamespace) {
					anyEnumNamespace = true;
				}
			}
		}
		Collections.sort(ontologyList);
		
		VelocityEngine engine = request.getEngine();
		VelocityContext context = request.getContext();
		context.put(ONTOLOGY_LIST, ontologyList);
		context.put(OverviewPage.SHOW_HIDE_ENUM_NAMESPACES, anyEnumNamespace);
		
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
