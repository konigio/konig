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
import java.util.Date;

import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.XMLSchema;

import com.helger.jcodemodel.AbstractJClass;
import com.helger.jcodemodel.AbstractJType;
import com.helger.jcodemodel.IJExpression;
import com.helger.jcodemodel.JBlock;
import com.helger.jcodemodel.JCodeModel;
import com.helger.jcodemodel.JConditional;
import com.helger.jcodemodel.JDefinedClass;
import com.helger.jcodemodel.JExpr;
import com.helger.jcodemodel.JForEach;
import com.helger.jcodemodel.JForLoop;
import com.helger.jcodemodel.JInvocation;
import com.helger.jcodemodel.JMethod;
import com.helger.jcodemodel.JMod;
import com.helger.jcodemodel.JVar;

import io.konig.core.showl.ShowlEnumIndivdiualReference;
import io.konig.core.showl.ShowlExpression;
import io.konig.core.showl.ShowlFilterExpression;
import io.konig.core.showl.ShowlFunctionExpression;
import io.konig.core.showl.ShowlIriReferenceExpression;
import io.konig.core.showl.ShowlPropertyExpression;
import io.konig.core.showl.ShowlStructExpression;
import io.konig.core.showl.ShowlSystimeExpression;
import io.konig.core.showl.expression.ShowlLiteralExpression;
import io.konig.formula.FunctionExpression;
import io.konig.formula.FunctionModel;

public class BeamExpressionTransformImpl implements BeamExpressionTransform {

	private BeamPropertyManager manager;
	private JCodeModel model;
	private JDefinedClass targetClass;
	
	private JMethod concatMethod;
	private JMethod localNameMethod;
	private JMethod stripSpacesMethod;
	
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
		
		if (e instanceof ShowlFilterExpression) {
			return filter((ShowlFilterExpression)e);
		}
		
		if (e instanceof ShowlIriReferenceExpression) {
			return iriReference((ShowlIriReferenceExpression)e);
		}
		
		if (e instanceof ShowlEnumIndivdiualReference) {
			return enumIndividualReference((ShowlEnumIndivdiualReference)e);
		}
		
		if (e instanceof ShowlSystimeExpression) {
			return systime();
		}
		
		throw new BeamTransformGenerationException("Failed to tranform " + e.toString());
	}

	private IJExpression systime() {
		AbstractJClass longClass = model.ref(Long.class);
		AbstractJClass dateClass = model.ref(Date.class);
		return longClass._new().arg(dateClass._new().invoke("getTime"));
	}

	private IJExpression enumIndividualReference(ShowlEnumIndivdiualReference e) {
		URI iri = e.getIriValue();
		return JExpr.lit(iri.getLocalName());
	}

	private IJExpression iriReference(ShowlIriReferenceExpression e) {
		return JExpr.lit(e.getIriValue().stringValue());
	}

	private IJExpression filter(ShowlFilterExpression e) throws BeamTransformGenerationException {		
		return transform(e.getValue());
	}

	private IJExpression function(ShowlFunctionExpression e) throws BeamTransformGenerationException {
	
		FunctionExpression function = e.getFunction();
		FunctionModel model = function.getModel();
		 if (model == FunctionModel.CONCAT) {
			 return concat(e);
		 } else if (model == FunctionModel.IRI) {
			 return iriFunction(e);
		 } else if (model == FunctionModel.STRIP_SPACES) {
			 return stripSpaces(e);
     } else {
     	fail("Function {0} not supported at {1}", function.toSimpleString(), e.getDeclaringProperty().getPath());
     }
		return null;
	}

	private IJExpression stripSpaces(ShowlFunctionExpression e) throws BeamTransformGenerationException {
		ShowlExpression arg = e.getArguments().get(0);
		
		IJExpression stringValue = transform(arg);
		
		JMethod method = stripSpacesMethod();
		return JExpr.invoke(method).arg(stringValue.invoke("toString"));
	}

	private JMethod stripSpacesMethod() {
		if (stripSpacesMethod == null) {
			AbstractJClass stringClass = model.ref(String.class);
			AbstractJClass stringBuilderClass = model.ref(StringBuilder.class);
			AbstractJType intType = model._ref(int.class);
			AbstractJClass characterClass = model.ref(Character.class);
			
			stripSpacesMethod = targetClass.method(JMod.PRIVATE, stringClass, "stripSpaces");
			JVar text = stripSpacesMethod.param(stringClass, "text");
			JBlock block = stripSpacesMethod.body();
			
			JVar builder = block.decl(stringBuilderClass, "builder").init(stringBuilderClass._new());
			
			JForLoop forLoop = block._for();
			JVar i = forLoop.init(intType, "i", JExpr.lit(0));
			forLoop.test(i.lt(text.invoke("length")));
			
			JBlock forBody = forLoop.body();
			
			JVar c = forBody.decl(intType, "c").init(text.invoke("codePointAt").arg(i));
			forBody._if(characterClass.staticInvoke("isSpaceChar").arg(c))._then().add(builder.invoke("appendCodePoint").arg(c));
			forBody.add(i.assignPlus(characterClass.staticInvoke("charCount").arg(c)));
			block._return(builder.invoke("toString"));
		}
		return stripSpacesMethod;
	}

	private IJExpression iriFunction(ShowlFunctionExpression e) throws BeamTransformGenerationException {
		
		ShowlExpression arg = e.getArguments().get(0);
		
		IJExpression stringValue = transform(arg);
		
		JMethod localName = localNameMethod();
		
		
		// For now, we assume that every IRI is a reference to a named individual within an Enumeration.
		// Thus, we should return just the local name portion of the string value.
		// We may need to support other use cases in the future.
		// We should probably confirm that the referenced individual is an enumeration.
		
		AbstractJClass stringClass = model.ref(String.class);
		
		return JExpr.invoke(localName).arg(stringValue.castTo(stringClass));
	}

	private JMethod localNameMethod() {
		if (localNameMethod == null) {
			AbstractJClass stringClass = model.ref(String.class);
			AbstractJType intType = model._ref(int.class);
			
			localNameMethod = targetClass.method(JMod.PRIVATE, stringClass, "localName");
			JVar iriString = localNameMethod.param(stringClass, "iriString");
			
			JConditional ifStatement = localNameMethod.body()._if(iriString.neNull());
			JBlock thenBlock = ifStatement._then();
			JVar start = thenBlock.decl(intType, "start").init(iriString.invoke("lastIndexOf").arg(JExpr.lit('/')));
			JConditional if2 = thenBlock._if(start.lt(0));
			JBlock then2 = if2._then();
			then2.assign(start, iriString.invoke("lastIndexOf").arg(JExpr.lit('#')));
			JConditional if3 = then2._if(start.lt(0));
			if3._then().assign(start, iriString.invoke("lastIndexOf").arg(JExpr.lit(':')));
			
			thenBlock._if(start.gte(JExpr.lit(0)))._then()._return(iriString.invoke("substring").arg(start.plus(JExpr.lit(1))));
			
			localNameMethod.body()._return(JExpr._null());
		}
		return localNameMethod;
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
			JVar arg = concatMethod.varParam(objectClass, "arg");
			
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
