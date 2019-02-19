package io.konig.spreadsheet;

import java.util.Set;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.BNodeImpl;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.model.vocabulary.XMLSchema;

import io.konig.core.Graph;
import io.konig.core.OwlReasoner;
import io.konig.core.impl.RdfUtil;
import io.konig.core.showl.ShowlClass;
import io.konig.core.showl.ShowlManager;
import io.konig.core.showl.ShowlProperty;

public class BuildTripleAction implements Action {
	
	private WorkbookLocation location;
	private WorkbookProcessor processor;
	private Resource subject;
	private URI predicate;
	private String objectValue;
	private URI context;

	public BuildTripleAction(WorkbookLocation location, WorkbookProcessor processor, Resource subject, URI predicate,
			String objectValue, URI context) {
		this.location = location;
		this.processor = processor;
		this.subject = subject;
		this.predicate = predicate;
		this.objectValue = objectValue;
		this.context = context;
	}

	@Override
	public void execute() throws SpreadsheetException {
		
		Value object = object();
		
		Graph graph = processor.getGraph();
		graph.edge(subject, predicate, object, context);
		
	}

	private Value object() throws SpreadsheetException {
		
		
	
		URI range = range();
		if (range == null) {
			String propertyName = RdfUtil.compactName(processor.getGraph().getNamespaceManager(), predicate);
			processor.fail(location, "Range is not known for property: {0}", propertyName);
		}
		
		if (XMLSchema.STRING.equals(range)) {
			return new LiteralImpl(objectValue);
		}
		
		OwlReasoner reasoner = processor.getOwlReasoner();
		
		if (reasoner.isDatatype(range)) {
			return new LiteralImpl(objectValue, range);
		}
		
		if (objectValue.startsWith("_:")) {
			String bnodeId = objectValue.substring(2);
			return new BNodeImpl(bnodeId);
		}
		
		
		return processor.expandCurie(objectValue, location);
	}

	private URI range() throws SpreadsheetException {
		

		String predicateNamespace = predicate.getNamespace();
		if (
			RDF.NAMESPACE.equals(predicateNamespace) ||
			RDFS.NAMESPACE.equals(predicateNamespace) ||
			OWL.NAMESPACE.equals(predicateNamespace)
		) {
			return OWL.THING;
		}

		URI range = null;
		ShowlManagerFactory factory = processor.getServiceManager().service(ShowlManagerFactory.class);
		
		ShowlManager showl = factory.createInstance();
		ShowlProperty property = showl.getProperty(predicate);
		
		if (property == null) {
			String propertyName = RdfUtil.compactName(processor.getGraph().getNamespaceManager(), predicate);
			processor.fail(location, "Property not found: {0}", propertyName);
		}
		
		ShowlClass owlClass = property.getRange();
		if (owlClass != null) {
			range = owlClass.getId();
		} else {
			
			
			Set<URI> options = property.rangeIncludes(processor.getOwlReasoner());
			if (options.size()==1) {
				range = options.iterator().next();
			} else if (options.size()>1) {
				
				OwlReasoner reasoner = processor.getOwlReasoner();
				
				boolean isDatatype = false;
				for (URI term : options) {
					if (reasoner.isDatatype(term)) {
						isDatatype = true;
						break;
					}
				}
				
				if (!isDatatype) {
					return OWL.THING;
				}
				StringBuilder text = new StringBuilder();
				text.append("Range is ambiguous for property ");
				text.append(RdfUtil.compactName(processor.getGraph().getNamespaceManager(), predicate));
				text.append(".  Possible values include ");
				String comma = "";
				for (URI term : options) {
					text.append(comma);
					comma = ", ";
					String name = RdfUtil.compactName(processor.getGraph().getNamespaceManager(), term);
					text.append(name);
				}
				processor.fail(location, text.toString());
			}
		}
		return range;
	}


}
