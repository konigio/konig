package io.konig.formula;

import org.openrdf.model.URI;

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


/**
 * An expression that MAY represent an IRI value such as a fully-qualified IRI, a CURIE, or local name (together with a context).
 * This expression may appear as a PathTerm in a DirectionStep. Or it may be a fully-qualified IRI that stands alone. 
 * This expression does not ALWAYS represent an IRI value.  The <code>getIri</code> may return null. 
 * @author Greg McFall
 *
 */
public interface IriValue extends PrimaryExpression {
// extends PathTerm, PathStep, PrimaryExpression
	URI getIri();
}
