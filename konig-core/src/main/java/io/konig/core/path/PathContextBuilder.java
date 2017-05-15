package io.konig.core.path;

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


import org.openrdf.model.Namespace;
import org.openrdf.model.URI;

import io.konig.core.Context;
import io.konig.core.NamespaceManager;
import io.konig.core.Path;
import io.konig.core.Term;
import io.konig.core.impl.BasicContext;
import io.konig.core.path.HasStep.PredicateValuePair;

public class PathContextBuilder  {
	

	static public Context buildContext(Path path, NamespaceManager nsManager) {
		Context context = new BasicContext(null);
		for (Step step : path.asList()) {
			if (step instanceof InStep) {
				InStep inStep = (InStep) step;
				register(nsManager, context, inStep.getPredicate());
			} else if (step instanceof OutStep) {
				OutStep outStep = (OutStep) step;
				register(nsManager, context, outStep.getPredicate());
			} else if (step instanceof HasStep) {
				HasStep hasStep = (HasStep) step;
				for (PredicateValuePair pair : hasStep.getPairList()) {
					register(nsManager, context, pair.getPredicate());
					if (pair.getValue() instanceof URI) {
						register(nsManager, context, (URI) pair.getValue());
					}
				}
			} else if (step instanceof VertexStep) {
				VertexStep vertex = (VertexStep)step;
				if (vertex.getResource() instanceof URI) {
					register(nsManager, context, (URI)vertex.getResource());
				}
			}
		}
		path.setContext(context);
		
		return context;
	}

	static private void register(NamespaceManager nsManager, Context context, URI resource) {
		
		String localName = resource.getLocalName();
		
		Term term = context.getTerm(localName);
		if (term == null) {

			String namespace = resource.getNamespace();
			Namespace ns = nsManager.findByName(namespace);
			
			String prefix = ns==null ? null : ns.getPrefix();
			Term namespaceTerm = null;
			if (prefix != null) {
				namespaceTerm = context.getTerm(prefix);
				if (namespaceTerm == null) {
					namespaceTerm = context.addTerm(prefix, namespace);
				} else if (!namespaceTerm.getId().equals(namespace)){
					namespaceTerm = null;
				}
			}
			
			if (namespaceTerm == null) {
				context.addTerm(localName, resource.stringValue());
			} else {
				StringBuilder builder = new StringBuilder();
				builder.append(prefix);
				builder.append(':');
				builder.append(localName);
				
				context.addTerm(localName, builder.toString());
			}
		}
		
		
		
	}


}
