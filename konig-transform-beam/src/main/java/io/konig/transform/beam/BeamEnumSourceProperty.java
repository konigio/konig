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


import org.openrdf.model.URI;

import com.helger.jcodemodel.AbstractJClass;
import com.helger.jcodemodel.IJExpression;
import com.helger.jcodemodel.JBlock;
import com.helger.jcodemodel.JCodeModel;
import com.helger.jcodemodel.JInvocation;
import com.helger.jcodemodel.JVar;

import io.konig.core.KonigException;
import io.konig.core.showl.ShowlExpression;
import io.konig.core.showl.ShowlNodeShape;
import io.konig.core.showl.ShowlPropertyShape;
import io.konig.core.showl.ShowlUtil;
import io.konig.core.util.StringUtil;
import io.konig.core.vocab.Konig;

public class BeamEnumSourceProperty extends BeamSourceProperty {

	public BeamEnumSourceProperty(BeamChannel beamChannel, ShowlPropertyShape propertyShape) {
		super(beamChannel, propertyShape);
	}

	@Override
	public void generateVar(JCodeModel model, JBlock block, BeamExpressionTransform etran) throws BeamTransformGenerationException {
		
		URI predicate = getPredicate();
		AbstractJClass objectClass = model.ref(Object.class);
		String sourcePropertyName = predicate.getLocalName();
		
		IJExpression enumMember = getBeamChannel().getSourceRowParam();
		
		if (enumMember == null) {
			
			ShowlNodeShape enumNode = enumNode();
			ShowlNodeShape enumTarget = enumNode.getTargetNode();
			
			ShowlPropertyShape targetAccessor = enumTarget.getAccessor();
			ShowlExpression targetAccessorExpression = targetAccessor==null ? null : targetAccessor.getSelectedExpression();
			
			if (targetAccessorExpression != null) {
				enumMember = etran.transform(targetAccessorExpression);
			}
			
			if (enumMember == null) {
				throw new KonigException("sourceRowParam not defined for " + getPropertyShape().getPath());
			}
		}
		
		String getterName = "get" + StringUtil.capitalize(sourcePropertyName);
		
		JInvocation initValue = enumMember.invoke(getterName);
		if (Konig.id.equals(predicate)){
			initValue = initValue.invoke("getLocalName");
		}
		
		JVar sourcePropertyVar = block.decl(objectClass, sourcePropertyName).init(initValue);
		
		setVar(sourcePropertyVar);
	}

	private ShowlNodeShape enumNode() throws BeamTransformGenerationException {
		
		ShowlPropertyShape p = getPropertyShape();
		while (p != null) {
			ShowlNodeShape node  = p.getDeclaringShape();
			if (ShowlUtil.isEnumNode(node)) {
				return node;
			}
			p = node.getAccessor();
		}
		throw new BeamTransformGenerationException("Enum node not found for " + getPropertyShape().getPath());
	}

}
