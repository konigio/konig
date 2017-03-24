package io.konig.transform;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.openrdf.model.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.konig.core.NamespaceManager;
import io.konig.core.io.PrettyPrintWriter;
import io.konig.core.path.PathFactory;
import io.konig.core.util.IOUtil;
import io.konig.core.vocab.Konig;
import io.konig.datasource.DataSource;
import io.konig.gcp.datasource.BigQueryTableReference;
import io.konig.gcp.datasource.GoogleBigQueryTable;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeManager;
import io.konig.sql.query.BigQueryCommandLine;
import io.konig.sql.query.DmlExpression;
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

			TransformFrame frame = null;
			
			for (Shape shape : shapeManager.listShapes()) {
				
				if (isLoadTransform(shape)) {
					frame = loadTransform(outDir, shape, scriptQueryWriter);
				}
				if (isCurrentState(shape)) {
					currentStateTransform(outDir, scriptQueryWriter, shape, frame);
				}
			}
		} finally {
			IOUtil.close(scriptQueryWriter, scriptFile.getName());
		}
	}
	

	private boolean isCurrentState(Shape shape) {
		
		for (DataSource ds : shape.getShapeDataSource()) {
			if (ds.isA(Konig.GoogleBigQueryTable) && ds.isA(Konig.CurrentState)) {
				return true;
			}
		}
		
		return false;
	}
	private GoogleBigQueryTable currentStateTable(Shape shape) {
		for (DataSource ds : shape.getShapeDataSource()) {
			if (ds instanceof GoogleBigQueryTable && ds.isA(Konig.CurrentState)) {
				return (GoogleBigQueryTable) ds;
			}
		}
		return null;
	}

	private TransformFrame loadTransform(File outDir, Shape shape, PrettyPrintWriter scriptQueryWriter) throws ShapeTransformException, IOException {
		TransformFrame frame = frameBuilder.create(shape);
		if (frame != null) {
			BigQueryCommandLine cmdline = queryBuilder.bigQueryCommandLine(frame);
			if (cmdline != null) {
				GoogleBigQueryTable table = loadTable(shape);
				File sqlFile = writeDml(outDir, table, cmdline.getDml(), "Load");
				addScript(scriptQueryWriter, sqlFile, cmdline);
			}
		}
		return frame;
	}


	private void currentStateTransform(File outDir, PrettyPrintWriter scriptQueryWriter, Shape shape,
			TransformFrame frame) throws ShapeTransformException, IOException {
		
		if (frame == null) {
			frame = frameBuilder.create(shape);
		}
		if (frame != null) {
			BigQueryCommandLine insert = queryBuilder.insertCommand(frame);
			BigQueryCommandLine update = queryBuilder.updateCommand(frame);
			
			if (insert != null && update!=null) {
				GoogleBigQueryTable table = currentStateTable(shape);
				File insertFile = writeDml(outDir, table, insert.getDml(), "Insert");
				addScript(scriptQueryWriter, insertFile, insert);
				File updateFile = writeDml(outDir, table, update.getDml(), "Update");
				addScript(scriptQueryWriter, updateFile, update);
			}
		}
		
	}

	

	private boolean isLoadTransform(Shape shape) {
		return
				shape.hasDataSourceType(Konig.GoogleBigQueryTable) && 
				!shape.hasDataSourceType(Konig.GoogleCloudStorageBucket) &&
				bucketShapeExists(shape);
	}

	private boolean bucketShapeExists(Shape shape) {
		URI targetClass = shape.getTargetClass();
		for (Shape s : shapeManager.getShapesByTargetClass(targetClass)) {
			if (s.hasDataSourceType(Konig.GoogleCloudStorageBucket)) {
				return true;
			}
		}
		return false;
	}

	private void addScript(PrettyPrintWriter out, File sqlFile, BigQueryCommandLine cmdline) {
		String fileName = sqlFile.getName();
		cmdline.print(out, fileName);
	}


	private File scriptFile(File outDir) {
		return new File(outDir, SCRIPT_FILE_NAME);
	}

	private File writeDml(File outDir, GoogleBigQueryTable table, DmlExpression dml, String action) throws IOException {
		
		File sqlFile = sqlLoadFile(outDir, table.getTableReference(), action);
		FileWriter fileWriter = new FileWriter(sqlFile);
		PrettyPrintWriter queryWriter = new PrettyPrintWriter(fileWriter);
		try {
			dml.print(queryWriter);
			queryWriter.println(';');
		} finally {
			IOUtil.close(queryWriter, sqlFile.getName());
		}
		
		return sqlFile;
	}
	
	
	
	

	private File sqlLoadFile(File outDir, BigQueryTableReference tableRef, String action) {
		
		StringBuilder builder = new StringBuilder();
		builder.append(tableRef.getDatasetId());
		builder.append('_');
		builder.append(tableRef.getTableId());
		builder.append('_');
		builder.append(action);
		builder.append(".sql");
		
		String fileName = builder.toString();
		
		return new File(outDir, fileName);
	}
	
	
	private GoogleBigQueryTable loadTable(Shape shape) {
		for (DataSource ds : shape.getShapeDataSource()) {
			if (ds instanceof GoogleBigQueryTable && !ds.isA(Konig.CurrentState)) {
				return (GoogleBigQueryTable) ds;
			}
		}
		return null;
	}


}
