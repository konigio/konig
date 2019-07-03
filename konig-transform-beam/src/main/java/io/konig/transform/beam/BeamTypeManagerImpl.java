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


import java.text.MessageFormat;
import java.util.Date;

import org.openrdf.model.Namespace;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.XMLSchema;

import com.helger.jcodemodel.AbstractJClass;
import com.helger.jcodemodel.AbstractJType;
import com.helger.jcodemodel.JCodeModel;

import io.konig.core.NamespaceManager;
import io.konig.core.OwlReasoner;
import io.konig.core.Vertex;

public class BeamTypeManagerImpl implements BeamTypeManager {
	
	private String basePackage;
	private OwlReasoner reasoner;
	private JCodeModel model;
	private NamespaceManager nsManager;

	public BeamTypeManagerImpl(String basePackage, OwlReasoner reasoner, JCodeModel model, NamespaceManager nsManager) {
		this.basePackage = basePackage;
		this.reasoner = reasoner;
		this.model = model;
		this.nsManager = nsManager;
	}

	@Override
	public AbstractJType javaType(URI rdfType) throws BeamTransformGenerationException {
		if (XMLSchema.BOOLEAN.equals(rdfType)) {
			return model.ref(Boolean.class);
		}
		if (XMLSchema.DATETIME.equals(rdfType)) {
			return model.ref(Long.class);
		}
		if (XMLSchema.STRING.equals(rdfType)) {
			return model.ref(String.class);
		}
		if (XMLSchema.INTEGER.equals(rdfType)) {
			return model.ref(Long.class);
		}
		if (XMLSchema.LONG.equals(rdfType)) {
			return model.ref(Long.class);
		}
		if (XMLSchema.INT.equals(rdfType)) {
			return model.ref(Integer.class);
		}
		if (XMLSchema.DATE.equals(rdfType)) {
			return model.ref(Date.class);
		}
		if (XMLSchema.FLOAT.equals(rdfType)) {
			return model.ref(Float.class);
		}			
		if (reasoner.isEnumerationClass(rdfType)) {
			return model.ref(enumClassName(rdfType));
		}
		fail("Java type not supported: {0}", rdfType.stringValue());
		return null;
	}

  private String enumClassName(URI enumClass) throws BeamTransformGenerationException {
    StringBuilder builder = new StringBuilder();
    builder.append(basePackage);
    builder.append('.');
    
    Namespace ns = nsManager.findByName(enumClass.getNamespace());
    if (ns == null) {
      fail("Prefix not found for namespace: {0}", enumClass.getNamespace());
    }
    builder.append(ns.getPrefix());
    builder.append('.');
    builder.append(enumClass.getLocalName());
    
    return builder.toString();
  }

  private BeamTransformGenerationException fail(String pattern, Object...args) throws BeamTransformGenerationException {
    throw new BeamTransformGenerationException(MessageFormat.format(pattern, args));
  }

	@Override
	public AbstractJClass errorBuilderClass() throws BeamTransformGenerationException {
		
		AbstractJClass result = model._getClass(errorBuilderClassName());
		if (result == null) {
			throw new BeamTransformGenerationException("ErrorBuilder class has not been generated");
		}
		return result;
	}

  private String errorBuilderClassName() {
  	return basePackage + ".common.ErrorBuilder";
  }

	@Override
	public AbstractJClass enumClass(URI owlClass) throws BeamTransformGenerationException {
		String className = enumClassName(owlClass);
		return model._getClass(className);
	}

	@Override
	public URI enumClassOfIndividual(URI individualId) throws BeamTransformGenerationException {
		Vertex v = reasoner.getGraph().getVertex(individualId);
		if (v != null) {
			return reasoner.mostSpecificTypeOf(v);
		}
		throw new BeamTransformGenerationException("Type of " + individualId.stringValue() + " is not known.");
	}
}
