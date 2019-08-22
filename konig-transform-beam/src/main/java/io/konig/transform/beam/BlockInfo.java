package io.konig.transform.beam;

import java.text.MessageFormat;

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


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.jcodemodel.AbstractJClass;
import com.helger.jcodemodel.AbstractJType;
import com.helger.jcodemodel.JBlock;
import com.helger.jcodemodel.JCodeModel;
import com.helger.jcodemodel.JDefinedClass;
import com.helger.jcodemodel.JExpr;
import com.helger.jcodemodel.JInvocation;
import com.helger.jcodemodel.JMethod;
import com.helger.jcodemodel.JMod;
import com.helger.jcodemodel.JVar;

import io.konig.core.showl.ShowlEffectiveNodeShape;
import io.konig.core.showl.ShowlExpression;
import io.konig.core.showl.ShowlNodeShape;
import io.konig.core.showl.ShowlPropertyExpression;
import io.konig.core.showl.ShowlPropertyShape;
import io.konig.core.showl.ShowlPropertyShapeGroup;

public class BlockInfo {

	private static Logger logger = LoggerFactory.getLogger(BlockInfo.class);

	private JBlock block;
	private int setCount=0;
	private int valueCount = 0;
	
	private Map<ShowlEffectiveNodeShape, JVar> tableRowMap = new HashMap<>();
	private JVar listVar;
	private JVar outputRow;
	private JVar errorBuilderVar;
	private JVar optionsVar;
	private BeamEnumInfo enumInfo;
	
	private BeamMethod beamMethod;
	private BeamPropertySink propertySink;

	private Map<ShowlPropertyShapeGroup, JVar> propertyValueMap = new HashMap<>();
	
	private Map<ShowlEffectiveNodeShape, JVar> enumMemberMap = new HashMap<>();
	private Map<JVar,JVar> varMap = new HashMap<>();
	
	
	public BlockInfo(JBlock block) {
		this.block = block;
	}

	public BeamMethod getBeamMethod() {
		return beamMethod;
	}

	public BlockInfo beamMethod(BeamMethod beamMethod) {
		this.beamMethod = beamMethod;
		return this;
	}
	
	

	public void setBlock(JBlock block) {
		this.block = block;
	}

	/**
	 * The row that this block is expected to populate.
	 */
	public JVar getOutputRow() {
		return outputRow;
	}

	/**
	 * Set the row that this block is expected to populate
	 */
	public BlockInfo outputRow(JVar outputRow) {
		this.outputRow = outputRow;
		return this;
	}

	/**
	 * Get the List to which this block will add elements.
	 */
	public JVar getListVar() {
		return listVar;
	}

	/**
	 * Set the list to which this block will add elements.
	 */
	public void setListVar(JVar listVar) {
		this.listVar = listVar;
	}

	
	
	
	public BeamEnumInfo getEnumInfo() {
		return enumInfo;
	}

	public void setEnumInfo(BeamEnumInfo enumInfo) {
		this.enumInfo = enumInfo;
	}

	public JVar getOptionsVar() throws BeamTransformGenerationException {
		return optionsVar;
	}

	public void setOptionsVar(JVar optionsVar) {
		this.optionsVar = optionsVar;
	}

	public JVar getErrorBuilderVar() throws BeamTransformGenerationException {
		if (errorBuilderVar == null) {
			throw new BeamTransformGenerationException("ErrorBuilder variable not declared");
		}
		return errorBuilderVar;
	}

