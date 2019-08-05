package io.konig.core.showl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openrdf.model.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SynsetNode {
	public static Logger logger = LoggerFactory.getLogger(SynsetNode.class);

	private List<SynsetProperty> propertyList = new ArrayList<>();
	private Map<URI,SynsetProperty> propertyMap = new HashMap<>();
	
	public SynsetNode(ShowlNodeShape node) {
		Map<ShowlPropertyShape, SynsetProperty> map = new HashMap<>();
		
		addProperties(map, this, node);
	}

	private SynsetNode() {
		
	}
	
	private void addProperties(Map<ShowlPropertyShape, SynsetProperty> map, SynsetNode synsetNode, ShowlNodeShape node) {
		
		addProperties(map, synsetNode, node.getProperties());
		for (ShowlDerivedPropertyList derived : node.getDerivedProperties()) {
			addProperties(map, synsetNode, derived);
		}
		
	}

	private void addProperties(Map<ShowlPropertyShape, SynsetProperty> map, SynsetNode synsetNode,
			Collection<? extends ShowlPropertyShape> propertyCollection) {
		
		for (ShowlPropertyShape p : propertyCollection) {
			if (map.get(p) == null) {
				SynsetProperty property = new SynsetProperty();
				SynsetNode valueNode = null;
				Set<ShowlPropertyShape> synonyms = p.synonyms();
				for (ShowlPropertyShape q : synonyms) {
					map.put(q, property);
					property.add(q);
					
					ShowlNodeShape nested = q.getValueShape();
					if (nested != null) {
						if (valueNode == null) {
							valueNode = new SynsetNode();
							property.setValueNode(valueNode);
						}
						addProperties(map, valueNode, nested);
					}
				}
				synsetNode.addProperty(property);
				if (logger.isTraceEnabled()) {
					logger.trace("addProperties: Add {} to {}", property.localNames(), synsetNode.toString());
					logger.isTraceEnabled();
				}
			}
		}
	}
	
	public SynsetProperty findPropertyByPath(List<URI> path) {
		SynsetProperty property = null;
		SynsetNode node = this;
		for (URI predicate : path) {
			if (node == null) {
				return null;
			}
			
			property = node.findPropertyByPredicate(predicate);
			node = property==null ? null : property.getValueNode();
			
		}
		
		return property;
	}
	
	

	public SynsetProperty findPropertyByPredicate(URI predicate) {
		return propertyMap.get(predicate);
	}
	
	public void addProperty(SynsetProperty p) {
		if (!propertyList.contains(p)) {
			propertyList.add(p);
			for (URI predicate : p.getPredicates()) {
				propertyMap.put(predicate, p);
			}
		}
	}
	
	public String toString() {
		for (SynsetProperty p : propertyList) {
			for (ShowlPropertyShape q : p) {
				return q.getDeclaringShape().getPath();
			}
		}
		return "EmptySynsetNode";
	}
	

}
