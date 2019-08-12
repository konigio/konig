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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.api.services.bigquery.model.TableRow;
import com.helger.jcodemodel.AbstractJClass;
import com.helger.jcodemodel.IJExpression;
import com.helger.jcodemodel.JBlock;
import com.helger.jcodemodel.JCodeModel;
import com.helger.jcodemodel.JDefinedClass;
import com.helger.jcodemodel.JExpr;
import com.helger.jcodemodel.JForEach;
import com.helger.jcodemodel.JMethod;
import com.helger.jcodemodel.JMod;
import com.helger.jcodemodel.JVar;

import io.konig.core.showl.ShowlArrayExpression;
import io.konig.core.showl.ShowlDirectPropertyShape;
import io.konig.core.showl.ShowlEffectiveNodeShape;
import io.konig.core.showl.ShowlExpression;
import io.konig.core.showl.ShowlNodeShape;
import io.konig.core.showl.ShowlPropertyShape;
import io.konig.core.showl.ShowlPropertyShapeGroup;
import io.konig.core.showl.ShowlStructExpression;
import io.konig.core.showl.ShowlUniqueKey;
import io.konig.core.showl.ShowlUniqueKeyCollection;
import io.konig.core.showl.UniqueKeyElement;
import io.konig.core.showl.UniqueKeyFactory;

/**
 * A utility for computing the transform for a property whose value consists of multiple
 * resources from multiple data sources.
 * @author Greg McFall
 *
 */
public class ResourceArrayTransform {

	private BeamExpressionTransform etran;

	public ResourceArrayTransform(BeamExpressionTransform etran) {
		this.etran = etran;
	}
	
	public IJExpression transform(ShowlArrayExpression array) throws BeamTransformGenerationException {
		
		
		
		JCodeModel model = etran.codeModel();
		BlockInfo blockInfo = etran.peekBlockInfo();
		BeamMethod beamMethod = blockInfo.getBeamMethod();
		ShowlPropertyShape targetProperty = beamMethod==null ? null : beamMethod.getTargetProperty();
		
		if (targetProperty == null) {
			throw new BeamTransformGenerationException("Cannot transform array because targetProperty is not defined");
		}
		ShowlNodeShape targetValueNode = targetProperty.getValueShape();
		if (targetValueNode == null) {
			throw new BeamTransformGenerationException("Expected value shape at " + targetProperty.getPath());
		}
		UniqueKeyFactory keyFactory = new UniqueKeyFactory(etran.getOwlReasoner());
		ShowlUniqueKeyCollection keyCollection = keyFactory.createKeyCollection(targetValueNode);
		
		if (keyCollection.isEmpty()) {
			throw new BeamTransformGenerationException("No key found for " + targetValueNode.getPath());
		}
		
		if (keyCollection.size()>1) {
			throw new BeamTransformGenerationException("Cannot handle multiple keys for " + targetValueNode.getPath());
		}
		
		ShowlUniqueKey uniqueKey = keyCollection.get(0);
		
		JBlock block = blockInfo.getBlock();
		AbstractJClass objectClass = model.ref(Object.class);
		AbstractJClass tableRowClass = model.ref(TableRow.class);
		AbstractJClass mapClass = model.ref(Map.class).narrow(objectClass).narrow(tableRowClass);
		AbstractJClass hashMapClass = model.ref(HashMap.class);
		AbstractJClass arrayListClass = model.ref(ArrayList.class).narrow(tableRowClass);

		// ArrayList<TableRow> list = new ArrayList<>();
		// Map<Object, TableRow> map = new HashMap<>();
		
//		JVar list = block.decl(arrayListClass, blockInfo.varName("list")).init(arrayListClass._new());
		JVar map = block.decl(mapClass, blockInfo.varName("map")).init(hashMapClass._new());
		
		int index = 0;
		String baseMemberName = beamMethod.getMethod().name();
		for (ShowlExpression member  : array.getMemberList()) {
			if (member instanceof ShowlStructExpression) {
				String memberMethodName = baseMemberName + (index++);
				BeamMethod memberMethod = memberMethod(map, uniqueKey, memberMethodName, (ShowlStructExpression) member);
				etran.invoke(memberMethod);
			}
		}
		
		
		return arrayListClass._new().arg(map.invoke("values"));
	}

