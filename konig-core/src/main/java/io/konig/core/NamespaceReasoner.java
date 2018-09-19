package io.konig.core;

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


import java.util.List;

import org.openrdf.model.Namespace;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

public class NamespaceReasoner {
	
	private NamespaceManager namespaceManager;
	
	public NamespaceReasoner(NamespaceManager namespaceManager) {
		this.namespaceManager = namespaceManager;
	}

	public NamespaceManager getNamespaceManager() {
		return namespaceManager;
	}
	
	public void gatherAllNamespaces(ContextManager contextManager) {
		List<String> list = contextManager.listContexts();
		for (String contextId : list) {
			Context context = contextManager.getContextByURI(contextId);
			gatherNamespaces(context);
		}
	}

	public void gatherNamespaces(Context context) {
		context.compile();
		Context inverse = context.inverse();
		List<Term> termList = context.asList();
		for (Term term : termList) {
			String expandedId = term.getExpandedIdValue();
			URI uri = new URIImpl(expandedId);
			String namespaceName = uri.getNamespace();
			Namespace ns = namespaceManager.findByName(namespaceName);
			if (ns == null) {
				Term namespaceTerm = inverse.getTerm(namespaceName);
				if (namespaceTerm != null) {
					String prefix = namespaceTerm.getKey();
					namespaceManager.add(prefix, namespaceName);
				}
			}
		}
	}

}
