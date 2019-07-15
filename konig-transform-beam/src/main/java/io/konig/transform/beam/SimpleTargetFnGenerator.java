package io.konig.transform.beam;

import com.helger.jcodemodel.JCodeModel;

import io.konig.core.NamespaceManager;
import io.konig.core.OwlReasoner;

public class SimpleTargetFnGenerator extends BaseTargetFnGenerator {

	public SimpleTargetFnGenerator(String basePackage, NamespaceManager nsManager, JCodeModel model, OwlReasoner reasoner,
			BeamTypeManager typeManager) {
		super(basePackage, nsManager, model, reasoner, typeManager);
	}
	
}
