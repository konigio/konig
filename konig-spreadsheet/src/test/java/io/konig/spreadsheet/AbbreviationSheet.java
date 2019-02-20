package io.konig.spreadsheet;

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
