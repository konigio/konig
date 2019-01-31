package io.konig.core.showl;

import java.util.HashMap;
import java.util.Map;

public class NodeNamer {

	private Map<ShowlJoinCondition,String> map = new HashMap<>();
	private VariableGenerator vargen = new VariableGenerator();

	public String varname(ShowlJoinCondition join) {
		String result = map.get(join);
		if (result == null) {
			result = vargen.next();
			map.put(join, result);
		}
		return result;
	}
}
