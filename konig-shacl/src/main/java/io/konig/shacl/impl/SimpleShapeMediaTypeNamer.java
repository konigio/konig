package io.konig.shacl.impl;

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


import org.openrdf.model.Resource;
import org.openrdf.model.URI;

import io.konig.shacl.Shape;
import io.konig.shacl.ShapeMediaTypeNamer;

public class SimpleShapeMediaTypeNamer implements ShapeMediaTypeNamer {

	@Override
	public String baseMediaTypeName(Shape shape) {
		String result = null;
		
		Resource id = shape.getId();
		if (id instanceof URI) {
			String value = id.stringValue();
			String[] array = value.split("/");
			String host = array[2];
			
			int colon = host.lastIndexOf(':');
			if (colon > 0) {
				host = host.substring(0,  colon);
			}
			String[] hostParts = host.split("[.]");
			
			host = hostParts[hostParts.length-2];
			
			StringBuilder builder = new StringBuilder();
			builder.append("application/vnd.");
			builder.append(host);
			builder.append('.');
			int len = array.length;
			builder.append(array[len-3].toLowerCase());
			builder.append('.');
			builder.append(array[len-2].toLowerCase());
			builder.append('.');
			builder.append(array[len-1].toLowerCase());
			
			
			result = builder.toString();
		}
		
		return result;
	}

}
