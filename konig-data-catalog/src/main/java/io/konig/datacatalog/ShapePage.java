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
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.openrdf.model.Namespace;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.konig.aws.datasource.AwsAurora;
import io.konig.core.Graph;
import io.konig.core.NamespaceManager;
import io.konig.core.OwlReasoner;
import io.konig.core.Vertex;
import io.konig.core.impl.URIVertex;
import io.konig.core.json.SampleJsonGenerator;
import io.konig.core.pojo.SimplePojoFactory;
import io.konig.core.util.IOUtil;
import io.konig.core.util.StringUtil;
import io.konig.core.vocab.Konig;
import io.konig.datasource.DataSource;
import io.konig.datasource.DatasourceFileLocator;
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
		if(shape.getShapeDataSource()!=null){
		
			Map<String,String> datasources=new HashMap<String,String>();
			for(DataSource ds:shape.getShapeDataSource()){	
				String types=null;
				String providedBy=null;
				String tableName=null;
				if(ds.getType()!=null){
					for(URI uri:ds.getType()){
						if(Arrays.asList(DATASOURCE_LIST).contains(uri)){	
							Graph graph=request.getGraph();
							List<Vertex> list = graph.v(uri).in(RDF.TYPE).toVertexList();
							if (!list.isEmpty()) {			
								for (Vertex v : list) {
									String shapeName=(String)context.get("ShapeName");
									String vertexName=((URIVertex)v.getId()).getLocalName();
									if(shapeName.equals(vertexName)){
										SimplePojoFactory pojoFactory = new SimplePojoFactory();
										if(Konig.GoogleCloudSqlTable.equals(uri)){
											GoogleCloudSqlTable table = pojoFactory.create(v, GoogleCloudSqlTable.class);
											tableName=table.getTableName();
											break;
										}
										else if(Konig.GoogleBigQueryTable.equals(uri)){
											GoogleBigQueryTable table =pojoFactory.create(v, GoogleBigQueryTable.class);
											tableName=table.getTableIdentifier();
											break;
										}
										else if(Konig.AwsAuroraTable.equals(uri)){
											AwsAurora table =pojoFactory.create(v, AwsAurora.class);
											tableName=table.getAwsTableName();
											break;
										}
										else if(Konig.GoogleBigQueryView.equals(uri)){
											GoogleBigQueryView table =pojoFactory.create(v, GoogleBigQueryView.class);
											tableName=table.getIdentifier();
											break;
										}
									}
								}
							}
						}
						if(!uri.equals(Konig.DataSource)){
							types=uri.getLocalName();
							break;
						}
					}
				}				
				if(ds.getIsPartof()!=null){
					for(URI uri:ds.getIsPartof()){
							providedBy=(providedBy==null)?uri.getLocalName():providedBy+","+uri.getLocalName();
					}
					
				}
				if(tableName==null){
					datasources.put(types,providedBy);
				}
				else{
					datasources.put(tableName+"("+types+")",providedBy);
				}
				
			}
		
			context.put("Datasources", datasources);
			
		}
		request.setPageId(shapeURI);
		request.setActiveLink(null);
		
		handleDdlFile(context, request);
		
		List<PropertyInfo> propertyList = new ArrayList<>();
		context.put("PropertyList", propertyList);
		for (PropertyConstraint p : shape.getProperty()) {
			propertyList.add(new PropertyInfo(shapeURI, p, request));
		}
		DataCatalogUtil.sortProperties(propertyList);
		addJsonSamples(request);
		
		Template template = engine.getTemplate(SHAPE_TEMPLATE);
		
		PrintWriter out = response.getWriter();
		template.merge(context, out);
		out.flush();
	}
	private void setTableName(VelocityContext context,ShapeRequest request) {
		URI dsUri=(URI)context.get("DataSourceURI");
		Graph graph=request.getGraph();
		List<Vertex> list = graph.v(dsUri).in(RDF.TYPE).toVertexList();
		if (!list.isEmpty()) {			
			for (Vertex v : list) {
				String shapeName=(String)context.get("ShapeName");
				String vertexName=((URIVertex)v.getId()).getLocalName();
				if(shapeName.equals(vertexName)){
					SimplePojoFactory pojoFactory = new SimplePojoFactory();
					if(Konig.GoogleCloudSqlTable.equals(dsUri)){
						GoogleCloudSqlTable table = pojoFactory.create(v, GoogleCloudSqlTable.class);
						context.put("TableName",table.getTableName());
					}
					else if(Konig.GoogleBigQueryTable.equals(dsUri)){
						GoogleBigQueryTable table =pojoFactory.create(v, GoogleBigQueryTable.class);
						context.put("TableName", table.getTableIdentifier());
					}
					else if(Konig.AwsAuroraTable.equals(dsUri)){
						AwsAurora table =pojoFactory.create(v, AwsAurora.class);
						context.put("TableName", table.getAwsTableName());
					}
					else if(Konig.GoogleBigQueryView.equals(dsUri)){
						GoogleBigQueryView table =pojoFactory.create(v, GoogleBigQueryView.class);
						context.put("TableName", table.getIdentifier());
					}
				}
			}
		}
		
	}
	
	private void handleDdlFile(VelocityContext context, ShapeRequest request) throws DataCatalogException {
		DatasourceFileLocator ddlLocator = request.getBuildRequest().getSqlDdlLocator();
		if (ddlLocator != null) {
			
			List<DataSource> dataSourceList = request.getShape().getShapeDataSource();
			if (dataSourceList != null) {
				List<Link> linkList = null;
				Set<String> memory = null;
				for (DataSource datasource : dataSourceList) {

					if (datasource instanceof TableDataSource) {
						File ddlFile = ddlLocator.locateFile(datasource);
						if (ddlFile != null && ddlFile.exists()) {
							TableDataSource table = (TableDataSource) datasource;
							String dialect = table.getSqlDialect();
							
							if (memory==null || !memory.contains(dialect)) {
								if (memory==null) {
									memory = new HashSet<>();
								}
								memory.add(dialect);
								String fileName = ddlFile.getName();
								fileName = txtFile(fileName);
								String href = "../sql/" + fileName;
								String name = dialect + " DDL";
								Link link = new Link(name, href);
								if (linkList == null) {
									linkList = new ArrayList<>();
								}
								linkList.add(link);

								File targetDir = new File(request.getBuildRequest().getOutDir(), "sql");
								File ddlTargetFile = new File(targetDir, fileName);
								
								try {
									FileUtils.copyFile(ddlFile, ddlTargetFile);
								} catch (IOException e) {
									throw new DataCatalogException(e);
								}
							}
						}
					}
				}
				if (linkList!=null) {
					context.put("RelatedArtifacts", linkList);
				}
				
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
