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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.XMLSchema;

import com.google.api.services.bigquery.model.TableRow;
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
import com.helger.jcodemodel.JStringLiteral;
import com.helger.jcodemodel.JVar;

import io.konig.core.OwlReasoner;
import io.konig.core.showl.ShowlBinaryRelationalExpression;
import io.konig.core.showl.ShowlCaseStatement;
import io.konig.core.showl.ShowlContainmentOperator;
import io.konig.core.showl.ShowlDirectPropertyShape;
import io.konig.core.showl.ShowlEnumIndividualReference;
import io.konig.core.showl.ShowlEnumPropertyExpression;
import io.konig.core.showl.ShowlEnumStructExpression;
import io.konig.core.showl.ShowlExpression;
import io.konig.core.showl.ShowlFilterExpression;
import io.konig.core.showl.ShowlFunctionExpression;
import io.konig.core.showl.ShowlIriReferenceExpression;
import io.konig.core.showl.ShowlListRelationalExpression;
import io.konig.core.showl.ShowlNodeShape;
import io.konig.core.showl.ShowlPropertyExpression;
import io.konig.core.showl.ShowlPropertyShape;
import io.konig.core.showl.ShowlStructExpression;
import io.konig.core.showl.ShowlSystimeExpression;
import io.konig.core.showl.ShowlUtil;
import io.konig.core.showl.ShowlWhenThenClause;
import io.konig.core.showl.expression.ShowlLiteralExpression;
import io.konig.core.util.StringUtil;
import io.konig.core.vocab.Konig;
import io.konig.formula.FunctionExpression;
import io.konig.formula.FunctionModel;

