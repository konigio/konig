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


import java.util.List;

import org.openrdf.model.URI;

import com.helger.jcodemodel.AbstractJClass;
import com.helger.jcodemodel.AbstractJType;
import com.helger.jcodemodel.IJExpression;
import com.helger.jcodemodel.JExpr;
import com.helger.jcodemodel.JInvocation;
import com.helger.jcodemodel.JVar;

import io.konig.core.showl.ShowlNodeShape;
import io.konig.core.showl.ShowlPropertyShape;
import io.konig.core.showl.ShowlUtil;
import io.konig.core.util.StringUtil;
import io.konig.core.vocab.Konig;

public class EnumPropertyGenerator extends SimplePropertyGenerator {

	public EnumPropertyGenerator(BeamExpressionTransform etran) {
		super(etran);
	}


	@Override
	protected JVar declareVariable(ShowlPropertyShape targetProperty) throws BeamTransformGenerationException {

		JVar enumMember = enumMember(targetProperty);
		if (enumMember == null) {
			fail("enumMember not found for {0}", targetProperty.getPath());
		}
		URI predicate = targetProperty.getPredicate();
		
		
		String getter = "get" + StringUtil.capitalize(predicate.getLocalName());
		
		AbstractJClass fieldType = null;
		JInvocation invoke = enumMember.invoke(getter);
		if (Konig.id.equals(predicate)) {
			fieldType = etran.codeModel().ref(String.class);
			invoke = invoke.invoke("getLocalName");
		}
		
		IJExpression fieldValue = JExpr.cond(enumMember.neNull(), invoke, JExpr._null());
		
		return etran.declarePropertyValue(targetProperty, fieldValue, fieldType);
	}
	private JVar enumMember(ShowlPropertyShape targetProperty) throws BeamTransformGenerationException {

		ShowlNodeShape enumNode = ShowlUtil.containingEnumNode(targetProperty, etran.getOwlReasoner());
		ShowlNodeShape enumClassNode = ShowlUtil.enumClassNode(enumNode);
		BlockInfo blockInfo = etran.peekBlockInfo();
		
		return blockInfo.getEnumMember(enumClassNode.effectiveNode());
	}


	@Override
	protected void addParameters(BeamMethod beamMethod, ShowlPropertyShape targetProperty)
			throws BeamTransformGenerationException {
		

		etran.addOutputRowAndErrorBuilderParams(beamMethod, targetProperty);
		etran.addTableRowParameters(beamMethod, targetProperty);
		ShowlNodeShape enumNode = ShowlUtil.containingEnumNode(targetProperty, etran.getOwlReasoner());
		ShowlNodeShape enumClassNode = ShowlUtil.enumClassNode(enumNode);

		if (enumClassNode == null) {
			throw new BeamTransformGenerationException("Enum class node not defined for " + targetProperty.getPath());
		}
		
		AbstractJType javaType = etran.getTypeManager().enumClass(enumNode.getOwlClass().getId());
		
		String enumName = enumNode.getAccessor().getPredicate().getLocalName();
		
		
		BeamParameter beamParam = BeamParameter.ofEnumValue(javaType, enumName, enumClassNode.effectiveNode());
		if (beamMethod.addParameter(beamParam) != null) {
			JVar enumVar = beamParam.getVar();
			BlockInfo blockInfo = etran.peekBlockInfo();
	
			
			// TODO: consider a redesign so that we don't have to map both enumNode and enumClassNode
			//       to enumVar.
			
			blockInfo.putEnumMember(enumNode.effectiveNode(), enumVar);
			blockInfo.putEnumMember(enumClassNode.effectiveNode(), enumVar);
	
			
			List<BeamParameter> paramList = beamMethod.getParameters();
			BeamParameter outputRowParam = paramList.get(paramList.size()-2);
			
				
			blockInfo.putTableRow(enumClassNode.effectiveNode(), outputRowParam.getVar());
		}

	}

}
