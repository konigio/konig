package io.konig.core.showl;

/*
 * #%L
 * Konig Core
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


import java.util.LinkedHashSet;
import java.util.Set;

import org.openrdf.model.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.konig.core.OwlReasoner;

public class ShowlAlternativePath {
	private static Logger logger = LoggerFactory.getLogger(ShowlAlternativePath.class);
	
	private ShowlNodeShape node;
	private Set<ShowlDirectPropertyShape> parameters = new LinkedHashSet<>();

	public static ShowlAlternativePath forNode(ShowlNodeShape node) {
		Set<ShowlDirectPropertyShape> set = new LinkedHashSet<>();
		addParameters(set, node);
		if (!set.isEmpty()) {
			if (logger.isTraceEnabled()) {
				logger.trace("created ShowlAlternativePath({})", node.getPath());
				return new ShowlAlternativePath(node, set);
			}
			
		}
		
		return null;
	}
	
	public static boolean canApplyPath(ShowlNodeShape sourceNode, ShowlNodeShape targetNode) {
		for (ShowlDirectPropertyShape targetDirect : targetNode.getProperties()) {
			ShowlDerivedPropertyList derivedList = sourceNode.getDerivedProperty(targetDirect.getPredicate());
			if (derivedList != null) {
				if (derivedList.size() == 1) {
					ShowlDerivedPropertyShape sourceDerived = derivedList.get(0);
					if (sourceDerived.getValueShape() != null) {
						if (targetDirect.getValueShape()!=null) {
							if (!canApplyPath(sourceDerived.getValueShape(), targetDirect.getValueShape())) {
								return false;
							};
						}
					}
					
					
				} else {
					return false;
				}
			}
		}
		return true;
	}
	
	private static boolean addParameters(Set<ShowlDirectPropertyShape> set, ShowlNodeShape node) {
		for (ShowlDerivedPropertyList derivedList : node.getDerivedProperties()) {
			if (derivedList.size() != 1) {
				if (logger.isTraceEnabled()) {
					logger.trace("addParameters:  Multiple branches detected at {}", node.getPath() );
					return false;
				}
			}
			
			ShowlDerivedPropertyShape derived = derivedList.get(0);
			ShowlPropertyShape peer = derived.maybeDirect();
			if (peer instanceof ShowlDirectPropertyShape)  {
				set.add((ShowlDirectPropertyShape) peer);
				if (logger.isTraceEnabled()) {
					logger.trace("addParameters: added {}", peer.getPath());
				}
			}
			
			if (derived.getValueShape() != null) {
				addParameters(set, derived.getValueShape());
			}
		}
		
		return true;
		
	}

	


	public ShowlAlternativePath(ShowlNodeShape node, Set<ShowlDirectPropertyShape> parameters) {
		this.node = node;
		this.parameters = parameters;
	}

	public ShowlNodeShape getNode() {
		return node;
	}

	public Set<ShowlDirectPropertyShape> getParameters() {
		return parameters;
	}

	public URI valueType(OwlReasoner reasoner) {
		return valueType(node, reasoner);
	}

	private URI valueType(ShowlNodeShape node, OwlReasoner reasoner) {

		URI result = null;
		while (result == null) {
			for (ShowlDerivedPropertyList derivedList : node.getDerivedProperties()) {
				
				
				ShowlDerivedPropertyShape derived = derivedList.get(0);
				node = derived.getValueShape();
				if (node == null) {
					ShowlPropertyShape p = derived.maybeDirect();
					result = p.getValueType(reasoner);
				}
			}
		}
		
		return result;
	}

}
