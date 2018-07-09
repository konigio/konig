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
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.XMLSchema;
import io.konig.core.KonigException;
import io.konig.core.NamespaceManager;
import io.konig.maven.TabularShapeGeneratorConfig;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;
import io.konig.shacl.io.ShapeFileGetter;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.create.table.ColDataType;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;
import net.sf.jsqlparser.statement.create.table.CreateTable;

public class TableShapeGenerator {
	
	private SQLShapeGenerator sqlShapeGenerator = new SQLShapeGenerator();
	private NamespaceManager nsManager;
	private TabularShapeGeneratorConfig tableConfig;
	
	public TableShapeGenerator(NamespaceManager nsManager,TabularShapeGeneratorConfig tableConfig) {
		this.nsManager = nsManager;
		this.tableConfig = tableConfig;
	}

	public void generateTable(File shapesDir, Statement createTableStmt) {
		ShapeFileGetter fileGetter = new ShapeFileGetter(shapesDir, nsManager);
		try {
			CreateTable createTable = (CreateTable) createTableStmt;
			List<ColumnDefinition> columnList = createTable.getColumnDefinitions();
			Shape shape = createTableShape(createTable, columnList);
			sqlShapeGenerator.writeShape(shape, fileGetter, nsManager);
		} catch (Exception ex) {
			throw new KonigException(ex);
		}
	}

	private Shape createTableShape(CreateTable createTable, List<ColumnDefinition> columnList) {
		String tableName = createTable.getTable().getName();
		String shapeId = tableName.replaceAll(tableConfig.getTableIriTemplate().getIriPattern(), tableConfig.getTableIriTemplate().getIriReplacement());
		Shape shape = new Shape(new URIImpl(shapeId));
		addPropertyContraint(shape, columnList);
		return shape;
	}
		
	private void addPropertyContraint(Shape shape,  List<ColumnDefinition> columnList) {
		
		List<PropertyConstraint> propertyConstraints = new ArrayList<PropertyConstraint>();
		for (ColumnDefinition column : columnList) {
			PropertyConstraint pc = new PropertyConstraint();
			pc.setPath(new URIImpl(tableConfig.getPropertyNamespace() + column.getColumnName()));
			pc.setMaxCount(1);
			pc.setMinCount(getMinCount(column));
			pc.setMaxLength(getMaxLength(column));
			pc.setMinLength(0);
			if (("ID").equals(column.getColumnName())) {
				pc.setDatatype(XMLSchema.ANYURI);
			} else {
				pc.setDatatype(getMySqlDataType(column));
			}
			if("DOUBLE".equals(column.getColDataType().getDataType()) 
					&& (getDecimalPrecision(column.getColDataType())!=0)){
			pc.setDecimalPrecision(getDecimalPrecision(column.getColDataType()));
			pc.setDecimalScale(getDecimalScale(column.getColDataType()));
			pc.setDatatype(XMLSchema.DECIMAL);
			}
			if("INT".equals(column.getColDataType().getDataType())){
				pc.setMaxInclusive((double) MySqlDatatype.SIGNED_INT_MAX);
			}
			String colDataType = column.getColDataType().getDataType();
			colDataType = getMySqlAttribute(column)+" "+colDataType;
			
			switch(colDataType){
			case "SIGNED TINYINT":
				pc.setMinInclusive((double) MySqlDatatype.SIGNED_TINYINT_MIN);
				pc.setMaxInclusive((double) MySqlDatatype.SIGNED_TINYINT_MAX);
				break;
			case "UNSIGNED TINYINT":
				pc.setMinInclusive((double) MySqlDatatype.UNSIGNED_TINYINT_MIN);
				pc.setMaxInclusive((double) MySqlDatatype.UNSIGNED_TINYINT_MAX);
				break;
			case "TINYINT":
				pc.setMaxInclusive((double) MySqlDatatype.SIGNED_TINYINT_MAX);
				break;
			case "SIGNED SMALLINT":
				pc.setMinInclusive((double) MySqlDatatype.SIGNED_SMALLINT_MIN);
				pc.setMaxInclusive((double) MySqlDatatype.SIGNED_SMALLINT_MAX);
				break;
			case "UNSIGNED SMALLINT":
				pc.setMinInclusive((double) MySqlDatatype.UNSIGNED_SMALLINT_MIN);
				pc.setMaxInclusive((double) MySqlDatatype.UNSIGNED_SMALLINT_MAX);
				break;
			case "SMALLINT":
				pc.setMaxInclusive((double) MySqlDatatype.SIGNED_SMALLINT_MAX);
				break;
			case "SIGNED MEDIUMINT":
				pc.setMinInclusive((double) MySqlDatatype.SIGNED_MEDIUMINT_MIN);
				pc.setMaxInclusive((double) MySqlDatatype.SIGNED_MEDIUMINT_MAX);
				break;
			case "UNSIGNED MEDIUMINT":
				pc.setMinInclusive((double) MySqlDatatype.UNSIGNED_MEDIUMINT_MIN);
				pc.setMaxInclusive((double) MySqlDatatype.UNSIGNED_MEDIUMINT_MAX);
				break;
			case "MEDIUMINT":
				pc.setMaxInclusive((double) MySqlDatatype.SIGNED_MEDIUMINT_MAX);
				break;
			case "SIGNED INT":
				pc.setMinInclusive((double) MySqlDatatype.SIGNED_INT_MIN);
				pc.setMaxInclusive((double) MySqlDatatype.SIGNED_INT_MAX);
				break;
			case "UNSIGNED INT":
				pc.setMinInclusive((double) MySqlDatatype.UNSIGNED_INT_MIN);
				pc.setMaxInclusive((double) MySqlDatatype.UNSIGNED_INT_MAX);
				break;
			case "INT":
				System.out.println(colDataType+" colDataType");

			case "SIGNED BIGINT":
				pc.setMinInclusive((double) MySqlDatatype.SIGNED_BIGINT_MIN);
				pc.setMaxInclusive((double) MySqlDatatype.SIGNED_BIGINT_MAX);
			case "BIGINT":
				pc.setMaxInclusive((double) MySqlDatatype.SIGNED_INT_MAX);
				break;
			}
			pc.setFormula(null);
			shape.add(pc);
			propertyConstraints.add(pc);
		}
	}
	private Integer getMaxLength(ColumnDefinition column){
		Integer maxLength = 0;
		ColDataType colDatatype = column.getColDataType();
		List<String> colList = colDatatype.getArgumentsStringList();
		if(colList!=null){
		for(String length : colList){
			maxLength = Integer.valueOf(length);
		}
		}
		return maxLength;
		
	}
	
