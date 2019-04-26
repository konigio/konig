package io.konig.lineage;

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

import io.konig.core.showl.ShowlPropertyShape;

public class LineageUtil {

	public static DatasourcePropertyPath toPropertyPath(ShowlPropertyShape p) {
		DatasourcePropertyPath path = new DatasourcePropertyPath();
		while (p != null) {
			path.add(p.getPredicate());
			p = p.getDeclaringShape().getAccessor();
		}
		if (path.size()>1) {
			Collections.reverse(path);
		}
		return path;
	}
	

}
