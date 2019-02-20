package io.konig.spreadsheet;

import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.vocabulary.RDFS;

public class LabelSheet extends BaseSheetProcessor {
	private static final SheetColumn SUBJECT = new SheetColumn("Subject", true);
	private static final SheetColumn LABEL = new SheetColumn("Label", true);
	private static final SheetColumn LANGUAGE = new SheetColumn("Language", true);
	
	private static final SheetColumn[] COLUMNS = new SheetColumn[]{
		SUBJECT,
		LABEL,
		LANGUAGE
	};
	

	public LabelSheet(WorkbookProcessor processor) {
		super(processor);
	}

	@Override
	public SheetColumn[] getColumns() {
		return COLUMNS;
	}

	@Override
	public void visit(SheetRow row) throws SpreadsheetException {
		URI subjectId = iriValue(row, SUBJECT);
		String label = stringValue(row, LABEL);
		String language = stringValue(row, LANGUAGE);


		Literal labelValue = new LiteralImpl(label, language);

		edge(subjectId, RDFS.LABEL, labelValue);

	}

}
