package io.konig.core.pojo.impl;

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


import java.util.Set;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.vocabulary.RDF;

import io.konig.core.Edge;
import io.konig.core.KonigException;
import io.konig.core.Vertex;
import io.konig.core.pojo.PojoContext;
import io.konig.core.pojo.PojoCreator;

public class MasterPojoHandler implements PojoHandler {
	

	@Override
	public void buildPojo(PojoInfo pojoInfo) throws KonigException {
		
		Class<?> javaClass = selectType(pojoInfo);
		if (javaClass != null) {

			PojoContext context = pojoInfo.getContext();
			PojoHandler delegate = context.getPojoHandler(javaClass);
			
			if (delegate == null) {
				delegate = createDelegate(javaClass);
				context.putPojoHandler(javaClass, delegate);
			}
			delegate.buildPojo(pojoInfo);
			Resource resource = pojoInfo.getVertex().getId();
			context.notify(resource, pojoInfo.getJavaObject());
			
		} 

	}

	private PojoHandler createDelegate(Class<?> javaClass) {

		Class<?> type = javaClass;
		// Check class hierarchy
		while (type != null && type!=Object.class) {
			CreatorPojoHandler handler = creatorPojoHandler(type);
			if (handler != null) {
				return handler;
			}
			type = type.getSuperclass();
		}
		
		return new BasicPojoHandler(javaClass);
	}
	
	private CreatorPojoHandler creatorPojoHandler(Class<?> javaClass) {
		String packageName = javaClass.getPackage().getName();
		
		StringBuilder builder = new StringBuilder(packageName);
		builder.append('.');
		builder.append(javaClass.getSimpleName());
		builder.append("Creator");
		
		String creatorName = builder.toString();
		
		try {
			@SuppressWarnings("unchecked")
			Class<? extends PojoCreator<?>> creatorClass = 
				(Class<? extends PojoCreator<?>>) Class.forName(creatorName);
			
			return new CreatorPojoHandler(javaClass, creatorClass);
			
		} catch (Throwable ignore) {
			
		}
		
		
		return interfaceCreatorPojoHandler(javaClass);
	}

	private CreatorPojoHandler interfaceCreatorPojoHandler(Class<?> javaClass) {
		Class<?>[] list = javaClass.getInterfaces();
		for (Class<?> type : list) {
			CreatorPojoHandler handler = creatorPojoHandler(type);
			if (handler != null) {
				return handler;
			}
		}
		return null;
	}

	private Class<?> selectType(PojoInfo pojoInfo) {
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
