package io.konig.schemagen;

/*
 * #%L
 * Konig Schema Generator
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


import java.util.Comparator;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;

import io.konig.core.Vertex;

public class LocalnameComparator implements Comparator<Vertex> {

	@Override
	public int compare(Vertex a, Vertex b) {
		Resource aId = a.getId();
		Resource bId = b.getId();
		
		String aString = (aId instanceof URI) ? ((URI)aId).getLocalName() : aId.stringValue();
		String bString = (bId instanceof URI) ? ((URI)bId).getLocalName() : bId.stringValue();
		
		return aString.compareTo(bString);
	}

}
