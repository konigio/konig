package io.konig.schemagen;

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

import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

import io.konig.aws.datasource.AwsAurora;
import io.konig.core.NamespaceManager;
import io.konig.core.vocab.Konig;
import io.konig.datasource.DataSource;
import io.konig.gcp.datasource.GoogleCloudSqlTable;
import io.konig.omcs.datasource.OracleTable;
import io.konig.schemagen.sql.FacetedSqlDatatype;
import io.konig.schemagen.sql.SqlDatatypeMapper;
import io.konig.schemagen.sql.StringSqlDatatype;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeManager;
import io.konig.shacl.ShapeMaxRowLength;
import io.konig.shacl.impl.MemoryShapeManager;

public class CalculateMaximumRowSize {
	private ShapeManager shapeManager = new MemoryShapeManager();
	private SqlDatatypeMapper datatypeMapper = new SqlDatatypeMapper();
	private int maxCharacterSize = 3;

	public void addMaximumRowSizeAll(Collection<Shape> shapeList, NamespaceManager nsManager)
			throws InvalidDatatypeException {
		
		for (Shape shape : shapeList) {
			addMaximumRowSize(shape, nsManager);

		}
	}
	
	

	public int getMaxCharacterSize() {
		return maxCharacterSize;
	}



	public void setMaxCharacterSize(int maxCharacterSize) {
		this.maxCharacterSize = maxCharacterSize;
	}



	public void addMaximumRowSize(Shape shape, NamespaceManager nsManager) throws InvalidDatatypeException {
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

	private Integer getsize(FacetedSqlDatatype sqlDataType) throws InvalidDatatypeException {
	
		int dataTypeSize = 0;
		
		switch (sqlDataType.getDatatype()) {
		case BIT:
		case TINYINT: 
			dataTypeSize = 1;
			break;
		case SMALLINT:
			dataTypeSize = 2;
			break;
		case MEDIUMINT:
			dataTypeSize = 3;
			break;
		case INT:
			dataTypeSize = 1;
			break;
		case INTEGER:
			dataTypeSize = 1;
			break;
		case TEXT:
			dataTypeSize = 12;
			break;


		case CLOB:
		case VARCHAR2:
		case VARCHAR:
		case CHAR:
			dataTypeSize = maxLengthBytes(sqlDataType);
			break;
			
		case DATE:
			dataTypeSize = 3;
			break;
		case DATETIME:
			dataTypeSize = 3;
			break;
		case DECIMAL:
			dataTypeSize = 4;
			break;
		case BIGINT:
			dataTypeSize = 8;
			break;
		case FLOAT:
			dataTypeSize = 4;
			break;
			
		case LONG:
		case DOUBLE:
			dataTypeSize = 8;
			break;
		case NUMBER:
		case BOOLEAN:
			dataTypeSize = 1;
			break;
		case BINARY_DOUBLE:
			break;
		case BINARY_FLOAT:
			break;
		case TIMESTAMP:
			break;
		}
		return dataTypeSize;
	}

	private int maxLengthBytes(FacetedSqlDatatype sqlDataType) throws InvalidDatatypeException {
		if (sqlDataType instanceof StringSqlDatatype) {
			StringSqlDatatype stringType = (StringSqlDatatype) sqlDataType;
			return maxCharacterSize * stringType.getMaxLength();
		}
		throw new InvalidDatatypeException("Expected StringSqlDatatype");
	}

	private int getMaxRowSize(List<FacetedSqlDatatype> sqlDataTypeList) throws InvalidDatatypeException {
		int rowSize = 0;
		for (FacetedSqlDatatype sqlDataType : sqlDataTypeList) {
			rowSize = getsize(sqlDataType) + rowSize;
		}

		return rowSize;
	}

}
