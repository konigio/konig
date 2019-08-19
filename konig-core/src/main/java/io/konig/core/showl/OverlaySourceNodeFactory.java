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


import java.util.HashSet;
import java.util.Set;

public class OverlaySourceNodeFactory implements ShowlSourceNodeFactory {
	private Set<ShowlNodeShape> candidates = new HashSet<>();

	private ShowlNodeShape targetNodeShape;
	private ShowlNodeShape sourceNode;
	private ShowlSourceNodeFactory defaultFactory;
	
	
	

	public OverlaySourceNodeFactory(ShowlNodeShape targetNodeShape, ShowlSourceNodeFactory defaultFactory) {
		this.targetNodeShape = targetNodeShape;
		this.defaultFactory = defaultFactory;
	}


	public void setSourceNode(ShowlNodeShape sourceNode) {
		this.sourceNode = sourceNode;
		candidates.clear();
		candidates.add(sourceNode);
	}


	public ShowlNodeShape getSourceNode() {
		return sourceNode;
	}


	@Override
	public Set<ShowlNodeShape> candidateSourceNodes(ShowlNodeShape targetNode) throws ShowlProcessingException {
		
		return targetNodeShape == targetNode ? 
				candidates :
				defaultFactory.candidateSourceNodes(targetNode);
	}

}
