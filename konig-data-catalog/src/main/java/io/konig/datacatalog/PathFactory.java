package io.konig.datacatalog;

/*
 * #%L
 * Konig Data Catalog
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


import java.nio.file.Path;
import java.nio.file.Paths;

import org.openrdf.model.Namespace;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.OWL;

import io.konig.core.NamespaceManager;
import io.konig.core.OwlReasoner;
import io.konig.core.vocab.Schema;

public class PathFactory {
	private OwlReasoner reasoner;
	private NamespaceManager nsManager;
	
	public PathFactory(OwlReasoner reasoner, NamespaceManager nsManager) {
		this.reasoner = reasoner;
		this.nsManager = nsManager;
	}
	
	public String relativePath(URI a, URI b) throws DataCatalogException {
		String aPage = pagePath(a);
		String bPage = pagePath(b);
		
		int aStart = 0;
		int bStart = 0;
		
		
		StringBuilder builder = new StringBuilder();
		while (aStart >= 0 && bStart>=0) {
			int aEnd = aPage.indexOf('/', aStart);
			int bEnd = bPage.indexOf('/', bStart);
			if (aEnd == bEnd && aEnd!=-1) {
				String aPart = aPage.substring(aStart, aEnd);
				String bPart = bPage.substring(bStart, bEnd);
				
				if (!aPart.equals(bPart)) {
					break;
				} 
				
				aStart = aEnd + 1;
				bStart = bEnd + 1;
			
			} else {
				break;
			}
		}
		if (aStart>=0) {
			aStart = aPage.indexOf('/', aStart+1);
			while (aStart>0) {
				builder.append("../");
				aStart = aPage.indexOf('/', aStart+1);
			}
			if (bStart>=0 && bStart < bPage.length()) {
				builder.append(bPage.substring(bStart));
			}
		}
		
		return builder.toString();
		
	}
	
	protected int adjustTarget(String bPage, int bStart) {
		return bStart;
	}

	public String pagePath(URI target) throws DataCatalogException {
		String uri = target.stringValue();
		int slash = uri.lastIndexOf('/');
		int hash = uri.lastIndexOf('#');
		int colon = uri.lastIndexOf(':');
		
		int mark = Math.max(slash, hash);
		mark = Math.max(mark, colon);
		
		
		while (mark > 0) {
			
			String namespace = uri.substring(0, mark+1);
			Namespace ns = nsManager.findByName(namespace);
			if (ns != null) {
				String localName = uri.substring(mark+1);
				String prefix = ns.getPrefix();
				

				StringBuilder builder = new StringBuilder();
				if (!excludePrefix(prefix)) {
					builder.append(prefix);
					builder.append('/');
				}
				addFolder(builder, target);
				builder.append(localName);
				builder.append(".html");
				
				return builder.toString();
			}
			mark = uri.lastIndexOf('/', mark-1);
		}
		throw new DataCatalogException("Namespace not found for resource: <" + uri + ">");
	}
	
	protected boolean excludePrefix(String prefix) {
		return false;
	}

	private String folderName(URI target) {
		if (reasoner.isTypeOf(target, OWL.CLASS)) {
			return "classes";
		} else if (reasoner.isProperty(target)) {
			return "properties";
		} else if (reasoner.isTypeOf(target, Schema.Enumeration)) {
			return "individuals";
		}
		return null;
	}

	private void addFolder(StringBuilder builder, URI target) {
		String folderName = folderName(target);
		if (folderName != null) {
			builder.append(folderName);
			builder.append('/');
		}
		
	}

}
