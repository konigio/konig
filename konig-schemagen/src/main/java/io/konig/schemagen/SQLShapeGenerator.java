package io.konig.schemagen;

/*
 * #%L
 * Konig Schema Generator
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


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.rio.RDFHandlerException;
import io.konig.aws.datasource.AwsAurora;
import io.konig.core.Graph;
import io.konig.core.NamespaceManager;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.RdfUtil;
import io.konig.core.vocab.Konig;
import io.konig.datasource.DataSource;
import io.konig.gcp.datasource.GoogleCloudSqlTable;
import io.konig.omcs.datasource.OracleTable;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeManager;
import io.konig.shacl.io.ShapeFileGetter;
import io.konig.shacl.io.ShapeWriter;


public class SQLShapeGenerator {
	
	

	protected void writeShape(Shape shape, ShapeFileGetter fileGetter,NamespaceManager nsManager) throws RDFHandlerException, IOException {
		ShapeWriter shapeWriter = new ShapeWriter();
		Graph graph = new MemoryGraph();
		shapeWriter.emitShape(shape, graph);
		File shapeFile = fileGetter.getFile(new URIImpl(shape.getId().toString()));
		RdfUtil.prettyPrintTurtle(nsManager, graph, shapeFile);
	}
	
	
	protected Shape getShape(String tableName,ShapeManager shapeManager) {
		List<Shape> shapes= shapeManager.listShapes();
		for(Shape shape : shapes) {
			String localName = new URIImpl(shape.getId().stringValue()).getLocalName();
			if(tableName.equals(localName)) {
				return shape;
			}
		}
		return null;
	}
	
	protected List<DataSource> getViewDatasource(Shape shape, String viewName) {
		List<DataSource> datasources = shape.getShapeDataSource();
		List<DataSource> ds = new ArrayList<>();
		for(DataSource datasource : datasources) {
			if(datasource instanceof AwsAurora){
				AwsAurora awsAurora = (AwsAurora) datasource;
				awsAurora.addType(Konig.AwsAuroraView);
				URI id = new URIImpl(awsAurora.getId().toString());
				awsAurora.setId((Resource)new URIImpl(id.getNamespace() + viewName));
				awsAurora.setAwsTableName(viewName);
				awsAurora.getTableReference().setAwsTableName(viewName);
				ds.add(awsAurora);
			} else if (datasource instanceof GoogleCloudSqlTable){
				GoogleCloudSqlTable cloudSql = (GoogleCloudSqlTable) datasource;
				//TODO : add type as GoogleCloudSqlView
				URI id = new URIImpl(cloudSql.getId().toString());
				cloudSql.setId((Resource)new URIImpl(id.getNamespace() + viewName));
				cloudSql.setTableName(viewName);
				ds.add(cloudSql);
			} else if (datasource instanceof OracleTable){
				OracleTable oracle = (OracleTable) datasource;
				//TODO : add type as OracleView
				URI id = new URIImpl(oracle.getId().toString());
				oracle.setId((Resource)new URIImpl(id.getNamespace() + viewName));
				oracle.getTableReference().setOmcsTableId(viewName);
				ds.add(oracle);
			}
		}
		return ds;
	}
	
}
