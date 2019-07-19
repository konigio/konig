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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.services.bigquery.model.TableRow;
import com.helger.jcodemodel.AbstractJClass;
import com.helger.jcodemodel.AbstractJType;
import com.helger.jcodemodel.IJExpression;
import com.helger.jcodemodel.JAtomDouble;
import com.helger.jcodemodel.JAtomFloat;
import com.helger.jcodemodel.JAtomInt;
import com.helger.jcodemodel.JAtomLong;
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
import com.helger.jcodemodel.JStringLiteral;
import com.helger.jcodemodel.JVar;

import io.konig.core.OwlReasoner;
import io.konig.core.showl.ShowlBasicStructExpression;
import io.konig.core.showl.ShowlBinaryRelationalExpression;
import io.konig.core.showl.ShowlCaseStatement;
import io.konig.core.showl.ShowlChannel;
import io.konig.core.showl.ShowlContainmentOperator;
import io.konig.core.showl.ShowlDirectPropertyShape;
import io.konig.core.showl.ShowlEffectiveNodeShape;
import io.konig.core.showl.ShowlEnumIndividualReference;
import io.konig.core.showl.ShowlEnumNodeExpression;
import io.konig.core.showl.ShowlEnumPropertyExpression;
import io.konig.core.showl.ShowlEnumStructExpression;
import io.konig.core.showl.ShowlEqualStatement;
import io.konig.core.showl.ShowlExpression;
import io.konig.core.showl.ShowlFilterExpression;
import io.konig.core.showl.ShowlFunctionExpression;
import io.konig.core.showl.ShowlIriReferenceExpression;
import io.konig.core.showl.ShowlListRelationalExpression;
import io.konig.core.showl.ShowlNodeShape;
import io.konig.core.showl.ShowlPropertyExpression;
import io.konig.core.showl.ShowlPropertyShape;
import io.konig.core.showl.ShowlStatement;
import io.konig.core.showl.ShowlStructExpression;
import io.konig.core.showl.ShowlSystimeExpression;
import io.konig.core.showl.ShowlUtil;
import io.konig.core.showl.ShowlWhenThenClause;
import io.konig.core.showl.expression.ShowlLiteralExpression;
import io.konig.core.util.StringUtil;
import io.konig.core.vocab.Konig;
import io.konig.formula.FunctionExpression;
import io.konig.formula.FunctionModel;

public class BeamExpressionTransform  {
	private static Logger logger = LoggerFactory.getLogger(BeamExpressionTransform.class);

	private OwlReasoner reasoner;
	private BeamTypeManager typeManager;
	private JCodeModel model;
	private JDefinedClass targetClass;
	
	private JMethod concatMethod;
	private JMethod localNameMethod;
	private JMethod stripSpacesMethod;
	private JMethod pathGetter;
	private int caseCount = 0;
	private int varCount = 0;
	
	private boolean treatNullAsFalse;
	private ArrayList<BlockInfo> blockStack;
	
	private BeamLiteralFactory literalFactory;
	
	private SimplePropertyGenerator simplePropertyGenerator;

	
	public BeamExpressionTransform(
			OwlReasoner reasoner,
			BeamTypeManager typeManager,
			JCodeModel model, 
			JDefinedClass targetClass) {
		this.reasoner = reasoner;
		this.typeManager = typeManager;
		this.model = model;
		this.targetClass = targetClass;
		
		literalFactory = new BeamLiteralFactory(model);
	}

