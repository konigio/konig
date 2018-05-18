package io.konig.showl;

/*
 * #%L
 * Konig Schema Generator
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
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.velocity.VelocityContext;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.rio.RDFHandlerException;

import io.konig.core.Graph;
import io.konig.core.NamespaceManager;
import io.konig.core.Vertex;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.RdfUtil;
import io.konig.core.io.VertexCopier;
import io.konig.core.vocab.AWS;
import io.konig.core.vocab.GCP;
import io.konig.core.vocab.Konig;
import io.konig.core.vocab.SH;
import io.konig.schemagen.gcp.TurtleGenerator;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeManager;
import io.konig.shacl.io.ShapeFileGetter;
import io.konig.spreadsheet.IdMapper;
import io.konig.spreadsheet.SpreadsheetException;
import io.konig.spreadsheet.WorkbookLoader;

/**
 * A utility which loads OWL + SHACL statements from a workbook and publishes
 * the workbook contents as a collection of Turtle files.
 * 
 * @author Greg McFall
 *
 */
public class WorkbookToTurtleTransformer {

	private IdMapper datasetMapper;
	private WorkbookLoader workbookLoader;
	private NamespaceManager nsManager;

	public WorkbookToTurtleTransformer(IdMapper datasetMapper, NamespaceManager nsManager) {
		this.datasetMapper = datasetMapper;

		this.nsManager = nsManager;
		workbookLoader = new WorkbookLoader(nsManager);
	}

	public WorkbookLoader getWorkbookLoader() {
		return workbookLoader;
	}

	public void transform(WorkbookToTurtleRequest request)
			throws IOException, SpreadsheetException, RDFHandlerException {
		File workbookFile = request.getWorkbookFile();
		File workbookDir = request.getWorkbookDir();
		File owlOutDir = request.getOwlOutDir();
		File shapesOutDir = request.getShapesOutDir();
		File gcpOutDir = request.getGcpOutDir();
		File awsOutDir = request.getAwsOutDir();
		File derivedDir = request.getDerivedFormOutDir();

		if (workbookFile != null && !workbookFile.exists()) {
			throw new SpreadsheetException("File not found: " + workbookFile);
		}

		List<File> files = new ArrayList<File>();
		if (workbookFile != null) {
			files.add(workbookFile);
		}
		if (workbookDir != null && workbookDir.exists() && workbookDir.isDirectory()) {
			File[] filesArr = workbookDir.listFiles();
			if (filesArr != null && filesArr.length > 0) {
				List<File> filesTmp = Arrays.asList(filesArr);
				files.addAll(filesTmp);
			}
		}
		if(files!=null && files.isEmpty()){
			throw new SpreadsheetException("No files available in workbookDir and workbookFile.");
		}
		Graph graph = new MemoryGraph();
		for (File file : files) {
			FileInputStream input = new FileInputStream(file);
			try {

				owlOutDir.mkdirs();
				if (shapesOutDir != null) {
					shapesOutDir.mkdirs();
				}
				Workbook workbook = new XSSFWorkbook(input);

				graph.setNamespaceManager(nsManager);

				workbookLoader.setDatasetMapper(datasetMapper);
				workbookLoader.load(workbook, graph);
			} finally {
				input.close();
			}
		}
		nsManager = workbookLoader.getNamespaceManager();
		graph.setNamespaceManager(nsManager);

		OntologyWriter ontologyWriter = new OntologyWriter(new OntologyFileGetter(owlOutDir, nsManager));
		ontologyWriter.writeOntologies(graph);

		if (shapesOutDir != null) {
			writeShapes(shapesOutDir);
		}

		TurtleGenerator turtleGenerator = new TurtleGenerator();
		VelocityContext context = workbookLoader.getDataSourceGenerator().getContext();
		if (gcpOutDir != null) {
			turtleGenerator.generateAll(GCP.GoogleCloudSqlInstance, gcpOutDir, graph, context);
		}
		if (awsOutDir != null) {
			turtleGenerator.generateAll(AWS.DbCluster, awsOutDir, graph, context);
			turtleGenerator.generateAll(AWS.CloudFormationTemplate, awsOutDir, graph, context);
			turtleGenerator.generateAll(AWS.SecurityTag, awsOutDir, graph, context);
		}

	}

	private void writeShapes(File shapesOutDir) throws RDFHandlerException, IOException {
		ShapeFileGetter fileGetter = new ShapeFileGetter(shapesOutDir, nsManager);
		ShapeManager shapeManager = workbookLoader.getShapeManager();
		VertexCopier copier = new VertexCopier();
		copier.excludeProperty(SH.shape, SH.path, SH.targetClass, SH.valueClass, Konig.aggregationOf, Konig.rollUpBy,
				Konig.defaultShapeFor, Konig.inputShapeOf);
		copier.excludeClass(OWL.CLASS, OWL.DATATYPEPROPERTY, OWL.OBJECTPROPERTY, OWL.FUNCTIONALPROPERTY, RDF.PROPERTY);
		for (Shape shape : shapeManager.listShapes()) {
			Resource shapeId = shape.getId();
			if (shapeId instanceof URI) {
				URI shapeURI = (URI) shapeId;

				Vertex shapeVertex = workbookLoader.getGraph().getVertex(shapeURI);
				Graph graph = new MemoryGraph();
				copier.deepCopy(shapeVertex, graph);
				File shapeFile = fileGetter.getFile(shapeURI);
				RdfUtil.prettyPrintTurtle(nsManager, graph, shapeFile);
			}

		}

	}

}
