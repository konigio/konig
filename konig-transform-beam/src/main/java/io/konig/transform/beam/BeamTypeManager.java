package io.konig.transform.beam;

import org.openrdf.model.URI;

import com.helger.jcodemodel.AbstractJClass;
import com.helger.jcodemodel.AbstractJType;

public interface BeamTypeManager {

	AbstractJType javaType(URI rdfType) throws BeamTransformGenerationException;
	AbstractJClass errorBuilderClass() throws BeamTransformGenerationException;
	
}
