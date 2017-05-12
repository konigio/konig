package io.konig.triplestore.core;

/*
 * #%L
 * Konig Triplestore Core
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


import java.util.Collection;

import org.openrdf.model.Namespace;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;

public interface Triplestore {

	void putNamespaces(Collection<Namespace> namespaces) throws TriplestoreException;
	Collection<Namespace> getNamespacesByPrefix(Collection<String> prefixes) throws TriplestoreException;
	Collection<Namespace> getNamespacesByName(Collection<String> names) throws TriplestoreException;
	void putResource(URI resourceId, Collection<Statement> outEdges) throws TriplestoreException;
	Collection<Statement> getStatements(URI subject, URI predicate, Value object) throws TriplestoreException;
	void remove(URI resourceId) throws TriplestoreException;
}
