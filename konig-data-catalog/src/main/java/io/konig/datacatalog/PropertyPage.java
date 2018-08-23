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
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.rio.turtle.TurtleUtil;

import io.konig.core.Path;
import io.konig.core.path.HasStep;
import io.konig.core.path.HasStep.PredicateValuePair;
import io.konig.core.path.InStep;
import io.konig.core.util.BasicJavaDatatypeMapper;
import io.konig.core.util.JavaDatatypeMapper;
import io.konig.core.path.OutStep;
import io.konig.core.path.Step;
import io.konig.shacl.PropertyStructure;
import io.konig.shacl.Shape;

public class PropertyPage {

	private static final String TEMPLATE = "data-catalog/velocity/property.vm";
	
	private JavaDatatypeMapper datatypeMapper = new BasicJavaDatatypeMapper();
	
	public void render(PropertyRequest request, PageResponse response) throws DataCatalogException, IOException {
		DataCatalogUtil.setSiteName(request);
		
		PropertyStructure structure = request.getPropertyStructure();

		
		URI predicate = structure.getPredicate();
		request.setPageId(predicate);
		VelocityEngine engine = request.getEngine();
		VelocityContext context = request.getContext();
		String description = structure.description();
		if (description == null) {
			description = "";
		}
		
		context.put("PropertyName", predicate.getLocalName());
		context.put("PropertyId", predicate.stringValue());
		context.put("PropertyDescription", description);
		setRangeList(request);
		setDomainList(request);
		setShapeList(request);
		setEquivalentPath(request);
		
		Template template = engine.getTemplate(TEMPLATE);
		PrintWriter out = response.getWriter();
		template.merge(context, out);
		out.flush();
	}
	
	

	private void setEquivalentPath(PropertyRequest request) throws DataCatalogException {
		
		PropertyStructure p = request.getPropertyStructure();
		Path path = p.getEquivalentPath();
		if (path != null) {
			
			List<PathElementView> list = new ArrayList<>();
			for (Step step : path.asList()) {
					list.add(pathElement(request, step));
			}
			
			request.getContext().put("EquivalentPath", list);
			
		}
		
	}

	private PathElementView pathElement(PropertyRequest request, Step step) throws DataCatalogException {
		if (step instanceof HasStep) {
			return hasStepElement(request, (HasStep) step);
		}
		URI predicate = null;
		String name = null;
		String href = null;
		String operator = null;
		if (step instanceof OutStep) {
			OutStep out = (OutStep) step;
			operator = "/";
			predicate = out.getPredicate();
		}
		if (step instanceof InStep) {
			InStep in = (InStep) step;
			operator = "^";
			predicate = in.getPredicate();
		}
		
		if (predicate != null) {
			name = predicate.getLocalName();
			href = request.relativePath(request.getPropertyStructure().getPredicate(), predicate);
		}
		if (operator==null || name==null || href==null) {
			throw new DataCatalogException("Unsupported step type: " + step.getClass().getSimpleName());
		}
		return new PathElementView(operator, name, href);
	}

	private PathElementView hasStepElement(PropertyRequest request, HasStep step) throws DataCatalogException {
		
		URI propertyId = request.getPropertyStructure().getPredicate();
		
		List<PropertyValuePair> list = new ArrayList<>();
		for (PredicateValuePair p : step.getPairList()) {
			URI predicate = p.getPredicate();
			Value rdfValue = p.getValue();

			String href = null;
			String stringValue = null;
			
			if (rdfValue instanceof Literal) {
				Literal literal = (Literal) rdfValue;
				stringValue = literal.stringValue();
				if (literal.getDatatype() != null) {
					TurtleUtil.encodeString("");
					Class<?> javaType = datatypeMapper.javaDatatype(literal.getDatatype());
					if (!Number.class.isAssignableFrom(javaType) && !Boolean.class.equals(javaType)) {
						StringBuilder builder = new StringBuilder();
						char quote = stringValue.indexOf('"')>=0 ? '\'' : '"';
						builder.append(quote);
						for (int i=0; i<stringValue.length();) {
							int c = stringValue.codePointAt(i);
							switch (c) {
							case '\r' :
								builder.append("\\r");
								break;
								
							case '\\' :
								builder.append("\\\\");
								break;
								
							case '\t' :
								builder.append("\\t");
								break;
								
							case '\n' :
								builder.append("\\n");
								break;
								
							case '\'' :
								if (quote == '"') {
									builder.appendCodePoint(c);
								} else {
									builder.append("\\'");
								}
								break;
							default:
								builder.appendCodePoint(c);
							}
							i += Character.charCount(c);
						}
						builder.append(quote);
						stringValue = builder.toString();
						href = request.relativePath(propertyId, literal.getDatatype());
					}
				}
				
			} else if (rdfValue instanceof URI) {
				
				URI uri = (URI) rdfValue;
				href = request.relativePath(propertyId, uri);
				stringValue = uri.getLocalName();
				
			}
			
			if (stringValue == null) {
				throw new DataCatalogException("Unsupported RDF Value: " + rdfValue);
			}
			
			list.add(new PropertyValuePair(new Link(predicate.getLocalName(), href), stringValue));
		}
		
		return new PathElementView(list);
	}

	private void setShapeList(PropertyRequest request) throws DataCatalogException {
		
		List<Link> result = new ArrayList<>();
		PropertyStructure structure = request.getPropertyStructure();
		URI predicate = structure.getPredicate();
		Set<Shape> source = structure.getUsedInShape();
		if (source != null) {
			for (Shape shape : source) {
				
				Resource id = shape.getId();
				if (id instanceof URI) {
					URI shapeId = (URI) id;
					String href = request.relativePath(predicate, shapeId);
					String name = shapeId.getLocalName();
					result.add(new Link(name, href));
				}
			}
		}
		
		request.getContext().put("ShapeList", result);
		
	}

	private void setDomainList(PropertyRequest request) throws DataCatalogException {
		PropertyStructure structure = request.getPropertyStructure();
		URI predicate = structure.getPredicate();
		List<Link> list = new ArrayList<>();
		for (URI uri : structure.domainIncludes()) {
			String href = request.relativePath(predicate, uri);
			String name = uri.getLocalName();
			list.add(new Link(name, href));
		}
		
		request.getContext().put("DomainList", list);
		
	}

	private void setRangeList(PropertyRequest request) throws DataCatalogException {

		PropertyStructure structure = request.getPropertyStructure();
		URI predicate = structure.getPredicate();
		List<Link> list = new ArrayList<>();
		for (URI uri : structure.rangeIncludes()) {
			String href = request.relativePath(predicate, uri);
			String name = uri.getLocalName();
			list.add(new Link(name, href));
		}
		
		request.getContext().put("RangeList", list);
		
	}
}
