package io.konig.transform.aws;

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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.List;

import org.openrdf.model.URI;

import io.konig.core.io.ShapeFileFactory;
import io.konig.core.project.ProjectFile;
import io.konig.core.project.ProjectFolder;
import io.konig.core.vocab.Konig;
import io.konig.datasource.DataSource;
import io.konig.datasource.TableDataSource;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeHandler;
import io.konig.sql.query.InsertStatement;
import io.konig.transform.ShapeTransformException;
import io.konig.transform.TransformProcessor;
import io.konig.transform.factory.ShapeRuleFactory;
import io.konig.transform.factory.TransformBuildException;
import io.konig.transform.proto.ShapeModelToShapeRule;
import io.konig.transform.rule.ShapeRule;
import io.konig.transform.sql.factory.SqlFactory;

public class AuroraTransformGenerator implements ShapeHandler {
	private ShapeRuleFactory shapeRuleFactory;
	private SqlFactory sqlFactory;
	private ProjectFolder folder;
	private ShapeFileFactory shapeFileFactory;
	private File rdfSourceDir;

	public AuroraTransformGenerator(ShapeRuleFactory shapeRuleFactory, SqlFactory sqlFactory,
			ProjectFolder folder, File rdfSourceDir) {
		this.shapeRuleFactory = shapeRuleFactory;
		this.sqlFactory = sqlFactory;
		this.folder = folder;
		this.rdfSourceDir = rdfSourceDir;
	}

	@Override
	public void visit(Shape shape) {
		ShapeRule shapeRule = null;
		InsertStatement statement = null;
		if (isTargetShape(shape)) {
			try {
				transferDerivedForm(shape, shapeRule);
			} catch (ShapeTransformException e) {
				e.printStackTrace();
			}
		}
		
		TableDataSource ds = datasource(shape);
		
		if (ds != null) {
			try {
				shapeRule = shapeRuleFactory.createShapeRule(shape);
				statement = sqlFactory.insertStatement(shapeRule);
			} catch (TransformBuildException e) {
				e.printStackTrace();
			}
			if (statement != null) {
				String dmlText = statement.toString();
				ProjectFile file = folder.createFile(ds.getDdlFileName());
				ds.setTransformFile(file);
				File dmlFile = file.getLocalFile();
				File dir = dmlFile.getParentFile();
				if (!dir.exists()) {
					dir.mkdirs();
				}
				try (PrintStream out = new PrintStream(new FileOutputStream(dmlFile))) {
					out.print(dmlText);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
			}

		}

	}

	private boolean isTargetShape(Shape shape) {
/*		List<URI> type = shape.getType();
		return type != null && type.contains(Konig.TargetShape);*/
	List<URI> inputShapeOf=shape.getInputShapeOf();
		return inputShapeOf==null || inputShapeOf.isEmpty();
	}


	@Override
	public void beginShapeTraversal() {
		// TODO Auto-generated method stub

	}

	@Override
	public void endShapeTraversal() {
		// TODO Auto-generated method stub

	}

	public ShapeRuleFactory getShapeRuleFactory() {
		return shapeRuleFactory;
	}

	public void setShapeRuleFactory(ShapeRuleFactory shapeRuleFactory) {
		this.shapeRuleFactory = shapeRuleFactory;
	}

	public SqlFactory getSqlFactory() {
		return sqlFactory;
	}

	public void setSqlFactory(SqlFactory sqlFactory) {
		this.sqlFactory = sqlFactory;
	}

	public ShapeFileFactory getShapeFileFactory() {
		return shapeFileFactory;
	}

	public void setShapeFileFactory(ShapeFileFactory shapeFileFactory) {
		this.shapeFileFactory = shapeFileFactory;
	}


	private TableDataSource datasource(Shape shape) {
		List<URI> inputShapeOf = shape.getInputShapeOf();
		if (inputShapeOf!=null && !inputShapeOf.isEmpty()) {
			return null;
		}
		List<DataSource> list = shape.getShapeDataSource();
		if (list != null) {
			for (DataSource ds : list) {
				if (ds.isA(Konig.AwsAuroraTable)) {
					return (TableDataSource) ds;
				}
			}
		}
		return null;
	}

	private void transferDerivedForm(Shape shape, ShapeRule shapeRule) throws ShapeTransformException {
		ShapeModelToShapeRule shapeModelToRule = shapeRuleFactory.getShapeModelToShapeRule();
		if (shapeRule == null) {
			shapeRule = shapeRuleFactory.createShapeRule(shape);
		}
		if (shapeRule != null) {
			TransformProcessor processor = new TransformProcessor(rdfSourceDir);
			shapeModelToRule.getListTransformprocess().add(processor);
			processor.process(shapeRule);
		}
	}
}
