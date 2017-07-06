package io.konig.transform.bigquery;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.openrdf.model.URI;

import io.konig.core.KonigException;
import io.konig.core.OwlReasoner;
import io.konig.core.io.PrettyPrintWriter;
import io.konig.core.vocab.Konig;
import io.konig.datasource.DataSource;
import io.konig.gcp.datasource.BigQueryTableReference;
import io.konig.gcp.datasource.GoogleBigQueryTable;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeHandler;
import io.konig.shacl.ShapeManager;
import io.konig.sql.query.BigQueryCommandLine;
import io.konig.sql.query.DmlExpression;
import io.konig.transform.ShapeTransformException;
import io.konig.transform.factory.BigQueryTransformStrategy;
import io.konig.transform.factory.ShapeRuleFactory;
import io.konig.transform.factory.TransformBuildException;
import io.konig.transform.rule.ShapeRule;
import io.konig.transform.sql.factory.SqlFactory;

public class BigQueryTransformGenerator implements ShapeHandler {

	private static final String SCRIPT_FILE_NAME = "bqScript.sh";
	
	private ShapeManager shapeManager;
	private File outDir;
	private List<CommandLineInfo> commandLineInfo;
	private ShapeRuleFactory shapeRuleFactory;
	private BigQueryCommandLineFactory bqCmdLineFactory;
	private List<Throwable> errorList;
	private int transformCount = 0;
	
	

	public BigQueryTransformGenerator(ShapeManager shapeManager, File outDir, ShapeRuleFactory shapeRuleFactory,
			BigQueryCommandLineFactory bqCmdLineFactory) {
		this.shapeManager = shapeManager;
		this.outDir = outDir;
		this.shapeRuleFactory = shapeRuleFactory;
		this.bqCmdLineFactory = bqCmdLineFactory;
		if (shapeRuleFactory.getStrategy()==null) {
			shapeRuleFactory.setStrategy(new BigQueryTransformStrategy());
		}
	}
	
	public BigQueryTransformGenerator(ShapeManager shapeManager, File outDir, OwlReasoner owlReasoner) {
		this(
			shapeManager, 
			outDir, 
			new ShapeRuleFactory(shapeManager, owlReasoner, new BigQueryTransformStrategy()), 
			new BigQueryCommandLineFactory(new SqlFactory())
		);
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


	public List<CommandLineInfo> getCommandLineInfo() {
		return commandLineInfo;
	}


	public void setCommandLineInfo(List<CommandLineInfo> commandLineInfo) {
		this.commandLineInfo = commandLineInfo;
	}


	public ShapeRuleFactory getShapeRuleFactory() {
		return shapeRuleFactory;
	}


	public void setShapeRuleFactory(ShapeRuleFactory shapeRuleFactory) {
		this.shapeRuleFactory = shapeRuleFactory;
	}


	public BigQueryCommandLineFactory getBqCmdLineFactory() {
		return bqCmdLineFactory;
	}


	public void setBqCmdLineFactory(BigQueryCommandLineFactory bqCmdLineFactory) {
		this.bqCmdLineFactory = bqCmdLineFactory;
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
				transformCount++;
			} catch (Throwable e) {
				addError(e);
			}
		}
		
