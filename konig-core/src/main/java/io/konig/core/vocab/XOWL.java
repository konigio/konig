package io.konig.core.vocab;

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


import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

public class XOWL {
	public static final URI termStatus = new URIImpl("http://schema.pearson.com/ns/xowl/termStatus");
	public static final URI Stable = new URIImpl("http://schema.pearson.com/ns/xowl/termStatus/statusStable");
	public static final URI Experimental = new URIImpl("http://schema.pearson.com/ns/xowl/termStatus/statusExperimental");
	public static final URI Invalid = new URIImpl("http://schema.pearson.com/ns/xowl/termStatus/statusInvalid");
	public static final URI Superseded = new URIImpl("http://schema.pearson.com/ns/xowl/termStatus/statusSuperseded");
	public static final URI Retired = new URIImpl("http://schema.pearson.com/ns/xowl/termStatus/statusRetired");
}
