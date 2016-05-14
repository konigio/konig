package io.konig.core.vocab;

/*
 * #%L
 * konig-core
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

public class XSD {
	public static final URI length = new URIImpl("http://www.w3.org/2001/XMLSchema#length");
	public static final URI maxExclusive = new URIImpl("http://www.w3.org/2001/XMLSchema#maxExclusive");
	public static final URI maxInclusive = new URIImpl("http://www.w3.org/2001/XMLSchema#maxInclusive");
	public static final URI maxLength = new URIImpl("http://www.w3.org/2001/XMLSchema#maxLength");
	public static final URI minExclusive = new URIImpl("http://www.w3.org/2001/XMLSchema#minExclusive");
	public static final URI minInclusive = new URIImpl("http://www.w3.org/2001/XMLSchema#minInclusive");
	public static final URI minLength = new URIImpl("http://www.w3.org/2001/XMLSchema#minLength");
	public static final URI pattern = new URIImpl("http://www.w3.org/2001/XMLSchema#pattern");
	
}
