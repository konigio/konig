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


import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class OverlayTransformService extends BasicTransformService {
	
	private Set<ShowlNodeShape> candidates;
	

	public OverlayTransformService(ShowlSchemaService schemaService, ShowlNodeShapeService nodeService,
			ShowlSourceNodeFactory sourceNodeFactory, Set<ShowlNodeShape> candidates) {
		super(schemaService, nodeService, sourceNodeFactory);
		this.candidates = candidates;
	}

	public Set<ShowlPropertyShapeGroup> computeTransform(ShowlNodeShape targetNode) throws ShowlProcessingException {
		
		ShowlSourceNodeFactory sourceNodeFactory = getSourceNodeFactory();
		
		OverlaySourceNodeFactory overlayFactory = new OverlaySourceNodeFactory(targetNode, sourceNodeFactory);
		setSourceNodeFactory(overlayFactory);
		
		Map<ShowlDirectPropertyShape,ShowlExpression> expressionMap = new HashMap<>();
		
		try {
		
			for (ShowlNodeShape sourceNode : candidates) {
				overlayFactory.setSourceNode(sourceNode);
				
				basicComputeTransform(targetNode);
				stashExpressions(targetNode, expressionMap);
				
			}
			
			
			setSelectedExpressions(targetNode, expressionMap);
			
			Set<ShowlPropertyShapeGroup> unmapped = new HashSet<>();
			collectUnmappedProperties(targetNode, unmapped);
			
			return unmapped;
		} finally {
			setSourceNodeFactory(sourceNodeFactory);
		}
	}

	private void collectUnmappedProperties(ShowlNodeShape targetNode, Set<ShowlPropertyShapeGroup> unmapped) {
		for (ShowlDirectPropertyShape p : targetNode.getProperties()) {
			if (p.getSelectedExpression()==null) {
				unmapped.add(p.asGroup());
			}
			if (p.getValueShape()!=null) {
				collectUnmappedProperties(p.getValueShape(), unmapped);
			}
		}
		
	}

	/**
	 * Stash the selected expressions from the given node into 
	 * a map and set them as null in the node.  If the map already holds an expression for
	 * a given property, merge it into an overlay expression.
	 */
	private void stashExpressions(ShowlNodeShape targetNode,
			Map<ShowlDirectPropertyShape, ShowlExpression> map) {
		
		for (ShowlDirectPropertyShape p : targetNode.getProperties()) {
			
			ShowlExpression e = p.getSelectedExpression();
			if (e != null) {
				p.setSelectedExpression(null);
				ShowlExpression prior = map.get(p);
				if (prior == null) {
					map.put(p, e);
				} else if (prior instanceof ShowlArrayExpression && e instanceof ShowlArrayExpression) {
					ShowlArrayExpression priorArray = (ShowlArrayExpression) prior;
					ShowlArrayExpression newArray = (ShowlArrayExpression) e;
					for (ShowlExpression member : newArray.getMemberList()) {
						priorArray.addMember(member);
					}
				} else if (prior instanceof ShowlOverlayExpression) {
					ShowlOverlayExpression overlay = (ShowlOverlayExpression) prior;
					overlay.add(e);
				} else {
					ShowlOverlayExpression overlay = new ShowlOverlayExpression();
					overlay.add(prior);
					overlay.add(e);
					map.put(p, overlay);
				}
				ShowlNodeShape valueShape = p.getValueShape();
				if (valueShape != null) {
					stashExpressions(valueShape, map);
				}
			}
		}
		
	}

	private void setSelectedExpressions(ShowlNodeShape targetNode, Map<ShowlDirectPropertyShape,ShowlExpression> map) {
		
		for (Map.Entry<ShowlDirectPropertyShape, ShowlExpression> entry : map.entrySet()) {
			ShowlDirectPropertyShape p = entry.getKey();
			ShowlExpression e = entry.getValue();
			if (e instanceof ShowlArrayExpression) {
				ShowlOverlayExpression overlay = new ShowlOverlayExpression();
				overlay.add(e);
				e = overlay;
			}
			p.setSelectedExpression(e);
		}
		
	}



}
