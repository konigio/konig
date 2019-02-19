package io.konig.spreadsheet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openrdf.model.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.konig.core.LocalNameService;
import io.konig.core.impl.CompositeLocalNameService;
import io.konig.core.impl.RdfUtil;
import io.konig.core.impl.SimpleLocalNameService;
import io.konig.core.path.NamespaceMapAdapter;
import io.konig.formula.FormulaParser;
import io.konig.rio.turtle.NamespaceMap;
import io.konig.shacl.Shape;
import io.konig.spreadsheet.nextgen.Workbook;

public class FormulaParserFactory  {
	
	private static final Logger logger = LoggerFactory.getLogger(FormulaParserFactory.class);
	
	private WorkbookProcessor processor;
	private Map<URI,FormulaParser> parserMap=new HashMap<>();
	private NamespaceMapAdapter nsMap = null;

	public FormulaParserFactory(WorkbookProcessor processor) {
		this.processor = processor;
	}

	
	public FormulaParser forShape(URI shapeId) {
	
		FormulaParser parser = parserMap.get(shapeId);
		if (parser == null) {
			logger.debug("forShape({})", shapeId.getLocalName());
			LocalNameService nameService = nameService(shapeId);
			NamespaceMap nsMap = namespaceMap();
			parser = new FormulaParser(null, nameService, nsMap);
			parserMap.put(shapeId, parser);
		}
		return parser;
	}

	private NamespaceMap namespaceMap() {
		if (nsMap == null) {
			nsMap = new NamespaceMapAdapter(processor.getGraph().getNamespaceManager());
		}
		return nsMap;
	}

	private LocalNameService nameService(URI shapeId) {
		
		SimpleLocalNameService global = processor.getServiceManager()
			.service(SimpleLocalNameService.class);
		
		Shape shape = processor.getShapeManager().getShapeById(shapeId);
		
		SimpleLocalNameService shapeNames = new SimpleLocalNameService();
		shapeNames.addShape(shape);
		
		return new CompositeLocalNameService(shapeNames, global);
	}

	
	

}
