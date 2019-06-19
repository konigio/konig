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


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.helger.jcodemodel.JBlock;
import com.helger.jcodemodel.JVar;

import io.konig.core.showl.ShowlExpression;
import io.konig.core.showl.ShowlNodeShape;
import io.konig.core.showl.ShowlPropertyExpression;
import io.konig.core.showl.ShowlPropertyShape;

public class BlockInfo implements BeamPropertyManager {


	private JBlock block;
	private int setCount=0;
	private int valueCount = 0;
	private Map<ShowlNodeShape, NodeTableRow> nodeTableRowMap;
	private JVar errorBuilderVar;
	private EnumValueType enumValueType = EnumValueType.LOCAL_NAME;

	private Map<ShowlPropertyShape, BeamSourceProperty> map = new HashMap<>();
	
	
	public BlockInfo(JBlock block) {
		this.block = block;
	}

	public BlockInfo nodeTableRowMap(Map<ShowlNodeShape, NodeTableRow> tableRowMap) {
		this.nodeTableRowMap = tableRowMap;
		return this;
		
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

}
