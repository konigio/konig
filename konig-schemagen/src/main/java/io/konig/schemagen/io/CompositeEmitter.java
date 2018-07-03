package io.konig.schemagen.io;

/*
 * #%L
 * Konig Schema Generator
 * %%
 * Copyright (C) 2015 - 2018 Gregory McFall
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


import java.io.IOException;
import java.util.ArrayList;

import io.konig.core.Graph;
import io.konig.core.KonigException;

public class CompositeEmitter extends ArrayList<Emitter> implements Emitter {
	private static final long serialVersionUID = 1L;

	@Override
	public void emit(Graph graph) throws IOException, KonigException {
		for (Emitter emitter : this) {
			emitter.emit(graph);
		}
		
	}


}
