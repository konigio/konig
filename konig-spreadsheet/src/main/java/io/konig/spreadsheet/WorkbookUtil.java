package io.konig.spreadsheet;

import java.util.Set;

import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.vocabulary.RDFS;

import io.konig.core.Graph;
import io.konig.core.OwlReasoner;
import io.konig.core.Vertex;
import io.konig.core.vocab.Schema;

public class WorkbookUtil {

	public static boolean assignValueType(OwlReasoner owlReasoner, URI predicate, SheetColumn c) {
		
		Graph graph = owlReasoner.getGraph();
		Vertex v = graph.getVertex(predicate);
		if (v != null) {
			URI range = v.getURI(RDFS.RANGE);
			if (range == null) {
				Set<Value> set = v.getValueSet(Schema.rangeIncludes);
				if (set.size()==1) {
					Value value = set.iterator().next();
					if (value instanceof URI) {
						range = (URI) value;
					}
				}
			}
			if (range == null) {
				return false;
			}
			if (owlReasoner.isDatatype(range)) {
				c.setDatatype(range);
			} else {
				c.setObjectType(range);
			}

			return true;
		}
		
		return false;
		
	}

}
