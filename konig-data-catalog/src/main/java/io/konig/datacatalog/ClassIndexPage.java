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
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.SKOS;

import io.konig.core.OwlReasoner;
import io.konig.core.Vertex;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.vocab.Konig;

public class ClassIndexPage {

	
	protected static final String CLASS_LIST_TEMPLATE = "data-catalog/velocity/classIndex.vm";
	protected static final String ENTITY_LIST_TEMPLATE = "data-catalog/velocity/entityIndex.vm";

	public void render(PageRequest request, PageResponse response) throws DataCatalogException {
		List<Vertex> source = request.getGraph().v(OWL.CLASS).in(RDF.TYPE).toVertexList();
		
		if (!request.getBuildRequest().getSettings().isMixEntitiesAndEnums()) {
			filter(request.getOwlReasoner(), source);
		}
		
		List<ClassInfo> classList = new ArrayList<>();
		
		addUndefinedClass(request, classList);
		
		for (Vertex v : source) {
			if (v.getId() instanceof URI) {
				classList.add(new ClassInfo(v, request));
			}
		}
		Collections.sort(classList);
		

		VelocityEngine engine = request.getEngine();
		VelocityContext context = request.getContext();
		buildContext(context);
		context.put("ClassList", classList);

		Template template = getTemplate(engine);
		
		PrintWriter out = response.getWriter();
		template.merge(context, out);
		out.flush();
	}
	
	protected void buildContext(VelocityContext context) {
		// Do nothing
		
	}

	protected Template getTemplate(VelocityEngine engine) {
		return engine.getTemplate(CLASS_LIST_TEMPLATE);
	}

	protected void filter(OwlReasoner reasoner, List<Vertex> source) {
		// Do nothing
	}

	protected void filter(OwlReasoner reasoner, List<Vertex> source, boolean truthValue) {
		Iterator<Vertex> sequence = source.iterator();
		while (sequence.hasNext()) {
			Vertex v = sequence.next();			
			if (reasoner.isEnumerationClass(v.getId()) == truthValue) {
				sequence.remove();
			}
		}
		
	}

	private void addUndefinedClass(PageRequest request, List<ClassInfo> classList) throws DataCatalogException {
		if (request.getBuildRequest().isShowUndefinedClass()) {
			MemoryGraph graph = new MemoryGraph();
			Vertex v = graph.vertex(Konig.Undefined);
			ClassInfo info = new ClassInfo(v, request);
			classList.add(info);
		}
		
	}

	public static class ClassInfo implements Comparable<ClassInfo> {
		String className;
		String name;
		String href;
		
		public ClassInfo(Vertex v, PageRequest request) throws DataCatalogException {
			URI id = (URI) v.getId();
			name = id.getLocalName();
			href = DataCatalogUtil.classFileName(request, id);
			className = request.getBuildRequest().classSubjects(v);
		}
		



		public String getClassName() {
			return className;
		}


		public void setClassName(String className) {
			this.className = className;
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
