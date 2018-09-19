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


import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.openrdf.model.Namespace;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.konig.aws.datasource.AwsAuroraTable;
import io.konig.core.NamespaceManager;
import io.konig.core.OwlReasoner;
import io.konig.core.json.SampleJsonGenerator;
import io.konig.core.project.ProjectFile;
import io.konig.core.util.IOUtil;
import io.konig.core.util.StringUtil;
import io.konig.core.vocab.Konig;
import io.konig.datasource.DataSource;
import io.konig.datasource.TableDataSource;
import io.konig.gcp.datasource.GoogleBigQueryTable;
import io.konig.gcp.datasource.GoogleBigQueryView;
import io.konig.gcp.datasource.GoogleCloudSqlTable;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;

public class ShapePage {
	private static final Logger logger = LoggerFactory.getLogger(ShapePage.class);
	private static final String SHAPE_TEMPLATE = "data-catalog/velocity/shape.vm";
	private static final URI[] DATASOURCE_LIST={Konig.GoogleBigQueryTable,Konig.GoogleBigQueryView,Konig.GoogleCloudSqlTable,Konig.AwsAuroraTable};
	
	
	private Set<URI> excludedDataSourceType = new HashSet<>();

	public ShapePage() {
		excludedDataSourceType.add(Konig.DataSource);
	}
	public void render(ShapeRequest request, PageResponse response) throws DataCatalogException {

		DataCatalogUtil.setSiteName(request);
		VelocityEngine engine = request.getEngine();
		VelocityContext context = request.getContext();
		Shape shape = request.getShape();
		Resource shapeId = shape.getId();
		URI shapeURI = null;
		if (shapeId instanceof URI) {
			shapeURI = (URI) shapeId;
			context.put("ShapeId", shapeURI.stringValue());
			context.put("ShapeName", shapeURI.getLocalName());
		} else {
			return;
		}
		URI targetClass = shape.getTargetClass();
		if (targetClass == null) {
			logger.warn("sh:targetClass not defined for shape <{}>", shape.getId().stringValue());
			return;
		}
		context.put("TargetClass", new Link(targetClass.getLocalName(), targetClass.stringValue()));

		if(shape.getShapeDataSource()!=null  && !shape.getShapeDataSource().isEmpty()){
		
			List<DataSourceInfo> datasourceList = new ArrayList<>();
			for(DataSource ds:shape.getShapeDataSource()){	
				String type=null;
				String providedBy=localNameList(ds.getIsPartOf(), null);
				String tableName="";
				
				if (ds instanceof GoogleCloudSqlTable) {
					GoogleCloudSqlTable table = (GoogleCloudSqlTable) ds;
					tableName = table.getTableName();
					type = "Google Cloud SQL Table";
				} else if (ds instanceof GoogleBigQueryTable) {
					GoogleBigQueryTable table = (GoogleBigQueryTable) ds;
					type = "Google BigQuery Table";
					tableName = table.getTableIdentifier();
				} else if (ds instanceof GoogleBigQueryView) {
					GoogleBigQueryView table = (GoogleBigQueryView) ds;
					type = "Google BigQuery View";
					tableName = table.getIdentifier();
					
				} else if (ds instanceof AwsAuroraTable) {
					AwsAuroraTable table = (AwsAuroraTable) ds;
					type = "AWS Aurora Table";
					tableName = table.getAwsTableName();
				}
				
				
				if (type == null) {
					type = localNameList(ds.getType(), excludedDataSourceType);
				}
				
				datasourceList.add(new DataSourceInfo(type, tableName, providedBy));
				
			}
		
			context.put("Datasources", datasourceList);
			
		}
		request.setPageId(shapeURI);
		request.setActiveLink(null);

		request.putTermStatus(shape.getTermStatus());
		handleDdlFile(context, request);
		
		List<PropertyInfo> propertyList = new ArrayList<>();
		context.put("PropertyList", propertyList);
		boolean anyTermStatus = false;
		for (PropertyConstraint p : shape.getProperty()) {
			PropertyInfo info = new PropertyInfo(shapeURI, p, request);
			propertyList.add(info);
			if (info.getTermStatus() != null) {
				anyTermStatus = true;
			}
		}
		context.put("AnyTermStatus", anyTermStatus);
		DataCatalogUtil.sortProperties(propertyList);
		addJsonSamples(request);
		Template template = engine.getTemplate(SHAPE_TEMPLATE);
		
		PrintWriter out = response.getWriter();
		template.merge(context, out);
		out.flush();
	}
	private String localNameList(Collection<URI> type, Set<URI> excludes) {
		if (type == null || type.isEmpty()) {
			return "";
		}
		ArrayList<String> nameList = new ArrayList<>();
		for (URI uri : type) {
			if (excludes == null || !excludes.contains(uri)) {
				nameList.add(uri.getLocalName());
			}
		}
		
		Collections.sort(nameList);
		
		StringBuilder builder = new StringBuilder();
		String comma = "";
		for (String name : nameList) {
			builder.append(comma);
			comma = ", ";
			builder.append(name);
		}
		return builder.toString();
	}
	
	
	private void handleDdlFile(VelocityContext context, ShapeRequest request) throws DataCatalogException {
		
		
		List<DataSource> dataSourceList = request.getShape().getShapeDataSource();
		if (dataSourceList != null) {
			List<Link> linkList = null;
			Set<String> memory = null;
			URI pageId = request.getPageId();
			for (DataSource datasource : dataSourceList) {

				if (datasource instanceof TableDataSource) {
					TableDataSource tds = (TableDataSource) datasource;
					ProjectFile file = tds.getDdlFile();
					if (file==null) {
						logger.warn("DDL file missing for {}", datasource.getId());
						continue;
					}
					
					File ddlFile = file.getLocalFile();
					
					if (ddlFile == null) {
						logger.warn("Local file missing for {}", datasource.getId());
						continue;
					}
					
					if (ddlFile != null && ddlFile.exists()) {
						String dialect = tds.getSqlDialect();
						
						if (memory==null || !memory.contains(dialect)) {
							if (memory==null) {
								memory = new HashSet<>();
							}
							URI artifactId = request.getBuildRequest().getFileFactory().catalogDdlFileIri(tds);
							String href = request.getBuildRequest().getPathFactory().relativePath(pageId, artifactId);
							
							memory.add(dialect);
							String name = dialect + " DDL";
							Link link = new Link(name, href);
							if (linkList == null) {
								linkList = new ArrayList<>();
							}
							linkList.add(link);
						}
					}
				}
			}
			if (linkList!=null && !linkList.isEmpty()) {
				context.put("RelatedArtifacts", linkList);
			}
			
		}
		
	}

