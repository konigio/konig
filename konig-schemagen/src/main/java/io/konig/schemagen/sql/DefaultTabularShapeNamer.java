package io.konig.schemagen.sql;

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


import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

import io.konig.core.util.StringUtil;

public class DefaultTabularShapeNamer implements TabularShapeNamer {

	@Override
	public URI reifiedPropertyShapeId(URI subjectShapeId, URI predicate) {
		String namespace = subjectShapeId.getNamespace();
		String subjectShapeLocalName = subjectShapeId.getLocalName();
		
		String suffix = "";
		if (subjectShapeLocalName.toLowerCase().endsWith("shape")) {
			subjectShapeLocalName = subjectShapeLocalName.substring(0, subjectShapeLocalName.length()-5);
			suffix = "_SHAPE";
		}
		
		String baseName = StringUtil.SNAKE_CASE(subjectShapeLocalName);
		String snakePredicate = StringUtil.SNAKE_CASE(predicate.getLocalName());
		
		StringBuilder builder = new StringBuilder();
		builder.append(namespace);
		builder.append(baseName);
		builder.append('_');
		builder.append(snakePredicate);
		builder.append(suffix);
		
		return new URIImpl(builder.toString());
	}

	@Override
	public String reifiedPropertyTableName(URI subjectShapeId, URI predicate) {
		String subjectShapeLocalName = subjectShapeId.getLocalName();
		
		if (subjectShapeLocalName.toLowerCase().endsWith("shape")) {
			subjectShapeLocalName = subjectShapeLocalName.substring(0, subjectShapeLocalName.length()-5);
		}
		
		String baseName = StringUtil.SNAKE_CASE(subjectShapeLocalName);
		String snakePredicate = StringUtil.SNAKE_CASE(predicate.getLocalName());
		
		StringBuilder builder = new StringBuilder();
		builder.append(baseName);
		builder.append('_');
		builder.append(snakePredicate);
		
		return builder.toString();
	}

}
