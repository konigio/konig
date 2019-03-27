package io.konig.core.showl;

/*
 * #%L
 * Konig Core
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


import java.util.HashSet;
import java.util.Iterator;

/**
 * The collection of all instances of ShowlNodeShape for a given SHACL Node Shape.
 * There is one instance for each usage of the SHACL Node shape.  
 * @author Greg McFall
 *
 */
public class ShowlNodeShapeSet extends HashSet<ShowlNodeShape> {
	private static final long serialVersionUID = 1L;
	
	public ShowlNodeShape findAny() {
		return isEmpty() ? null : iterator().next();
	}
	
	public ShowlNodeShape top() {
		Iterator<ShowlNodeShape> sequence = iterator();
		while (sequence.hasNext()) {
			ShowlNodeShape node = sequence.next();
			if (node.getAccessor() == null) {
				return node;
			}
		}
		return null;
	}
	

}
