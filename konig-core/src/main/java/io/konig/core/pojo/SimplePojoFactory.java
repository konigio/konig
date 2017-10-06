package io.konig.core.pojo;

/*
 * #%L
 * Konig Core
 * %%
 * Copyright (C) 2015 - 2017 Gregory McFall
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


import org.openrdf.model.Resource;
import org.openrdf.model.URI;

import io.konig.core.Graph;
import io.konig.core.KonigException;
import io.konig.core.Vertex;
import io.konig.core.pojo.impl.PojoHandler;
import io.konig.core.pojo.impl.PojoInfo;

public class SimplePojoFactory implements PojoFactory {
	
	private PojoContext context;
	
	public SimplePojoFactory() {
		context = new PojoContext();
	}
	
	public SimplePojoFactory(PojoContext context) {
		this.context = context;
	}
	
	public PojoContext getContext() {
		return context;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T create(Vertex v, Class<T> type) throws KonigException {
		
		Object result = context.getIndividual(v.getId());
		if (result == null) {
			PojoHandler pojoHandler = context.getPojoHandler();
			PojoInfo info = new PojoInfo();
			info.setContext(context);
			info.setVertex(v);
			info.setExpectedJavaClass(type);
			
			pojoHandler.buildPojo(info);
			result = info.getJavaObject();
			
			
		}
		
		
		
		return (T) result;
	}

	@Override
	public void createAll(Graph graph) throws KonigException {
		for (Vertex v : graph.vertices()) {
			Resource id = v.getId();
			if (id instanceof URI) {
				create(v, null);
			}
		}
	}

}
