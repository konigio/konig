package io.konig.cadl;

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


import org.openrdf.model.Resource;

import io.konig.core.Graph;
import io.konig.core.pojo.PojoContext;
import io.konig.core.pojo.PojoListener;
import io.konig.core.pojo.SimplePojoFactory;
import io.konig.core.vocab.CADL;

public class CubeLoader {
	
	public void load(Graph graph, CubeManager manager) {
	
		PojoContext context = new PojoContext();
		context.mapClass(CADL.Cube, Cube.class);
		context.setListener(new PojoListener() {

			@Override
			public void map(Resource id, Object pojo) {
				if (pojo instanceof Cube) {
					manager.add((Cube)pojo);
				}
				
			}});

		SimplePojoFactory factory = new SimplePojoFactory(context);
		factory.createAll(graph);
		
	}

}
