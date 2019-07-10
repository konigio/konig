package io.konig.core.showl;

import java.util.ArrayList;

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


import java.util.List;
import java.util.Set;

import org.openrdf.model.URI;

import io.konig.core.OwlReasoner;

/**
 * A structure that is derived from a collection of alternative paths.
 * @author Greg McFall
 *
 */
public class AlternativePathsExpression implements ShowlExpression {

	private List<ShowlAlternativePath> pathList;
	
	public AlternativePathsExpression(List<ShowlAlternativePath> pathList) {
		this.pathList = pathList;
	}

	public List<ShowlAlternativePath> getPathList() {
		return pathList;
	}

	@Override
	public String displayValue() {
		
		return  "AlternativePathsExpression(" + pathList.get(0).getNode().getPath() + ")";
	}

	@Override
	public void addDeclaredProperties(ShowlNodeShape sourceNodeShape, Set<ShowlPropertyShape> set)
			throws ShowlProcessingException {
		
		ShowlNodeShape node = pathList.get(0).getNode();
		if (node.getRoot() == sourceNodeShape.getRoot()) {
			addProperties(set);
		}

	}

	@Override
	public void addProperties(Set<ShowlPropertyShape> set) {
		
		for (ShowlAlternativePath path : pathList) {
			set.addAll(path.getParameters());
		}

	}

	@Override
	public URI valueType(OwlReasoner reasoner) {
		URI result = null;
		for (ShowlAlternativePath p : pathList) {
			URI type = p.valueType(reasoner);
			result = (URI) reasoner.leastCommonSuperClass(result, type);
		}
		return result;
	}

	@Override
	public ShowlExpression transform() {
		// Not sure this is correct
		return this;
	}

}
