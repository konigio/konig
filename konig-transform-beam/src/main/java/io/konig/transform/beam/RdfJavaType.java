package io.konig.transform.beam;

import org.openrdf.model.URI;

import com.helger.jcodemodel.AbstractJType;

public class RdfJavaType {
	
	private URI rdfType;
	private AbstractJType javaType;
	
	public RdfJavaType(URI rdfType, AbstractJType javaType) {
		this.rdfType = rdfType;
		this.javaType = javaType;
	}

	public URI getRdfType() {
		return rdfType;
	}

	public AbstractJType getJavaType() {
		return javaType;
	}
	
	public boolean isSimpleType() {
		return javaType.fullName().startsWith("java.");
	}

}
