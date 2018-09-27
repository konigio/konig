package io.konig.schemagen;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

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

import java.util.List;
import java.util.Set;

import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.rio.RDFHandlerException;

import io.konig.aws.datasource.AwsAurora;
import io.konig.core.NamespaceManager;
import io.konig.core.vocab.Konig;
import io.konig.datasource.DataSource;
import io.konig.gcp.datasource.GoogleCloudSqlTable;
import io.konig.omcs.datasource.OracleTable;
import io.konig.schemagen.sql.FacetedSqlDatatype;
import io.konig.schemagen.sql.SqlDatatypeMapper;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeManager;
import io.konig.shacl.ShapeMaxRowLength;
import io.konig.shacl.impl.MemoryShapeManager;

public class CalculateMaximumRowSize {
	private ShapeManager shapeManager = new MemoryShapeManager();
	private SqlDatatypeMapper datatypeMapper = new SqlDatatypeMapper();

	public void addMaximumRowSizeAll(Collection<Shape> shapeList, NamespaceManager nsManager)
			throws RDFHandlerException, IOException {
		// List<Shape> shapeList = shapeManager.listShapes();
		for (Shape shape : shapeList) {
			addMaximumRowSize(shape, nsManager);

		}
	}

	public void addMaximumRowSize(Shape shape, NamespaceManager nsManager) throws RDFHandlerException, IOException {
		List<PropertyConstraint> propList = new ArrayList<PropertyConstraint>();
		propList = shape.getProperty();
		for (URI shapeType : shape.getType()) {
			if (Konig.TabularNodeShape.equals(shapeType)) {
				List<FacetedSqlDatatype> sqlDataTypeList = new ArrayList<FacetedSqlDatatype>();

				for (PropertyConstraint p : propList) {
					FacetedSqlDatatype datatype = datatypeMapper.type(p);
					sqlDataTypeList.add(datatype);
				}

				List<ShapeMaxRowLength> shapeMaxRowLengthList = new ArrayList<ShapeMaxRowLength>();
				ShapeMaxRowLength shapeMaxRowLength = new ShapeMaxRowLength();
				shapeMaxRowLength.setTargetDatasource(getDatasource(shape));
				shapeMaxRowLength.setMaxRowLength(getMaxRowSize(sqlDataTypeList));
				shapeMaxRowLengthList.add(shapeMaxRowLength);
				shape.setShapeMaxRowLengthList(shapeMaxRowLengthList);

			}
		}
	}

	protected URI getDatasource(Shape shape) {
		URI id = null;
		List<DataSource> datasources = shape.getShapeDataSource();
		for (DataSource datasource : datasources) {
			if (datasource instanceof AwsAurora) {
				AwsAurora awsAurora = (AwsAurora) datasource;
				id = new URIImpl(awsAurora.getId().toString());
			} else if (datasource instanceof GoogleCloudSqlTable) {
				GoogleCloudSqlTable cloudSql = (GoogleCloudSqlTable) datasource;
				id = new URIImpl(cloudSql.getId().toString());

			} else if (datasource instanceof OracleTable) {
				OracleTable oracle = (OracleTable) datasource;
				id = new URIImpl(oracle.getId().toString());
			}
		}
		return id;
	}

	private Integer getsize(String dataTypeWithLen) {
		String dataType = dataTypeWithLen;
		Integer lenColumn = 1;
		if (dataTypeWithLen.contains("(")) {
			dataType = dataTypeWithLen.substring(0, dataTypeWithLen.indexOf("("));
			String length = dataTypeWithLen.substring(dataTypeWithLen.indexOf("(") + 1, dataTypeWithLen.indexOf(")"));
			lenColumn = Integer.valueOf(length);
		}
		Integer dataTypeSize = 0;
		switch (dataType) {
		case "TINYINT":
			dataTypeSize = 1;
			break;
		case "SMALLINT":
			dataTypeSize = 2;
			break;
		case "MEDIUMINT":
			dataTypeSize = 3;
			break;
		case "INT":
			dataTypeSize = 1;
			break;
		case "INTEGER":
			dataTypeSize = 1;
			break;
		case "TEXT":
			dataTypeSize = 12;
			break;
		case "CHAR":
			dataTypeSize = 3;
			dataTypeSize = dataTypeSize * lenColumn;
			break;
		case "VARCHAR":
			dataTypeSize = 3;
			dataTypeSize = dataTypeSize * lenColumn;
			break;
		case "DATE":
			dataTypeSize = 3;
			break;
		case "DATETIME":
			dataTypeSize = 3;
			break;
		case "DECIMAL":
			dataTypeSize = 4;
			break;
		case "BIGINT":
			dataTypeSize = 8;
			break;
		case "FLOAT":
			dataTypeSize = 4;
			break;
		case "DOUBLE":
			dataTypeSize = 8;
			break;
		case "BOOLEAN":
			dataTypeSize = 1;
			break;
		}
		return dataTypeSize;
	}

	private Integer getMaxRowSize(List<FacetedSqlDatatype> sqlDataTypeList) {
		Integer rowSize = 0;
		for (FacetedSqlDatatype sqlDataType : sqlDataTypeList) {
			String dataType = sqlDataType.toString().toUpperCase();
			rowSize = getsize(dataType) + rowSize;
		}

		return rowSize;
	}

}