	private String txtFile(String fileName) {
		int dot = fileName.lastIndexOf('.');
		if (dot > 0) {
			fileName = fileName.substring(0, dot);
		}
		return fileName + ".txt";
	}
	private void addJsonSamples(ShapeRequest request) throws DataCatalogException {
		List<NamedText> list = new ArrayList<>();
		

		Shape shape = request.getShape();
		
		Resource shapeId = shape.getId();
		if (shapeId instanceof URI) {
			URI shapeURI = (URI) shapeId;
			NamespaceManager nsManager = request.getNamespaceManager();
			Namespace ns = nsManager.findByName(shapeURI.getNamespace());
			if (ns != null) {
				String prefix = ns.getPrefix();
				File srcDir = new File(request.getExamplesDir(), prefix + '/' + shapeURI.getLocalName());
				if (srcDir.exists()) {
					try {
						addCustomJson(list, srcDir);
					} catch (IOException e) {
						throw new DataCatalogException(e);
					}
					if (!list.isEmpty()) {
						request.getContext().put("JsonExamples", list);
					}
					return;
				}
			}
			
		}
		
		
		OwlReasoner reasoner = new OwlReasoner(request.getGraph());
		SampleJsonGenerator generator = new SampleJsonGenerator(reasoner);
		StringWriter out = new StringWriter();
		try {
			generator.generate(shape, out);
			String text = out.toString();
			list.add(new NamedText("Example", text));
		} catch (IOException e) {
			throw new DataCatalogException(e);
		}
		
		request.getContext().put("JsonExamples", list);
		
	}

	private void addCustomJson(List<NamedText> list, File srcDir) throws IOException {
		File[] files = srcDir.listFiles();
		for (File file : files) {
			String fileName = file.getName();
			int dot = fileName.lastIndexOf('.');
			if (dot > 0) {
				String suffix = fileName.substring(dot+1);
				if ("json".equals(suffix) || "jsonld".equals(suffix)) {
					String name = fileName.substring(0, dot);
					name = name.replace('_', ' ');
					name = StringUtil.capitalize(name);
					
					String text = IOUtil.stringContent(file);
					list.add(new NamedText(name, text));
				}
			}
		}
		
	}
	
	
}
