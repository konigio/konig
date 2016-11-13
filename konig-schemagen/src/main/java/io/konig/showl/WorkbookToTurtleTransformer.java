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
import io.konig.spreadsheet.SpreadsheetException;
import io.konig.spreadsheet.WorkbookLoader;

/**
 * A utility which loads OWL + SHACL statements from a workbook and publishes the
 * workbook contents as a collection of Turtle files.
 * @author Greg McFall
 *
 */
public class WorkbookToTurtleTransformer {

	public void transform(File workbookFile, File outDir) throws IOException, SpreadsheetException, RDFHandlerException {
		
		FileInputStream input = new FileInputStream(workbookFile);
		try {

			outDir.mkdirs();
			Workbook workbook = new XSSFWorkbook(input);
			
			NamespaceManager nsManager = new MemoryNamespaceManager();
			Graph graph = new MemoryGraph();
			graph.setNamespaceManager(nsManager);
			
			WorkbookLoader loader = new WorkbookLoader(nsManager);
			loader.load(workbook, graph);
			
			
			OntologyWriter ontologyWriter = new OntologyWriter(new OntologyFileGetter(outDir, nsManager));
			ShapeWriter shapeWriter = new ShapeWriter(new ShapeFileGetter(outDir, nsManager));
			
			ontologyWriter.writeOntologies(graph);
			shapeWriter.writeShapes(graph);
			
			
		} finally {
			input.close();
		}
	}
	
	public static void main(String[] args) throws Exception {
		File workbookFile = new File(args[0]);
		File outDir = new File(args[1]);
		WorkbookToTurtleTransformer transformer = new WorkbookToTurtleTransformer();
		transformer.transform(workbookFile, outDir);
	}
}
