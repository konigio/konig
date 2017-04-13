package io.konig.core.util;

import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.XMLSchema;

import io.konig.core.DatatypeRestriction;
import io.konig.core.OwlReasoner;

public class SmartJavaDatatypeMapper extends BasicJavaDatatypeMapper {
	
	private OwlReasoner reasoner;

	public SmartJavaDatatypeMapper(OwlReasoner reasoner) {
		this.reasoner = reasoner;
	}

	@Override
	public Class<?> javaDatatype(URI datatype) {
		if (XMLSchema.NAMESPACE.equals(datatype.getNamespace())) {
			return super.javaDatatype(datatype);
		} else {
			DatatypeRestriction r = reasoner.datatypeRestriction(datatype);
			if (r != null) {
				URI type = r.getOnDatatype();
				if (type != null && XMLSchema.NAMESPACE.equals(type.getNamespace())) {
					return super.javaDatatype(type);
				}
			}
		}
		throw new RuntimeException("Java datatype not found: " + datatype);
	}

	@Override
	public Class<?> primitive(Class<?> javaType) {
		return super.primitive(javaType);
	}

}
