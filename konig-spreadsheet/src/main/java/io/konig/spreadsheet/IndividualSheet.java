package io.konig.spreadsheet;

import java.util.List;

import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.DCTERMS;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.model.vocabulary.XMLSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.konig.core.Graph;
import io.konig.core.Vertex;
import io.konig.core.impl.RdfUtil;
import io.konig.core.vocab.GCP;
import io.konig.core.vocab.Schema;

public class IndividualSheet extends BaseSheetProcessor {
	private static final Logger logger = LoggerFactory.getLogger(IndividualSheet.class);
	private static final URI NO_VALUE = new URIImpl("urn:NO_VALUE");
	private static final SheetColumn INDIVIDUAL_NAME = new SheetColumn("Individual Name");
	private static final SheetColumn COMMENT = new SheetColumn("Comment");
	private static final SheetColumn INDIVIDUAL_ID = new SheetColumn("Individual Id", true);
	private static final SheetColumn INDIVIDUAL_TYPE = new SheetColumn("Individual Type", true);
	private static final SheetColumn CODE = new SheetColumn("Code");
	private static final SheetColumn STATUS = new SheetColumn("Status");
	
	private static final SheetColumn[] COLUMNS = new SheetColumn[]{
		INDIVIDUAL_NAME,
		COMMENT,
		INDIVIDUAL_ID,
		INDIVIDUAL_TYPE,
		CODE,
		STATUS
	};
	
	private SettingsSheet settings;

	@SuppressWarnings("unchecked")
	public IndividualSheet(WorkbookProcessor processor, SettingsSheet settings) {
		super(processor);
		this.settings = settings;
		dependsOn(OntologySheet.class);
		dependsOn(SettingsSheet.class);
		dependsOn(PropertySheet.class);
	}

	@Override
	public SheetColumn[] getColumns() {
		return COLUMNS;
	}

	@Override
	public void visit(SheetRow row) throws SpreadsheetException {
		Literal name = stringLiteral(row, INDIVIDUAL_NAME);
		Literal comment = stringLiteral(row, COMMENT);
		URI individualId = iriValue(row, INDIVIDUAL_ID);
		List<URI> typeList = iriList(row, INDIVIDUAL_TYPE);
		Literal codeValue = stringLiteral(row, CODE);
		Literal gcpDatasetId = settings.gcpDatasetId();
		URI termStatus = iriValue(row, STATUS);
		
		if (logger.isDebugEnabled()) {
			logger.debug("visit({})", compactName(individualId));
		}
		
		Graph graph = processor.getGraph();
		Vertex prior = graph.getVertex(individualId);
		if (prior != null) {
			
			warn(location(row, INDIVIDUAL_ID), "Duplicate definition of named individual: {0}", individualId.stringValue());
		}
		if (!typeList.isEmpty()) {
			for (URI value : typeList) {
				if (!value.equals(Schema.Enumeration)) {
					graph.edge(individualId, RDF.TYPE, value);
					graph.edge(value, RDF.TYPE, OWL.CLASS);
					graph.edge(value, RDFS.SUBCLASSOF, Schema.Enumeration);
				}
				edge(value, GCP.preferredGcpDatasetId, gcpDatasetId);
			}
		} else {
			graph.edge(individualId, RDF.TYPE, Schema.Enumeration);
		}
		if (name == null && settings.useDefaultName()) {
			name = new LiteralImpl(individualId.getLocalName());
		}

		edge(individualId, Schema.name, name);
		edge(individualId, RDFS.COMMENT, comment);
		edge(individualId, DCTERMS.IDENTIFIER, codeValue);
		termStatus(individualId, termStatus);
		
		for (SheetColumn c : row.getUndeclaredColumns()) {
			undeclaredColumn(individualId, row, c);
		}

	}

	private void undeclaredColumn(URI subject, SheetRow row, SheetColumn col) throws SpreadsheetException {

		String stringValue = stringValue(row, col);
		if (stringValue != null) {
		
			if (valueTypeIsDefined(row, col)) {
			
				URI predicate = col.getIri();
				Value value = null;
				URI datatype = col.getDatatype();
				if (datatype != null ) {
					
					if(XMLSchema.BOOLEAN.equals(datatype)) {
						stringValue = stringValue.toLowerCase();
					}
					value = new LiteralImpl(stringValue, datatype);
					edge(subject, predicate, value);
				} else {
					value = iriValue(row, col);
					edge(subject, predicate, value);
				}
				
			} else {
				URI predicate = col.getIri();
				if (predicate != null) {
					if (stringValue != null) {
						processor.defer(new AddEdgeAction(
								processor, 
								location(row, col), 
								subject, predicate, stringValue));
					}
				}
			}
		}
		
		
		
	}

	private boolean valueTypeIsDefined(SheetRow row, SheetColumn col) {
		return predicate(row, col) != null && col.getValueType()!=null;
	}

	private URI predicate(SheetRow row, SheetColumn c) {
		URI predicate = c.getIri();
		if (predicate == null) {
			SheetRow header = row.getSheet().getHeaderRow();
			try {
				predicate = iriValue(header, c);
				c.setIri(predicate);
				WorkbookUtil.assignValueType(processor.getOwlReasoner(), predicate, c);
			} catch (Throwable oops) {
				predicate = NO_VALUE;
			}
		}
		
		return NO_VALUE.equals(predicate) ? null : predicate;
	}

	
	

	

}
