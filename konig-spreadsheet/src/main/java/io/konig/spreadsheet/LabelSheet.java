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
