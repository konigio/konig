package io.konig.spreadsheet;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

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


import java.util.List;

import org.openrdf.model.Literal;
import org.openrdf.model.Namespace;
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
import io.konig.core.NamespaceManager;
import io.konig.core.Vertex;
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
	private static final SheetColumn SUBJECT = new SheetColumn("Subject");
	private static final SheetColumn PROJECT = new SheetColumn("Project");
	
	private static final SheetColumn[] COLUMNS = new SheetColumn[]{
		INDIVIDUAL_NAME,
		COMMENT,
		INDIVIDUAL_ID,
		INDIVIDUAL_TYPE,
		CODE,
		STATUS,
		SUBJECT,
		PROJECT
	};
	
	private SettingsSheet settings;
	private IndividualManager individualManager = new IndividualManager();

	@SuppressWarnings("unchecked")
	public IndividualSheet(WorkbookProcessor processor, SettingsSheet settings) {
		super(processor);
		this.settings = settings;
		dependsOn(OntologySheet.class, SettingsSheet.class, PropertySheet.class);
	}
	
	public IndividualManager getIndividualManager() {
		return individualManager;
	}

	@Override
	public SheetColumn[] getColumns() {
		return COLUMNS;
	}

	@Override
	public void visit(SheetRow row) throws SpreadsheetException {
		Literal name = stringLiteral(row, INDIVIDUAL_NAME);
		Literal comment = stringLiteral(row, COMMENT);
		URI individualId = idValue(row, INDIVIDUAL_ID);
		List<URI> typeList = iriList(row, INDIVIDUAL_TYPE);
		Literal codeValue = stringLiteral(row, CODE);
		Literal gcpDatasetId = settings.gcpDatasetId();
		URI termStatus = iriValue(row, STATUS);
		List<URI> subjects = iriList(row, SUBJECT);
		
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

		if (name != null) {
			individualManager.put(name.stringValue(), individualId);
		}
		edge(individualId, Schema.name, name);
		edge(individualId, RDFS.COMMENT, comment);
		edge(individualId, DCTERMS.IDENTIFIER, codeValue);
		termStatus(individualId, termStatus);
		assignSubject(individualId, subjects);
		
		for (SheetColumn c : row.getUndeclaredColumns()) {
			undeclaredColumn(individualId, row, c);
		}

	}

	private URI idValue(SheetRow row, SheetColumn individualId) throws SpreadsheetException {
		String text = stringValue(row, individualId);
		
		if (text.startsWith("http://") || text.startsWith("https://") || text.startsWith("uri:") ) {
			URI uri = new URIImpl(text);
			String localName = uri.getLocalName();
			if (isUnsafe(localName)) {
				fail(row, individualId, "The local name ''{0}'' contains illegal URI path segment characters.", localName);
			}
			return uri;
		}
		
		int colon = text.indexOf(':');
		if (colon <= 0) {
			fail(row, individualId, "The value must be a fully-qualified IRI or a CURIE, but found ''{0}''", text);
		}
		String prefix = text.substring(0, colon);
		
		NamespaceManager nsManager = processor.getGraph().getNamespaceManager();
		Namespace ns = nsManager.findByPrefix(prefix);
		
		if (ns == null) {
			fail(row, individualId, "Namespace with prefix not found ''{0}''", prefix);
		}
		
		
		String localName = text.substring(colon+1);
		
		switch (settings.getIndividualLocalNameEncoding()) {
		case URL_ENCODING :
			try {
				String encoded = URLEncoder.encode(localName, "UTF-8");
				if (!encoded.equals(localName)) {
					encoded = encoded.replace("+", "%20");
					warn(location(row, individualId), 
							"The local name ''{0}'' has been URL Encoded as ''{1}''.  Consider renaming.", localName, encoded);
				}
				
				return new URIImpl(ns.getName() + encoded);
			} catch (UnsupportedEncodingException e) {
				fail(row, individualId, "Failed to url encode: {0}", localName);
			}
			
		case NONE :
			if (isUnsafe(localName)) {
				fail(row, individualId, "The localname ''{0}'' contains illegal path segment characters", localName);
			}
			
			return new URIImpl(ns.getName() + localName);
		}
		
		
		return null;
	}
	
	private boolean isUnsafe(String text) {
		for (int i=0; i<text.length(); ) {
			
			int c = text.codePointAt(i);
			if (isUnsafe(c)) {
				return true;
			}
			i += Character.charCount(c);
		}
		
		return false;
	}

	private boolean isUnsafe(int ch) {
        if (ch < 33 || ch==127)
            return true;
        return " /%$&+,:;=?@<>#%{}~".indexOf(ch) >= 0;
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
