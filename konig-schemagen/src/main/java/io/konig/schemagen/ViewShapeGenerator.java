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
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.maven.model.FileSet;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

import io.konig.core.KonigException;
import io.konig.core.NamespaceManager;
import io.konig.maven.ViewShapeGeneratorConfig;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeManager;
import io.konig.shacl.io.ShapeFileGetter;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.create.view.CreateView;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectBody;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.util.TablesNamesFinder;

public class ViewShapeGenerator{
	
	private NamespaceManager nsManager;
	private ShapeManager shapeManager;
	private ViewShapeGeneratorConfig config;
	private SQLShapeGenerator sqlShapeGenerator = new SQLShapeGenerator();
	public ViewShapeGenerator(NamespaceManager nsManager, ShapeManager shapeManager, ViewShapeGeneratorConfig config) {
		this.nsManager = nsManager;
		this.shapeManager = shapeManager;
		this.config = config;
	}

	public void generateView(File shapesDir) {
		ShapeFileGetter fileGetter = new ShapeFileGetter(shapesDir, nsManager);
		FileSet[] fileSets = config.getViewFiles();
		for(FileSet fileSet : fileSets) {
			if(fileSet.getDirectory() != null){
				File viewDir = new File(fileSet.getDirectory());
				File[] files = viewDir.listFiles();
				for(File file : files) {
					try (InputStream inputStream = new FileInputStream(file)) {
						String sqlQuery = IOUtils.toString(inputStream);
						
						Statement createViewStmt = CCJSqlParserUtil.parse(sqlQuery);
						CreateView createView = (CreateView) createViewStmt;
						SelectBody selectStmt = createView.getSelectBody();
						
						Select selectStatement = (Select) CCJSqlParserUtil.parse(selectStmt.toString());
						TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
						List<String> tableList = tablesNamesFinder.getTableList(selectStatement);
						
						Shape shape = createViewShape(createView,tableList);
						
						sqlShapeGenerator.writeShape(shape, fileGetter,nsManager);
						
					} catch(Exception ex) {
						throw new KonigException(ex);
					}
				}
			}	
		}
	}
	
	public Shape createViewShape(CreateView createView, List<String> tableList) {
		String viewName = createView.getView().getName();
		String shapeId = viewName.replaceAll(config.getShapeIriPattern(), config.getShapeIriReplacement());
		Shape shape = new Shape(new URIImpl(shapeId));
		SelectBody selectStmt = createView.getSelectBody();
		
		for(String tableName : tableList) {
			Shape selectShape = sqlShapeGenerator.getShape(tableName, shapeManager);
			shape.setShapeDataSource(sqlShapeGenerator.getViewDatasource(selectShape, viewName));
			if (selectStmt instanceof PlainSelect) {
				PlainSelect select = (PlainSelect)selectStmt;
				List<SelectItem> selectItems = select.getSelectItems();
				addViewPropertyContraint(shape, selectShape, selectItems);
			}
		}
		return shape;
	}
	
	private void addViewPropertyContraint(Shape shape, Shape selectShape, List<SelectItem> selectItems) {
		List<PropertyConstraint> propertyConstraints = selectShape.getProperty();
		for(SelectItem selectItem : selectItems) {
			if (selectItem instanceof SelectExpressionItem) {
				SelectExpressionItem expItem = (SelectExpressionItem) selectItem;
				for(PropertyConstraint pc : propertyConstraints) {
					URI path = new URIImpl(pc.getPath().toString());	
					String pathName = path.getLocalName().replace(">", "").trim();
					if(pathName.equals(expItem.getExpression().toString())){
						pc.setPath(new URIImpl(config.getPropertyNamespace()+expItem.getAlias().getName()));
						pc.setFormula(null);
						shape.add(pc);
					}
				}
			}
		}
	}

}
