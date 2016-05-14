package io.konig.schemagen.avro.impl;

import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.XMLSchema;

import io.konig.core.DatatypeRestriction;
import io.konig.core.OwlReasoner;
import io.konig.schemagen.avro.AvroDatatype;
import io.konig.schemagen.avro.AvroDatatypeMapper;

public class SmartAvroDatatypeMapper implements AvroDatatypeMapper {
	
	private OwlReasoner reasoner;
	private SimpleAvroDatatypeMapper simple = new SimpleAvroDatatypeMapper();

	public SmartAvroDatatypeMapper(OwlReasoner reasoner) {
		this.reasoner = reasoner;
	}

	@Override
	public AvroDatatype toAvroDatatype(URI rdfDatatype) {
		
		if (rdfDatatype.getNamespace().equals(XMLSchema.NAMESPACE)) {
			return simple.toAvroDatatype(rdfDatatype);
		}
		
		DatatypeRestriction r = reasoner.datatypeRestriction(rdfDatatype);
		
		URI datatype = r.getOnDatatype();
		if (datatype != null && XMLSchema.NAMESPACE.equals(datatype.getNamespace())) {
			return simple.toAvroDatatype(datatype);
		}
		
		return null;
	}

}
