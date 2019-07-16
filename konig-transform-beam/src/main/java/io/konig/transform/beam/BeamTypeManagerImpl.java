package io.konig.transform.beam;

import java.awt.List;

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

import com.google.api.services.bigquery.model.TableRow;
import com.helger.jcodemodel.AbstractJClass;
import com.helger.jcodemodel.AbstractJType;
import com.helger.jcodemodel.JCodeModel;

import io.konig.core.NamespaceManager;
import io.konig.core.OwlReasoner;
import io.konig.core.Vertex;
import io.konig.core.showl.ShowlExpression;
import io.konig.core.showl.ShowlPropertyShape;
import io.konig.core.vocab.Konig;
import io.konig.shacl.NodeKind;
import io.konig.shacl.PropertyConstraint;

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
		if (XMLSchema.ANYURI.equals(rdfType)) {
			return model.ref(String.class);
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
			return model.ref(Long.class);
		}
		if (XMLSchema.DOUBLE.equals(rdfType)) {
			return model.ref(Double.class);
		}
		if (XMLSchema.DECIMAL.equals(rdfType)) {
			return model.ref(Double.class);
		}
		if (XMLSchema.FLOAT.equals(rdfType)) {
			return model.ref(Float.class);
		}			
		if (reasoner.isEnumerationClass(rdfType)) {
			return model.ref(enumClassName(rdfType));
		}
		
		return model.ref(TableRow.class);
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
		AbstractJClass result = model._getClass(className);
		if (result == null) {
			throw new BeamTransformGenerationException("Class not found: " + className);
		}
		return result;
	}

	@Override
	public URI enumClassOfIndividual(URI individualId) throws BeamTransformGenerationException {
		Vertex v = reasoner.getGraph().getVertex(individualId);
		if (v != null) {
			return reasoner.mostSpecificTypeOf(v);
		}
		throw new BeamTransformGenerationException("Type of " + individualId.stringValue() + " is not known.");
	}

	@Override
	public RdfJavaType rdfJavaType(ShowlPropertyShape p) throws BeamTransformGenerationException {
		

		URI rdfType = p.getValueType(reasoner);
		if (rdfType == null) {
			throw new BeamTransformGenerationException("RDF type not known for " + p.getPath());
		}
		
		if (Konig.id.equals(p.getPredicate())) {
			return new RdfJavaType(rdfType, model.ref(String.class)); 
		}
		
		PropertyConstraint constraint = p.getPropertyConstraint();
		if (constraint != null) {
			if (constraint.getMaxCount() == null) {
				
				// For now, we only support lists of records.
				// In the future, we'll need to support lists of simple values.

				AbstractJClass tableRowClass = model.ref(TableRow.class);
				AbstractJClass listClass = model.ref(List.class).narrow(tableRowClass);
				
				return new RdfJavaType(rdfType, listClass);
			}
			if (constraint.getNodeKind() == NodeKind.IRI  && constraint.getShape()==null) {
				return new RdfJavaType(rdfType, model.ref(String.class));
			}
		} 
		return new RdfJavaType(rdfType, javaType(rdfType));
	}

	@Override
	public AbstractJType javaType(ShowlExpression e) throws BeamTransformGenerationException {
		URI rdfType = e.valueType(reasoner);
		
		return javaType(rdfType);
	}
}
