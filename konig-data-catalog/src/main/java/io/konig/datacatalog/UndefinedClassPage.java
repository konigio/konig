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


import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;

import io.konig.core.OwlReasoner;
import io.konig.core.Vertex;
import io.konig.core.util.ClassHierarchyPaths;
import io.konig.core.vocab.Konig;
import io.konig.shacl.ClassStructure;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;

public class UndefinedClassPage {
	private static final String UNDEFINED_CLASS_TEMPLATE = "data-catalog/velocity/undefinedClass.vm";

	
	public void render(ClassRequest request, PageResponse response) throws DataCatalogException, IOException {
		

		DataCatalogUtil.setSiteName(request);

		VelocityEngine engine = request.getEngine();
		VelocityContext context = request.getContext();
		
		URI classId = Konig.Undefined;
		request.setPageId(classId);
		request.setActiveLink(null);
		
		context.put("ClassName", classId.getLocalName());
		context.put("ClassId", classId.stringValue());
		setShapes(request, classId);
		
		
		Template template = engine.getTemplate(UNDEFINED_CLASS_TEMPLATE);

		PrintWriter out = response.getWriter();
		template.merge(context, out);
		out.flush();
	}


	private void setShapes(ClassRequest request, URI classId) throws DataCatalogException {
		
		List<Shape> shapeList = request.getShapeManager().listShapes();
		if (!shapeList.isEmpty()) {
			List<Link> linkList = new ArrayList<>();
			for (Shape shape : shapeList) {
				Resource id = shape.getId();
				if (id instanceof URI && shape.getTargetClass()==null) {
					URI shapeId = (URI) id;
					String shapeName = shapeId.getLocalName();
					String href = request.relativePath(classId, shapeId);
					
					linkList.add(new Link(shapeName, href));
				}
			}
			if (!linkList.isEmpty()) {
				request.getContext().put("ShapeList", linkList);
			}
		}
		
	}

}
