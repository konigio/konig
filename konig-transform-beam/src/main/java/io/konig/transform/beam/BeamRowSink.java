package io.konig.transform.beam;

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import org.openrdf.model.URI;

import com.helger.jcodemodel.AbstractJType;

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

import com.helger.jcodemodel.IJExpression;
import com.helger.jcodemodel.JBlock;
import com.helger.jcodemodel.JCatchBlock;
import com.helger.jcodemodel.JConditional;
import com.helger.jcodemodel.JExpr;
import com.helger.jcodemodel.JMethod;
import com.helger.jcodemodel.JMod;
import com.helger.jcodemodel.JStringLiteral;
import com.helger.jcodemodel.JTryBlock;
import com.helger.jcodemodel.JVar;

import io.konig.core.showl.ShowlPropertyShape;
import io.konig.shacl.PropertyConstraint;

public class BeamRowSink implements BeamPropertySink {

	public static BeamRowSink INSTANCE = new BeamRowSink();

	private BeamRowSink() {
	}

	@Override
	public void captureProperty(BeamExpressionTransform etran, JConditional ifStatement,
			ShowlPropertyShape targetProperty, IJExpression propertyValue) throws BeamTransformGenerationException {

		JVar outputRow = etran.peekBlockInfo().getOutputRow();
		BeamTypeManager typeManager = etran.peekBlockInfo().getTypeManager();
		if (outputRow == null) {
			throw new BeamTransformGenerationException(
					"outputRow for property " + targetProperty.getPath() + " is null");
		}

		JStringLiteral fieldName = JExpr.lit(targetProperty.getPredicate().getLocalName());
		PropertyConstraint propertyConstraint = targetProperty.getPropertyConstraint();
		if (propertyConstraint != null) {
			URI datatype = propertyConstraint.getDatatype();
			if (datatype != null) {
				AbstractJType targetDatatype = typeManager.javaType(datatype);
				String validationMethod = targetDatatype.name().toLowerCase() + "Value";
				declareDatatypeValidationMethod(targetDatatype, targetProperty, etran.peekBlockInfo());
				ifStatement._then().add(JExpr.ref("outputRow").invoke("set").arg(fieldName)
						.arg(JExpr.invoke(validationMethod).arg(propertyValue).arg(JExpr.ref("errorBuilder")).arg(fieldName)));
			} else {
				ifStatement._then().add(outputRow.invoke("set").arg(fieldName).arg(propertyValue));
			}
		} else {
			ifStatement._then().add(outputRow.invoke("set").arg(fieldName).arg(propertyValue));
		}

	}

	private void declareDatatypeValidationMethod(AbstractJType targetDatatype, ShowlPropertyShape p, BlockInfo info) throws BeamTransformGenerationException {
		String methodName = targetDatatype.name().toLowerCase() + "Value";
		isMethodExist(info, methodName);
		if (!isMethodExist(info, methodName)) {

			JMethod method = info.getDefinedClass().method(JMod.PRIVATE, targetDatatype, methodName);
			JBlock methodBody = method.body();
			JTryBlock tryBlock = methodBody._try();
			JBlock tryBody = tryBlock.body();
			JVar fieldObject = method.param(info.getCodeModel().ref(Object.class), p.getPredicate().getLocalName());
			JVar errorBuilder = method.param(info.getTypeManager().errorBuilderClass(), "errorBuilder");
			JVar targetProperty = method.param(info.getCodeModel().ref(String.class), "targetPropertyName");
			JConditional condition = tryBody._if(fieldObject.neNull().cand(fieldObject._instanceof(targetDatatype)));
			condition._then()._return(fieldObject.castTo(targetDatatype));
			JCatchBlock catchBlock = tryBlock._catch(info.getCodeModel().ref(Exception.class));
			JVar message = catchBlock.body().decl(info.getCodeModel().directClass("String"), "message");
			message.init(info.getCodeModel().directClass("String").staticInvoke("format")
					.arg("Invalid " + targetDatatype.name() + " value %s for field " + p.getPredicate().getLocalName()
							+ ";")
					.arg(info.getCodeModel().directClass("String").staticInvoke("valueOf").arg(fieldObject))
					.arg(JExpr.ref(targetProperty)));
			catchBlock.body().add(errorBuilder.invoke("addError").arg(message));
			method.body()._return(JExpr._null());
		}

	}

	private boolean isMethodExist(BlockInfo info, String methodName) {
		Collection<JMethod> methods = info.getDefinedClass().methods();
		Iterator<JMethod> i = methods.iterator();
		while (i.hasNext()) {
			JMethod method = i.next();
			if (methodName.equals(method.name())) {
				return true;
			}
		}
		return false;
	}
}
