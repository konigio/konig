package io.konig.schemagen;

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
