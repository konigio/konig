package io.konig.spreadsheet;

import java.util.StringTokenizer;

import org.openrdf.model.Literal;

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
import org.openrdf.model.impl.BNodeImpl;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.vocabulary.OWL;

import io.konig.core.Graph;
import io.konig.core.NamespaceManager;
import io.konig.core.OwlReasoner;
import io.konig.core.impl.RdfUtil;

public class AddEdgeAction implements Action {
	
	private WorkbookProcessor processor;
	private WorkbookLocation location;
	private Resource subject;
	private URI predicate;
	private String stringValue;
	
	public AddEdgeAction(WorkbookProcessor processor, WorkbookLocation location, Resource subject,
			URI predicate, String stringValue) {
		this.processor = processor;
		this.location = location;
		this.subject = subject;
		this.predicate = predicate;
		this.stringValue = stringValue.trim();
	}

	@Override
	public void execute() throws SpreadsheetException {
		
		Graph graph = processor.getOwlReasoner().getGraph();
		OwlReasoner reasoner = processor.getOwlReasoner();
		
		SheetColumn column = new SheetColumn(predicate.stringValue());
			
		
		if (!WorkbookUtil.assignValueType(reasoner, predicate, column)) {

			NamespaceManager nsManager = reasoner.getGraph().getNamespaceManager();
			String subjectName = RdfUtil.compactName(nsManager, subject);
			String predicateName = RdfUtil.compactName(reasoner.getGraph().getNamespaceManager(), predicate);
			
			processor.fail(location, "Cannot set value of {0}: Range of {1} must be defined", 
					subjectName, predicateName);
		}
		
		if (column.getDatatype()!=null) {
			Literal value = new LiteralImpl(stringValue, column.getDatatype());
			graph.edge(subject, predicate, value);
		} else {
		
			StringTokenizer tokenizer = new StringTokenizer(stringValue, " \t\r\n");
			while (tokenizer.hasMoreTokens()) {
				String token = tokenizer.nextToken();
				Resource value = token.startsWith("_:") ?
						new BNodeImpl(token.substring(2)) :
						processor.expandCurie(token, location);
				graph.edge(subject, predicate, value);
			}
		}
		
	}

}
