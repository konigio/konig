package io.konig.spreadsheet;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;

public class TripleSheet extends BaseSheetProcessor {
	
	private static final SheetColumn SUBJECT = new SheetColumn("Subject", true);
	private static final SheetColumn PREDICATE = new SheetColumn("Predicate", true);
	private static final SheetColumn OBJECT = new SheetColumn("Object", true);
	
	private static final SheetColumn[] COLUMNS = new SheetColumn[] {
		SUBJECT,
		PREDICATE,
		OBJECT
	};

	@SuppressWarnings("unchecked")
	public TripleSheet(WorkbookProcessor processor) {
		super(processor);
		dependsOn(OntologySheet.class);
	}

	@Override
	public SheetColumn[] getColumns() {
		return COLUMNS;
	}

	@Override
	public void visit(SheetRow row) throws SpreadsheetException {
		
		Resource subject = resourceValue(row, SUBJECT);
		URI predicate = iriValue(row, PREDICATE);
		String objectText = stringValue(row, OBJECT);
		URI context = processor.getActiveWorkbook().getURI();
		
		processor.defer(
			new BuildTripleAction(
				location(row, null), 
				processor, 
				subject, predicate, objectText, context)
		);
	}

}
