package io.konig.shacl;

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


import java.util.HashMap;
import java.util.Map;

public class SimpleMediaTypeManager implements MediaTypeManager {

	private Map<String,Shape> map;
	
	public SimpleMediaTypeManager(ShapeManager shapeManager) {
		map = new HashMap<>();
		for (Shape shape : shapeManager.listShapes()) {
			String key = shape.getMediaTypeBaseName();
			if (key != null) {
				map.put(key, shape);
			}
		}
	}

	@Override
	public Shape shapeOfMediaType(String mediaType) {
		int plus = mediaType.lastIndexOf('+');
		if (plus>0) {
			mediaType = mediaType.substring(0, plus);
		}
		return map.get(mediaType);
	}

}
