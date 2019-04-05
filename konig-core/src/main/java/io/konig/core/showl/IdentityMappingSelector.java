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


import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import io.konig.core.vocab.Konig;
import io.konig.shacl.Shape;

public class IdentityMappingSelector implements ShowlSourceNodeSelector {

	@Override
	public Set<Shape> selectCandidateSources(ShowlNodeShape targetShape) {
		
		Shape target = targetShape.getShape();
		
		if (target.hasDataSourceType(Konig.GoogleBigQueryTable) && target.hasDataSourceType(Konig.GoogleCloudStorageBucket)) {
			Set<Shape> set = new HashSet<>();
			set.add(target);
			return set;
		}
		
		return Collections.emptySet();
	}


}
