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
