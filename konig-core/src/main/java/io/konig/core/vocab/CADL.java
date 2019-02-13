package io.konig.core.vocab;

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


import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

public class CADL {

	public static final String PREFERRED_NAMESPACE_PREFIX = "cadl";
	public static final String NAMESPACE = "http://www.konig.io/ns/cadl/";
	
	public static class Term {
		public static final String Cube = "http://www.konig.io/ns/cadl/Cube";
		public static final String Variable = "http://www.konig.io/ns/cadl/Variable";
		public static final String Dimension = "http://www.konig.io/ns/cadl/Dimension";
		public static final String Measure = "http://www.konig.io/ns/cadl/Measure";
		public static final String Level = "http://www.konig.io/ns/cadl/Level";

		public static final String dimension = "http://www.konig.io/ns/cadl/dimension";
		public static final String measure = "http://www.konig.io/ns/cadl/measure";
		public static final String level = "http://www.konig.io/ns/cadl/level";
		public static final String formula = "http://www.konig.io/ns/cadl/formula";
		public static final String valueType = "http://www.konig.io/ns/cadl/valueType";
		public static final String source = "http://www.konig.io/ns/cadl/source";
		public static final String rollUpTo = "http://www.konig.io/ns/cadl/rollUpTo";
		
	}
	

	public static final URI Cube = new URIImpl(Term.Cube);
	public static final URI Variable = new URIImpl(Term.Variable);
	public static final URI Dimension = new URIImpl(Term.Dimension);
	public static final URI Measure = new URIImpl(Term.Measure);
	public static final URI Level = new URIImpl(Term.Level);


	public static final URI dimension = new URIImpl(Term.dimension);
	public static final URI measure = new URIImpl(Term.measure);
	public static final URI level = new URIImpl(Term.level);
	public static final URI formula = new URIImpl(Term.formula);
	public static final URI valueType = new URIImpl(Term.valueType);
	public static final URI source = new URIImpl(Term.source);
	public static final URI rollUpTo = new URIImpl(Term.rollUpTo);

}
