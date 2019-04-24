package io.konig.core.pojo.impl;

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


import java.util.Set;

import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.vocabulary.RDF;

import io.konig.core.Edge;
import io.konig.core.Vertex;
import io.konig.core.pojo.PojoContext;

public class PojoUtil {

	public static Class<?> selectType(PojoInfo pojoInfo) {
		Class<?> best = pojoInfo.getExpectedJavaClass();
		PojoContext context = pojoInfo.getContext();
		Vertex v = pojoInfo.getVertex();
		if (v != null) {
			Set<Edge> typeSet = v.outProperty(RDF.TYPE);
			for (Edge e : typeSet) {
				Value object = e.getObject();
				if (object instanceof URI) {
					URI typeId = (URI) object;
					Class<?> type = context.getJavaClass(typeId);
					if (type!=null && (best == null || best.isAssignableFrom(type))) {
						best = type;
					}
				}
			}
		}
		return best;
	}

}
