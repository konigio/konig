package io.konig.transform.beam;

/*
 * #%L
 * Konig Transform Beam
 * %%
 * Copyright (C) 2015 - 2019 Gregory McFall
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


import org.openrdf.model.URI;

import com.helger.jcodemodel.AbstractJClass;
import com.helger.jcodemodel.AbstractJType;
import com.helger.jcodemodel.IJExpression;

import io.konig.core.OwlReasoner;
import io.konig.core.showl.ShowlExpression;
import io.konig.core.showl.ShowlPropertyShape;

public interface BeamTypeManager {

	RdfJavaType rdfJavaType(ShowlPropertyShape p) throws BeamTransformGenerationException;
	
	/**
	 * Compute the Java class used to represent values of a given RDF type.
	 * @param rdfType The RDF type whose corresponding Java type is requested.
	 * @return The Java class that represents values of the given RDF type. 
	 * If rdfType is a datatype, then return a simple Java class such as String, Boolean, Long, etc.
	 * If rdfType is a subclass of schema:Enumeration, return the corresponding Java enum.
	 * Otherwise, return TableRow (for arbitrary Resource values).
	 * @throws BeamTransformGenerationException
	 */
	AbstractJType javaType(URI rdfType) throws BeamTransformGenerationException;
	AbstractJClass errorBuilderClass() throws BeamTransformGenerationException;
	AbstractJClass enumClass(URI owlClass) throws BeamTransformGenerationException;
	AbstractJClass mainClass(URI shapeId) throws BeamTransformGenerationException;
	AbstractJClass pipelineOptionsClass(URI shapeId) throws BeamTransformGenerationException;
	URI enumClassOfIndividual(URI iri) throws BeamTransformGenerationException;
	AbstractJType javaType(ShowlExpression e) throws BeamTransformGenerationException;
	
}
