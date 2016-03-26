package io.konig.shacl.jsonld;

/*
 * #%L
 * konig-shacl
 * %%
 * Copyright (C) 2015 - 2016 Gregory McFall
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

import com.fasterxml.jackson.core.JsonGenerator;

import io.konig.core.Context;
import io.konig.core.Vertex;
import io.konig.shacl.Shape;

public interface JsonldShapeWriter {
	
	public static enum ContextOption {
		IGNORE,
		URI,
		OBJECT
	}
	

	public void toJson(Vertex subject, Shape shape, JsonGenerator json) throws IOException;
	public void toJson(Vertex subject, Shape shape, Context context, ContextOption contextOption, JsonGenerator json) throws IOException;
}
