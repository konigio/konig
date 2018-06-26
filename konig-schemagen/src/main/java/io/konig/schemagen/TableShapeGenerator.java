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

import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.XMLSchema;

import io.konig.core.KonigException;
import io.konig.core.NamespaceManager;
import io.konig.maven.TableShapeGeneratorConfig;
import io.konig.shacl.NodeKind;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeManager;
import io.konig.shacl.io.ShapeFileGetter;
import java.io.FileInputStream;
import java.io.InputStream;
import org.apache.commons.io.IOUtils;
import org.apache.maven.model.FileSet;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.create.table.ColDataType;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;
import net.sf.jsqlparser.statement.create.table.CreateTable;

public class TableShapeGenerator {
	
	private SQLShapeGenerator sqlShapeGenerator = new SQLShapeGenerator();

	private NamespaceManager nsManager;
	private TableShapeGeneratorConfig tableConfig;
	
	public TableShapeGenerator(NamespaceManager nsManager,TableShapeGeneratorConfig tableConfig) {
		this.nsManager = nsManager;
		this.tableConfig = tableConfig;
	}

	public void generateTable(File shapesDir) {
		ShapeFileGetter fileGetter = new ShapeFileGetter(shapesDir, nsManager);
		FileSet[] fileSets = tableConfig.getTableFiles();
		for(FileSet fileSet : fileSets) {
			if(fileSet.getDirectory() != null){
				File viewDir = new File(fileSet.getDirectory());
				File[] files = viewDir.listFiles();
				for(File file : files) {
					try (InputStream inputStream = new FileInputStream(file)) {
						String sqlQuery = IOUtils.toString(inputStream);
						Statement createTableStmt = CCJSqlParserUtil.parse(sqlQuery); 
						CreateTable createTable = (CreateTable)createTableStmt;
						List<ColumnDefinition> columnList = createTable.getColumnDefinitions();
						Shape shape = createTableShape(createTable,columnList);
						sqlShapeGenerator.writeShape(shape, fileGetter, nsManager);
					} catch(Exception ex) {
						throw new KonigException(ex);
					}
				}
			}	
		}
	}
	
	
	private Shape createTableShape(CreateTable createTable, List<ColumnDefinition> columnList) {
		String URI ="http://example.com/shapes/";
		String tableName = createTable.getTable().getName();
		String shapeId = URI+tableName;
		Shape shape = new Shape(new URIImpl(shapeId));
		addPropertyContraint(shape, columnList);
		return shape;
	}
		
	private void addPropertyContraint(Shape shape,  List<ColumnDefinition> columnList) {
		
		List<PropertyConstraint> propertyConstraints = new ArrayList<PropertyConstraint>();
			for(ColumnDefinition column : columnList) {
				PropertyConstraint pc = new PropertyConstraint();			
						pc.setPath(new URIImpl(tableConfig.getPropertyNamespace()+column.getColumnName()));
						pc.setMaxCount(1);
						pc.setMinCount(getMinCount(column));
						pc.setFormula(null);
						if(("ID").equals(column.getColumnName())){
							pc.setDatatype(XMLSchema.ANYURI);
						}else{
						pc.setDatatype(getDataType(column));
						}
						shape.add(pc);
						propertyConstraints.add(pc);
				}
			}
	private URI getDataType(ColumnDefinition column){
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

		}
		return datatypeURI;
		
	}
	private int getMinCount(ColumnDefinition column){
		int minCount = 0;
		List<String> colSpecList = column.getColumnSpecStrings();
		if (colSpecList!=null && colSpecList.size() >0){
			minCount = 1;
		}
		return minCount;
	}
	}

