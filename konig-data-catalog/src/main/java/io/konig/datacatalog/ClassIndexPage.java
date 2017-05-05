package io.konig.datacatalog;

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
