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


import java.util.Collection;
import java.util.List;

import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.model.vocabulary.SKOS;

import io.konig.core.impl.SimpleLocalNameService;


public class ClassSheet extends BaseSheetProcessor {
	private static final SheetColumn CLASS_NAME = new SheetColumn("Class Name");
	private static final SheetColumn COMMENT = new SheetColumn("Comment");
	private static final SheetColumn CLASS_ID = new SheetColumn("Class Id", true);
	private static final SheetColumn SUBCLASS_OF = new SheetColumn("Subclass Of");
	private static final SheetColumn IRI_TEMPLATE = new SheetColumn("IRI Template");
	private static final SheetColumn SUBJECT = new SheetColumn("Subject");
	private static final SheetColumn STATUS = new SheetColumn("Status");
	
	private static final SheetColumn[] COLUMNS = new SheetColumn[] {
		CLASS_NAME,
		COMMENT,
		CLASS_ID,
		SUBCLASS_OF,
		IRI_TEMPLATE,
		SUBJECT,
		STATUS
	};
	
	private SettingsSheet settings;

	@SuppressWarnings("unchecked")
	public ClassSheet(WorkbookProcessor processor, SettingsSheet settings) {
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
		Literal className = stringLiteral(row, CLASS_NAME);
		Literal comment = stringLiteral(row, COMMENT);
		URI classId = iriValue(row, CLASS_ID);
		List<URI> subclassOf = iriList(row, SUBCLASS_OF);
		String iriTemplate = stringValue(row, IRI_TEMPLATE);
		Collection<URI> subjectArea = iriList(row, SUBJECT);
		URI termStatus = iriValue(row, STATUS);
		
		if (subjectArea.isEmpty()) {
			subjectArea = settings.getDefaultSubject();
		}

		edge(classId, RDF.TYPE, OWL.CLASS);
		if (className != null) {
			edge(classId, RDFS.LABEL, className);
		}
		if (comment != null) {
			edge(classId, RDFS.COMMENT, comment);
		}
		for (URI subclassId : subclassOf) {
			edge(classId, RDFS.SUBCLASSOF, subclassId);
			edge(subclassId, RDF.TYPE, OWL.CLASS);
		}
		
		termStatus(classId, termStatus);

		if (iriTemplate != null) {
			processor.defer(new BuildClassTemplateAction(
				location(row, IRI_TEMPLATE),
				processor,
				processor.getGraph(),
				processor.getShapeManager(),
				processor.getGraph().getNamespaceManager(),
				service(SimpleLocalNameService.class),
				classId,
				iriTemplate
			));
		}
		if (!subjectArea.isEmpty()) {
			addNamespace("skos", SKOS.NAMESPACE);
			for (URI subject : subjectArea) {
				edge(classId, SKOS.BROADER, subject);
			}
		}

	}


}
