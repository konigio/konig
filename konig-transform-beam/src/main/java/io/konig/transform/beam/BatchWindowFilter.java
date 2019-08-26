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


import com.google.api.client.util.DateTime;
import com.helger.jcodemodel.AbstractJClass;
import com.helger.jcodemodel.AbstractJType;
import com.helger.jcodemodel.IJExpression;
import com.helger.jcodemodel.JBlock;
import com.helger.jcodemodel.JCodeModel;
import com.helger.jcodemodel.JVar;

import io.konig.core.showl.ShowlNodeShape;

public class BatchWindowFilter implements SourceRowFilter {
	
	private ShowlNodeShape targetNode;
	private AbstractJClass optionsClass;

	public BatchWindowFilter(ShowlNodeShape targetNode, AbstractJClass optionsClass) {
		this.targetNode = targetNode;
		this.optionsClass = optionsClass;
	}



	@Override
	public void addFilter(BeamExpressionTransform etran, ShowlNodeShape sourceNode, JVar processContext, JVar row)
			throws BeamTransformGenerationException {
		
		boolean isTargetNode = sourceNode.getId().equals(targetNode.getId());
		
		BlockInfo blockInfo = etran.peekBlockInfo();
		JCodeModel model = etran.codeModel();
		
		AbstractJClass dateTimeClass = model.ref(DateTime.class);
		AbstractJType longType = model._ref(long.class);
		AbstractJClass stringClass = model.ref(String.class);
		
		
		JBlock block = blockInfo.getBlock();
		
		JVar options = block.decl(optionsClass, blockInfo.varName("options")).init(
				processContext.invoke("getPipelineOptions").invoke("as").arg(optionsClass.staticRef("class")));
		
		JVar batchBegin = null;
		if (!isTargetNode) {
			batchBegin = block.decl(longType, blockInfo.varName("batchBegin")).init(
					options.invoke("getBatchBeginUnixTime"));
		}
		
		JVar batchEnd = block.decl(longType, blockInfo.varName("batchEnd")).init(
				options.invoke("getModifiedUnixTime"));

		JVar modified = block.decl(longType, blockInfo.varName("modified")).init(
				dateTimeClass._new().arg(row.invoke("get").arg("modified").castTo(stringClass)).invoke("getValue").div(1000));
		
		IJExpression condition = modified.gt(batchEnd);
		
		if (!isTargetNode) {
			condition = condition.cor(modified.lte(batchBegin));
		}
		
		block._if(condition)._then()._return();

	}

}
