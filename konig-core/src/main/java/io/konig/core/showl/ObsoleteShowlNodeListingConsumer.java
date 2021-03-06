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


import java.util.ArrayList;
import java.util.List;

public class ObsoleteShowlNodeListingConsumer implements ShowlNodeShapeConsumer {
	
	private List<ShowlNodeShape> list = new ArrayList<>();
	private ObsoleteMappingStrategy mappingStrategy;
	
	

	public ObsoleteShowlNodeListingConsumer(ObsoleteMappingStrategy mappingStrategy) {
		this.mappingStrategy = mappingStrategy;
	}

	@Override
	public void consume(ShowlNodeShape node) throws ShowlProcessingException {
		if (mappingStrategy != null) {
//			mappingStrategy.selectMappings(node);
		}
		list.add(node);
	}

	public List<ShowlNodeShape> getList() {
		return list;
	}
	
	

}
