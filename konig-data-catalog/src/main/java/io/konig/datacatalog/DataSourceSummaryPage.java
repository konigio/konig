package io.konig.datacatalog;

/*
 * #%L
 * Konig Data Catalog
 * %%
 * Copyright (C) 2015 - 2018 Gregory McFall
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.openrdf.model.URI;

import io.konig.core.vocab.Konig;
import io.konig.datasource.DataSource;
import io.konig.datasource.TableDataSource;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeManager;

public class DataSourceSummaryPage {
	private static final String DATASOURCE_LIST = "DataSourceList";
	private static final String VELOCITY_TEMPLATE = "data-catalog/velocity/datasources.vm";
	
	public void render(PageRequest request, PageResponse response) throws DataCatalogException {
		Map<URI,DataSourceSummary> map = new HashMap<>();
		ShapeManager shapeManager = request.getShapeManager();

		request.setPageId(DataCatalogBuilder.DATASOURCE_SUMMARY_URI);
		buildMap(request, map, shapeManager);
		
		List<DataSourceSummary> list = new ArrayList<>(map.values());
		

		VelocityContext context = request.getContext();
		context.put(DATASOURCE_LIST, list);
		VelocityEngine engine = request.getEngine();
		Template template = engine.getTemplate(VELOCITY_TEMPLATE);

		PrintWriter writer = response.getWriter();
		template.merge(context, writer);
		writer.flush();
		
	}

	private void buildMap(PageRequest request, Map<URI, DataSourceSummary> map, ShapeManager shapeManager) throws DataCatalogException {
		for (Shape shape : shapeManager.listShapes()) {
			if (shape.getId() instanceof URI) {
				List<DataSource> dsList = shape.getShapeDataSource();
				if (dsList != null) {
					for (DataSource ds : dsList) {
						if (ds.getId() instanceof URI) {
							URI dsId = (URI) ds.getId();
							DataSourceSummary summary = map.get(dsId);
							if (summary == null) {
								summary = createSummary(request, shape, ds);
								map.put(dsId, summary);
							}
						}
						
					}
				}
			}
		}
		
	}

	private DataSourceSummary createSummary(PageRequest request, Shape shape, DataSource ds) throws DataCatalogException {
		
		Link name = name(request, ds);
		String type = type(request, ds);
		Link shapeLink = shape(request, shape);
		
		
		return new DataSourceSummary(name, type, shapeLink);
	}

	private Link shape(PageRequest request, Shape shape) throws DataCatalogException {
		
		URI shapeId = (URI) shape.getId();
		String href = DataCatalogUtil.path(request, shapeId);
		String name = shapeId.getLocalName();
		return new Link(name, href);
	}

	private Link name(PageRequest request, DataSource ds) {
		String identifier = ds.getIdentifier();
		if (identifier == null) {

			if (ds instanceof TableDataSource) {
			
				identifier = ((TableDataSource) ds).getTableIdentifier();
				int dot = identifier.lastIndexOf('.');
				if (dot > 0) {
					identifier = identifier.substring(dot+1);
				}
			}
		}
		if (identifier == null) {
			identifier = ds.getId().stringValue();
		}
		if (identifier.startsWith("$")) {
			int dot = identifier.indexOf('.');
			if (dot > 0) {
				identifier = identifier.substring(dot+1);
			}
		}
		// TODO: compute the actual href
		String href = "#";
		return new Link(identifier, href);
	}

	private String type(PageRequest request, DataSource ds) {
		StringBuilder builder = new StringBuilder();
		List<String> list = new ArrayList<>();
		for (URI id : ds.getType()) {
			if (Konig.DataSource.equals(id)) {
				continue;
			}
			list.add(id.getLocalName());
		}
		
		Collections.sort(list);
		String br = "";
		for (String text : list) {
			builder.append(br);
			br = "<br>\n";
			builder.append(text);
		}
		
		return builder.toString();
	}

}
