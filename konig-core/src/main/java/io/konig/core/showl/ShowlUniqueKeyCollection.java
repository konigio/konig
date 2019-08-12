package io.konig.core.showl;

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


import java.util.ArrayList;
import java.util.List;

/**
 * A list of unique keys available for a given Node Shape.
 * In general, a given Node may have more than one unique key.  Therefore, it 
 * is useful to have a collection of keys.
 * @author Greg McFall
 *
 */
@SuppressWarnings("serial")
public class ShowlUniqueKeyCollection extends ArrayList<ShowlUniqueKey> {
	
	private ShowlNodeShape nodeShape;

	public ShowlUniqueKeyCollection(ShowlNodeShape nodeShape) {
		this.nodeShape = nodeShape;
	}
	
	/*
	 * Get the NodeShape that has the keys within this collection.
	 */
	public ShowlNodeShape getNodeShape() {
		return nodeShape;
	}

	/**
	 * Flatten this list if possible.  A ShowlUniqueKeyCollection can be flattened only if
	 * there is a single way to identify the associated NodeShape.  In other words, the
	 * collection can be flattened if it contains a single key, and each ShowlUniqueKeyCollection
	 * nested within it also contains a single key.
	 * 
	 * @return A flat list of PropertyShape entities contained within this collection provided it can be
	 * flattened, and null otherwise.
	 */
	public List<ShowlPropertyShape> flatten() {
		if (canFlatten()) {
			List<ShowlPropertyShape> list = new ArrayList<>();
			addProperties(list, this);
			return list;
		}
		return null;	
	}
	
	public ShowlUniqueKey findMatch(ShowlUniqueKey pattern) {
		for (ShowlUniqueKey key : this) {
			if (key.matches(pattern)) {
				return key;
			}
		}
		return null;
	}

	private void addProperties(List<ShowlPropertyShape> list, ShowlUniqueKeyCollection c) {
		
		for (ShowlUniqueKey key : c) {
			for (UniqueKeyElement e : key) {
				ShowlUniqueKeyCollection k = e.getValueKeys();
				if (k != null) {
					addProperties(list, k);
				} else {
					list.add(e.getPropertyShape());
				}
			}
		}
		
	}

	private boolean canFlatten() {
		if (size()==1) {
			for (ShowlUniqueKey key : this) {
				for (UniqueKeyElement e : key) {
					ShowlUniqueKeyCollection c = e.getValueKeys();
					if (c!=null && !c.canFlatten()) {
						return false;
					}
				}
			}
			return true;
		}
		return false;
	}
	

}