	public BlockInfo errorBuilderVar(JVar errorBuilderVar) {
		this.errorBuilderVar = errorBuilderVar;
		return this;
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

	public JBlock getBlock() {
		return block;
	}


	

//	public void invoke(BeamMethod beamMethod) throws BeamTransformGenerationException {
//		JMethod method = beamMethod.getMethod();
//		
//		JInvocation invoke = JExpr.invoke(method);
//		for (BeamParameter param : beamMethod.getParameters()) {
//			switch (param.getParamType()) {
//			
//			case ENUM_VALUE :
//				
//				if (enumInfo==null || enumInfo.getEnumValue() == null) {
//					throw new BeamTransformGenerationException("Cannot invoke " + method.name() + " because the call does not declare a variable that holds the enum value");
//				}
//				invoke.arg(enumInfo.getEnumValue());
//				break;
//				
//			case ERROR_BUILDER :
//				
//				if (errorBuilderVar == null) {
//					throw new BeamTransformGenerationException("Cannot invoke " + method.name() + " because the caller block does not declare the errorBuilder variable.");
//				}
//				invoke.arg(errorBuilderVar);
//				break;
//
//			case TARGET_TABLE_ROW:
//			case SOURCE_TABLE_ROW :
//				ShowlNodeShape node = param.getSourceNode();
//				NodeTableRow rowInfo = maybeNullNodeTableRow(node);
//				
//				if (rowInfo == null || rowInfo.getTableRowVar()==null) {
//					throw new BeamTransformGenerationException(
//							"Cannot invoke " + method.name() + " because the caller block does not declare a TableRow for " + node.getPath());
//				}
//				invoke.arg(rowInfo.getTableRowVar());
//				break;
//				
//			case LIST_VALUE :
//				if (listVar == null) {
//					throw new BeamTransformGenerationException(
//							"Cannot invoke " + method.name() + " because the caller block does not declare the List variable");
//				}
//				invoke.arg(listVar);
//				break;
//				
//				
//			}
//		}
//		
//		block.add(invoke);
//		
//	}

	public void addListParam(BeamMethod beamMethod, AbstractJType paramType, String paramName) throws BeamTransformGenerationException {
		BeamParameter param = beamMethod.addListParam(paramType, paramName);
		listVar = param.getVar();
		propertySink = new BeamListSink(listVar);
	}

	public BeamPropertySink getPropertySink() throws BeamTransformGenerationException {
		if (propertySink == null) {
			throw new BeamTransformGenerationException("propertySink is null");
		}
		return propertySink;
	}

	public void setPropertySink(BeamPropertySink propertySink) {
		this.propertySink = propertySink;
	}

	public BeamMethod createMethod(String localName, AbstractJType returnType) throws BeamTransformGenerationException {
		if (beamMethod == null) {
			String message = MessageFormat.format("Cannot create method ''{0}'' because caller method is not defined", localName);
			throw new BeamTransformGenerationException(message);
		}
		
		
		StringBuilder builder = new StringBuilder();
		builder.append(beamMethod.name());
		builder.append('_');
		builder.append(localName);
		
		JDefinedClass theClass = beamMethod.getMethod().owningClass();
		JMethod method = theClass.method(JMod.PRIVATE, returnType, builder.toString());
		
		BeamMethod result = new BeamMethod(method);
		return result;
	}

	public void putTableRow(ShowlEffectiveNodeShape node, JVar tableRowVar) {
//		if (logger.isTraceEnabled()) {
//			logger.trace("putTableRow: {} (hashCode={})", node.canonicalNode().getPath(), node.hashCode());
//		}
		tableRowMap.put(node, tableRowVar);
	}

	
	public JVar getTableRowVar(ShowlEffectiveNodeShape node) {
		JVar var = tableRowMap.get(node);
	
//		if (var == null) {
//			logger.warn("TableRow variable not found for {} (hashCode={})", node.canonicalNode().getPath() , node.hashCode());
//		}
		return var;
	}


	public JVar getPropertyValue(ShowlPropertyShapeGroup group) {
		return propertyValueMap.get(group);
	}
	
	public void putPropertyValue(ShowlPropertyShapeGroup group, JVar value) {
		propertyValueMap.put(group, value);
	}

	public void putEnumMember(ShowlEffectiveNodeShape enumNode, JVar enumVar) {
		enumMemberMap.put(enumNode, enumVar);
		if (logger.isTraceEnabled()) {
			logger.trace("putEnumMember({}, hashCode={})", enumNode.canonicalNode().getPath(), enumNode.hashCode());
			System.out.print("");
		}
	}
	
	public JVar getEnumMember(ShowlEffectiveNodeShape enumNode) throws BeamTransformGenerationException {
		if (enumNode==null) {
			throw new BeamTransformGenerationException("enumNode is null");
		}
		JVar result = enumMemberMap.get(enumNode);
		if (result == null && logger.isTraceEnabled()) {
			logger.trace("getEnumMember({}, hashCode={})... enum member not found", 
					enumNode.canonicalNode().getPath(), 
					enumNode.hashCode());
		}
		return result;
	}

	public String varNameFor(ShowlNodeShape enumNode) throws BeamTransformGenerationException {
		
		ShowlPropertyShape p = enumNode.getTargetProperty();
		if (p == null) {
			throw new BeamTransformGenerationException(
					"targetProperty is not defined for " + enumNode.getPath());
		}
		
		return varName(p.getPredicate().getLocalName());
	}

	public String varName(String baseName) throws BeamTransformGenerationException {
		
		for (int i=0; i<1000; i++) {
			String varName = i==0 ? baseName : baseName + i;
			
			if (findVarByName(varName) == null) {
				return varName;
			}
		}
		throw new BeamTransformGenerationException("Cannot create variable with base name " + baseName);
	}

	private JVar findVarByName(String name) {
	
		for (JVar var : enumMemberMap.values()) {
			if (var.name().equals(name)) {
				return var;
			}
		}
		for (JVar var : propertyValueMap.values()) {
			if (var.name().equals(name)) {
				return var;
			}
		}
		for (JVar var : tableRowMap.values()) {
			if (var.name().equals(name)) {
				return var;
			}
		}
		return null;
	}

	public void putMappedVar(JVar key, JVar value) {
		varMap.put(key, value);
	}

	public JVar getMappedVar(JVar key) throws BeamTransformGenerationException {
		JVar result = varMap.get(key);
		if (result == null) {
			String msg = MessageFormat.format("In {0} method , variable not mapped: {1}", currentMethodName(), key.name());
			throw new BeamTransformGenerationException(msg);
		}
		return result;
	}

	private String currentMethodName() {
	
		return beamMethod==null ? "unknown" : beamMethod.getMethod().name();
	}

	public JVar getPropertyValueOrFail(ShowlPropertyShapeGroup group) throws BeamTransformGenerationException {
		JVar result = getPropertyValue(group);
		if (result == null) {
			throw new BeamTransformGenerationException("Property value not found: " + group.pathString());
		}
		return result;
	}




}
