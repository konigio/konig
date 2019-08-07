package io.konig.transform.beam;


import java.util.ArrayList;
import java.util.List;

import com.google.api.services.bigquery.model.TableRow;
import com.helger.jcodemodel.AbstractJClass;
import com.helger.jcodemodel.JBlock;
import com.helger.jcodemodel.JCodeModel;
import com.helger.jcodemodel.JVar;

import io.konig.core.showl.ShowlPropertyShapeGroup;
import io.konig.core.showl.ShowlStructExpression;
import io.konig.core.showl.ShowlUniqueKey;
import io.konig.core.showl.UniqueKeyElement;

public class BeamCompositeKeyGenerator extends BeamUniqueKeyGenerator {

	public BeamCompositeKeyGenerator(BeamExpressionTransform etran, ShowlUniqueKey uniqueKey) {
		super(etran, uniqueKey);
	}

	@Override
	public JVar createKeyVar(ShowlStructExpression e) throws BeamTransformGenerationException {
		createValues(e);
		JCodeModel model = etran.codeModel();
		BlockInfo blockInfo = etran.peekBlockInfo();
		AbstractJClass objectClass = model.ref(Object.class);
		AbstractJClass listClass = model.ref(List.class).narrow(objectClass);
		AbstractJClass arrayListClass = model.ref(ArrayList.class);
		JBlock block = blockInfo.getBlock();
		
		JVar list = block.decl(listClass, blockInfo.varName("key")).init(arrayListClass._new());

		for (UniqueKeyElement element : uniqueKey) {
			ShowlPropertyShapeGroup group = element.getPropertyShape().asGroup();
			JVar elementVar = blockInfo.getPropertyValue(group);
			block.add(list.invoke("add").arg(elementVar));
		}
		return list;
	}

}
