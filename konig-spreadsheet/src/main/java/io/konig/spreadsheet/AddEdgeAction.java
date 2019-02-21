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
import org.openrdf.model.Value;
import org.openrdf.model.impl.BNodeImpl;
import org.openrdf.model.impl.LiteralImpl;

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
		this.stringValue = stringValue;
	}

	@Override
	public void execute() throws SpreadsheetException {
		
		Value value = value();
		processor.getOwlReasoner().getGraph().edge(subject, predicate, value);
	}

	private Value value() throws SpreadsheetException {
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
			return new LiteralImpl(stringValue, column.getDatatype());
		}
		
		if (stringValue.startsWith("_:")) {
			String bnodeId = stringValue.substring(2);
			return new BNodeImpl(bnodeId);
		}
		return processor.expandCurie(stringValue, location);
	}

}
