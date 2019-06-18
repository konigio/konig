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
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
import io.konig.core.showl.ShowlCaseStatement;
import io.konig.core.showl.ShowlContainmentOperator;
import io.konig.core.showl.ShowlEnumIndividualReference;
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
import io.konig.formula.FunctionExpression;
import io.konig.formula.FunctionModel;

public class BeamExpressionTransformImpl implements BeamExpressionTransform {

	private OwlReasoner reasoner;
	private BeamPropertyManager propertyManager;
	private BeamTypeManager typeManager;
	private JCodeModel model;
	private JDefinedClass targetClass;
	
	private JMethod concatMethod;
	private JMethod localNameMethod;
	private JMethod stripSpacesMethod;
	private JMethod pathGetter;
	private int caseCount = 0;
	private ArrayDeque<BlockInfo> blockStack;

	
	public BeamExpressionTransformImpl(
			OwlReasoner reasoner,
			BeamTypeManager typeManager,
			BeamPropertyManager manager, 
			JCodeModel model, 
			JDefinedClass targetClass) {
		this.reasoner = reasoner;
		this.typeManager = typeManager;
		this.propertyManager = manager;
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
		
		throw new BeamTransformGenerationException("Failed to tranform " + e.toString());
	}

	private IJExpression property(ShowlPropertyExpression e) throws BeamTransformGenerationException {

		BlockInfo blockInfo = peekBlockInfo();
		if (blockInfo != null) {
			
			ShowlPropertyShape p = e.getSourceProperty();
			if (p.isTargetProperty()) {
				return invokePathGetter(p);
			} else {
				fail("Don't know how to get source property {0}", p.getPath());
			}
			
		}

		BeamSourceProperty b = propertyManager.forPropertyShape(e.getSourceProperty());
		return b.getVar();
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
		JBlock block = blockInfo.block;
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

	private BlockInfo peekBlockInfo() {
		return blockStack==null || blockStack.isEmpty() ? null : blockStack.getLast();
	}

	private IJExpression caseStatement(ShowlCaseStatement e) throws BeamTransformGenerationException {
		caseCount++;
		String methodName = "case" + caseCount;
		
		URI valueType = e.valueType(reasoner);
		AbstractJType resultType = typeManager.javaType(valueType);
		AbstractJType errorBuilderClass = typeManager.errorBuilderClass();
		
		
		JMethod method = targetClass.method(JMod.PRIVATE, resultType, methodName);

		
		String resultParamName = StringUtil.firstLetterLowerCase(valueType.getLocalName());
		JVar resultParam = method.body().decl(resultType, resultParamName).init(JExpr._null());
		
		List<ShowlPropertyShape> propertyList = caseParamList(e);
		Map<ShowlNodeShape, NodeTableRow> tableRowMap = tableRowMap(method, propertyList);
		
		JVar errorBuilderVar = method.param(errorBuilderClass, "errorBuilder");
		
		BlockInfo blockInfo = beginBlock(method.body())
				.nodeTableRowMap(tableRowMap)
				.enumValueType(EnumValueType.OBJECT)
				.errorBuilderVar(errorBuilderVar);
		
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
		
		JInvocation invoke = JExpr.invoke(method);
		
		
		return resultParam;
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
			.enumValueType(EnumValueType.OBJECT)
			.errorBuilderVar(errorBuilderVar);
		
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
		
		endBlock();
		
		return invocation;
	}

	private void endBlock() {
		blockStack.removeLast();
	}

	private BlockInfo beginBlock(JBlock block) {
		if (blockStack == null) {
			blockStack = new ArrayDeque<>();
		}
		BlockInfo info = new BlockInfo(block);
		blockStack.addLast(info);
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
		return longClass._new().arg(dateClass._new().invoke("getTime"));
	}

	private IJExpression enumIndividualReference(ShowlEnumIndividualReference e) throws BeamTransformGenerationException {
		URI iri = e.getIriValue();
		JStringLiteral localName = JExpr.lit(iri.getLocalName());
		BlockInfo blockInfo = peekBlockInfo();
		if (blockInfo != null && blockInfo.getEnumValueType()==EnumValueType.OBJECT) {
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
	
	private static class BlockInfo {
		JBlock block;
		int setCount=0;
		int valueCount = 0;
		Map<ShowlNodeShape, NodeTableRow> nodeTableRowMap;
		JVar errorBuilderVar;
		EnumValueType enumValueType = EnumValueType.LOCAL_NAME;
		
		
		public BlockInfo(JBlock block) {
			this.block = block;
		}

		public BlockInfo nodeTableRowMap(Map<ShowlNodeShape, NodeTableRow> tableRowMap) {
			this.nodeTableRowMap = tableRowMap;
			return this;
			
		}


		public EnumValueType getEnumValueType() {
			return enumValueType;
		}

		public BlockInfo enumValueType(EnumValueType enumValueType) {
			this.enumValueType = enumValueType;
			return this;
		}

		public NodeTableRow getNodeTableRow(ShowlNodeShape node) throws BeamTransformGenerationException {
			NodeTableRow result = nodeTableRowMap == null ? null : nodeTableRowMap.get(node);
			if (result == null) {
				throw new BeamTransformGenerationException("NodeTableRow not found for " + node.getPath());
			}
			
			return result;
		}

		public JVar getErrorBuilderVar() {
			return errorBuilderVar;
		}

		public BlockInfo errorBuilderVar(JVar errorBuilderVar) {
			this.errorBuilderVar = errorBuilderVar;
			return this;
		}

		public Map<ShowlNodeShape, NodeTableRow> getNodeTableRowMap() {
			return nodeTableRowMap;
		}

		public String valueName(ShowlExpression e) {
			if (e instanceof ShowlPropertyExpression) {
				ShowlPropertyShape p = ((ShowlPropertyExpression) e).getSourceProperty();
				return valueName(p);
			}
			return ++valueCount==1 ? "value" : "value" + valueCount;
		}

		private String valueName(ShowlPropertyShape p) {
			List<String> nameParts = new ArrayList<>();
			while (p != null) {
				nameParts.add(p.getPredicate().getLocalName());
				p = p.getDeclaringShape().getAccessor();
			}
			Collections.reverse(nameParts);
			
			StringBuilder builder = new StringBuilder();
			String delim = "";
			for (String text : nameParts) {
				builder.append(delim);
				delim = "_";
				builder.append(text);
			}
			
			return builder.toString();
		}

		public String nextSetName() {
			return ++setCount==1 ? "set" : "set" + setCount;
		}

		
	}
	
	private enum EnumValueType {
		OBJECT,
		LOCAL_NAME;
	}

}
