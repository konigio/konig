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


import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;

import org.openrdf.model.URI;

@SuppressWarnings("serial")
public class ShowlPredicatePath extends ArrayList<URI> {
	
	private ShowlNodeShape root;

	public ShowlPredicatePath(ShowlNodeShape root) {
		this.root = root;
	}
	
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(root.getPath());
		for (URI predicate : this) {
			builder.append('.');
			builder.append(predicate.getLocalName());
		}
		
		return builder.toString();
	}
	
	public URI getLast() {
		return get(size()-1);
	}


	public static ShowlPredicatePath forProperty(ShowlPropertyShape p) {
		ShowlPredicatePath result = new ShowlPredicatePath(p.getRootNode());
		while (p != null) {
			result.add(p.getPredicate());
			p = p.getDeclaringShape().getAccessor();
		}
		Collections.reverse(result);
		return result;
	}
	
}
