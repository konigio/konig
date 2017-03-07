package io.konig.transform;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.konig.core.NamespaceManager;
import io.konig.core.io.PrettyPrintWriter;
import io.konig.core.path.PathFactory;
import io.konig.core.util.IOUtil;
import io.konig.core.vocab.Konig;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeManager;
import io.konig.sql.query.BigQueryCommandLine;
import io.konig.sql.query.DmlExpression;
import io.konig.sql.query.SelectExpression;
import io.konig.transform.sql.query.QueryBuilder;

public class TransformGenerator {
	private static final Logger logger = LoggerFactory.getLogger(TransformGenerator.class);
	
	private static final String SCRIPT_FILE_NAME = "bqScript.sh";
	private ShapeManager shapeManager;
	private TransformFrameBuilder frameBuilder;
	private QueryBuilder queryBuilder;
	
	public TransformGenerator(
		NamespaceManager nsManager, 
		ShapeManager shapeManager, 
		PathFactory pathFactory, 
		QueryBuilder queryBuilder
	) {

		this.shapeManager = shapeManager;
		frameBuilder = new TransformFrameBuilder(shapeManager, pathFactory);
		this.queryBuilder = queryBuilder;
	}
	
	public void generateAll(File outDir) throws ShapeTransformException, IOException {
		outDir.mkdirs();
		File scriptFile = scriptFile(outDir);
		FileWriter scriptFileWriter = new FileWriter(scriptFile);
		PrettyPrintWriter scriptQueryWriter = new PrettyPrintWriter(scriptFileWriter);
		try {

			
			for (Shape shape : shapeManager.listShapes()) {
				
				if (
					shape.hasDataSourceType(Konig.GoogleBigQueryTable) && 
					!shape.hasDataSourceType(Konig.GoogleCloudStorageBucket)
				) {
					
					TransformFrame frame = frameBuilder.create(shape);
					if (frame != null) {
						BigQueryCommandLine cmdline = queryBuilder.bigQueryCommandLine(frame);
						if (cmdline != null) {
							File sqlFile = writeQuery(outDir, shape, cmdline.getSelect());
							addScript(scriptQueryWriter, sqlFile, cmdline);
						}
					}
				}
			}
		} finally {
			IOUtil.close(scriptQueryWriter, scriptFile.getName());
		}
	}



	private void addScript(PrettyPrintWriter out, File sqlFile, BigQueryCommandLine cmdline) {
		String fileName = sqlFile.getName();
		cmdline.print(out, fileName);
	}


	private File scriptFile(File outDir) {
		return new File(outDir, SCRIPT_FILE_NAME);
	}

	private File writeQuery(File outDir, Shape shape, DmlExpression select) throws IOException {
		
		File sqlFile = sqlFile(outDir, shape);
		FileWriter fileWriter = new FileWriter(sqlFile);
		PrettyPrintWriter queryWriter = new PrettyPrintWriter(fileWriter);
		try {
			select.print(queryWriter);
			queryWriter.println(';');
		} finally {
			IOUtil.close(queryWriter, sqlFile.getName());
		}
		
		return sqlFile;
	}

	private File sqlFile(File outDir, Shape shape) {
		String bigQueryTableId = shape.bigQueryTableId();
		
		String fileName = bigQueryTableId.replace('.', '_') + ".sql";
		
		return new File(outDir, fileName);
	}


}
