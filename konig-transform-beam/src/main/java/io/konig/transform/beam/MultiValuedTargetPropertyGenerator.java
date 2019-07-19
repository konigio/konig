package io.konig.transform.beam;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.openrdf.model.URI;

import com.google.api.services.bigquery.model.TableRow;
import com.helger.jcodemodel.AbstractJClass;
import com.helger.jcodemodel.AbstractJType;
import com.helger.jcodemodel.IJExpression;
import com.helger.jcodemodel.JBlock;
import com.helger.jcodemodel.JCodeModel;
import com.helger.jcodemodel.JConditional;
import com.helger.jcodemodel.JDefinedClass;
import com.helger.jcodemodel.JExpr;
import com.helger.jcodemodel.JInvocation;
import com.helger.jcodemodel.JMethod;
import com.helger.jcodemodel.JMod;
import com.helger.jcodemodel.JVar;

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


import io.konig.core.showl.ShowlArrayExpression;
import io.konig.core.showl.ShowlBasicStructExpression;
import io.konig.core.showl.ShowlExpression;
import io.konig.core.showl.ShowlNodeShape;
import io.konig.core.showl.ShowlPropertyShape;

public class MultiValuedTargetPropertyGenerator extends AlternativePathsGenerator {

	public MultiValuedTargetPropertyGenerator(BeamExpressionTransform etran) {
		super(etran);
	}


	@Override
	protected void generateBody(BeamMethod beamMethod, ShowlPropertyShape targetProperty)
			throws BeamTransformGenerationException {
		

		ShowlArrayExpression e =  (ShowlArrayExpression) targetProperty.getSelectedExpression();
	
		// For now, we assume that collection of values contains nested records.
		// In the future, we'll need to support collections of simple values
		
		JCodeModel model = etran.codeModel();
		AbstractJClass tableRowClass = model.ref(TableRow.class);
		AbstractJClass listClass = model.ref(List.class).narrow(tableRowClass);
		AbstractJClass arrayListClass = model.ref(ArrayList.class).narrow(tableRowClass);
		
		JBlock body = beamMethod.getMethod().body();
		
		JVar listVar = body.decl(listClass, "list").init(arrayListClass._new());
		JVar memberVar = body.decl(tableRowClass, "member");
		
		int index = 0;
		for (ShowlExpression member : e.getMemberList()) {
			
			IJExpression memberValue = memberValue(beamMethod, targetProperty, member, index++);
			
			
			IJExpression condition = new ParenExpression(JExpr.assign(memberVar, memberValue)).neNull();
			
			JConditional ifStatement = body._if(condition);
			ifStatement._then().add(listVar.invoke("add").arg(memberVar));
			
		}
		
		JConditional ifStatement = body._if(listVar.invoke("isEmpty"));
		ifStatement._then()._return(JExpr.FALSE);
		JVar parentRow = etran.peekBlockInfo().getTableRowVar(targetProperty.getDeclaringShape().effectiveNode());
		
		ifStatement._else().add(parentRow.invoke("put").arg(JExpr.lit(targetProperty.getPredicate().getLocalName())).arg(listVar));
		ifStatement._else()._return(JExpr.TRUE);
		

	}


	private IJExpression memberValue(BeamMethod callerMethod, ShowlPropertyShape targetProperty, ShowlExpression member, int index) 
			throws BeamTransformGenerationException {
			
		
		String methodName = callerMethod.getMethod().name() + index;
		
		JCodeModel model = etran.codeModel();
		
		JDefinedClass targetClass = etran.getTargetClass();
		AbstractJClass tableRowClass = model.ref(TableRow.class);
		
		JMethod memberMethod = targetClass.method(JMod.PRIVATE, tableRowClass, methodName);
		BeamMethod memberBeamMethod = new BeamMethod(memberMethod);
		if (member instanceof ShowlBasicStructExpression) {
			ShowlBasicStructExpression struct = (ShowlBasicStructExpression) member;
			
			memberBody(memberBeamMethod, struct, targetProperty);
			
		} else {
			fail("Expression type {0} not supported at {1}", member.getClass().getSimpleName(), targetProperty.getPath());
		}
		
		
		return etran.createInvocation(memberBeamMethod);
	}
	

	private void memberBody(BeamMethod beamMethod, ShowlBasicStructExpression struct, ShowlPropertyShape targetProperty) throws BeamTransformGenerationException {

		JCodeModel model = etran.codeModel();
		AbstractJClass tableRowClass = model.ref(TableRow.class);
		BlockInfo blockInfo = etran.beginBlock(beamMethod);
		try {
			JBlock block = blockInfo.getBlock();
			beamMethod.excludeParamFor(BeamParameter.pattern(BeamParameterType.TABLE_ROW, targetProperty.getValueShape().effectiveNode()));
			
			JVar outputRow = block.decl(tableRowClass, "outputRow").init(tableRowClass._new());

			etran.addRowParameters(beamMethod, struct);
			
			
	
			JVar structVar = block.decl(tableRowClass, targetProperty.getPredicate().getLocalName()).init(tableRowClass._new());
			blockInfo.putTableRow(targetProperty.getValueShape().effectiveNode(), structVar);
			
			JCodeModel codeModel = etran.codeModel();
			AbstractJType booleanType = codeModel._ref(boolean.class);
			
			ShowlNodeShape node = targetProperty.getValueShape();

			JVar ok = block.decl(booleanType, "ok");
			if (struct.size()==1) {
				Entry<URI, ShowlExpression> entry = struct.entrySet().iterator().next();

				URI predicate = entry.getKey();
				ShowlExpression e = entry.getValue();
				
				ShowlPropertyShape p = node.getProperty(predicate);
				
				
				BeamMethod fieldMethod = fieldMethod(beamMethod, p, e);
				
				JInvocation invoke = etran.createInvocation(fieldMethod);
				ok.init(invoke);
				
				
			} else {
			
				ok.init(JExpr.TRUE);
				
				for (Entry<URI, ShowlExpression> entry : struct.entrySet()) {
					URI predicate = entry.getKey();
					ShowlExpression e = entry.getValue();
					
					ShowlPropertyShape p = node.getProperty(predicate);
					
					
					BeamMethod fieldMethod = fieldMethod(beamMethod, p, e);
					
					JInvocation invoke = etran.createInvocation(fieldMethod);
					
					block.assign(ok, ok.cand(invoke));
				}
				
			}

			JConditional ifStatement = block._if(ok);
			
			ifStatement._then()._return(outputRow);
			block._return(JExpr._null());
			
		} finally {
			etran.endBlock();
		}
		
	}

}