public class BeamExpressionTransformImpl implements BeamExpressionTransform {

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

	
	public BeamExpressionTransformImpl(
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

	@Override
	public IJExpression transform(ShowlExpression e) throws BeamTransformGenerationException {
		
		if (e instanceof ShowlLiteralExpression) {

      Literal literal = ((ShowlLiteralExpression) e).getLiteral();
      return literalFactory.javaExpression(literal);
		} 
		
		if (e instanceof ShowlPropertyExpression) {
			return property((ShowlPropertyExpression)e);
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
		
		throw new BeamTransformGenerationException("Failed to tranform " + e.toString());
	}

	private IJExpression binaryRelationalExpression(ShowlBinaryRelationalExpression e) throws BeamTransformGenerationException {
		IJExpression left = transform(e.getLeft());
		IJExpression right = transform(e.getRight());
		
		BlockInfo blockInfo = peekBlockInfo();
		JBlock block = blockInfo.getBlock();
		
		AbstractJClass objectClass = model.ref(Object.class);
		
		JVar leftVar = block.decl(objectClass, nextVarName()).init(left);
		
		
		switch (e.getOperator()) {
		case EQUALS :
			
			if (treatNullAsFalse) {
				return JExpr.cond(leftVar.neNull().cand(leftVar.invoke("equals").arg(right)), leftVar, JExpr.FALSE);
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
			return invokePathGetter(p);
		} 
			

		BeamSourceProperty b = blockInfo.getBeamSourceProperty(p);
		if (b != null) {
			return b.getVar();
		}
		
		// There is no pre-declared variable for this property.  Try getting it directly from a TableRow.
		
		NodeTableRow nodeRow = blockInfo.getNodeTableRow(p.getRootNode());
		return nodeRow.getTableRowVar().invoke("get").arg(JExpr.lit(p.getPredicate().getLocalName()));
		
	}

	

	private IJExpression invokePathGetter(ShowlPropertyShape p) throws BeamTransformGenerationException {
		BlockInfo info = peekBlockInfo();
		
		ShowlNodeShape rootNode = p.getRootNode();
		NodeTableRow rowInfo = info.getNodeTableRow(rootNode);
		

		List<String> fieldList = new ArrayList<>();
		while (p != null) {
			fieldList.add(p.getPredicate().getLocalName());
			p = p.getDeclaringShape().getAccessor();
		}
		Collections.reverse(fieldList);
		
		JMethod pathGetter = pathGetter();
		
		JInvocation result = JExpr.invoke(pathGetter);
		result.arg(rowInfo.getTableRowVar());
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
		
		
		String valueName = blockInfo.valueName(left);
		JVar value = block.decl(objectClass, valueName).init(transform(left));
		
		IJExpression result = set.invoke("contains").arg(value);
		if (e.getOperator() == ShowlContainmentOperator.NOT_IN) {
			result.not();
		}
	
		return result;
	}

	@Override
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
		AbstractJType errorBuilderClass = typeManager.errorBuilderClass();
		
		
		
		JMethod method = targetClass.method(JMod.PRIVATE, resultType, methodName);

		
		String resultParamName = StringUtil.firstLetterLowerCase(valueType.getLocalName()) + "Value";
		JVar resultParam = method.body().decl(resultType, resultParamName).init(JExpr._null());
		
		List<ShowlPropertyShape> propertyList = caseParamList(e);
		Map<ShowlNodeShape, NodeTableRow> tableRowMap = tableRowMap(method, propertyList);
		
		JVar errorBuilderVar = method.param(errorBuilderClass, "errorBuilder");
		
		BlockInfo blockInfo = beginBlock(method.body())
				.nodeTableRowMap(tableRowMap)
				.errorBuilderVar(errorBuilderVar);
		
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
			JInvocation condition = whenExpression(whenMethodName, whenExpression, blockInfo);
			
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
		
		endBlock();
		
		method.body()._return(resultParam);
		
		// Next up, generate an invocation of the method that we just created.
		
		// Start by getting information about the caller's block.

		BlockInfo callerBlock = peekBlockInfo();
		
		JInvocation invoke = JExpr.invoke(method);
		for (NodeTableRow paramInfo : tableRowMap.values()) {
			ShowlNodeShape node = paramInfo.getNode();
			NodeTableRow  callerParamInfo = callerBlock.getNodeTableRow(node);
			invoke.arg(callerParamInfo.getTableRowVar());
		}
		
		JVar callerErrorBuilder = callerBlock.getErrorBuilderVar();
		if (callerErrorBuilder == null) {
			fail("ErrorBuilder variable not defined while invoking {0}", e.displayValue());
		}
		invoke.arg(callerErrorBuilder);
		
		return invoke;
	}


	private JInvocation whenExpression(String whenMethodName, ShowlExpression whenExpression, BlockInfo callerBlock ) throws BeamTransformGenerationException {
		AbstractJType booleanType = model._ref(boolean.class);
		
		JMethod method = targetClass.method(JMod.PRIVATE, booleanType, whenMethodName);
		
		JInvocation invocation = JExpr.invoke(method);
		
		Set<ShowlPropertyShape> propertyParameters = new HashSet<>();
		
		whenExpression.addProperties(propertyParameters);
		
		Map<ShowlNodeShape, NodeTableRow> thisTableRowMap = tableRowMap(method, propertyParameters);
		AbstractJClass errorBuilderClass = typeManager.errorBuilderClass();
		
		JVar errorBuilderVar = method.param(errorBuilderClass, "errorBuilder");
		beginBlock(method.body())
			.nodeTableRowMap(thisTableRowMap)
			.errorBuilderVar(errorBuilderVar);

		boolean treatValue = treatNullAsFalse(true);
		try {
		
			for (NodeTableRow param : thisTableRowMap.values()) {
				NodeTableRow argValue = callerBlock.getNodeTableRowMap().get(param.getNode());
				if (argValue == null) {
					fail("While building WHEN expression {0}, argument not found: {1}", 
							whenExpression.displayValue(), param.getNode().getPath());
				}
				invocation.arg(argValue.getTableRowVar());
			}
			invocation.arg(callerBlock.getErrorBuilderVar());
			
			IJExpression result = transform(whenExpression);
			
			method.body()._return(result);
		} finally {
			treatNullAsFalse(treatValue);
			endBlock();
		}
		
		
		return invocation;
	}

	private boolean treatNullAsFalse(boolean value) {
		boolean result = treatNullAsFalse;
		treatNullAsFalse = value;
		return result;
	}

	@Override
	public void endBlock() {
		blockStack.remove(blockStack.size()-1);
	}

	@Override
	public BlockInfo beginBlock(JBlock block) {
		if (blockStack == null) {
			blockStack = new ArrayList<>();
		}
		BlockInfo info = new BlockInfo(block);
		blockStack.add(info);
		return info;
	}

	private Map<ShowlNodeShape, NodeTableRow> tableRowMap(JMethod method, Collection<ShowlPropertyShape> propertyList) {
		Set<ShowlNodeShape> nodeSet = new HashSet<>();
		for (ShowlPropertyShape p : propertyList) {
			ShowlNodeShape node = p.getRootNode();
			nodeSet.add(node);
		}
		
		List<ShowlNodeShape> nodeList = new ArrayList<>(nodeSet);
		Collections.sort(nodeList, new BeamNodeComparator());
		
		AbstractJClass tableRowClass = model.ref(TableRow.class);
		List<NodeTableRow> tableRowList = new ArrayList<>(nodeList.size());
		for (ShowlNodeShape node : nodeList) {
			String paramName = StringUtil.firstLetterLowerCase(ShowlUtil.shortShapeName(node)) + "Row";
			JVar var = method.param(tableRowClass, paramName);
			tableRowList.add(new NodeTableRow(node, var));
		}
		
		Map<ShowlNodeShape,NodeTableRow> map = new LinkedHashMap<>();
		for (NodeTableRow tableRow : tableRowList) {
			map.put(tableRow.getNode(), tableRow);
		}
		
		return map;
	}

	private List<ShowlPropertyShape> caseParamList(ShowlCaseStatement caseStatement) {
		Set<ShowlPropertyShape> propertySet = new HashSet<>();
		caseStatement.addProperties(propertySet);
		List<ShowlPropertyShape> propertyList = new ArrayList<>(propertySet);
		
		return propertyList;
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

	private IJExpression struct(ShowlStructExpression e) {
		// TODO Auto-generated method stub
		return null;
	}

	private void fail(String pattern, Object... arg) throws BeamTransformGenerationException {
		String msg = MessageFormat.format(pattern, arg);
		throw new BeamTransformGenerationException(msg);
		
	}
	

	@Override
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
			blockInfo.addNodeTableRow(new NodeTableRow(enumNode, structVar));
		} else {
			blockInfo.addNodeTableRow(new NodeTableRow(targetProperty.getValueShape(), structVar));
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
		BlockInfo thisBlockInfo = beginBlock(beamMethod.getMethod().body());
		
		try {
		  thisBlockInfo.beamMethod(beamMethod);
		  thisBlockInfo.setDefinedClass(callerBlockInfo.getDefinedClass());
		  thisBlockInfo.setTypeManager(typeManager);
		 
			addRowParameters(beamMethod, e);
			addOutputRowParam(beamMethod, direct.getDeclaringShape());
			 if(!hasErrorBuilderParam(beamMethod)){
					JVar errorBuilder = beamMethod.getMethod().param(typeManager.errorBuilderClass(), "errorBuilder");				
					beamMethod.addParameter(BeamParameter.ofErrorBuilder(errorBuilder));
					thisBlockInfo.errorBuilderVar(errorBuilder);
				}
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
		
		callerBlockInfo.invoke(beamMethod);
	}
	

	private void addOutputRowParam(BeamMethod beamMethod, ShowlNodeShape node) throws BeamTransformGenerationException {
		
		BlockInfo blockInfo = peekBlockInfo();
		
		JCodeModel model = beamMethod.getMethod().owner();
		AbstractJClass tableRowClass = model.ref(TableRow.class);
		
		JVar outputRow = beamMethod.getMethod().param(tableRowClass, "outputRow");
		blockInfo.outputRow(outputRow);
		
		BeamEnumInfo enumInfo = findEnumInfo();
		if (enumInfo!=null) {
			node = enumInfo.getEnumNode();
		}
		
		beamMethod.addParameter(BeamParameter.ofTargetRow(outputRow, node));
		
		blockInfo.addNodeTableRow(new NodeTableRow(node, outputRow));
		
		
		
		
	}

	private void declareLocalVariables(ShowlDirectPropertyShape targetProperty, ShowlExpression e) throws BeamTransformGenerationException {
		
		BlockInfo blockInfo = peekBlockInfo();
		JBlock block = blockInfo.getBlock();
		
		if (targetProperty.getValueShape() != null) {
			// Declare a TableRow variable that will hold the values of the value shape.
			AbstractJClass tableRowClass = model.ref(TableRow.class);
			
			String rowName = targetProperty.getPredicate().getLocalName() + "Row";
			
			JVar rowVar = block.decl(tableRowClass, rowName).init(tableRowClass._new());
			blockInfo.addNodeTableRow(new NodeTableRow(targetProperty.getValueShape(), rowVar));
			
			if (e instanceof ShowlEnumStructExpression) {
				ShowlEnumStructExpression enumStruct = (ShowlEnumStructExpression) e;				

				AbstractJClass valueType = typeManager.enumClass(enumStruct.getEnumNode().getOwlClass().getId());
				blockInfo.addNodeTableRow(new NodeTableRow(enumStruct.getEnumNode(), rowVar));
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
			blockInfo.add(new BeamSourceProperty(p, var));
			
			
		} else {
			// Declare the source properties
			AbstractJClass objectClass = model.ref(Object.class);
			BeamMethod beamMethod = blockInfo.getBeamMethod();
			
			
			
			Set<ShowlPropertyShape> sourceProperties = beamMethod.getSourceProperties();
			for (ShowlPropertyShape p : sourceProperties) {
				
				JVar sourceTableRow = blockInfo.getNodeTableRow(p.getDeclaringShape()).getTableRowVar();
				String varName = p.getPredicate().getLocalName();
				JVar var = block.decl(objectClass, varName);
				var.init(sourceTableRow.invoke("get").arg(JExpr.lit(varName)));
				
				blockInfo.add(new BeamSourceProperty(p, var));
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
			
			value = blockInfo.getNodeTableRow(targetProperty.getValueShape()).getTableRowVar();
			
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

	@Override
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
		
		
		Set<ShowlNodeShape> nodeSet = new HashSet<>();
		for (ShowlPropertyShape p : propertySet) {
			nodeSet.add(p.getDeclaringShape());
		}
		
		List<ShowlNodeShape> nodeList = new ArrayList<>(nodeSet);
		Collections.sort(nodeList, new Comparator<ShowlNodeShape>() {

			@Override
			public int compare(ShowlNodeShape a, ShowlNodeShape b) {
				String aPath = a.getPath();
				String bPath = b.getPath();
				
				return aPath.compareTo(bPath);
			}
		});
		
		JMethod method = beamMethod.getMethod();
		JCodeModel model = method.owner();
		
		AbstractJClass tableRowClass = model.ref(TableRow.class);
		for (ShowlNodeShape node : nodeList) {
			
			if (e instanceof ShowlEnumPropertyExpression) {
				BeamEnumInfo enumInfo = findEnumInfo();
				JVar enumValueVar = enumInfo==null ? null : enumInfo.getEnumValue();
				if (enumValueVar == null) {
					fail("Enum value not found in call stack while generating {0}", beamMethod.getMethod().name());
				}
				
				JVar enumValueParam = method.param(enumValueVar.type(), enumValueVar.name());
				beamMethod.addParameter(BeamParameter.ofEnumValue(enumValueParam));
			} else {
				String rowName = StringUtil.firstLetterLowerCase(ShowlUtil.shortShapeName(node) + "Row");
				JVar param = method.param(tableRowClass, rowName);
				blockInfo.addNodeTableRow(new NodeTableRow(node, param));
				beamMethod.addParameter(BeamParameter.ofSourceRow(param, node));
			}
			if(!hasErrorBuilderParam(beamMethod)){
				JVar errorBuilder = method.param(typeManager.errorBuilderClass(), "errorBuilder");				
				beamMethod.addParameter(BeamParameter.ofErrorBuilder(errorBuilder));
				blockInfo.errorBuilderVar(errorBuilder);
			}
		}
		
	}
	private boolean hasErrorBuilderParam(BeamMethod beamMethod) {
		for (BeamParameter param : beamMethod.getParameters()) {
			if(param.getParamType().equals(BeamParameterType.ERROR_BUILDER)){
				return true;
			}					
		}
		return false;
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
}
