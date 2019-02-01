package io.konig.core.showl;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NodeNamer {

	private static final Logger logger = LoggerFactory.getLogger(NodeNamer.class);
	
	private Map<ShowlNodeShape,String> map = new HashMap<>();
	private VariableGenerator vargen = new VariableGenerator();
	
	public NodeNamer() {
		
	}

	public String varname(ShowlNodeShape node) {
		String result = map.get(node);
		if (result == null) {
			result = vargen.next();
			map.put(node, result);
			if (logger.isTraceEnabled()) {
				logger.trace("varname: node={}, alias={}", node.getPath(), result);
			}
		}
		return result;
	}
}
