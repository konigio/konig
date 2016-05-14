package io.konig.schemagen.jsonschema.impl;

import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.XMLSchema;

import io.konig.core.DatatypeRestriction;
import io.konig.core.OwlReasoner;
import io.konig.schemagen.jsonschema.JsonSchemaDatatype;
import io.konig.schemagen.jsonschema.JsonSchemaTypeMapper;
import io.konig.shacl.PropertyConstraint;

public class SmartJsonSchemaTypeMapper implements JsonSchemaTypeMapper {
	
	private OwlReasoner reasoner;
	private SimpleJsonSchemaTypeMapper simple = new SimpleJsonSchemaTypeMapper();
	
	

	public SmartJsonSchemaTypeMapper(OwlReasoner reasoner) {
		this.reasoner = reasoner;
	}


	@Override
	public JsonSchemaDatatype type(PropertyConstraint property) {
		
		URI datatype = property.getDatatype();
		
		if (datatype != null) {
			if (XMLSchema.NAMESPACE.equals(datatype.getNamespace())) {
				return simple.type(property);
			
			} else {
				DatatypeRestriction r = reasoner.datatypeRestriction(datatype);
				URI onDatatype = r.getOnDatatype();
				
				if (onDatatype != null) {
					if (XMLSchema.NAMESPACE.equals(onDatatype.getNamespace())) {
						PropertyConstraint p = new PropertyConstraint(property.getPredicate());
						p.setDatatype(onDatatype);
						JsonSchemaDatatype j = simple.type(p);
						if (j != null) {
							String typeName = j.getTypeName();
							String format = j.getFormat();
							Boolean exclusiveMinimum = null;
							Boolean exclusiveMaximum = null;
							Number maximum = r.getMaxExclusive();
							Number minimum = r.getMinExclusive();
							if (maximum == null) {
								maximum = r.getMaxInclusive();
							} else {
								exclusiveMaximum = true;
							}
							if (minimum == null) {
								minimum = r.getMinInclusive();
							} else {
								exclusiveMinimum = true;
							}
							
							return new JsonSchemaDatatype(typeName, format, exclusiveMinimum, exclusiveMaximum, minimum, maximum);
						}
					}
				}
				
			}
		}
		
		return null;
	}

}
