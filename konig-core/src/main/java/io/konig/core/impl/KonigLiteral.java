package io.konig.core.impl;

import org.openrdf.model.URI;
import org.openrdf.model.impl.LiteralImpl;

public class KonigLiteral extends LiteralImpl {
	private static final long serialVersionUID = 1L;
	
	public KonigLiteral(String value) {
		super(value);
		setDatatype(null);
	}
	
	public KonigLiteral(String value, String language) {
		super(value, language);
		setDatatype(null);
	}
	
	public KonigLiteral(String value, URI datatype) {
		super(value, datatype);
	}
	
	public String toString() {
		URI type = getDatatype();
		if (type != null) {
			return stringValue() + "^^" + type.stringValue();
		}
		String language = getLanguage();
		if (language != null) {
			return stringValue() + "@" + language;
		}
		return stringValue();
	}

}