	public IJExpression transform(ShowlExpression e) throws BeamTransformGenerationException {
		
		if (e instanceof ShowlLiteralExpression) {

      Literal literal = ((ShowlLiteralExpression) e).getLiteral();
      return literalFactory.javaExpression(literal);
		} 
		
		if (e instanceof ShowlPropertyExpression) {
			return property((ShowlPropertyExpression)e);
		}
		
		if (e instanceof ShowlBasicStructExpression) {
			return struct((ShowlBasicStructExpression)e);
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
		
		if (e instanceof ShowlEnumIndividualReference) {
			return enumIndividualReference((ShowlEnumIndividualReference)e);
		}
		
		if (e instanceof ShowlSystimeExpression) {
			return systime();
		}
		
		if (e instanceof ShowlCaseStatement) {
			return caseStatement((ShowlCaseStatement)e);
		}
		
		if (e instanceof ShowlListRelationalExpression) {
			return listRelationalExpression((ShowlListRelationalExpression)e);
		}
		
		if (e instanceof ShowlBinaryRelationalExpression) {
			return binaryRelationalExpression((ShowlBinaryRelationalExpression)e);
		}
		
		if (e instanceof ShowlEnumNodeExpression) {
			return enumNode((ShowlEnumNodeExpression)e);
		}
		
		throw new BeamTransformGenerationException("Failed to tranform " + e.toString());
	}
	

	public JVar declareEnumIndividual(ShowlNodeShape enumNode, ShowlStatement statement) throws BeamTransformGenerationException {
		if (statement == null) {
			fail("Cannot declare enum individual {0} because join statement is not defined", enumNode.getPath());
		}
		
		
		if (statement instanceof ShowlEqualStatement) {
			ShowlEqualStatement equal = (ShowlEqualStatement) statement;
			ShowlExpression enumPropertyExpression = equal.expressionOf(enumNode);
			if (enumPropertyExpression instanceof ShowlPropertyExpression) {
				ShowlPropertyShape enumPropertyShape = ((ShowlPropertyExpression)enumPropertyExpression).getSourceProperty();
				URI enumClassId = enumNode.getOwlClass().getId();
				AbstractJClass enumClass = typeManager.enumClass(enumClassId);
				ShowlExpression otherExpression = equal.otherExpression(enumPropertyExpression);
				IJExpression otherJavaExpression = transform(otherExpression);
				
				URI predicate = enumPropertyShape.getPredicate();
				
				String getter = predicate.equals(Konig.id) ?
						"findByLocalName" :
						"findBy" + StringUtil.capitalize(predicate.getLocalName());
				
				URI rdfType = otherExpression.valueType(reasoner);
				AbstractJType otherType = typeManager.javaType(rdfType);
				
				IJExpression initExpr = enumClass.staticInvoke(getter).arg(otherJavaExpression.castTo(otherType));

				BlockInfo blockInfo = peekBlockInfo();
				String varName = blockInfo.varNameFor(enumNode);
				
				
				JBlock block = blockInfo.getBlock();
				JVar var = block.decl(enumClass, varName).init(initExpr);
				blockInfo.putEnumMember(enumNode.effectiveNode(), var);
				
				return var;
			}
			
		} 

		fail("Cannot declare enum individual {0} because join statement not supported: {1}", enumNode.getPath(), statement.toString());
		
		return null;
	}


	private JVar enumNode(ShowlEnumNodeExpression e) throws BeamTransformGenerationException {
		ShowlChannel channel = e.getChannel();
		if (channel == null) {
			fail("Cannot transform {0} because channel is not defined", e.displayValue());
		}
		ShowlStatement statement = channel.getJoinStatement();
		return declareEnumIndividual(e.getEnumNode(), statement);
	}


	private IJExpression binaryRelationalExpression(ShowlBinaryRelationalExpression e) throws BeamTransformGenerationException {
		IJExpression left = transform(e.getLeft());
		IJExpression right = transform(e.getRight());
		
		BlockInfo blockInfo = peekBlockInfo();
		JBlock block = blockInfo.getBlock();
		
		AbstractJClass objectClass = model.ref(Object.class);
		AbstractJClass numberClass = model.ref(Number.class);
		
		if (
				left instanceof JAtomLong ||
				left instanceof JAtomDouble ||
				left instanceof JAtomInt ||
				left instanceof JAtomFloat
		) {
			IJExpression temp = left;
			left = right;
			right = temp;
		}
		
		JVar leftVar = block.decl(objectClass, nextVarName()).init(left);
		
	
		
		
		switch (e.getOperator()) {
		case EQUALS :
			
			if (treatNullAsFalse) {
				IJExpression condition = null;
				
				if (right instanceof JAtomLong) {
					
					condition = leftVar._instanceof(numberClass).cand(
							leftVar.castTo(numberClass).invoke("longValue").eq(right));
				} else if (right instanceof JAtomDouble) {

					condition = leftVar._instanceof(numberClass).cand(
							leftVar.castTo(numberClass).invoke("doubleValue").eq(right));
					
				} else if (right instanceof JAtomFloat) {

					condition = leftVar._instanceof(numberClass).cand(
							leftVar.castTo(numberClass).invoke("floatValue").eq(right));
					
				} else if (right instanceof JAtomInt) {

					condition = leftVar._instanceof(numberClass).cand(
							leftVar.castTo(numberClass).invoke("intValue").eq(right));
					
				} else {
					condition = leftVar.neNull().cand(leftVar.invoke("equals").arg(right));
				} 
				return JExpr.cond(condition, JExpr.TRUE, JExpr.FALSE);
			}
			
			JConditional ifStatement = block._if(leftVar.eqNull());
			JVar errorBuilder = blockInfo.getErrorBuilderVar();
			StringBuilder message = new StringBuilder();
			message.append(e.displayValue());
			message.append(" must not be null");
			
			ifStatement._then().add(errorBuilder.invoke("addError").arg(JExpr.lit(message.toString())));
			ifStatement._then()._return(JExpr._null());
			
			return leftVar.neNull().cand(leftVar.invoke("equals").arg(right));
			
			
		default :
			fail("{0} operator not supported in {1}", e.getOperator().name(), e.displayValue());
		}
		
		return null;
	}

	

	private String nextVarName() {
		varCount++;
		return "var" + varCount;
	}

	private IJExpression property(ShowlPropertyExpression e) throws BeamTransformGenerationException {

		BlockInfo blockInfo = peekBlockInfo();
		if (blockInfo == null) {
			fail("Cannot generate expression for {0} because BlockInfo is not defined.", 
					e.getSourceProperty().getPath());
		}
			
		ShowlPropertyShape p = e.getSourceProperty();
		if (p.isTargetProperty()) {
			return targetProperty(p);
		} 
			

		JVar bVar = blockInfo.getPropertyValue(p.asGroup());
		if (bVar != null) {
			return bVar;
		}
		
		// There is no pre-declared variable for this property.  Try getting it directly from a TableRow.
		
		ShowlEffectiveNodeShape node = p.getDeclaringShape().effectiveNode();
		JVar tableRowVar = tableRowVar(node);
		if (tableRowVar != null) {
			return tableRowVar.invoke("get").arg(JExpr.lit(p.getPredicate().getLocalName()));
		}
		
		fail("TableRow variable not found for expression {0}", e.displayValue());
		return null;
		
	}

	

	private JVar tableRowVar(ShowlEffectiveNodeShape node) throws BeamTransformGenerationException {
		BlockInfo blockInfo = peekBlockInfo();
		JVar tableRowVar = blockInfo.getTableRowVar(node);
		if (tableRowVar == null) {
			// Try injecting the parameter
			BeamMethod beamMethod = blockInfo.getBeamMethod();
			if (beamMethod != null) {
				tableRowVar = addTableRowParam(beamMethod, node);
			}
		}
		return tableRowVar;
	}

	private IJExpression targetProperty(ShowlPropertyShape p) throws BeamTransformGenerationException {
		return invokePathGetter(p);
	}


	private IJExpression invokePathGetter(ShowlPropertyShape p) throws BeamTransformGenerationException {
		BlockInfo info = peekBlockInfo();
		
		ShowlNodeShape rootNode = p.getRootNode();
		JVar tableRowVar = info.getTableRowVar(rootNode.effectiveNode());
		

		List<String> fieldList = new ArrayList<>();
		while (p != null) {
			fieldList.add(p.getPredicate().getLocalName());
			p = p.getDeclaringShape().getAccessor();
		}
		Collections.reverse(fieldList);
		
		JMethod pathGetter = pathGetter();
		
		JInvocation result = JExpr.invoke(pathGetter);
		result.arg(tableRowVar);
		for (String fieldName : fieldList) {
			result.arg(JExpr.lit(fieldName));
		}
		
		return result;
	}

	private JMethod pathGetter() {
		if (pathGetter == null) {
			AbstractJClass objectClass = model.ref(Object.class);
			AbstractJClass stringClass = model.ref(String.class);
			AbstractJClass tableRowClass = model.ref(TableRow.class);
			
			pathGetter = targetClass.method(JMod.PRIVATE, objectClass, "get");
			JVar value = pathGetter.param(objectClass, "value");
			JVar fieldNameList = pathGetter.varParam(stringClass, "fieldNameList");
			
			JForEach loop = pathGetter.body().forEach(stringClass, "fieldName", fieldNameList);
			JConditional ifStatement = loop.body()._if(value._instanceof(tableRowClass));
			ifStatement._then().assign(value, value.castTo(tableRowClass).invoke("get").arg(loop.var()));
			ifStatement._else()._return(JExpr._null());
			pathGetter.body()._return(value);
		}
		return pathGetter;
	}

	private IJExpression listRelationalExpression(ShowlListRelationalExpression e) throws BeamTransformGenerationException {
		BlockInfo blockInfo = peekBlockInfo();
		if (blockInfo == null) {
			// We should implement a method that returns the boolean value from this expression.
			// For now, however, we'll just throw an exception.
			fail("Cannot support IN operation without BlockInfo yet.");
		}
		JBlock block = blockInfo.getBlock();
		AbstractJClass objectClass = model.ref(Object.class);
		AbstractJClass setClass = model.ref(Set.class).narrow(objectClass);
		AbstractJClass hashSetClass = model.ref(HashSet.class);
		
		JVar set = block.decl(setClass, blockInfo.nextSetName()).init(hashSetClass._new());
		
		List<IJExpression> argList = new ArrayList<>(e.getRight().size());
		
		for (ShowlExpression member : e.getRight()) {
			argList.add(transform(member));
		}
		
		for (IJExpression value : argList) {
			block.add(set.invoke("add").arg(value));
		}
		
		ShowlExpression left = e.getLeft();
		
		// The following block of code is unfortunate.
		// Special handling for the case where a target property is being referenced.
		// Is there a better way to address this?
//		if (left instanceof ShowlPropertyExpression) {
//			ShowlPropertyShape p = ((ShowlPropertyExpression) left).getSourceProperty();
//			if (p.isTargetProperty()) {
//				if (p instanceof ShowlDerivedPropertyShape) {
//					ShowlPropertyShapeGroup group = p.asGroup();
//					ShowlDirectPropertyShape direct = group.direct();
//					if (direct != null) {
//						ShowlExpression s = direct.getSelectedExpression();
//						if (s != null) {
//							left = s;
//						}
//					}
//				}
//			}
//		}
		
		
		IJExpression valueInit = transform(left);
		String valueName = blockInfo.valueName(left);

		URI leftRdfType = left.valueType(reasoner);
		
		if (reasoner.isEnumerationClass(leftRdfType) && left instanceof ShowlPropertyExpression) {
			valueInit = JExpr.cond(valueInit.eqNull(), JExpr._null(), valueInit);
		} else if(reasoner.isEnumerationClass(leftRdfType)) {
			valueInit = JExpr.cond(valueInit.eqNull(), JExpr._null(), valueInit.invoke("getId").invoke("stringValue"));
		}
		
		JVar value = block.decl(objectClass, valueName).init(valueInit);
		
		
		
		IJExpression result = set.invoke("contains").arg(value);
		if (e.getOperator() == ShowlContainmentOperator.NOT_IN) {
			result.not();
		}
	
		return result;
	}

	
	public BlockInfo peekBlockInfo() throws BeamTransformGenerationException {
		if (blockStack==null || blockStack.isEmpty()) {
			fail("BlockInfo stack is empty");
		}
		return  blockStack.get(blockStack.size()-1);
	}

	private IJExpression caseStatement(ShowlCaseStatement e) throws BeamTransformGenerationException {
		
		// First we generate a method that computes and returns the output of the CASE statement.
		// Then we'll generate an invocation of that method and return the invocation.
		
		// Here we go.  First up, generate the method that compute the output of the CASE statement.
		
		caseCount++;
		String methodName = "case" + caseCount;
		
		URI valueType = e.valueType(reasoner);
		AbstractJType resultType = typeManager.javaType(valueType);
		
		
		
		JMethod method = targetClass.method(JMod.PRIVATE, resultType, methodName);
		BeamMethod beamMethod = new BeamMethod(method);
		BlockInfo blockInfo = beginBlock(beamMethod);
		try {
			addErrorBuilderParam(beamMethod);
			addRowParameters(beamMethod, e);
	
			
			String resultParamName = StringUtil.firstLetterLowerCase(valueType.getLocalName()) + "Value";
			JVar resultParam = method.body().decl(resultType, resultParamName).init(JExpr._null());
			
			
			
			
			if (reasoner.isEnumerationClass(valueType)) {
				
				// For now we will use EnumValueType.OBJECT.
				// But that's not always true.  How do we detect whether it should be EnumValueType.LOCAL_NAME?
				
				blockInfo.setEnumInfo(new BeamEnumInfo(EnumValueType.OBJECT, resultParam, null));
			}
			
			JConditional ifStatement = null;
			
			int whenIndex = 0;
			for (ShowlWhenThenClause whenThen : e.getWhenThenList()) {
				whenIndex++;
				
				ShowlExpression whenExpression = whenThen.getWhen();
				String whenMethodName = methodName + "_when" + whenIndex;
				JInvocation condition = whenExpression(whenMethodName, whenExpression);
				
				JBlock thenBlock = null;
				if (ifStatement == null) {
					ifStatement = method.body()._if(condition);
					thenBlock = ifStatement._then();
					
				} else {
					thenBlock = ifStatement._elseif(condition)._then();
				}
				IJExpression then = transform(whenThen.getThen());
				thenBlock.assign(resultParam, then);
			}
			method.body()._return(resultParam);
		} finally {
			endBlock();
		}
				
		return createInvocation(beamMethod);
	}
	
	public BlockInfo beginBlock(BeamMethod beamMethod) {
		BlockInfo result = beginBlock(beamMethod.getMethod().body());
		result.beamMethod(beamMethod);
		return result;
	}


	private JInvocation whenExpression(String whenMethodName, ShowlExpression whenExpression) throws BeamTransformGenerationException {
		AbstractJType booleanType = model._ref(boolean.class);
		
		JMethod method = targetClass.method(JMod.PRIVATE, booleanType, whenMethodName);
		BeamMethod beamMethod = new BeamMethod(method);
		
		
		beginBlock(beamMethod);

		boolean treatValue = treatNullAsFalse(true);
		try {

			addErrorBuilderParam(beamMethod);
			addRowParameters(beamMethod, whenExpression);
			
			IJExpression result = transform(whenExpression);
			
			method.body()._return(result);
		} finally {
			treatNullAsFalse(treatValue);
			endBlock();
		}
		
		
		return createInvocation(beamMethod);
	}

	private boolean treatNullAsFalse(boolean value) {
		boolean result = treatNullAsFalse;
		treatNullAsFalse = value;
		return result;
	}

	
	public void endBlock() {
		blockStack.remove(blockStack.size()-1);
	}

	
	public BlockInfo beginBlock(JBlock block) {
		if (blockStack == null) {
			blockStack = new ArrayList<>();
		}
		BlockInfo info = new BlockInfo(block);
		blockStack.add(info);
		return info;
	}

	

	private IJExpression systime() {
		AbstractJClass longClass = model.ref(Long.class);
		AbstractJClass dateClass = model.ref(Date.class);
		return longClass._new().arg(dateClass._new().invoke("getTime").div(1000L));
	}

	private IJExpression enumIndividualReference(ShowlEnumIndividualReference e) throws BeamTransformGenerationException {
		URI iri = e.getIriValue();
		JStringLiteral localName = JExpr.lit(iri.getLocalName());
		BlockInfo blockInfo = peekBlockInfo();
		BeamEnumInfo enumInfo = blockInfo==null ? null : blockInfo.getEnumInfo();
		
		if (enumInfo != null && enumInfo.getEnumValueType()==EnumValueType.OBJECT) {
			URI owlClass = typeManager.enumClassOfIndividual(iri);
			AbstractJClass javaClass = typeManager.enumClass(owlClass);
			return javaClass.staticInvoke("findByLocalName").arg(localName);
		}
		return localName;
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

	private IJExpression struct(ShowlBasicStructExpression e) throws BeamTransformGenerationException {
		
		AbstractJClass tableRowClass = model.ref(TableRow.class);
		
		JBlock block = peekBlockInfo().getBlock();
		
		ShowlDirectPropertyShape p = e.getPropertyShape();
		
		JVar structVar = block.decl(tableRowClass, p.getPredicate().getLocalName()).init(tableRowClass._new());
	
		// TODO: finish building the struct
		
		fail("Not implemented");
		
		
		return structVar;
	}

	private void fail(String pattern, Object... arg) throws BeamTransformGenerationException {
		String msg = MessageFormat.format(pattern, arg);
		throw new BeamTransformGenerationException(msg);
		
	}
	

	
	public void processProperty(ShowlDirectPropertyShape targetProperty, ShowlExpression e) throws BeamTransformGenerationException {
		
		if (e instanceof ShowlStructExpression) {
			processStruct(targetProperty, (ShowlStructExpression) e);
		} else {
			fail("Unsupported expression type ''{0}'' at {1}", e.getClass().getSimpleName(), targetProperty.getPath());
		}
		
	}


	private void processStruct(ShowlDirectPropertyShape targetProperty, ShowlStructExpression struct) throws BeamTransformGenerationException {
		
		
		AbstractJClass tableRowClass = model.ref(TableRow.class);
		
		
		BlockInfo blockInfo = peekBlockInfo();
		JBlock block = blockInfo.getBlock();
		
		String fieldName = targetProperty.getPredicate().getLocalName();
		JVar structVar = block.decl(tableRowClass, fieldName + "Row")
				.init(tableRowClass._new());
		
		
		if (struct instanceof ShowlEnumStructExpression) {
			ShowlNodeShape enumNode = ((ShowlEnumStructExpression) struct).getEnumNode();
			blockInfo.putTableRow(enumNode.effectiveNode(), structVar);
		} else {
			blockInfo.putTableRow(targetProperty.getValueShape().effectiveNode(), structVar);
		}
		
		processStructPropertyList(targetProperty, struct);
		
		

		JConditional ifStatement = block._if(structVar.invoke("isEmpty").not());
		
		blockInfo.getPropertySink().captureProperty(this, ifStatement, targetProperty, structVar);
		
	}

	private void processStructPropertyList(ShowlDirectPropertyShape targetProperty, ShowlStructExpression struct) throws BeamTransformGenerationException {
		ShowlNodeShape targetNode = targetProperty.getValueShape();
		
		for (Entry<URI, ShowlExpression> entry : struct.entrySet()) {
			URI predicate = entry.getKey();
			ShowlExpression e = entry.getValue();
			
			ShowlDirectPropertyShape direct = targetNode.getProperty(predicate);
			processStructField(direct, e);
		}
		
	}

	private void processStructField(ShowlDirectPropertyShape direct, ShowlExpression e) throws BeamTransformGenerationException {
		
		BlockInfo callerBlockInfo = peekBlockInfo();
		URI predicate = direct.getPredicate();
		BeamMethod beamMethod = callerBlockInfo.createMethod(predicate.getLocalName(), model.VOID);
		
		BlockInfo thisBlockInfo = beginBlock(beamMethod);
		try {
		  thisBlockInfo.beamMethod(beamMethod);
			addRowParameters(beamMethod, e);
			addOutputRowParam(beamMethod, direct.getDeclaringShape());
			
			// Declare variables that hold the source values.
			
			declareLocalVariables(direct, e);

			
			if (e instanceof ShowlStructExpression) {
				processStructPropertyList(direct, (ShowlStructExpression) e);
			}

			// For now, we assume that this method should set values on an output TableRow.
			// Later we'll need to handle the case where this method must append values to a List.
			thisBlockInfo.setPropertySink(BeamRowSink.INSTANCE);
			
	
			captureValue(direct, e);
			
			
			
		} finally {
			endBlock();
		}
		
		invoke(beamMethod);
	}
	

	private void addOutputRowParam(BeamMethod beamMethod, ShowlNodeShape node) throws BeamTransformGenerationException {
		
		BlockInfo blockInfo = peekBlockInfo();
		
		JCodeModel model = beamMethod.getMethod().owner();
		AbstractJClass tableRowClass = model.ref(TableRow.class);
		
		
		BeamEnumInfo enumInfo = findEnumInfo();
		if (enumInfo!=null) {
			node = enumInfo.getEnumNode();
		}
		
		ShowlEffectiveNodeShape enode = node.effectiveNode();
		BeamParameter p = beamMethod.addParameter(BeamParameter.ofNodeRow(tableRowClass, "outputRow", enode));
		if (p != null) {
			JVar outputRow = p.getVar();
			blockInfo.putTableRow(enode, outputRow);
			blockInfo.outputRow(outputRow);
		}
	}

	private void declareLocalVariables(ShowlDirectPropertyShape targetProperty, ShowlExpression e) throws BeamTransformGenerationException {
		
		BlockInfo blockInfo = peekBlockInfo();
		JBlock block = blockInfo.getBlock();
		
		if (targetProperty.getValueShape() != null) {
			// Declare a TableRow variable that will hold the values of the value shape.
			AbstractJClass tableRowClass = model.ref(TableRow.class);
			
			String rowName = targetProperty.getPredicate().getLocalName() + "Row";
			
			JVar rowVar = block.decl(tableRowClass, rowName).init(tableRowClass._new());
			blockInfo.putTableRow(targetProperty.getValueShape().effectiveNode(), rowVar);
			
			if (e instanceof ShowlEnumStructExpression) {
				ShowlEnumStructExpression enumStruct = (ShowlEnumStructExpression) e;				

				AbstractJClass valueType = typeManager.enumClass(enumStruct.getEnumNode().getOwlClass().getId());
				blockInfo.putTableRow(enumStruct.getEnumNode().effectiveNode(), rowVar);
				JVar enumValueVar = block.decl(valueType, targetProperty.getPredicate().getLocalName());
				BeamEnumInfo info = new BeamEnumInfo(EnumValueType.OBJECT, enumValueVar, enumStruct.getEnumNode());
				blockInfo.setEnumInfo(info);
				
			}
			
		} else if (e instanceof ShowlEnumPropertyExpression) {

			AbstractJClass objectClass = model.ref(Object.class);

			
			ShowlPropertyShape p = ((ShowlEnumPropertyExpression) e).getSourceProperty();

			// The first parameter of the enclosing method should be the enum object from which we can get field values.
			// Is there a better way to get a reference to this parameter?
			
			JVar enumObject = blockInfo.getBeamMethod().getParameters().get(0).getVar();
			
			String getterName = "get" + StringUtil.capitalize(p.getPredicate().getLocalName());
			
			JInvocation initValue = enumObject.invoke(getterName);
			
			String varName = p.getPredicate().getLocalName();
			JVar var = block.decl(objectClass, varName).init(initValue);
			blockInfo.putPropertyValue(p.asGroup(), var);
			
			
		} else {
			// Declare the source properties
			AbstractJClass objectClass = model.ref(Object.class);
			BeamMethod beamMethod = blockInfo.getBeamMethod();
			
			
			
			Set<ShowlPropertyShape> sourceProperties = beamMethod.getSourceProperties();
			for (ShowlPropertyShape p : sourceProperties) {
				
				
				JVar sourceTableRow = blockInfo.getTableRowVar(p.getDeclaringShape().effectiveNode());
				String varName = p.getPredicate().getLocalName();
				JVar var = block.decl(objectClass, varName);
				var.init(sourceTableRow.invoke("get").arg(JExpr.lit(varName)));
				
				blockInfo.putPropertyValue(p.asGroup(), var);
			}
		}
		
	}

	private void captureValue(ShowlPropertyShape targetProperty, ShowlExpression e) throws BeamTransformGenerationException {
		
		IJExpression value = null;
		BlockInfo blockInfo = peekBlockInfo();
		JBlock block = blockInfo.getBlock();
		
		JConditional ifStatement = null;
		if (e instanceof ShowlEnumStructExpression) {
			ShowlEnumStructExpression enumStruct = (ShowlEnumStructExpression) e;
			BeamEnumInfo enumInfo = blockInfo.getEnumInfo();
			
			JVar valueVar = enumInfo==null ? null : enumInfo.getEnumValue();
			
			if (valueVar == null) {
				fail("Variable for enum value not found at {0}", targetProperty.getPath());
			}
			
			ShowlExpression idExpression = enumStruct.get(Konig.id);
			if (idExpression == null) {
				fail("No expression for konig:id at {0}", targetProperty.getPath());
			}
			value = transform(idExpression);
			valueVar.init(value);
			
			value = blockInfo.getTableRowVar(targetProperty.getValueShape().effectiveNode());
			
			ifStatement = block._if(value.invoke("isEmpty").not());
		} else {
			value = transform(e);
			if (!(value instanceof JVar)) {
				
				AbstractJType valueType =  model.ref(Object.class);
						
				JVar valueVar = block.decl(valueType, targetProperty.getPredicate().getLocalName());
				valueVar.init(value);
				value = valueVar;
			}
			ifStatement = block._if(value.neNull());
		}
		
		

		blockInfo.getPropertySink().captureProperty(this, ifStatement, targetProperty, value);
		
	}

	
	public void addRowParameters(BeamMethod beamMethod, ShowlExpression e) throws BeamTransformGenerationException {
		
		BlockInfo blockInfo = peekBlockInfo();
		
	  // Collect the source properties upon which the expression depends.
		
		Set<ShowlPropertyShape> propertySet = new HashSet<>();
		e.addProperties(propertySet);

		// We need to filter out the Enum properties unless the expression is an enum field.
		// It would be better if we could pass a filter when we 'addProperties'.
		// But since that is not an option, we filter them now.
		
		if (!ShowlUtil.isEnumField(e)) {
			Iterator<ShowlPropertyShape> sequence = propertySet.iterator();
			while (sequence.hasNext()) {
				ShowlPropertyShape p = sequence.next();
				if (ShowlUtil.isEnumProperty(p)) {
					sequence.remove();
				}
			}
		}
		
		beamMethod.setSourceProperties(propertySet);
		
		
		Set<ShowlEffectiveNodeShape> nodeSet = new HashSet<>();
		for (ShowlPropertyShape p : propertySet) {
			if (p.isTargetProperty()) {
				nodeSet.add(p.getRootNode().effectiveNode());
			} else {
				nodeSet.add(p.getDeclaringShape().effectiveNode());
			}
		}
		
		List<ShowlEffectiveNodeShape> nodeList = new ArrayList<>(nodeSet);
		Collections.sort(nodeList, new Comparator<ShowlEffectiveNodeShape>() {

			
			public int compare(ShowlEffectiveNodeShape a, ShowlEffectiveNodeShape b) {
				String aPath = a.canonicalNode().getPath();
				String bPath = b.canonicalNode().getPath();
				
				return aPath.compareTo(bPath);
			}
		});
		
		JMethod method = beamMethod.getMethod();
		
		AbstractJClass tableRowClass = model.ref(TableRow.class);
		for (ShowlEffectiveNodeShape node : nodeList) {
			
			URI owlClass = node.canonicalNode().getOwlClass().getId();
			
			if (reasoner.isEnumerationClass(owlClass)) {
				
				String enumMemberName = 
						StringUtil.firstLetterLowerCase(
								StringUtil.javaIdentifier(node.canonicalNode().getId().stringValue()));
				
				AbstractJType enumType = typeManager.javaType(owlClass);
				
				
				BeamParameter p = beamMethod.addParameter(BeamParameter.ofEnumValue(enumType, enumMemberName, node));
				if (p != null) {
					JVar enumValueParam = p.getVar();
					ShowlNodeShape nodeShape = node.canonicalNode();
					ShowlExpression s = nodeShape.getAccessor().getSelectedExpression();
					if (s instanceof ShowlEnumNodeExpression) {
						ShowlEnumNodeExpression en = (ShowlEnumNodeExpression)s;
						ShowlNodeShape enumNode = en.getEnumNode();
						blockInfo.putEnumMember(enumNode.effectiveNode(), enumValueParam);
					}
					blockInfo.putEnumMember(node, enumValueParam);
				}
				
			} else {
				
				
				String rowName = StringUtil.firstLetterLowerCase(
						StringUtil.javaIdentifier(
								ShowlUtil.shortShapeName(node.canonicalNode()))) + "Row";
								
				BeamParameter p = beamMethod.addParameter(BeamParameter.ofNodeRow(tableRowClass, rowName, node));
				if (p != null) {
					blockInfo.putTableRow(node, p.getVar());
				}
			}
		}
		
	}

	
	private BeamEnumInfo findEnumInfo() {
		for (int i=blockStack.size()-1; i>=0; i--) {
			BlockInfo info = blockStack.get(i);
			BeamEnumInfo enumInfo = info.getEnumInfo();
			if (enumInfo!=null) {
				return enumInfo;
			}
			
		}
		return null;
	}

	
	public JDefinedClass getTargetClass() {
		return targetClass;
	}

	
	public BeamTypeManager getTypeManager() {
		return typeManager;
	}

	
	public TargetPropertyGenerator targetPropertyGenerator(ShowlDirectPropertyShape targetProperty)
			throws BeamTransformGenerationException {
		
		RdfJavaType type = typeManager.rdfJavaType(targetProperty);
		
		
		if (type.isSimpleType()) {
			return new SimplePropertyGenerator(this);
		}
		
		fail("Type of {0} not supported", targetProperty.getPath());
		
		return null;
	}



	public JVar addEnumParamForEnumProperty(BeamMethod beamMethod, ShowlPropertyShape targetProperty) throws BeamTransformGenerationException {
		
		BlockInfo blockInfo = peekBlockInfo();
		ShowlNodeShape enumNode = targetProperty.getDeclaringShape();
		ShowlPropertyShape enumAccessor = enumNode.getAccessor();
		if (enumAccessor == null) {
			fail("enum accesssor is not defined for {0}", targetProperty.getPath());
		}
		String varName = blockInfo.varName(enumAccessor.getPredicate().getLocalName());
		URI enumClassId = enumNode.getOwlClass().getId();
		AbstractJClass enumClass = typeManager.enumClass(enumClassId);
		JVar enumVar = beamMethod.getMethod().param(enumClass, varName);
		
		blockInfo.putEnumMember(enumNode.effectiveNode(), enumVar);
		
		
		return enumVar;
	}
	
	public JVar addTableRowParam(BeamMethod beamMethod, ShowlEffectiveNodeShape node) throws BeamTransformGenerationException {
		BlockInfo blockInfo = peekBlockInfo();
		JVar var = blockInfo.getTableRowVar(node);
		if (var == null) {
			AbstractJClass tableRowClass = model.ref(TableRow.class);
			ShowlNodeShape canonical = node.canonicalNode();
			String baseName = canonical.getAccessor()==null ? 
					ShowlUtil.shortShapeName(canonical) : 
					canonical.getAccessor().getPredicate().getLocalName();
					
			
			String varName = StringUtil.firstLetterLowerCase(baseName) + "Row";
			logger.trace("addTableRowParam: {} ({} {})", beamMethod.getMethod().name(), node.canonicalNode().getPath(), varName);
			
			BeamParameter param  = BeamParameter.ofNodeRow(tableRowClass, varName, node);
			if (beamMethod.addParameter(param)!=null) {
				var = param.getVar();
				blockInfo.putTableRow(node, var);
			}
			
		}
		return var;
		
	}

	
	public JVar addErrorBuilderParam(BeamMethod beamMethod) throws BeamTransformGenerationException {
		
		AbstractJClass errorBuilderClass = typeManager.errorBuilderClass();
		
		BeamParameter param = BeamParameter.ofErrorBuilder(errorBuilderClass);
		beamMethod.addParameter(param);

		JVar var = param.getVar();
		peekBlockInfo().errorBuilderVar(var);
		
		return var;
		
	}


	public JInvocation createInvocation(BeamMethod method) throws BeamTransformGenerationException {
		if (logger.isTraceEnabled()) {
			logger.trace("invoke: {}", method.getMethod().name());
		}
		
		BlockInfo blockInfo = peekBlockInfo();
		
		JInvocation invoke = JExpr.invoke(method.getMethod().name());
		
		for (BeamParameter param : method.getParameters()) {
			switch (param.getParamType()) {
			case TABLE_ROW : {
					ShowlEffectiveNodeShape node = param.getNode();
					JVar var = tableRowVar(node);
					if (var == null) {
						fail("TableRow variable not found for {0}", node.canonicalNode().getPath());
					}
					
					invoke.arg(var);
				}
				break;
				
			case ENUM_VALUE : {
				ShowlEffectiveNodeShape node = param.getNode();
				JVar var = blockInfo.getEnumMember(node);
				if (var == null) {
					fail("Enum member not found for {0}", node.canonicalNode().getPath());
				}
				invoke.arg(var);
			}
			break;
				
			case ERROR_BUILDER :
				invoke.arg(blockInfo.getErrorBuilderVar());
				break;
				
			default:
				fail("Parameter type not supported: {0}", param.getParamType());
			}
		}
		
		return invoke;
	}
	
	
	public JInvocation invoke(BeamMethod method) throws BeamTransformGenerationException {
		JInvocation invoke = createInvocation(method);
		peekBlockInfo().getBlock().add(invoke);
		
		return invoke;
	}

	
	public JVar declarePropertyValue(ShowlPropertyShape targetProperty) throws BeamTransformGenerationException {

		IJExpression fieldValue = transform(targetProperty.getSelectedExpression());
		return declarePropertyValue(targetProperty, fieldValue, null);
	}


	
	public JVar declarePropertyValue(ShowlPropertyShape targetProperty, IJExpression fieldValue, AbstractJType fieldType) throws BeamTransformGenerationException {
		
		
		if (fieldType == null) {
		
			if (Konig.id.equals(targetProperty.getPredicate())) {
				fieldType = model.ref(String.class);
				if (fieldValue instanceof JVar) {
					JVar fieldValueVar = (JVar) fieldValue;
					String fieldValueType = fieldValueVar.type().fullName();
					if (!String.class.getName().equals(fieldValueType)) {
						fieldValue = fieldValue.invoke("getId").invoke("getLocalName");
					}
							
				}
			} else {
				ShowlExpression e = targetProperty.getSelectedExpression();
				fieldType = getTypeManager().javaType(e);
			}
		}
		
		String fieldName = targetProperty.getPredicate().getLocalName();
		
		BlockInfo blockInfo = peekBlockInfo();
		
		
		
		JVar var = blockInfo.getBlock().decl(fieldType, fieldName).init(fieldValue.castTo(fieldType));
		
		blockInfo.putPropertyValue(targetProperty.asGroup(), var);
		
		
		return var;
	}

	
	public OwlReasoner getOwlReasoner() {
		return reasoner;
	}

	
	public JCodeModel codeModel() {
		return model;
	}

	public void addParametersFromPropertySet(BeamMethod beamMethod, ShowlPropertyShape targetProperty, Set<ShowlPropertyShape> set)
			throws BeamTransformGenerationException {

		Set<ShowlEffectiveNodeShape> nodeSet = new HashSet<>();
		
		addNodes(nodeSet, set);
		
		
		
		List<ShowlEffectiveNodeShape> paramList = new ArrayList<>(nodeSet);
		Collections.sort(paramList);

		for (ShowlEffectiveNodeShape node : paramList) {
			addTableRowParam(beamMethod, node);
		}
		
	}

	private void addNodes(Set<ShowlEffectiveNodeShape> nodeSet, Set<ShowlPropertyShape> set) throws BeamTransformGenerationException {
		for (ShowlPropertyShape p : set) {
		
			ShowlPropertyShape q = p.asGroup().direct();
			if (q != null) {
				p = q;
			} else {
				p = p.maybeDirect();
			}
			ShowlNodeShape enumNode = ShowlUtil.parentEnumNode(p, reasoner);
			if (enumNode == null) {
				ShowlEffectiveNodeShape node = p.getDeclaringShape().effectiveNode();
				nodeSet.add(node);
			}
		}
		
	}


	public ShowlStatement enumJoinStatement(ShowlNodeShape enumNode) throws BeamTransformGenerationException {
		
		ShowlEnumNodeExpression e = null;
		
		ShowlPropertyShape targetProperty = enumNode.getTargetProperty();
		if (targetProperty != null) {
			ShowlExpression s = targetProperty.getSelectedExpression();
			if (s instanceof ShowlEnumNodeExpression) {
				e = (ShowlEnumNodeExpression) s;
			}
		}
		
		if (e == null) {
			ShowlPropertyShape accessor = enumNode.getAccessor();
			if (accessor != null) {
				ShowlExpression s = accessor.getSelectedExpression();
				if (s instanceof ShowlEnumNodeExpression) {
					e = (ShowlEnumNodeExpression) s;
				}
			}
		}
		
		if (e != null) {

			ShowlChannel channel = e.getChannel();
			if (channel != null && channel.getJoinStatement()!=null) {
				return channel.getJoinStatement();	
			}
		}
		throw new BeamTransformGenerationException("Failed to find join statement for " + enumNode.getPath());
	}


	public void addTableRowParameters(BeamMethod beamMethod, ShowlPropertyShape targetProperty) throws BeamTransformGenerationException {
		
		ShowlExpression e = targetProperty.getSelectedExpression();
		if (e == null) {
			fail("Cannot add TableRow parameters because selectedExpression not found at {0}", targetProperty.getPath());
		}
		
		Set<ShowlPropertyShape> set = new HashSet<>();
		e.addProperties(set);
		
		addParametersFromPropertySet(beamMethod, targetProperty, set);
		
	}


	public void addOutputRowAndErrorBuilderParams(BeamMethod beamMethod, ShowlPropertyShape targetProperty) 
			throws BeamTransformGenerationException {
		addErrorBuilderParam(beamMethod);
		addTableRowParam(beamMethod, targetProperty.getDeclaringShape().effectiveNode());
	}

	public void generateSourceProperty(ShowlPropertyShape p) throws BeamTransformGenerationException {
		
		BlockInfo blockInfo = peekBlockInfo();
		RdfJavaType type = typeManager.rdfJavaType(p);
		AbstractJType javaType = type.getJavaType();
		
		String fieldName = p.getPredicate().getLocalName();
		
		JVar tableRow = blockInfo.getTableRowVar(p.getDeclaringShape().effectiveNode());
		IJExpression fieldValue = tableRow.invoke("get").arg(JExpr.lit(fieldName)).castTo(javaType);
		JVar var = blockInfo.getBlock().decl(javaType, fieldName).init(fieldValue);
		
		blockInfo.putPropertyValue(p.asGroup(), var);
	}
	
	public void addParametersFromInvocationSet(BeamMethod beamMethod) throws BeamTransformGenerationException {
				
		List<BeamParameter> temp = new ArrayList<>();
		for (BeamMethod invoked : beamMethod.getInvocationSet()) {
			for (BeamParameter param : invoked.getParameters()) {
				if (beamMethod.accept(param) && !contains(temp, param)) {
					temp.add(param);
				}
			}
		}
		
		Collections.sort(temp);
		
		BlockInfo blockInfo = peekBlockInfo();
		
		for (BeamParameter param : temp) {
			BeamParameter copy = beamMethod.copyParam(param);
			
			switch (copy.getParamType()) {
			case ENUM_VALUE:
				blockInfo.putEnumMember(copy.getSourceNode().effectiveNode(), copy.getVar());
				break;
				
			case ERROR_BUILDER :
				blockInfo.errorBuilderVar(copy.getVar());
				break;
				
			case LIST_VALUE :
				// Do nothing
				break;
				
			case TABLE_ROW :
				blockInfo.putTableRow(copy.getSourceNode().effectiveNode(), copy.getVar());
				break;
				
			}
		}
	}

	private boolean contains(List<BeamParameter> list, BeamParameter param) {
		for (BeamParameter prior : list) {
			if (prior.matches(param)) {
				return true;
			}
		}
		return false;
	}
	

	@SuppressWarnings("unchecked")
	public Collection<ShowlDirectPropertyShape> sortProperties(ShowlNodeShape targetNode) {
		
		Collection<ShowlDirectPropertyShape> list = targetNode.getProperties();
		if (!hasFormula(list)) {
			return list;
		}
		
		List<PropertyDependencies> dependList = new ArrayList<>();
		for (ShowlDirectPropertyShape p : list) {
			Set<ShowlPropertyShape> set = null;
			ShowlExpression formula = p.getFormula();
			if (formula == null) {
				set = Collections.EMPTY_SET;
			} else {
				set = new HashSet<>();
				formula.addProperties(set);
			}
			dependList.add(new PropertyDependencies(p, set));
		}
		
		Collections.sort(dependList);
		
		List<ShowlDirectPropertyShape> result = new ArrayList<>();
		for (PropertyDependencies p : dependList) {
			result.add(p.getTargetProperty());
		}
		
		
		return result;
	}

	
	private boolean hasFormula(Collection<ShowlDirectPropertyShape> list) {
		for (ShowlDirectPropertyShape direct : list) {
			if (direct.getFormula()!=null) {
				return true;
			}
		}
		return false;
	}

}
