package io.konig.transform.mysql;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.openrdf.model.URI;

import io.konig.core.OwlReasoner;
import io.konig.core.io.PrettyPrintWriter;
import io.konig.core.vocab.Konig;
import io.konig.datasource.DataSource;
import io.konig.gcp.datasource.GoogleCloudSqlTable;
import io.konig.gcp.datasource.GoogleCloudSqlTableInfo;
import io.konig.gcp.io.GoogleCloudSqlJsonUtil;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeHandler;
import io.konig.shacl.ShapeManager;
import io.konig.sql.query.DmlExpression;
import io.konig.sql.query.InsertStatement;
import io.konig.sql.query.SelectExpression;
import io.konig.sql.query.UpdateExpression;
import io.konig.transform.ShapeTransformException;
import io.konig.transform.factory.ShapeRuleFactory;
import io.konig.transform.proto.MySqlChannelFactory;
import io.konig.transform.proto.ShapeModelFactory1;
import io.konig.transform.proto.ShapeModelToShapeRule;
import io.konig.transform.rule.ShapeRule;
import io.konig.transform.sql.factory.SqlFactory;

/*
 * #%L
 * Konig Transform
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

public class MySqlTransformGenerator implements ShapeHandler {

	private ShapeManager shapeManager;
	private File outDir;
	private ShapeRuleFactory shapeRuleFactory;
	private List<Throwable> errorList;

	public MySqlTransformGenerator(ShapeManager shapeManager, File outDir, OwlReasoner owlReasoner) {
		this(shapeManager, outDir,
				new ShapeRuleFactory(shapeManager,
						new ShapeModelFactory1(shapeManager, new MySqlChannelFactory(), owlReasoner),
						new ShapeModelToShapeRule()));
	}

	public MySqlTransformGenerator(ShapeManager shapeManager, File outDir, ShapeRuleFactory shapeRuleFactory) {
		this.shapeManager = shapeManager;
		this.outDir = outDir;
		this.shapeRuleFactory = shapeRuleFactory;
	}

	public void generateAll() {
		beginShapeTraversal();
		for (Shape shape : shapeManager.listShapes()) {
			visit(shape);
		}
		endShapeTraversal();
	}

	public ShapeManager getShapeManager() {
		return shapeManager;
	}

	public void setShapeManager(ShapeManager shapeManager) {
		this.shapeManager = shapeManager;
	}

	public File getOutDir() {
		return outDir;
	}

	public void setOutDir(File outDir) {
		this.outDir = outDir;
	}

	public ShapeRuleFactory getShapeRuleFactory() {
		return shapeRuleFactory;
	}

	public void setShapeRuleFactory(ShapeRuleFactory shapeRuleFactory) {
		this.shapeRuleFactory = shapeRuleFactory;
	}

	public List<Throwable> getErrorList() {
		return errorList;
	}

	public void setErrorList(List<Throwable> errorList) {
		this.errorList = errorList;
	}

	@Override
	public void visit(Shape shape) {
		ShapeRule shapeRule = null;
		if (isLoadTransform(shape)) {
			try {
				shapeRule = loadTransform(shape);
			} catch (Throwable e) {
				addError(e);
			}
		}
		if (isCurrentStateTransform(shape)) {
			try {
				currentStateTransform(shape, shapeRule);
			} catch (Throwable e) {
				addError(e);
			}
		}
	}

	private ShapeRule loadTransform(Shape shape) throws ShapeTransformException, IOException {
		ShapeRule shapeRule = shapeRuleFactory.createShapeRule(shape);
		
		if (shapeRule != null) {
			SqlFactory sqlFactory = new SqlFactory();
			InsertStatement insert = sqlFactory.insertStatement(shapeRule);
			if (insert != null) {
				GoogleCloudSqlTable table = loadTable(shape);
				File sqlFile = writeDml(table, insert);
				writeJson(table, sqlFile);
			}
		}
		return shapeRule;
	}
	private void currentStateTransform(Shape shape, ShapeRule shapeRule) throws ShapeTransformException, IOException {
		if (shapeRule == null) {
			shapeRule = shapeRuleFactory.createShapeRule(shape);
		}
		if (shapeRule != null) {
			SqlFactory sqlFactory = new SqlFactory();
			UpdateExpression update = sqlFactory.updateExpression(shapeRule);
			if (update != null) {
				GoogleCloudSqlTable table = currentStateTable(shape);
				File sqlFile = writeDml(table, update);
				writeJson(table, sqlFile);
			}
		}
	}

	private File writeJson(GoogleCloudSqlTable table, File sqlFile) throws IOException {
		GoogleCloudSqlTableInfo tableInfo = new GoogleCloudSqlTableInfo();
		tableInfo.setTableName(table.getTableName());
		tableInfo.setDatabase(table.getDatabase());
		tableInfo.setId(table.getId());
		tableInfo.setDdlFile(sqlFile);
		tableInfo.setInstance(table.getInstance());
		tableInfo.setInstanceFile(new File(table.getInstance() + ".json"));
		File jsonFile = sqlLoadFile(table, "json");
		try (FileWriter writer = new FileWriter(jsonFile)) {
			GoogleCloudSqlJsonUtil.writeJson(tableInfo, writer);
		}
		return null;
	}

	private GoogleCloudSqlTable currentStateTable(Shape shape) {
		for (DataSource ds : shape.getShapeDataSource()) {
			if (ds instanceof GoogleCloudSqlTable && ds.isA(Konig.CurrentState)) {
				return (GoogleCloudSqlTable) ds;
			}
		}
		return null;
	}

	private boolean isCurrentStateTransform(Shape shape) {
		if (shape.getShapeDataSource() != null) {
			for (DataSource ds : shape.getShapeDataSource()) {
				if (ds.isA(Konig.GoogleCloudSqlTable) && ds.isA(Konig.CurrentState)) {
					return true;
				}
			}
		}
		return false;
	}
	private GoogleCloudSqlTable loadTable(Shape shape) {
		for (DataSource ds : shape.getShapeDataSource()) {
			if (ds instanceof GoogleCloudSqlTable && !ds.isA(Konig.CurrentState)) {
				return (GoogleCloudSqlTable) ds;
			}
		}
		return null;
	}
	private boolean isLoadTransform(Shape shape) {
		return
				isDerivedShape(shape) || (
				shape.hasDataSourceType(Konig.GoogleCloudSqlTable) && 
				!shape.hasDataSourceType(Konig.CurrentState) 
				|| !shape.getVariable().isEmpty());
	}
	
	private boolean isDerivedShape(Shape shape) {
		List<URI> type = shape.getType();
		return type!=null && type.contains(Konig.DerivedShape);
	}

	
	private void addError(Throwable e) {

		if (errorList == null) {
			errorList = new ArrayList<>();
		}
		errorList.add(e);

	}

	private File writeDml(GoogleCloudSqlTable table, DmlExpression dml) throws IOException {
		File sqlFile = sqlLoadFile(table, "sql");
		sqlFile.getParentFile().mkdirs();
		try (FileWriter fileWriter = new FileWriter(sqlFile);
				PrettyPrintWriter queryWriter = new PrettyPrintWriter(fileWriter);) {
			dml.print(queryWriter);
			queryWriter.println(';');
		}
		return sqlFile;
	}

	private File sqlLoadFile(GoogleCloudSqlTable table, String fileType) {
		String instance = table.getInstance();
		String database = table.getDatabase();
		String tableName = table.getTableName();

		String fileName = MessageFormat.format("{0}_{1}_{2}.{3}", instance, database, tableName, fileType);

		return new File(outDir, fileName);
	}

	@Override
	public void beginShapeTraversal() {
		errorList = new ArrayList<>();
	}

	@Override
	public void endShapeTraversal() {
		
	}
}
