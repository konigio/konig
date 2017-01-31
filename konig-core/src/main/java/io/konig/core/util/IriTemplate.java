package io.konig.core.util;

import org.openrdf.model.Literal;

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

import org.openrdf.model.URI;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.XMLSchema;

public class IriTemplate extends SimpleValueFormat {
	
	
	public IriTemplate(String text) {
		super(text);
		
	}

	public URI expand(ValueMap map) {
		
		return new URIImpl(format(map));
		
	}
	
	public Literal toValue() {
		return new LiteralImpl(toString(), XMLSchema.STRING);
	}
	
}
