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
import org.openrdf.model.Value;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.SKOS;

import io.konig.core.vocab.Konig;

public class AbbreviationSheet extends BaseSheetProcessor {

	private static final SheetColumn TERM = new SheetColumn("Term", true);
	private static final SheetColumn ABBREVIATION = new SheetColumn("Abbreviation", true);
	
	private static final SheetColumn[] COLUMNS = new SheetColumn[]{
		TERM, 
		ABBREVIATION
	};

	private SettingsSheet settings;
	private URI schemeId;
	
	@SuppressWarnings("unchecked")
	public AbbreviationSheet(WorkbookProcessor processor, SettingsSheet settings) {
		super(processor);
		this.settings = settings;
		dependsOn(OntologySheet.class, SettingsSheet.class);
	}

	@Override
	public SheetColumn[] getColumns() {
		return COLUMNS;
	}

	@Override
	public void visit(SheetRow row) throws SpreadsheetException {
		
		String baseIRI = settings.getAbbreviationSchemeIri();
		if (baseIRI == null) {
			fail(row, null, "The {0} setting must be defined.", SettingsSheet.ABBREVIATION_SCHEME_IRI);
		}
		
		String termText = stringValue(row, TERM);
		Literal abbreviation = stringLiteral(row, ABBREVIATION);
		Literal termLiteral = new LiteralImpl(termText);
		
		URI conceptId = new URIImpl(baseIRI + termText);
		edge(conceptId, RDF.TYPE, SKOS.CONCEPT);
		edge(conceptId, SKOS.PREF_LABEL, termLiteral);
		edge(conceptId, Konig.abbreviationLabel, abbreviation);
		edge(conceptId, SKOS.IN_SCHEME, schemeId(baseIRI));
	}

	private Value schemeId(String baseIRI) {
		if (schemeId == null || !schemeId.stringValue().equals(baseIRI)) {
			schemeId = new URIImpl(baseIRI);
		}
		return schemeId;
	}

}
