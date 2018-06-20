package io.konig.schemagen.aws;

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
import java.io.FileWriter;
import java.io.IOException;
import java.text.MessageFormat;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.konig.aws.datasource.AwsAuroraTable;
import io.konig.aws.datasource.AwsAuroraDefinition;
import io.konig.aws.datasource.AwsAuroraTableReference;
import io.konig.aws.datasource.AwsAuroraView;
import io.konig.core.KonigException;
import io.konig.datasource.DatasourceFileLocator;
import io.konig.schemagen.sql.SqlTable;
import io.konig.schemagen.sql.SqlTableGenerator;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeVisitor;
import io.konig.sql.query.SelectExpression;
import io.konig.transform.ShapeTransformException;
import io.konig.transform.factory.ShapeRuleFactory;
import io.konig.transform.proto.ShapeModel;
import io.konig.transform.proto.ShapeModelFactory;
import io.konig.transform.proto.ShapeModelToShapeRule;
import io.konig.transform.rule.ShapeRule;
import io.konig.transform.sql.factory.SqlFactory;

public class AwsAuroraViewWriter implements ShapeVisitor{
	private File baseDir;
	private SqlTableGenerator generator;
	private DatasourceFileLocator sqlFileLocator;
	private ShapeModelFactory shapeModelFactory;
	private File abbrevDir;

	// TODO: Add a DatasourceFileLocator as a private field and pass it to the constructor. 
	
	public AwsAuroraViewWriter(File baseDir,SqlTableGenerator generator, DatasourceFileLocator sqlFileLocator,ShapeModelFactory shapeModelFactory,
			File abbrevDir) {
		this.baseDir = baseDir;
		this.generator = generator;
		this.sqlFileLocator = sqlFileLocator;
		this.shapeModelFactory=shapeModelFactory;
		this.abbrevDir=abbrevDir;
	}

	@Override
	public void visit(Shape shape) {
		AwsAuroraView view = shape.findDataSource(AwsAuroraView.class);
		if (view != null) {
			AwsAuroraDefinition tableDefinition = new AwsAuroraDefinition();
			AwsAuroraTableReference tableReference = view.getTableReference();
			SqlTable sqlTable = generator.generateTable(shape,abbrevDir);
			File file = sqlFile(view);
			String tableName = sqlTable.getTableName();
			String schemaName = tableReference.getAwsSchema();
			if (schemaName != null) {
				StringBuilder builder = new StringBuilder();
				builder.append(schemaName);
				builder.append('.');
				builder.append(tableName);
				sqlTable.setTableName(builder.toString());
			}
			try{
				writeDDL(file, sqlTable,shape);
			}
			catch(Exception ex) {
				throw new KonigException("Unable to create the view for the shape " + shape, ex);
			}
			sqlTable.setTableName(tableName);
			tableDefinition.setQuery(file.getName());
			tableDefinition.setTableReference(view.getTableReference());
			File jsonFile = jsonFile(tableReference);
			writeTable(jsonFile, tableDefinition);
		}

	}
	private void writeTable(File jsonFile, AwsAuroraDefinition table) {
		try {
			ObjectMapper mapper = new ObjectMapper();
			mapper.writeValue(jsonFile, table);
		} catch (IOException e) {
			throw new KonigException(e);
		}
	}
	
	private void writeDDL(File file, SqlTable sqlTable,Shape shape) throws ShapeTransformException {
		if (!baseDir.exists()) {
			baseDir.mkdirs();
		}
		try (FileWriter out = new FileWriter(file)) {
			ShapeModel shapeModel = shapeModelFactory.createShapeModel(shape);
			ShapeModelToShapeRule shapeRuleFactory = new ShapeModelToShapeRule();
			SqlFactory sqlFactory =new SqlFactory();
			ShapeRule shapeRule = shapeRuleFactory.toShapeRule(shapeModel);
			SelectExpression select = sqlFactory.selectExpression(shapeRule);
			out.write("CREATE VIEW "+sqlTable.getTableName()+" AS ");
			out.write(select.toString());
			
		} catch (IOException e) {
			throw new KonigException(e);
		}
		
	}

	private File fileName(AwsAuroraTableReference table, String fileType) {
		String instance = table.getAwsAuroraHost();
		String database = table.getAwsSchema();
		String tableName = table.getAwsTableName();
		
		String fileName = MessageFormat.format("{0}_{1}_{2}.{3}", instance, database, tableName , fileType);
		
		return new File(baseDir, fileName);
	}
	
	private File jsonFile(AwsAuroraTableReference table) {
		return fileName(table, "json");
	}

	private File sqlFile(AwsAuroraView view) {		
		return sqlFileLocator.locateFile(view);
	}
}
