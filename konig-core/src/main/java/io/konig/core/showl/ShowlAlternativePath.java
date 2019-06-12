package io.konig.core.showl;

import java.util.LinkedHashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	


}
