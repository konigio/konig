package io.konig.transform;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.konig.core.NamespaceManager;
import io.konig.core.path.PathFactory;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeManager;
import io.konig.sql.query.BigQueryCommandLine;
import io.konig.sql.query.QueryWriter;
import io.konig.sql.query.SelectExpression;
import io.konig.transform.sql.query.SqlQueryBuilder;

public class TransformGenerator {
	private static final Logger logger = LoggerFactory.getLogger(TransformGenerator.class);
	
	private static final String SCRIPT_FILE_NAME = "bqScript.sh";
	private ShapeManager shapeManager;
	private ShapeTransformModelBuilder modelBuilder;
	private SqlQueryBuilder queryBuilder;
	
	public TransformGenerator(NamespaceManager nsManager, ShapeManager shapeManager, PathFactory pathFactory) {

		this.shapeManager = shapeManager;
		modelBuilder = new ShapeTransformModelBuilder(shapeManager, pathFactory);
		queryBuilder = new SqlQueryBuilder(nsManager);
	}
	
	public void generateAll(File outDir) throws ShapeTransformException, IOException {
		outDir.mkdirs();
		File scriptFile = scriptFile(outDir);
		FileWriter scriptFileWriter = new FileWriter(scriptFile);
		try {

			QueryWriter scriptQueryWriter = new QueryWriter(scriptFileWriter);
			
			for (Shape shape : shapeManager.listShapes()) {
				if (shape.getSourceShape() != null && shape.getBigQueryTableId()!=null) {
					ShapeTransformModel model = modelBuilder.build(shape);
					if (model != null) {
						BigQueryCommandLine cmdline = queryBuilder.toBigQueryCommandLine(model);
						File sqlFile = writeQuery(outDir, shape, cmdline.getSelect());
						addScript(scriptQueryWriter, sqlFile, cmdline);
					}
				}
			}
		} finally {
			close(scriptFileWriter);
		}
	}


	private void addScript(QueryWriter scriptQueryWriter, File sqlFile, BigQueryCommandLine cmdline) {
		String fileName = sqlFile.getName();
		scriptQueryWriter.print(cmdline, fileName);
	}

	private void close(FileWriter writer) {
		try {
			writer.close();
		} catch (Exception ignore) {
			logger.warn("Failed to close file", ignore);
		}
		
	}

	private File scriptFile(File outDir) {
		return new File(outDir, SCRIPT_FILE_NAME);
	}

	private File writeQuery(File outDir, Shape shape, SelectExpression select) throws IOException {
		
		File sqlFile = sqlFile(outDir, shape);
		FileWriter fileWriter = new FileWriter(sqlFile);
		try {
			QueryWriter queryWriter = new QueryWriter(fileWriter);
			queryWriter.print(select);
		} finally {
			close(fileWriter);
		}
		
		return sqlFile;
	}

	private File sqlFile(File outDir, Shape shape) {
		String fileName = shape.getBigQueryTableId().replace('.', '_') + ".sql";
		
		return new File(outDir, fileName);
	}

}
