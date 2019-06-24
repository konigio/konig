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
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.openrdf.model.URI;

import com.google.api.services.bigquery.model.TableRow;
import com.helger.jcodemodel.AbstractJClass;
import com.helger.jcodemodel.AbstractJType;
import com.helger.jcodemodel.JBlock;
import com.helger.jcodemodel.JCodeModel;
import com.helger.jcodemodel.JExpr;
import com.helger.jcodemodel.JInvocation;
import com.helger.jcodemodel.JMethod;
import com.helger.jcodemodel.JVar;

import io.konig.core.showl.ShowlDirectPropertyShape;
import io.konig.core.showl.ShowlExpression;
import io.konig.core.showl.ShowlNodeShape;
import io.konig.core.showl.ShowlPropertyExpression;
import io.konig.core.showl.ShowlPropertyShape;
import io.konig.core.showl.ShowlStructExpression;
import io.konig.core.showl.ShowlUtil;
import io.konig.core.util.StringUtil;

public class BlockInfo implements BeamPropertyManager {


	private JBlock block;
	private int setCount=0;
	private int valueCount = 0;
	private Map<ShowlNodeShape, NodeTableRow> nodeTableRowMap;
	private JVar listVar;
	private JVar outputRow;
	private JVar errorBuilderVar;
	private EnumValueType enumValueType = EnumValueType.LOCAL_NAME;
	private BeamMethod beamMethod;
	private BeamPropertySink propertySink;

	private Map<ShowlPropertyShape, BeamSourceProperty> map = new HashMap<>();
	
	
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

	public BlockInfo nodeTableRowMap(Map<ShowlNodeShape, NodeTableRow> tableRowMap) {
		this.nodeTableRowMap = tableRowMap;
		return this;
		
	}
	
	public BeamSourceProperty createSourceProperty(ShowlPropertyShape p) throws BeamTransformGenerationException {
		BeamSourceProperty result = map.get(p);
		if (result == null) {
			result = new BeamSourceProperty(null, p);
			JCodeModel model = beamMethod.getMethod().owner();
			AbstractJClass objectClass = model.ref(Object.class);
			String fieldName = p.getPredicate().getLocalName();
			JVar var = block.decl(objectClass, fieldName);
			
			ShowlNodeShape node = p.getDeclaringShape();
			NodeTableRow row = getNodeTableRow(node);
			
			var.init(row.getTableRowVar().invoke("get").arg(JExpr.lit(fieldName)));
			
			result.setVar(var);
			
			map.put(p, result);
			
		}
		return result;
	}

	/**
	 * The row that this block is expected to populate.
	 */
	public JVar getOutputRow() {
		return outputRow;
	}

	public BlockInfo outputRow(JVar outputRow) {
		this.outputRow = outputRow;
		return this;
	}

	public JVar getListVar() {
		return listVar;
	}

	public void setListVar(JVar listVar) {
		this.listVar = listVar;
	}

	public void addNodeTableRow(NodeTableRow value) {
		if (nodeTableRowMap == null) {
			nodeTableRowMap = new LinkedHashMap<>();
		}
		nodeTableRowMap.put(value.getNode(), value);
	}
	public EnumValueType getEnumValueType() {
		return enumValueType;
	}

	public BlockInfo enumValueType(EnumValueType enumValueType) {
		this.enumValueType = enumValueType;
		return this;
	}
	
	public NodeTableRow maybeNullNodeTableRow(ShowlNodeShape node) {
		return nodeTableRowMap == null ? null : nodeTableRowMap.get(node);
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

	public JBlock getBlock() {
		return block;
	}


	@Override
	public void add(BeamSourceProperty p) {
		map.put(p.getPropertyShape(), p);

	}

	@Override
	public BeamSourceProperty forPropertyShape(ShowlPropertyShape p) throws BeamTransformGenerationException {
		BeamSourceProperty result = map.get(p);
		if (result == null) {
			throw new BeamTransformGenerationException("Failed to find BeamSourceProperty for " + p.getPath());
		}
		return result;
	}

	public void addRowParameters(BeamMethod beamMethod, ShowlNodeShape targetNode, ShowlExpression e) {
		
		Set<ShowlPropertyShape> propertySet = new HashSet<>();
		e.addProperties(propertySet);
		
		Set<ShowlNodeShape> nodeSet = new HashSet<>();
		for (ShowlPropertyShape p : propertySet) {
			nodeSet.add(p.getRootNode());
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
			String rowName = StringUtil.firstLetterLowerCase(ShowlUtil.shortShapeName(node) + "Row");
			JVar param = method.param(tableRowClass, rowName);
			addNodeTableRow(new NodeTableRow(node, param));
			beamMethod.addParameter(BeamParameter.ofSourceRow(param, node));
		}
		
	}

	public void invoke(BeamMethod beamMethod) throws BeamTransformGenerationException {
		JMethod method = beamMethod.getMethod();
		
		JInvocation invoke = JExpr.invoke(method);
		for (BeamParameter param : beamMethod.getParameters()) {
			switch (param.getParamType()) {
			
			case ENUM_VALUE :
				throw new BeamTransformGenerationException("enum value not supported yet");
				
			case ERROR_BUILDER :
				
				if (errorBuilderVar == null) {
					throw new BeamTransformGenerationException("Cannot invoke " + method.name() + " because the caller block does not declare the errorBuilder variable.");
				}
				invoke.arg(errorBuilderVar);
				break;
				
			case SOURCE_TABLE_ROW :
				ShowlNodeShape node = param.getSourceNode();
				NodeTableRow rowInfo = maybeNullNodeTableRow(node);
				
				if (rowInfo == null || rowInfo.getTableRowVar()==null) {
					throw new BeamTransformGenerationException(
							"Cannot invoke " + method.name() + " because the caller block does not declare a TableRow for " + node.getPath());
				}
				invoke.arg(rowInfo.getTableRowVar());
				break;
				
			case LIST_VALUE :
				if (listVar == null) {
					throw new BeamTransformGenerationException(
							"Cannot invoke " + method.name() + " because the caller block does not declare the List variable");
				}
				invoke.arg(listVar);
				break;
				
			}
		}
		
		block.add(invoke);
		
	}

	public void addListParam(BeamMethod beamMethod, AbstractJType paramType, String paramName) {
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


}
