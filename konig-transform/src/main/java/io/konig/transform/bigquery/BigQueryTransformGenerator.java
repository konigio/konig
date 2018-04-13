package io.konig.transform.bigquery;

/*
 * #%L
 * Konig Transform
 * %%
 * Copyright (C) 2015 - 2017 Gregory McFall
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
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.openrdf.model.URI;

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
import io.konig.transform.TransformProcessor;
import io.konig.transform.factory.ShapeRuleFactory;
import io.konig.transform.factory.TransformBuildException;
import io.konig.transform.proto.BigQueryChannelFactory;
import io.konig.transform.proto.ShapeModelFactory;
import io.konig.transform.proto.ShapeModelToShapeRule;
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
	private File rdfSourceDir;
	

	

	public BigQueryTransformGenerator(ShapeManager shapeManager, File outDir, ShapeRuleFactory shapeRuleFactory,
			BigQueryCommandLineFactory bqCmdLineFactory) {
		this.shapeManager = shapeManager;
		this.outDir = outDir;
		this.shapeRuleFactory = shapeRuleFactory;
		this.bqCmdLineFactory = bqCmdLineFactory;
		
	}
	
	public BigQueryTransformGenerator(ShapeManager shapeManager, File outDir, OwlReasoner owlReasoner) {
		this(
			shapeManager, 
			outDir, 
			new ShapeRuleFactory(shapeManager, new ShapeModelFactory(shapeManager, new BigQueryChannelFactory(), owlReasoner), new ShapeModelToShapeRule()),
			new BigQueryCommandLineFactory(new SqlFactory())
		);
	}
	
	public BigQueryTransformGenerator(ShapeManager shapeManager, File outDir, OwlReasoner owlReasoner,File rdfSourceDir) {
		this(
			shapeManager, 
			outDir, 
			new ShapeRuleFactory(shapeManager, new ShapeModelFactory(shapeManager, new BigQueryChannelFactory(), owlReasoner), new ShapeModelToShapeRule()),
			new BigQueryCommandLineFactory(new SqlFactory())			
		);
		this.rdfSourceDir=rdfSourceDir;
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
				transferDerivedForm(shape, shapeRule);
				transformCount++;
			} catch (Throwable e) {
				addError(e);
			}
		}
		
		if (isCurrentStateTransform(shape)) {
			try {
				shapeRule = currentStateTransform(shape, shapeRule);
				transferDerivedForm(shape, shapeRule);
				transformCount++;
			} catch (Throwable e) {
				addError(e);
			}
		}
		
		
	}
	private ShapeRule currentStateTransform(Shape shape, ShapeRule shapeRule) throws ShapeTransformException, IOException {
		if (shapeRule == null) {
			shapeRule = shapeRuleFactory.createShapeRule(shape);
		}
		if (shapeRule != null) {
			BigQueryCommandLine cmdline = bqCmdLineFactory.updateCommand(shapeRule);
			if (cmdline != null) {
				GoogleBigQueryTable table = currentStateTable(shape);
				File sqlFile = writeDml(table, cmdline.getDml(), "Load", 0);
				commandLineInfo.add(new CommandLineInfo(sqlFile, cmdline));
			}
		}
		return shapeRule;
		
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
				File sqlFile = writeDml(table, cmdline.getDml(), "Load", 0);
				handleRollup(shapeRule, table);
				commandLineInfo.add(new CommandLineInfo(sqlFile, cmdline));
			}
		}
		return shapeRule;
	}
	
	private void transferDerivedForm(Shape shape, ShapeRule shapeRule) throws ShapeTransformException
	{
		ShapeModelToShapeRule shapeModelToRule=shapeRuleFactory.getShapeModelToShapeRule();
		if (shapeRule == null) {
			shapeRule = shapeRuleFactory.createShapeRule(shape);
		}
		
		if (shapeRule != null) {
			TransformProcessor processor=new TransformProcessor(rdfSourceDir);
			shapeModelToRule.getListTransformprocess().add(processor);
			processor.process(shapeRule);
		}
	}

	private void handleRollup(ShapeRule shapeRule, GoogleBigQueryTable table) throws ShapeTransformException, IOException {
		
		ShapeRule rollup = shapeRule.getRollUp();
		int count = 0;
		while (rollup != null) {
			count++;
			BigQueryCommandLine cmdline = bqCmdLineFactory.insertCommand(rollup);
			File sqlFile = writeDml(table, cmdline.getDml(), "Load", count);
			commandLineInfo.add(new CommandLineInfo(sqlFile, cmdline));
			rollup = rollup.getRollUp();
		}
		
	}

	private File writeDml(GoogleBigQueryTable table, DmlExpression dml, String action, int count) throws IOException {

		File sqlFile = sqlLoadFile(table.getTableReference(), action, count);
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
	
	private File sqlLoadFile(BigQueryTableReference tableRef, String action, int count) {
		
		StringBuilder builder = new StringBuilder();
		builder.append(tableRef.getDatasetId());
		builder.append('_');
		builder.append(tableRef.getTableId());
		builder.append('_');
		builder.append(action);
		if (count>0) {
			builder.append('_');
			builder.append(count);
		}
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
				isDerivedShape(shape) &&
				shape.hasDataSourceType(Konig.GoogleBigQueryTable) && 
				!shape.hasDataSourceType(Konig.GoogleCloudStorageBucket) &&
				!shape.hasDataSourceType(Konig.CurrentState) &&
				(bucketShapeExists(shape)) || !shape.getVariable().isEmpty();
	}
	/**
	 * Test whether a given shape is derived from some input shapes.
	 * @return true if the given Shape is NOT the input to any system component.  If it is not an input, then it must be derived.
	 */
	private boolean isDerivedShape(Shape shape) {
		// TODO: refactor the logic so that we are testing whether it is a derived shape
		// relative to some given system component -- not for any system component.
		List<URI> inputShapeOf=shape.getInputShapeOf();
		return inputShapeOf==null || inputShapeOf.isEmpty();
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
