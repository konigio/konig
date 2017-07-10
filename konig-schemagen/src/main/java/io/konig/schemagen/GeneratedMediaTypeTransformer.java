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


import java.util.HashSet;
import java.util.Set;

import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.URIImpl;

import io.konig.core.impl.KonigLiteral;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;

public class GeneratedMediaTypeTransformer implements ShapeTransformer {
	private static final URI GENERATED_MEDIATYPE = new URIImpl("http://schema.pearson.com/ns/activity/generatedMediaType");
	
	private String suffix;

	public GeneratedMediaTypeTransformer(String suffix) {
		this.suffix = suffix;
	}

	@Override
	public PropertyConstraint transform(Shape shape, PropertyConstraint constraint) {
		
		if (GENERATED_MEDIATYPE.equals(constraint.getPredicate())) {
			Set<Value> valueSet = constraint.getHasValue();
			if (valueSet != null && !valueSet.isEmpty()) {
				PropertyConstraint clone = constraint.clone();
				clone.setHasValue(new HashSet<Value>());
				for (Value value : valueSet) {
					String text = value.stringValue();
					if (text.lastIndexOf('+') < 0) {
						StringBuilder builder = new StringBuilder(text);
						builder.append(suffix);
						value = new KonigLiteral(builder.toString());
					}
					clone.addHasValue(value);
				}
				constraint = clone;
			}
		
		}
		return constraint;
	}

}
