package io.konig.transform.beam;

import java.text.MessageFormat;

import org.openrdf.model.Namespace;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.XMLSchema;

import com.helger.jcodemodel.AbstractJClass;
import com.helger.jcodemodel.AbstractJType;
import com.helger.jcodemodel.JCodeModel;

import io.konig.core.NamespaceManager;
import io.konig.core.OwlReasoner;

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
}
