package io.konig.transform.beam;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

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

import io.konig.core.showl.ShowlPropertyExpression;
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
		Map<AbstractJType, String> getterMap = etran.peekBlockInfo().getGetterMap();
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
				if (getterMap.get(targetDatatype) == null) {
					declareDatatypeValidationMethod(targetDatatype, targetProperty, fieldName, etran.peekBlockInfo());
					ifStatement._then().add(JExpr.ref("outputRow").invoke("set").arg(fieldName)
							.arg(JExpr.invoke(validationMethod).arg(propertyValue)));
				}
				getterMap.put(targetDatatype, validationMethod);
			}
		} else {
			ifStatement._then().add(outputRow.invoke("set").arg(fieldName).arg(propertyValue));
		}

	}

	private void declareDatatypeValidationMethod(AbstractJType targetDatatype, ShowlPropertyShape p,
			JStringLiteral targetPropertyName, BlockInfo info) {
		String methodName = targetDatatype.name().toLowerCase() + "Value";
		isMethodExist(info, methodName);
		if (!isMethodExist(info, methodName)) {

			JMethod method = info.getDefinedClass().method(JMod.PRIVATE, targetDatatype, methodName);
			JBlock methodBody = method.body();
			JTryBlock tryBlock = methodBody._try();
			JBlock tryBody = tryBlock.body();
			JVar fieldObject = method.param(info.getCodeModel().ref(Object.class), p.getPredicate().getLocalName());
			JVar errorBuilder = method.param(info.getCodeModel().ref(ErrorBuilder.class), "errorBuilder");

			if (targetDatatype == info.getCodeModel().ref(String.class)) {
				tryBody._return(fieldObject);

			} else if (targetDatatype == info.getCodeModel().ref(Boolean.class)) {
				tryBody._return(JExpr.lit("true").invoke("equalsIgnoreCase").arg(fieldObject));

			} else if (targetDatatype == info.getCodeModel().ref(Long.class)
					|| targetDatatype == info.getCodeModel().ref(Integer.class)) {
				tryBody._return(targetDatatype._new().arg(fieldObject));

			} else if (targetDatatype == info.getCodeModel().ref(Double.class)) {
				tryBody._return(targetDatatype._new().arg(fieldObject));

			} else if (targetDatatype == info.getCodeModel().ref(Float.class)) {
				tryBody._return(targetDatatype._new().arg(fieldObject));
			}

			JCatchBlock catchBlock = tryBlock._catch(info.getCodeModel().ref(Exception.class));
			JVar message = catchBlock.body().decl(info.getCodeModel()._ref(String.class), "message");
			message.init(info.getCodeModel().directClass("String").staticInvoke("format")
					.arg("Invalid " + targetDatatype.name() + " value %s for field " + targetPropertyName + ";")
					.arg(fieldObject.invoke("toString")));
			catchBlock.body().add(errorBuilder.invoke("addError").arg(message));
			method.body()._return(JExpr._null());
		}

	}

	private boolean isMethodExist(BlockInfo info, String methodName){
		Collection<JMethod> methods = info.getDefinedClass().methods();
		Iterator<JMethod> i = methods.iterator();
		while(i.hasNext()) {
			JMethod method = i.next();
			if(methodName.equals(method.name())){
				return true;
			}
		}
		return false;
	}
}
