package io.konig.schemagen.bigquery;

import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.XMLSchema;

import io.konig.shacl.NodeKind;
import io.konig.shacl.PropertyConstraint;

public class BigQueryDatatypeMapper {

	public BigQueryDatatype type(PropertyConstraint c) {
		
		URI datatype = c.getDatatype();
		
		if (datatype != null) {
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
