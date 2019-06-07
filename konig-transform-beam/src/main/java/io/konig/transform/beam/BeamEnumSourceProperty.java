package io.konig.transform.beam;

import org.openrdf.model.URI;

import com.helger.jcodemodel.AbstractJClass;
import com.helger.jcodemodel.JBlock;
import com.helger.jcodemodel.JCodeModel;
import com.helger.jcodemodel.JInvocation;
import com.helger.jcodemodel.JVar;

import io.konig.core.showl.ShowlPropertyShape;
import io.konig.core.util.StringUtil;
import io.konig.core.vocab.Konig;

public class BeamEnumSourceProperty extends BeamSourceProperty {

	public BeamEnumSourceProperty(BeamChannel beamChannel, ShowlPropertyShape propertyShape) {
		super(beamChannel, propertyShape);
	}

	public void generateVar(JCodeModel model, JBlock block) {
		
		URI predicate = getPredicate();
		AbstractJClass objectClass = model.ref(Object.class);
		String sourcePropertyName = predicate.getLocalName();
		
		JVar enumMember = getBeamChannel().getSourceRowParam();
		
		String getterName = "get" + StringUtil.capitalize(sourcePropertyName);
		
		JInvocation initValue = enumMember.invoke(getterName);
		if (Konig.id.equals(predicate)){
			initValue = initValue.invoke("getLocalName");
		}
		
		JVar sourcePropertyVar = block.decl(objectClass, sourcePropertyName).init(initValue);
		
		setVar(sourcePropertyVar);
	}

}
