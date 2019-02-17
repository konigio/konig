package io.konig.spreadsheet;

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
