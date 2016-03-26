package io.konig.services.impl;

/*
 * #%L
 * Konig Services
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


import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

import io.konig.core.UidGenerator;
import io.konig.services.VersionNamer;

public class VersionNamerImpl implements VersionNamer {
	
	private static final String HISTORY = "kcs:history/";

	@Override
	public URI nextVersionId(URI subjectOfChange) {
		
		String baseURI = subjectOfChange.stringValue();
		String uid = UidGenerator.INSTANCE.next();
		
		StringBuilder builder = new StringBuilder();
		builder.append(baseURI);
		if (!baseURI.endsWith("/")) {
			builder.append('/');
		}
		builder.append(HISTORY);
		builder.append(uid);

		return new URIImpl(builder.toString());
	}

}
