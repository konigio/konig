package io.konig.gcp.datasource;

/*
 * #%L
 * Konig Core
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


import java.util.LinkedHashSet;
import java.util.Set;

import org.openrdf.model.URI;

import com.google.api.services.bigquery.model.ExternalDataConfiguration;

import io.konig.annotation.RdfProperty;
import io.konig.core.KonigException;
import io.konig.core.vocab.GCP;
import io.konig.core.vocab.Konig;
import io.konig.datasource.DataSource;
import io.konig.datasource.TableDataSource;
import io.konig.gcp.datasource.GoogleCloudSqlTable.Builder;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeBuilder;

public class GoogleBigQueryTable extends TableDataSource {
	
	private Set<DataSource> bigQuerySource;
	private BigQueryTableReference tableReference;
	private ExternalDataConfiguration externalDataConfiguration;
	private String tabularFieldNamespace;

	public GoogleBigQueryTable() {
		addType(Konig.GoogleBigQueryTable);
	}

	public void addBigQuerySource(DataSource source) {
		if (bigQuerySource == null) {
			bigQuerySource = new LinkedHashSet<>();
		}
		bigQuerySource.add(source);
	}

	@RdfProperty(Konig.BIG_QUERY_SOURCE)
	public Set<DataSource> getBigQuerySource() {
		return bigQuerySource;
	}

	public void setBigQuerySource(Set<DataSource> bigQuerySource) {
		this.bigQuerySource = bigQuerySource;
	}

	@RdfProperty(GCP.TABLE_REFERENCE)
	public BigQueryTableReference getTableReference() {
		return tableReference;
	}

	public void setTableReference(BigQueryTableReference tableReference) {
		this.tableReference = tableReference;
	}

	@Override
	public String getTableIdentifier() {
		
		if (tableReference == null) {
			throw new KonigException("tableReference must be defined for <" + getId() + ">");
		}
		
		StringBuilder builder = new StringBuilder();
		String datasetId = tableReference.getDatasetId();
		String tableId = tableReference.getTableId();
		
		builder.append(datasetId);
		builder.append('.');
		builder.append(tableId);
		return builder.toString();
	}

	public ExternalDataConfiguration getExternalDataConfiguration() {
		return externalDataConfiguration;
	}

	public void setExternalDataConfiguration(ExternalDataConfiguration externalDataConfiguration) {
		this.externalDataConfiguration = externalDataConfiguration;
	}

	@Override
	public String getUniqueIdentifier() {
		StringBuilder builder = new StringBuilder();
		builder.append("BigQuery:");
		builder.append(tableReference.getProjectId());
		builder.append(':');
		builder.append(tableReference.getDatasetId());
		builder.append(':');
		builder.append(tableReference.getTableId());
		
		return builder.toString();
	}

	@Override
	public String getSqlDialect() {
		return "SQL 2011";
	}
	@RdfProperty(Konig.TABULAR_FIELD_NAMESPACE)
	public String getTabularFieldNamespace() {
		return tabularFieldNamespace;
	}

	public void setTabularFieldNamespace(String tabularFieldNamespace) {
		this.tabularFieldNamespace = tabularFieldNamespace;
	}

	@Override
	public String getDdlFileName() {
		BigQueryTableReference ref = getTableReference();
		StringBuilder builder = new StringBuilder();
		builder.append(ref.getDatasetId());
		builder.append('.');
		builder.append(ref.getTableId());
		builder.append(".json");
		return builder.toString();
	}


	@Override
	public String getTransformFileName() {

		BigQueryTableReference ref = getTableReference();
		StringBuilder builder = new StringBuilder();
		builder.append(ref.getDatasetId());
		builder.append('.');
		builder.append(ref.getTableId());
		builder.append(".dml.sql");
		return builder.toString();
	}

	@Override
	public String getQualifiedTableName() {
		if (tableReference==null) {
			throw new KonigException("tableReference must be defined");
		}
		StringBuilder builder = new StringBuilder();
		builder.append(tableReference.getDatasetId());
		builder.append('.');
		builder.append(tableReference.getTableId());
		return builder.toString();
	}

	@Override
	public TableDataSource generateAssociationTable(Shape subjectShape, URI predicate) {
		throw new UnsupportedOperationException();
	}
	

	public static class Builder {
		private GoogleBigQueryTable table = new GoogleBigQueryTable();
		private ShapeBuilder shapeBuilder;
		
		public Builder(ShapeBuilder shapeBuilder) {
			this.shapeBuilder = shapeBuilder;
		}
		
		
		
		public Builder id(URI id) {
			table.setId(id);
			return this;
		}
		
		public ShapeBuilder endDataSource() {
			shapeBuilder.peekShape().addShapeDataSource(table);
			return shapeBuilder;
		}
		
		
		
	}


}
