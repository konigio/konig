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
import org.openrdf.model.impl.URIImpl;

import io.konig.core.Graph;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.io.ShapeFileFactory;
import io.konig.core.vocab.Konig;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeHandler;
import io.konig.shacl.ShapeManager;
import io.konig.shacl.impl.MemoryShapeManager;
import io.konig.shacl.io.ShapeLoader;
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
	private ShapeFileFactory shapeFileFactory;
	private File rdfSourceDir;

	public AuroraTransformGenerator(ShapeRuleFactory shapeRuleFactory, SqlFactory sqlFactory,
			ShapeFileFactory shapeFileFactory, File rdfSourceDir) {
		this.shapeRuleFactory = shapeRuleFactory;
		this.sqlFactory = sqlFactory;
		this.shapeFileFactory = shapeFileFactory;
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
		if (isAuroraTransform(shape)) {
			try {
				shapeRule = shapeRuleFactory.createShapeRule(shape);
				statement = sqlFactory.insertStatement(shapeRule);
			} catch (TransformBuildException e) {
				e.printStackTrace();
			}
			if (statement != null) {
				String dmlText = statement.toString();
				File dmlFile = shapeFileFactory.createFile(shape);
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

	private boolean isAuroraTransform(Shape shape) {
		List<URI> inputShapeOf=shape.getInputShapeOf();
		return (inputShapeOf==null || inputShapeOf.isEmpty()) && shape.hasDataSourceType(Konig.AwsAuroraTable) ;
/*		return shape.getType() != null && shape.getType().contains(Konig.TargetShape)
				&& shape.hasDataSourceType(Konig.AwsAuroraTable);*/
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
