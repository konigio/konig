package io.konig.schemagen.gcp;

/*
 * #%L
 * Konig Schema Generator
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


import java.util.List;

import com.google.api.services.bigquery.model.ExternalDataConfiguration;
import com.google.api.services.bigquery.model.Table;
import com.google.api.services.bigquery.model.TableReference;
import com.google.api.services.bigquery.model.TableSchema;

import io.konig.core.vocab.Konig;
import io.konig.datasource.DataSource;
import io.konig.gcp.datasource.BigQueryTableReference;
import io.konig.gcp.datasource.GoogleBigQueryTable;
import io.konig.gcp.datasource.GoogleBigQueryView;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeVisitor;
import io.konig.transform.proto.ShapeModelFactory;

/**
 * A utility that transforms a Shape into a BigQuery Table.
 * @author Greg McFall
 *
 */
public class ShapeToBigQueryTransformer implements ShapeVisitor {
	
	private BigQueryTableGenerator tableGenerator;
	private BigQueryTableVisitor tableVisitor;
	private CurrentStateViewGenerator currentStateViewGenerator;
	
	public ShapeToBigQueryTransformer(BigQueryTableGenerator tableGenerator, BigQueryTableVisitor tableVisitor, ShapeModelFactory shapeModelFactory) {
		this.tableGenerator = tableGenerator;
		this.tableVisitor = tableVisitor;
		this.currentStateViewGenerator = new CurrentStateViewGenerator(shapeModelFactory);
	}
	
	@Override
	public void visit(Shape shape) {
		
		List<DataSource> list = shape.getShapeDataSource();
		if (list != null) {
			for (DataSource dataSource : list) {
				try {
				if (dataSource instanceof GoogleBigQueryTable) {
					if (dataSource instanceof GoogleBigQueryView && tableVisitor instanceof BigQueryTableWriter) {
						Table table = toTable(shape, (GoogleBigQueryView) dataSource);
						table.setView(currentStateViewGenerator.createViewDefinition(shape, dataSource));
						if (table.getView() != null) {
							table.setType("VIEW");
							tableVisitor.visit(dataSource, table);
						}
					} else if (!(dataSource instanceof GoogleBigQueryView) && tableVisitor instanceof BigQueryTableWriter) {
						Table table = toTable(shape, (GoogleBigQueryTable) dataSource);
						try {
						if (dataSource.isA(Konig.CurrentState)) {
							table.setView(currentStateViewGenerator.createViewDefinition(shape, dataSource));
						}
						if (table.getExternalDataConfiguration() != null) {
							table.setType("EXTERNAL");
							tableVisitor.visit(dataSource, table);
						} else {
							table.setType("TABLE");
							tableVisitor.visit(dataSource, table);
						}
						}catch(Exception ex){
							ex.printStackTrace();
						}
					}
				}
				}catch(Exception ex){
					ex.printStackTrace();
				}
			}
				
		}
	}

	private Table toTable(Shape shape, GoogleBigQueryTable dataSource) {
		
		Table table = new Table();
		try {
			BigQueryTableReference ref = dataSource.getTableReference();
			
			TableReference reference = new TableReference();
			reference.setProjectId(ref.getProjectId());
			reference.setDatasetId(ref.getDatasetId());
			reference.setTableId(ref.getTableId());
			
			table.setTableReference(reference);
			if (!(dataSource instanceof GoogleBigQueryView)){
				TableSchema tableSchema = tableGenerator.toTableSchema(shape);
				table.setSchema(tableSchema);
			}
			ExternalDataConfiguration external = dataSource.getExternalDataConfiguration();
			if (external != null) {
				table.setExternalDataConfiguration(external);
				table.setType("EXTERNAL");
			}
		}catch(Exception ex) {
			ex.printStackTrace();
		}
		return table;
		
	}

}
