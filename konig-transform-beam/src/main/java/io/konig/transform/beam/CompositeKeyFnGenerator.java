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



import java.util.ArrayList;
import java.util.List;

import org.apache.beam.sdk.values.KV;

import com.google.api.services.bigquery.model.TableRow;
import com.helger.jcodemodel.AbstractJClass;
import com.helger.jcodemodel.JBlock;
import com.helger.jcodemodel.JCodeModel;
import com.helger.jcodemodel.JVar;

import io.konig.core.showl.ShowlNodeShape;
import io.konig.core.showl.ShowlPropertyShape;
import io.konig.core.showl.ShowlUniqueKey;
import io.konig.core.showl.UniqueKeyElement;

public class CompositeKeyFnGenerator extends TableRowToKvFnGenerator {

	protected CompositeKeyFnGenerator(String mainPackage, BeamExpressionTransform etran, ShowlNodeShape sourceNode,
			ShowlUniqueKey uniqueKey, SourceRowFilter windowFilter) {
		super(mainPackage, etran, sourceNode, uniqueKey, windowFilter);
	}

	@Override
	protected void computeKeyType() throws BeamTransformGenerationException {

		JCodeModel model = etran.codeModel();

		AbstractJClass objectClass = model.ref(Object.class);
		AbstractJClass listClass = model.ref(List.class).narrow(objectClass);
	
		AbstractJClass tableRowClass = model.ref(TableRow.class);
		
		keyType = listClass;
		kvClass = model.ref(KV.class).narrow(keyType).narrow(tableRowClass);
	}


	@Override
	protected JVar computeKey(JVar row, JVar processContext) throws BeamTransformGenerationException {
		JCodeModel model = etran.codeModel();
		AbstractJClass objectClass = model.ref(Object.class);
		AbstractJClass arrayListClass = model.ref(ArrayList.class).narrow(objectClass);
		
		BlockInfo blockInfo = etran.peekBlockInfo();
		JBlock block = blockInfo.getBlock();
		
		JVar keyVar = block.decl(keyType, blockInfo.varName("key")).init(arrayListClass._new());
		
		List<UniqueKeyElement> elementList = uniqueKey.flatten();
		for (UniqueKeyElement element : elementList) {
			ShowlPropertyShape p = element.getPropertyShape();
			
			JVar var = etran.declareSourcePropertyValue(p);
			block.add(keyVar.invoke("add").arg(var));
		}
		
		return keyVar;
	}



}