		if (isCurrentStateTransform(shape)) {
			try {
				currentStateTransform(shape, shapeRule);
				transformCount++;
			} catch (Throwable e) {
				addError(e);
			}
		}
		
	}
	private void currentStateTransform(Shape shape, ShapeRule shapeRule) throws ShapeTransformException, IOException {
		if (shapeRule == null) {
			shapeRule = shapeRuleFactory.createShapeRule(shape);
		}
		if (shapeRule != null) {
			BigQueryCommandLine cmdline = bqCmdLineFactory.updateCommand(shapeRule);
			if (cmdline != null) {
				GoogleBigQueryTable table = currentStateTable(shape);
				File sqlFile = writeDml(table, cmdline.getDml(), "Load");
				commandLineInfo.add(new CommandLineInfo(sqlFile, cmdline));
			}
		}
		
	}
	

	private GoogleBigQueryTable currentStateTable(Shape shape) {
		for (DataSource ds : shape.getShapeDataSource()) {
			if (ds instanceof GoogleBigQueryTable && ds.isA(Konig.CurrentState)) {
				return (GoogleBigQueryTable) ds;
			}
		}
		return null;
	}
	
	private boolean isCurrentStateTransform(Shape shape) {

		if (shape.getShapeDataSource() != null) {
			for (DataSource ds : shape.getShapeDataSource()) {
				if (ds.isA(Konig.GoogleBigQueryTable) && ds.isA(Konig.CurrentState)) {
					return true;
				}
			}
		}
		
		return false;
	}

	private void addError(Throwable e) {
	
		if (errorList == null) {
			errorList = new ArrayList<>();
		}
		errorList.add(e);
		
	}

	private ShapeRule loadTransform(Shape shape) throws ShapeTransformException, IOException {
		ShapeRule shapeRule = shapeRuleFactory.createShapeRule(shape);
		
		if (shapeRule != null) {
			BigQueryCommandLine cmdline = bqCmdLineFactory.insertCommand(shapeRule);
			if (cmdline != null) {
				GoogleBigQueryTable table = loadTable(shape);
				File sqlFile = writeDml(table, cmdline.getDml(), "Load");
				commandLineInfo.add(new CommandLineInfo(sqlFile, cmdline));
			}
		}
		return shapeRule;
	}

	private File writeDml(GoogleBigQueryTable table, DmlExpression dml, String action) throws IOException {

		File sqlFile = sqlLoadFile(table.getTableReference(), action);
		sqlFile.getParentFile().mkdirs();
		try (
			FileWriter fileWriter = new FileWriter(sqlFile);
			PrettyPrintWriter queryWriter = new PrettyPrintWriter(fileWriter);
		) {
			dml.print(queryWriter);
			queryWriter.println(';');
		} 
		
		return sqlFile;
	}
	
	private File sqlLoadFile(BigQueryTableReference tableRef, String action) {
		
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
	
	private boolean isLoadTransform(Shape shape) {
		return
				shape.hasDataSourceType(Konig.GoogleBigQueryTable) && 
				!shape.hasDataSourceType(Konig.GoogleCloudStorageBucket) &&
				!shape.hasDataSourceType(Konig.CurrentState) &&
				bucketShapeExists(shape);
	}
	private boolean bucketShapeExists(Shape shape) {
		URI targetClass = shape.getTargetClass();
		if (targetClass != null) {
			for (Shape s : shapeManager.getShapesByTargetClass(targetClass)) {
				if (s.hasDataSourceType(Konig.GoogleCloudStorageBucket)) {
					return true;
				}
			}
		}
		return false;
	}
	
	private static class CommandLineInfo {
		File file;
		BigQueryCommandLine cmdLine;
		public CommandLineInfo(File file, BigQueryCommandLine cmdLine) {
			this.file = file;
			this.cmdLine = cmdLine;
		}
		
		public File getFile() {
			return file;
		}

		public BigQueryCommandLine getCommandLine() {
			return cmdLine;
		}
	}

	@Override
	public void beginShapeTraversal() {
		errorList = new ArrayList<>();
		commandLineInfo = new ArrayList<>();
		transformCount = 0;
	}

	@Override
	public void endShapeTraversal() {


		if (transformCount > 0) {
			File scriptFile = scriptFile();
			File parentDir = scriptFile.getParentFile();
			if (parentDir.exists()) {
				try (
					FileWriter scriptFileWriter = new FileWriter(scriptFile);
					PrettyPrintWriter scriptQueryWriter = new PrettyPrintWriter(scriptFileWriter)
				) {
					writeBufferedCommands(scriptQueryWriter);
				} catch (IOException e) {
					errorList.add(e);
				} finally {
					commandLineInfo = null;
				}
			}
		}
		
	}

	private void writeBufferedCommands(PrettyPrintWriter scriptQueryWriter) {
		if (commandLineInfo != null) {
			for (CommandLineInfo info : commandLineInfo) {
				addScript(scriptQueryWriter, info.getFile(), info.getCommandLine());
			}
		}
	}


	private void addScript(PrettyPrintWriter out, File sqlFile, BigQueryCommandLine cmdline) {
		String fileName = sqlFile.getName();
		cmdline.print(out, fileName);
	}
	
	private File scriptFile() {
		return new File(outDir, SCRIPT_FILE_NAME);
	}
}
