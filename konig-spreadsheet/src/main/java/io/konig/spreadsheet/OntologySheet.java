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


import java.util.List;

import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.konig.core.NamespaceManager;
import io.konig.core.vocab.VANN;

public class OntologySheet extends BaseSheetProcessor {
	
	private static final Logger logger = LoggerFactory.getLogger(OntologySheet.class);

	private static SheetColumn ONTOLOGY_NAME = new SheetColumn("Ontology Name");
	private static SheetColumn COMMENT = new SheetColumn("Comment");
	private static SheetColumn NAMESPACE_URI = new SheetColumn("Namespace URI", true);
	private static SheetColumn PREFIX = new SheetColumn("Prefix", true);
	private static SheetColumn IMPORTS = new SheetColumn("Imports");
	private static final SheetColumn PROJECT = new SheetColumn("Project");
	

	private NamespaceManager nsManager;
	
	public OntologySheet(WorkbookProcessor processor, NamespaceManager nsManager) {
		super(processor);
		this.nsManager = nsManager;
		
	}

	@Override
	public SheetColumn[] getColumns() {
		
		return new SheetColumn[]{
			ONTOLOGY_NAME,
			COMMENT,
			NAMESPACE_URI,
			PREFIX,
			IMPORTS,
			PROJECT
		};
	}

	@Override
	public void visit(SheetRow row) throws SpreadsheetException {
		
		Literal ontologyName = stringLiteral(row, ONTOLOGY_NAME);
		Literal comment = stringLiteral(row, COMMENT);
		String namespaceURI = stringValue(row, NAMESPACE_URI);
		Literal prefix = stringLiteral(row, PREFIX);
		List<String> importList = stringList(row, IMPORTS);
		
		logger.debug("visit - namespaceURI: {}", namespaceURI);

		if (!namespaceURI.endsWith("/") && !namespaceURI.endsWith("#")) {
			fail(row, NAMESPACE_URI, "Namespace must end with ''/'' or ''#'' but found: {0}", namespaceURI);
		}
		
		nsManager.add(prefix.stringValue(), namespaceURI);
		URI subject = uri(namespaceURI);
		

		edge(subject, RDF.TYPE, OWL.ONTOLOGY);
		edge(subject, VANN.preferredNamespacePrefix, prefix);
		edge(subject, RDFS.LABEL, ontologyName);
		edge(subject, RDFS.COMMENT, comment);
		
		if (!importList.isEmpty()) {
			WorkbookLocation location = location(row, IMPORTS);
			processor.defer(new ImportAction(processor, location, processor.getGraph(), subject, importList));
		}
		
	
	}

}