	private Integer getDecimalPrecision(ColDataType colDatatype){
		Integer decimalPrecision = 0;
		List<String> colList = colDatatype.getArgumentsStringList();
		if(colList!=null && colList.size() >1){
		for(int i =0; i<colList.size(); i++){
			decimalPrecision = Integer.valueOf(colList.get(0));
		}
		
	}return decimalPrecision;
		}
	
	private Integer getDecimalScale(ColDataType colDatatype){
		Integer decimalScale = 0;
		List<String> colList = colDatatype.getArgumentsStringList();
		if(colList!=null && colList.size() >1){
		for(int i =0; i<colList.size(); i++){
			decimalScale = Integer.valueOf(colList.get(1));
		}
	}
		return decimalScale;
	}
	private URI getMySqlDataType(ColumnDefinition column){
		URI datatypeURI = null;
		ColDataType colDatatype = column.getColDataType();
		switch(colDatatype.getDataType()){
		case "VARCHAR":
			datatypeURI = XMLSchema.STRING;
			break;
		case "DATE":
			datatypeURI = XMLSchema.DATE;
			break;
		case "BOOLEAN":
			datatypeURI = XMLSchema.BOOLEAN;
			break;
		case "BIT":
			datatypeURI = XMLSchema.INT;
			break;
		case "INT":
			datatypeURI = XMLSchema.INT;
			break;
		case "FLOAT":
			datatypeURI = XMLSchema.FLOAT;
			break;
		case "DOUBLE":
			datatypeURI = XMLSchema.DOUBLE;
			break;
		case "DATETIME":
			datatypeURI = XMLSchema.DATETIME;
			break;
		case "TEXT":
			datatypeURI = XMLSchema.STRING;
			break;
		case "CHAR":
			datatypeURI = XMLSchema.STRING;
			break;
		case "TINYINT":
			datatypeURI = XMLSchema.INTEGER;
			break;
		case "SMALLINT":
			datatypeURI = XMLSchema.INTEGER;
			break;
		case "MEDIUMINT":
			datatypeURI = XMLSchema.INTEGER;
			break;
		case "BIGINT":
			datatypeURI = XMLSchema.LONG;
			break;
		}
		return datatypeURI;
		
	}
	private int getMinCount(ColumnDefinition column){
		int minCount = 0;
		List<String> colSpecList = column.getColumnSpecStrings();
		if (colSpecList!=null && colSpecList.size() >0 && colSpecList.get(0).equals("NOT")){
			minCount = 1;
		}
		return minCount;
	}
	
	private String getMySqlAttribute(ColumnDefinition column){
		String attribute = "";
		List<String> colSpecList = column.getColumnSpecStrings();
		if (colSpecList!=null && colSpecList.size() >0){
			if((colSpecList.get(0)).contains("SIGNED")){
			attribute = colSpecList.get(0);
			}
		}
		return attribute;
	}
	
	

	}

