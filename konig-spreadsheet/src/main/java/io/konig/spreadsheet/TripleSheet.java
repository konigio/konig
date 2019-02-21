package io.konig.spreadsheet;

/*
 * #%L
 * Konig Spreadsheet
 * %%
 * Copyright (C) 2015 - 2019 Gregory McFall
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
