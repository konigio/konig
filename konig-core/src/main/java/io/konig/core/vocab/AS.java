package io.konig.core.vocab;

/*
 * #%L
 * konig-core
 * %%
 * Copyright (C) 2015 Gregory McFall
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

public class AS {
	public static final URI actor = new URIImpl("http://www.w3.org/ns/activitystreams#actor");
	public static final URI endTime = new URIImpl("http://www.w3.org/ns/activitystreams#endTime");
	public static final URI object = new URIImpl("http://www.w3.org/ns/activitystreams#object");
	public static final URI result = new URIImpl("http://www.w3.org/ns/activitystreams#result");
}
