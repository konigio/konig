package io.konig.schemagen.gcp;

import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.XMLSchema;

import io.konig.core.OwlReasoner;
import io.konig.shacl.NodeKind;
import io.konig.shacl.PropertyConstraint;

public class BigQueryDatatypeMapper {
	
	private OwlReasoner owlReasoner = new OwlReasoner(null);

	public BigQueryDatatype type(PropertyConstraint c) {
		
		URI datatype = c.getDatatype();
		
		if (datatype != null) {

			if (owlReasoner.isRealNumber(datatype)) {
				return BigQueryDatatype.FLOAT;
			}
			
			if (owlReasoner.isIntegerDatatype(datatype)) {
				return BigQueryDatatype.INTEGER;
			}
			
			if (XMLSchema.STRING.equals(datatype)) {
				return BigQueryDatatype.STRING;
			}
			
			if (
				XMLSchema.DATETIME.equals(datatype) ||
				XMLSchema.DATE.equals(datatype) 
			) {
				return BigQueryDatatype.TIMESTAMP;
			}
		}
		
		if (c.getNodeKind() == NodeKind.IRI) {
			return BigQueryDatatype.STRING;
		}
		if (c.getValueShapeId() != null) {
			return BigQueryDatatype.RECORD;
		}
		
		return BigQueryDatatype.STRING;
	}
}
