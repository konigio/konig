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
	
	
	
	public WorkbookToTurtleTransformer(IdMapper datasetMapper) {
		this.datasetMapper = datasetMapper;
	}

	public void transform(File workbookFile, File owlOutDir, File shapesOutDir) throws IOException, SpreadsheetException, RDFHandlerException {
		
		FileInputStream input = new FileInputStream(workbookFile);
		try {

			owlOutDir.mkdirs();
			shapesOutDir.mkdirs();
			Workbook workbook = new XSSFWorkbook(input);
			
			NamespaceManager nsManager = new MemoryNamespaceManager();
			Graph graph = new MemoryGraph();
			graph.setNamespaceManager(nsManager);
			
			WorkbookLoader loader = new WorkbookLoader(nsManager);
			loader.setDatasetMapper(datasetMapper);
			loader.load(workbook, graph);
			
			
			OntologyWriter ontologyWriter = new OntologyWriter(new OntologyFileGetter(owlOutDir, nsManager));
			ShapeWriter shapeWriter = new ShapeWriter(new ShapeFileGetter(shapesOutDir, nsManager));
			
			ontologyWriter.writeOntologies(graph);
			shapeWriter.writeShapes(graph);
			
			
		} finally {
			input.close();
		}
	}
	
}
