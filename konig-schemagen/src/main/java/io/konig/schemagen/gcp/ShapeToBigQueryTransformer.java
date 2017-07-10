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

import io.konig.datasource.DataSource;
import io.konig.gcp.datasource.BigQueryTableReference;
import io.konig.gcp.datasource.GoogleBigQueryTable;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeVisitor;

/**
 * A utility that transforms a Shape into a BigQuery Table.
 * @author Greg McFall
 *
 */
public class ShapeToBigQueryTransformer implements ShapeVisitor {
	
	private BigQueryTableGenerator tableGenerator;
	private BigQueryTableVisitor tableVisitor;
	private CurrentStateViewGenerator currentStateViewGenerator;
	
	public ShapeToBigQueryTransformer(BigQueryTableGenerator tableGenerator, BigQueryTableVisitor tableVisitor) {
		this.tableGenerator = tableGenerator;
		this.tableVisitor = tableVisitor;
		this.currentStateViewGenerator = new CurrentStateViewGenerator();
	}

	@Override
	public void visit(Shape shape) {
		
		List<DataSource> list = shape.getShapeDataSource();
		if (list != null) {
			for (DataSource dataSource : list) {
				if (dataSource instanceof GoogleBigQueryTable) {
					Table table = toTable(shape, (GoogleBigQueryTable) dataSource);
					table.setView(currentStateViewGenerator.createViewDefinition(shape, dataSource));
					if (table.getExternalDataConfiguration() != null) {
						table.setType("EXTERNAL");
					} else if (table.getView() != null) {
						table.setType("VIEW");
					} else {
						table.setType("TABLE");
					}
					tableVisitor.visit(table);
				}
			}
		}

	}

	private Table toTable(Shape shape, GoogleBigQueryTable dataSource) {

		Table table = new Table();
		BigQueryTableReference ref = dataSource.getTableReference();
		
		TableReference reference = new TableReference();
		reference.setProjectId(ref.getProjectId());
		reference.setDatasetId(ref.getDatasetId());
		reference.setTableId(ref.getTableId());
		
		table.setTableReference(reference);
		
		TableSchema tableSchema = tableGenerator.toTableSchema(shape);
		table.setSchema(tableSchema);
		ExternalDataConfiguration external = dataSource.getExternalDataConfiguration();
		if (external != null) {
			table.setExternalDataConfiguration(external);
			table.setType("EXTERNAL");
		}
		
		return table;
		
	}

}
