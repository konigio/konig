package io.konig.schemagen.gcp;

import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.XMLSchema;

import io.konig.core.OwlReasoner;
import io.konig.shacl.NodeKind;
import io.konig.shacl.PropertyConstraint;

public class SpannerDatatypeMapper {
	
	private OwlReasoner owlReasoner = new OwlReasoner(null);

	public SpannerDatatype type(PropertyConstraint c) {
		
		URI datatype = c.getDatatype();
		
		if (datatype != null) {

			if (owlReasoner.isRealNumber(datatype)) {
				return SpannerDatatype.FLOAT64;
			}
			
			if (owlReasoner.isIntegerDatatype(datatype)) {
				return SpannerDatatype.INT64;
			}
			
			if (XMLSchema.STRING.equals(datatype)) {
				return SpannerDatatype.STRING;
			}
			
			if (
				XMLSchema.DATETIME.equals(datatype) ||
				XMLSchema.DATE.equals(datatype) 
			) {
				return SpannerDatatype.TIMESTAMP;
			}
		}
		
		if (c.getNodeKind() == NodeKind.IRI) {
			return SpannerDatatype.STRING;
		}
		if (c.getShapeId() != null) {
			return SpannerDatatype.ARRAY;
		}
		
		return SpannerDatatype.STRING;
	}
}
