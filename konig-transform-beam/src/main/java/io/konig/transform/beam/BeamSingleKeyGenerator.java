package io.konig.transform.beam;

import com.helger.jcodemodel.AbstractJClass;
import com.helger.jcodemodel.JBlock;
import com.helger.jcodemodel.JCodeModel;
import com.helger.jcodemodel.JVar;

import io.konig.core.showl.ShowlStructExpression;
import io.konig.core.showl.ShowlUniqueKey;

public class BeamSingleKeyGenerator extends BeamUniqueKeyGenerator {

	public BeamSingleKeyGenerator(BeamExpressionTransform etran, ShowlUniqueKey uniqueKey) {
		super(etran, uniqueKey);
	}

	@Override
	public JVar createKeyVar(ShowlStructExpression struct) throws BeamTransformGenerationException {

		createValues(struct);
		
		JCodeModel model = etran.codeModel();
		BlockInfo blockInfo = etran.peekBlockInfo();
		AbstractJClass objectClass = model.ref(Object.class);
		JBlock block = blockInfo.getBlock();
		
		return block.decl(objectClass, blockInfo.varName("key"));
	}

}
