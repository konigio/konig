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


import java.text.MessageFormat;

import org.openrdf.model.Literal;
import org.openrdf.model.vocabulary.XMLSchema;

import com.helger.jcodemodel.AbstractJClass;
import com.helger.jcodemodel.IJExpression;
import com.helger.jcodemodel.JCodeModel;
import com.helger.jcodemodel.JDefinedClass;
import com.helger.jcodemodel.JExpr;
import com.helger.jcodemodel.JForEach;
import com.helger.jcodemodel.JInvocation;
import com.helger.jcodemodel.JMethod;
import com.helger.jcodemodel.JMod;
import com.helger.jcodemodel.JVar;

import io.konig.core.showl.ShowlExpression;
import io.konig.core.showl.ShowlFunctionExpression;
import io.konig.core.showl.ShowlPropertyExpression;
import io.konig.core.showl.ShowlStructExpression;
import io.konig.core.showl.expression.ShowlLiteralExpression;
import io.konig.formula.FunctionExpression;
import io.konig.formula.FunctionModel;

public class BeamExpressionTransformImpl implements BeamExpressionTransform {

	private BeamPropertyManager manager;
	private JCodeModel model;
	private JDefinedClass targetClass;
	
	private JMethod concatMethod;
	
	public BeamExpressionTransformImpl(
			BeamPropertyManager manager, 
			JCodeModel model, 
			JDefinedClass targetClass) {
		this.manager = manager;
		this.model = model;
		this.targetClass = targetClass;
	}

	@Override
	public IJExpression transform(ShowlExpression e) throws BeamTransformGenerationException {
		
		if (e instanceof ShowlLiteralExpression) {

      Literal literal = ((ShowlLiteralExpression) e).getLiteral();
      if (literal.getDatatype().equals(XMLSchema.STRING)) {
        return JExpr.lit(literal.stringValue());
      } else {
        fail("Typed literal not supported in expression: {0}", e.toString());
      }
		} 
		
		if (e instanceof ShowlPropertyExpression) {
			ShowlPropertyExpression p = (ShowlPropertyExpression) e;
			BeamSourceProperty b = manager.forPropertyShape(p.getSourceProperty());
			return b.getVar();
		}
		
		if (e instanceof ShowlStructExpression) {
			return struct((ShowlStructExpression)e);
		}
		
		if (e instanceof ShowlFunctionExpression) {
			return function((ShowlFunctionExpression) e);
		}
		
		throw new BeamTransformGenerationException("Failed to tranform " + e.toString());
	}

	private IJExpression function(ShowlFunctionExpression e) throws BeamTransformGenerationException {
	
		FunctionExpression function = e.getFunction();
		 if (function.getModel() == FunctionModel.CONCAT) {
			 return concat(e);
     } else {
     	fail("Function {0} not supported at {1}", function.toSimpleString(), e.getDeclaringProperty().getPath());
     }
		return null;
	}

	private IJExpression concat(ShowlFunctionExpression e) throws BeamTransformGenerationException {
		JMethod concatMethod = concatMethod();
		
		JInvocation invoke = JExpr.invoke(concatMethod);
		
		for (ShowlExpression arg : e.getArguments()) {
			IJExpression javaArg = transform(arg);
			invoke.arg(javaArg);
		}
		
		
		return invoke;
	}


	private JMethod concatMethod() {
		if (concatMethod == null) {
			AbstractJClass stringClass = model.ref(String.class);
			AbstractJClass objectClass = model.ref(Object.class);
			
			// private String concat(Object...arg) {
			
			concatMethod = targetClass.method(JMod.PRIVATE, stringClass, "concat");
			JVar arg = concatMethod.param(objectClass, "arg");
			
			//   for (Object obj : arg) {
			//     if (obj == null) {
			//       return null;
			//     }
			//   }
			
			JForEach validationLoop = concatMethod.body().forEach(objectClass, "obj", arg);
			validationLoop.body()._if(validationLoop.var().eqNull())._then()._return(JExpr._null());
			

//      StringBuilder builder = new StringBuilder();
//      for (Object obj : arg) {
//        builder.append(obj);
//      }
//      
//      return builder;	
			
			AbstractJClass stringBuilderClass = model.ref(StringBuilder.class);
			JVar builder = concatMethod.body().decl(stringBuilderClass, "builder").init(stringBuilderClass._new());
			
			JForEach loop = concatMethod.body().forEach(objectClass, "obj", arg);
			loop.body().add(builder.invoke("append").arg(loop.var()));
			
			concatMethod.body()._return(builder.invoke("toString"));
		}
		return concatMethod;
	}

	private IJExpression struct(ShowlStructExpression e) {
		// TODO Auto-generated method stub
		return null;
	}

	private void fail(String pattern, Object... arg) throws BeamTransformGenerationException {
		String msg = MessageFormat.format(pattern, arg);
		throw new BeamTransformGenerationException(msg);
		
	}

}
