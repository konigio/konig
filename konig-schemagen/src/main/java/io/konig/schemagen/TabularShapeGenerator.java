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
import org.apache.commons.io.IOUtils;
import org.apache.maven.model.FileSet;
import io.konig.core.KonigException;
import io.konig.core.NamespaceManager;
import io.konig.maven.TabularShapeGeneratorConfig;
import io.konig.shacl.ShapeManager;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.create.view.CreateView;

public class TabularShapeGenerator {
	
	private NamespaceManager nsManager;
	private ShapeManager shapeManager;
	
	public TabularShapeGenerator(NamespaceManager nsManager, ShapeManager shapeManager) {
		this.nsManager = nsManager;
		this.shapeManager = shapeManager;
	}

	public void generateTabularShapes(File shapesDir, TabularShapeGeneratorConfig config)
			throws TabularShapeGenerationException {
		FileSet[] fileSets = config.getSqlFiles();
		for (FileSet fileSet : fileSets) {
			if (fileSet.getDirectory() != null) {
				File viewDir = new File(fileSet.getDirectory());
				File[] files = viewDir.listFiles();
				for (File file : files) {
					try (InputStream inputStream = new FileInputStream(file)) {
						String sqlQuery = IOUtils.toString(inputStream);
						sqlQuery = sqlQuery.replaceAll("\\bIF NOT EXISTS\\b", "");
						Statement statement = CCJSqlParserUtil.parse(sqlQuery);
						if (statement != null && !"".equals(statement) && statement instanceof CreateView) {
							ViewShapeGenerator viewShapeGenerator = new ViewShapeGenerator(nsManager, shapeManager,
									config);
							viewShapeGenerator.generateView(shapesDir, statement);
						}
						if (statement != null && !"".equals(statement) && statement instanceof CreateTable) {

							TableShapeGenerator tableShapeGenerator = new TableShapeGenerator(nsManager, config);
							tableShapeGenerator.generateTable(shapesDir, statement);

						}
					} catch (Exception ex) {
						throw new KonigException(ex);
					}
				}
			}
		}
		 
	 }
}
