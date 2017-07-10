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
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;

import io.konig.core.Vertex;

public class ClassIndexPage {

	
	private static final String CLASS_LIST_TEMPLATE = "data-catalog/velocity/classIndex.vm";

	public void render(PageRequest request, PageResponse response) throws DataCatalogException {
		List<Vertex> source = request.getGraph().v(OWL.CLASS).in(RDF.TYPE).toVertexList();
		
		List<ClassInfo> classList = new ArrayList<>();
		
		for (Vertex v : source) {
			if (v.getId() instanceof URI) {
				classList.add(new ClassInfo(v, request));
			}
		}
		Collections.sort(classList);
		

		VelocityEngine engine = request.getEngine();
		VelocityContext context = request.getContext();
		context.put("ClassList", classList);

		Template template = engine.getTemplate(CLASS_LIST_TEMPLATE);
		
		PrintWriter out = response.getWriter();
		template.merge(context, out);
		out.flush();
	}
	
	public static class ClassInfo implements Comparable<ClassInfo> {
		String name;
		String href;
		
		public ClassInfo(Vertex v, PageRequest request) throws DataCatalogException {
			URI id = (URI) v.getId();
			name = id.getLocalName();
			href = DataCatalogUtil.classFileName(request, id);
			
		}

		public String getName() {
			return name;
		}

		public String getHref() {
			return href;
		}

		@Override
		public int compareTo(ClassInfo other) {
			return name.compareTo(other.name);
		}
		
		
	}

}