	private BeamMethod memberMethod(JVar callerMap, ShowlUniqueKey uniqueKey, String memberMethodName, ShowlStructExpression member) throws BeamTransformGenerationException {

		JCodeModel model = etran.codeModel();
		AbstractJClass tableRowClass = model.ref(TableRow.class);
		
		BlockInfo parentBlock = etran.peekBlockInfo();
		BeamMethod parentMethod = parentBlock.getBeamMethod();
		ShowlPropertyShape targetProperty = parentMethod.getTargetProperty();
		RdfJavaType returnType = new RdfJavaType(targetProperty.getValueType(etran.getOwlReasoner()), model.VOID);
		
		JDefinedClass targetClass = etran.getTargetClass();
		JMethod method = targetClass.method(JMod.PRIVATE, model.VOID, memberMethodName);
		
		
		BeamMethod beamMethod = new BeamMethod(method);
		beamMethod.setReturnType(returnType);
		beamMethod.setTargetProperty(targetProperty);
		BlockInfo blockInfo = etran.beginBlock(beamMethod);
		try {	
			// Add a parameter for the map that will hold the output of the member method.
			
			JVar map = beamMethod.addParameter(BeamParameter.ofMappedValue(callerMap.type(), "map")).getVar();
			parentBlock.putMappedVar(map, callerMap);
			
			ShowlPropertyShapeGroup rowListAccessor = addSourceRowParameters(beamMethod, member);
			JVar rowList = blockInfo.getPropertyValue(rowListAccessor);
			
			JBlock methodBlock = blockInfo.getBlock();
			BeamUniqueKeyGenerator keyGenerator = keyGenerator(uniqueKey);

			String rowName = targetProperty.getPredicate().getLocalName() + "Row";
			JForEach forEach = methodBlock.forEach(tableRowClass, rowName, rowList);
			JBlock block = forEach.body();
			blockInfo.setBlock(block);
			blockInfo.putTableRow(rowListAccessor.getValueShape(), forEach.var());
			
			JVar key = keyGenerator.createKeyVar(member);
			
			JVar row = block.decl(
					tableRowClass, 
					blockInfo.varName(targetProperty.getPredicate().getLocalName() + "Row"))
					.init(map.invoke("get").arg(key));
			
			JBlock thenBlock = block._if(row.eqNull())._then();
			
			thenBlock.assign(row, tableRowClass._new());
			thenBlock.add(map.invoke("put").arg(key).arg(row));
			
			blockInfo.putTableRow(targetProperty.getValueShape().effectiveNode(), row);
			
			if (!(targetProperty instanceof ShowlDirectPropertyShape)) {
				throw new BeamTransformGenerationException("Expected direct property shape: " + targetProperty.getPath());
			}

			ShowlStructExpression memberCopy = member.shallowClone();
			for (UniqueKeyElement element : uniqueKey) {
				ShowlPropertyShape p = element.getPropertyShape();
				JVar propertyVar = blockInfo.getPropertyValueOrFail(p.asGroup());
				block.add(row.invoke("set").arg(p.getPredicate().getLocalName()).arg(propertyVar));
				memberCopy.remove(p.getPredicate());
			}
			
			etran.processStructPropertyList((ShowlDirectPropertyShape)targetProperty, memberCopy);
			
		
			
		} finally {
			etran.endBlock();
		}
		
		return beamMethod;
	}

	private ShowlPropertyShapeGroup addSourceRowParameters(BeamMethod beamMethod, ShowlStructExpression struct) throws BeamTransformGenerationException {

		// This method is similar to BeamExpressionTransform.addParametersFromPropertySet.
		// We should consider refactoring so that we don't have to replicate the logic here.
		// The main difference is that here we include logic to build the 'condition' for an
		// early return, namely if any of the TableRow parameters is null.
		
		// In addition, this method constructs a List of TableRow elements and returns it.
		// Surely, there is a way to generalize the solution so that we can reuse 
		// BeamExpressionTransform.addParametersFromPropertySet instead of creating this specialized method.
		
		ShowlPropertyShapeGroup tableRowList = null;
				
		BlockInfo blockInfo = etran.peekBlockInfo();
		JBlock block = blockInfo.getBlock();
		
		IJExpression condition = null;

		List<ShowlEffectiveNodeShape> nodeList = requiredNodes(struct);
		for (ShowlEffectiveNodeShape node : nodeList) {
			
			JVar var = blockInfo.getTableRowVar(node);
			
			if (var == null) {
				ShowlPropertyShapeGroup accessor = node.getAccessor();
				if (accessor == null) {
					// The declaring node is the root of a source channel.
					// Demand that it be passed along as a parameter
					
					var = etran.addTableRowParam(beamMethod, node);
					
					
				} else {
					// The declaring node is NOT the root of a source channel.
					// Demand that its parent is passed as a parameter and fetch the value from the parent.
					
					ShowlEffectiveNodeShape parentNode = accessor.getDeclaringShape();
					JVar parentVar = etran.addTableRowParam(beamMethod, parentNode);
					block._if(parentVar.eqNull().cor(parentVar.invoke("isEmpty")))._then()._return();
					
					
					if (tableRowList != null) {
						String msg = MessageFormat.format(
								"Multiple TableRow lists not supported for {0}. First list was ''{1}''.  Second list is for {2}", 
								struct.getPropertyShape().getPath(),
								tableRowList.pathString(),
								accessor.getPredicate().getLocalName());
						throw new BeamTransformGenerationException(msg);
					}
					var = etran.declareTableRowList(accessor);
					tableRowList = accessor;
				}
			}
			
			if (condition == null) {
				condition = var.eqNull();
			} else {
				condition = condition.cor(var.eqNull());
			}
			
		}
		
		block._if(condition)._then()._return();
		
		if (tableRowList == null) {
			throw new BeamTransformGenerationException("No TableRow list found for " + beamMethod.name());
		}
		
		return tableRowList;
		
	}
	
	private List<ShowlEffectiveNodeShape> requiredNodes(ShowlExpression e) {
		Set<ShowlPropertyShape> set = new HashSet<>();
		e.addProperties(set);
		
		List<ShowlEffectiveNodeShape> list = new ArrayList<>();
		for (ShowlPropertyShape p : set) {
			ShowlPropertyShapeGroup group = p.asGroup();
			ShowlEffectiveNodeShape node = group.getDeclaringShape();
			
			if (!list.contains(node)) {
				list.add(node);
			}
		}
		
		Collections.sort(list);
		return list;
	}

	private BeamUniqueKeyGenerator keyGenerator(ShowlUniqueKey uniqueKey) {
		
		return uniqueKey.size()==1 ? new BeamSingleKeyGenerator(etran, uniqueKey) : new BeamCompositeKeyGenerator(etran, uniqueKey);
	}


}
