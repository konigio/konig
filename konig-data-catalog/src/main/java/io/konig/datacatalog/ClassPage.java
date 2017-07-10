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
import io.konig.shacl.ClassStructure;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;

public class ClassPage {
	private static final String CLASS_TEMPLATE = "data-catalog/velocity/class.vm";
	private static final String ANCESTOR_LIST = "AncestorList";
	private static final String SUBCLASS_LIST = "SubclassList";
	private static final String SUPERCLASS_PROPERTY_LIST = "SuperclassPropertyList";

	
	public void render(ClassRequest request, PageResponse response) throws DataCatalogException, IOException {
		ClassStructure structure = request.getClassStructure();
		
		Vertex owlClass = request.getOwlClass();

		DataCatalogUtil.setSiteName(request);
		Shape shape = structure.getShapeForClass(owlClass.getId());

		VelocityEngine engine = request.getEngine();
		VelocityContext context = request.getContext();
		
		URI classId = (URI) owlClass.getId();
		request.setPageId(classId);
		request.setActiveLink(null);
		
		context.put("ClassName", classId.getLocalName());
		context.put("ClassId", classId.stringValue());
		setAncestorPaths(request);
		setSubClasses(request);
		setShapes(request, classId);
		defineEnumerationMembers(request);
		setSuperclassProperties(request);
		
		List<PropertyInfo> propertyList = propertyList(request, shape);
				
		context.put("PropertyList", propertyList);
		
		
		Template template = engine.getTemplate(CLASS_TEMPLATE);

		PrintWriter out = response.getWriter();
		template.merge(context, out);
		out.flush();
	}



	private List<PropertyInfo> propertyList(ClassRequest request, Shape shape) throws DataCatalogException {
		URI classId = shape.getTargetClass();
		List<PropertyInfo> propertyList = new ArrayList<>();
		for (PropertyConstraint p : shape.getProperty()) {
			if (RDF.TYPE.equals(p.getPredicate())) {
				continue;
			}
			propertyList.add(new PropertyInfo(classId, p, request));
		}
		
		DataCatalogUtil.sortProperties(propertyList);
		return propertyList;
	}



	private void setSubClasses(ClassRequest request) throws DataCatalogException {
		Vertex owlClass = request.getOwlClass();
		URI targetClass = (URI) owlClass.getId();
		OwlReasoner reasoner = request.getClassStructure().getReasoner();
		List<Vertex> subClassList = reasoner.subClasses(owlClass);
		if (!subClassList.isEmpty()) {
			List<Link> linkList = new ArrayList<>();
			for (Vertex v : subClassList) {
				if (v.getId() instanceof URI) {
					URI subClassId = (URI) v.getId();
					String name = request.localName(subClassId);
					String href = request.relativePath(targetClass, subClassId);
					Link link = new Link(name, href);
					linkList.add(link);
				}
			}
			Collections.sort(linkList);
			request.getContext().put(SUBCLASS_LIST, linkList);
		}
	}


	private void setSuperclassProperties(ClassRequest request) throws DataCatalogException {
		
		Set<Resource> memory = new HashSet<>();
		List<SuperclassProperties> list = new ArrayList<>();
		
		Vertex owlClass = request.getOwlClass();

		URI classId = (URI) owlClass.getId();
		LinkedList<Vertex> stack = new LinkedList<>();
		stack.add(owlClass);
		while (!stack.isEmpty()) {
			Vertex v = stack.removeFirst();
			List<Vertex> superlist = v.asTraversal().out(RDFS.SUBCLASSOF).toVertexList();
			for (Vertex s : superlist) {
				Resource id = s.getId();
				if (!memory.contains(id)) {
					memory.add(id);
					stack.add(s);
					if (id instanceof URI) {
						URI superId = (URI) id;
						
						Shape shape = request.getClassStructure().getShapeForClass(superId);
						List<PropertyInfo> propertyList = propertyList(request, shape);

						if (!propertyList.isEmpty()) {
							String name = superId.getLocalName();
							String href = request.relativePath(classId, superId);
							Link link = new Link(name, href);
							
							SuperclassProperties element = new SuperclassProperties(link, propertyList);
							list.add(element);
						}
					}
				}
			}
		}
		
		
		if (!list.isEmpty()) {
			request.getContext().put(SUPERCLASS_PROPERTY_LIST, list);
		}
	}

	private void setAncestorPaths(ClassRequest request) throws DataCatalogException {
		
		List<List<Link>> ancestorList = new ArrayList<>();
		request.getContext().put(ANCESTOR_LIST, ancestorList);
		Vertex owlClass = request.getOwlClass();
		URI targetClassId = (URI) owlClass.getId();
		ClassHierarchyPaths pathList = new ClassHierarchyPaths(owlClass);
		for (List<URI> path : pathList) {
			List<Link> linkList = new ArrayList<>();
			ancestorList.add(linkList);
			for (int i=path.size()-1; i>=0; i--) {
				URI classId = path.get(i);
				String relativePath = request.relativePath(targetClassId, classId);
				String localName = request.localName(classId);
				linkList.add(new Link(localName, relativePath));
			}
		}
	}



	private void defineEnumerationMembers(ClassRequest request) throws DataCatalogException, IOException {
		
		Vertex owlClass = request.getOwlClass();
		Resource id = owlClass.getId();
		if (id instanceof URI) {
			URI classId = (URI) id;
			OwlReasoner reasoner = request.getClassStructure().getReasoner();
			if (reasoner.isEnumerationClass(owlClass.getId())) {
				IndividualPage individualPage = new IndividualPage();
				Set<URI> set = owlClass.asTraversal().in(RDF.TYPE).toUriSet();
				if (!set.isEmpty()) {
					List<Link> memberList = new ArrayList<>();
					request.getContext().put("Members", memberList);
					
					for (URI memberId : set) {
						String href = request.relativePath(classId, memberId);
						Link link = new Link(memberId.getLocalName(), href);
						memberList.add(link);

						Vertex member = reasoner.getGraph().getVertex(memberId);
						IndividualRequest individual = new IndividualRequest(request, member, classId);
						Writer writer = request.getWriterFactory().createWriter(individual, memberId);
						PageResponse individualResponse = new PageResponseImpl(writer);
						individualPage.render(individual, individualResponse);
					}
				}
				
				
			}
		}
		
	}



	private void setShapes(ClassRequest request, URI classId) throws DataCatalogException {
		
		List<Shape> shapeList = request.getShapeManager().getShapesByTargetClass(classId);
		if (!shapeList.isEmpty()) {
			List<Link> linkList = new ArrayList<>();
			for (Shape shape : shapeList) {
				Resource id = shape.getId();
				if (id instanceof URI) {
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
