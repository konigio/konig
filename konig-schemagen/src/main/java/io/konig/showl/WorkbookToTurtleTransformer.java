package io.konig.showl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openrdf.rio.RDFHandlerException;

import io.konig.core.Graph;
import io.konig.core.NamespaceManager;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.MemoryNamespaceManager;
import io.konig.shacl.io.ShapeFileGetter;
import io.konig.spreadsheet.IdMapper;
import io.konig.spreadsheet.SpreadsheetException;
import io.konig.spreadsheet.WorkbookLoader;

/**
 * A utility which loads OWL + SHACL statements from a workbook and publishes the
 * workbook contents as a collection of Turtle files.
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

	public void transform(File workbookFile, File owlOutDir, File shapesOutDir) throws IOException, SpreadsheetException, RDFHandlerException {
		
		FileInputStream input = new FileInputStream(workbookFile);
		try {

			owlOutDir.mkdirs();
			if (shapesOutDir != null) {
				shapesOutDir.mkdirs();
			}
			Workbook workbook = new XSSFWorkbook(input);
			
			Graph graph = new MemoryGraph();
			graph.setNamespaceManager(nsManager);
			
			workbookLoader.setDatasetMapper(datasetMapper);
			workbookLoader.load(workbook, graph);
			
			nsManager = workbookLoader.getNamespaceManager();
			graph.setNamespaceManager(nsManager);
			
			OntologyWriter ontologyWriter = new OntologyWriter(new OntologyFileGetter(owlOutDir, nsManager));
			ontologyWriter.writeOntologies(graph);
			
			if (shapesOutDir != null) {
				ShapeWriter shapeWriter = new ShapeWriter(new ShapeFileGetter(shapesOutDir, nsManager));
				shapeWriter.writeShapes(graph);
			}
			
			
		} finally {
			input.close();
		}
	}
	
}
